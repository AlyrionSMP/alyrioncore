package xyz.alyrion.alyrioncore.world.weather;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.network.MarsWeatherPayload;
import xyz.alyrion.alyrioncore.world.ModDimensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MarsWeatherSavedData extends SavedData {
    public static final String DATA_NAME = "alyrion_mars_weather";
    public static final SavedData.Factory<MarsWeatherSavedData> FACTORY =
            new SavedData.Factory<>(MarsWeatherSavedData::new, MarsWeatherSavedData::load);

    private MarsWeatherState currentState = MarsWeatherState.CLEAR;
    private int stateDuration = 24000;
    private float currentIntensity = 0.0F;
    private float windAngle = 0.85F; // in radians
    private float windSpeed = 0.05F;

    private final List<DustDevilInstance> activeDustDevils = new ArrayList<>();
    private int syncTimer = 0;

    public MarsWeatherSavedData() {
    }

    public static MarsWeatherSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static MarsWeatherSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MarsWeatherSavedData data = new MarsWeatherSavedData();
        data.currentState = MarsWeatherState.byName(tag.getString("WeatherState"));
        data.stateDuration = tag.contains("StateDuration") ? tag.getInt("StateDuration") : 24000;
        data.currentIntensity = tag.getFloat("CurrentIntensity");
        data.windAngle = tag.contains("WindAngle") ? tag.getFloat("WindAngle") : 0.85F;
        data.windSpeed = tag.contains("WindSpeed") ? tag.getFloat("WindSpeed") : 0.05F;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("WeatherState", this.currentState.getSerializedName());
        tag.putInt("StateDuration", this.stateDuration);
        tag.putFloat("CurrentIntensity", this.currentIntensity);
        tag.putFloat("WindAngle", this.windAngle);
        tag.putFloat("WindSpeed", this.windSpeed);
        return tag;
    }

    public void tick(ServerLevel level) {
        RandomSource random = level.random;

        // 1. Progress State Machine Countdown
        this.stateDuration--;
        if (this.stateDuration <= 0) {
            transitionToNextState(level, random);
        }

        // 2. Smoothly Interpolate Intensity & Wind
        float targetIntensity = this.currentState.getBaseIntensity();
        if (this.currentIntensity < targetIntensity) {
            this.currentIntensity = Math.min(targetIntensity, this.currentIntensity + 0.003F);
        } else if (this.currentIntensity > targetIntensity) {
            this.currentIntensity = Math.max(targetIntensity, this.currentIntensity - 0.003F);
        }

        float targetWind = this.currentState.getMaxWindSpeed();
        this.windSpeed = Mth.lerp(0.01F, this.windSpeed, targetWind);
        this.windAngle = (this.windAngle + 0.0005F) % (float) (Math.PI * 2.0);

        // 3. Tick & Spawn Dust Devils
        tickDustDevils(level, random);

        // 4. Synchronize with Mars Dimension Players
        this.syncTimer++;
        if (this.syncTimer >= 20) {
            this.syncTimer = 0;
            broadcastWeather(level);
        }

        if (this.stateDuration % 1200 == 0) {
            setDirty();
        }
    }

    private void transitionToNextState(ServerLevel level, RandomSource random) {
        int sol = getSeasonSol(level);
        boolean isPerihelion = (sol >= 420 && sol <= 580); // Perihelion storm season (Ls 200° - 300°)

        MarsWeatherState nextState;
        float roll = random.nextFloat();

        if (isPerihelion) {
            // Perihelion / Southern Summer: Elevated chance of regional and planet-encircling storms
            if (this.currentState == MarsWeatherState.GLOBAL_DUST_STORM) {
                // After global storm subsides, transitions to regional or dust devils
                nextState = roll < 0.60F ? MarsWeatherState.REGIONAL_STORM : MarsWeatherState.CLEAR;
            } else if (this.currentState == MarsWeatherState.REGIONAL_STORM) {
                // Regional storm can cascade into a global storm!
                nextState = roll < 0.40F ? MarsWeatherState.GLOBAL_DUST_STORM : (roll < 0.75F ? MarsWeatherState.DUST_DEVILS : MarsWeatherState.CLEAR);
            } else {
                if (roll < 0.25F) {
                    nextState = MarsWeatherState.GLOBAL_DUST_STORM;
                } else if (roll < 0.65F) {
                    nextState = MarsWeatherState.REGIONAL_STORM;
                } else if (roll < 0.85F) {
                    nextState = MarsWeatherState.DUST_DEVILS;
                } else {
                    nextState = MarsWeatherState.CLEAR;
                }
            }
        } else {
            // Aphelion / Northern Summer: Calm, clear skies and midday thermal dust devils dominate
            if (this.currentState == MarsWeatherState.GLOBAL_DUST_STORM) {
                nextState = MarsWeatherState.REGIONAL_STORM;
            } else if (this.currentState == MarsWeatherState.REGIONAL_STORM) {
                nextState = roll < 0.70F ? MarsWeatherState.CLEAR : MarsWeatherState.DUST_DEVILS;
            } else {
                if (roll < 0.02F) {
                    nextState = MarsWeatherState.GLOBAL_DUST_STORM; // Very rare
                } else if (roll < 0.15F) {
                    nextState = MarsWeatherState.REGIONAL_STORM;
                } else if (roll < 0.60F) {
                    nextState = MarsWeatherState.DUST_DEVILS;
                } else {
                    nextState = MarsWeatherState.CLEAR;
                }
            }
        }

        setWeather(nextState, getRandomDurationForState(nextState, random));
    }

    private int getRandomDurationForState(MarsWeatherState state, RandomSource random) {
        return switch (state) {
            case CLEAR -> 12000 + random.nextInt(24000); // 0.5 to 1.5 sols
            case DUST_DEVILS -> 8000 + random.nextInt(16000); // 0.33 to 1.0 sol
            case REGIONAL_STORM -> 10000 + random.nextInt(20000); // ~0.5 to 1.25 sols
            case GLOBAL_DUST_STORM -> 24000 + random.nextInt(48000); // 1.0 to 3.0 sols
        };
    }

    private void tickDustDevils(ServerLevel level, RandomSource random) {
        // Remove expired dust devils
        Iterator<DustDevilInstance> it = this.activeDustDevils.iterator();
        while (it.hasNext()) {
            DustDevilInstance dd = it.next();
            dd.tick(level);
            if (!dd.isAlive()) {
                it.remove();
            }
        }

        // Spawn new dust devils around players during midday or DUST_DEVILS state
        int dayTime = (int) (Math.abs(level.getDayTime()) % 24000L);
        boolean isMidday = (dayTime >= 3500 && dayTime <= 8500);

        int maxDevils = (this.currentState == MarsWeatherState.DUST_DEVILS) ? 8 : (isMidday ? 4 : 1);
        if (this.currentState == MarsWeatherState.GLOBAL_DUST_STORM) {
            maxDevils = 0; // Planet-encircling storms disperse localized convective columns
        }

        if (this.activeDustDevils.size() < maxDevils && random.nextInt(60) == 0) {
            List<ServerPlayer> players = level.players();
            if (!players.isEmpty()) {
                ServerPlayer p = players.get(random.nextInt(players.size()));
                double spawnDist = 18.0 + random.nextDouble() * 36.0;
                double spawnAngle = random.nextDouble() * Math.PI * 2.0;
                double sx = p.getX() + Math.cos(spawnAngle) * spawnDist;
                double sz = p.getZ() + Math.sin(spawnAngle) * spawnDist;

                DustDevilInstance newDevil = DustDevilInstance.createRandom(sx, sz, level, random);
                this.activeDustDevils.add(newDevil);
            }
        }
    }

    public void setWeather(MarsWeatherState state, int durationTicks) {
        this.currentState = state;
        this.stateDuration = durationTicks;
        setDirty();
    }

    public void broadcastWeather(ServerLevel level) {
        List<MarsWeatherPayload.DustDevilData> devilDataList = new ArrayList<>();
        for (DustDevilInstance dd : this.activeDustDevils) {
            devilDataList.add(new MarsWeatherPayload.DustDevilData(
                    dd.getX(), dd.getY(), dd.getZ(), dd.getRadius(), dd.getHeight()
            ));
        }

        MarsWeatherPayload payload = new MarsWeatherPayload(
                this.currentState.ordinal(),
                this.currentIntensity,
                this.windAngle,
                this.windSpeed,
                getSeasonSol(level),
                devilDataList
        );

        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public int getSeasonSol(ServerLevel level) {
        long time = Math.max(0, level.getDayTime());
        return (int) ((time / 24000L) % 668L);
    }

    public MarsWeatherState getCurrentState() {
        return currentState;
    }

    public float getCurrentIntensity() {
        return currentIntensity;
    }

    public int getStateDuration() {
        return stateDuration;
    }

    public float getWindAngle() {
        return windAngle;
    }

    public float getWindSpeed() {
        return windSpeed;
    }
}

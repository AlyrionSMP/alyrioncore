package xyz.alyrion.alyrioncore.client.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.network.MarsWeatherPayload;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.world.ModDimensions;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherState;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AlyrionCore.MODID, value = Dist.CLIENT)
public class MarsClientWeatherHandler {

    private static MarsWeatherState currentState = MarsWeatherState.CLEAR;
    private static float stormIntensity = 0.0F;
    private static float smoothedIntensity = 0.0F;
    private static float windAngle = 0.85F;
    private static float windSpeed = 0.05F;
    private static int seasonSol = 0;
    private static List<MarsWeatherPayload.DustDevilData> activeDustDevils = new ArrayList<>();

    private static int clientTickCount = 0;

    private static final Vector3f DUST_COLOR_BASE = new Vector3f(0.78F, 0.38F, 0.18F); // Mars rust orange
    private static final Vector3f DUST_COLOR_DEVIL = new Vector3f(0.85F, 0.44F, 0.22F); // Bright swirling dust

    public static void updateFromServer(MarsWeatherPayload payload) {
        if (payload.weatherStateOrdinal() >= 0 && payload.weatherStateOrdinal() < MarsWeatherState.values().length) {
            currentState = MarsWeatherState.values()[payload.weatherStateOrdinal()];
        }
        stormIntensity = payload.stormIntensity();
        windAngle = payload.windAngle();
        windSpeed = payload.windSpeed();
        seasonSol = payload.seasonSol();
        activeDustDevils = payload.dustDevils();
    }

    public static float getSmoothedIntensity() {
        return smoothedIntensity;
    }

    public static MarsWeatherState getCurrentState() {
        return currentState;
    }

    public static int getSeasonSol() {
        return seasonSol;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;

        if (!level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            smoothedIntensity = 0.0F;
            return;
        }

        clientTickCount++;

        // Smoothly interpolate intensity towards server target
        smoothedIntensity = Mth.lerp(0.05F, smoothedIntensity, stormIntensity);

        RandomSource random = level.random;

        // 1. Spawn Ambient Blowing Wind & Dust Particles
        int particleCount = (int) (smoothedIntensity * 28) + (currentState == MarsWeatherState.DUST_DEVILS ? 4 : 1);
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        for (int i = 0; i < particleCount; i++) {
            double ox = px + (random.nextDouble() - 0.5) * 32.0;
            double oy = py + (random.nextDouble() - 0.3) * 12.0;
            double oz = pz + (random.nextDouble() - 0.5) * 32.0;

            double speed = (0.25 + smoothedIntensity * 0.95);
            double vx = Math.cos(windAngle) * speed;
            double vz = Math.sin(windAngle) * speed;
            double vy = -0.01 + (random.nextDouble() - 0.5) * 0.04;

            if (random.nextInt(3) == 0) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.MARTIAN_SAND.get().defaultBlockState()),
                        ox, oy, oz, vx, vy, vz
                );
            } else {
                level.addParticle(
                        new DustParticleOptions(DUST_COLOR_BASE, 1.0F + random.nextFloat() * 0.8F),
                        ox, oy, oz, vx, vy, vz
                );
            }
        }

        // 2. Spawn Towering Dust Devil Columns
        for (MarsWeatherPayload.DustDevilData dd : activeDustDevils) {
            double distSq = player.distanceToSqr(dd.x(), dd.y(), dd.z());
            if (distSq < 10000.0) { // Within 100 blocks
                int devilParticles = 24;
                for (int j = 0; j < devilParticles; j++) {
                    double h = random.nextDouble() * dd.height();
                    // Conical widening vortex: radius expands with altitude
                    double r = dd.radius() * (0.35 + 0.65 * (h / dd.height()));
                    double theta = (clientTickCount * 0.35 + h * 0.3 + random.nextDouble() * 0.6);

                    double sx = dd.x() + Math.cos(theta) * r;
                    double sz = dd.z() + Math.sin(theta) * r;
                    double sy = dd.y() + h;

                    double rotSpeed = 0.25 + (1.0 - h / dd.height()) * 0.15;
                    double vx = -Math.sin(theta) * rotSpeed;
                    double vz = Math.cos(theta) * rotSpeed;
                    double vy = 0.18 + (1.0 - h / dd.height()) * 0.22;

                    if (random.nextBoolean()) {
                        level.addParticle(
                                new DustParticleOptions(DUST_COLOR_DEVIL, 1.3F + random.nextFloat() * 0.7F),
                                sx, sy, sz, vx, vy, vz
                        );
                    } else {
                        level.addParticle(
                                new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.MARTIAN_REGOLITH.get().defaultBlockState()),
                                sx, sy, sz, vx, vy, vz
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }

        float baseFar = event.getFarPlaneDistance();
        float baseNear = event.getNearPlaneDistance();

        // Atmospheric Fog Ramp
        float stormFar = switch (currentState) {
            case CLEAR -> baseFar;
            case DUST_DEVILS -> Math.min(baseFar, 160.0F);
            case REGIONAL_STORM -> 42.0F + (1.0F - smoothedIntensity) * 40.0F;
            case GLOBAL_DUST_STORM -> 12.0F + (1.0F - smoothedIntensity) * 18.0F; // Dense Martian dust blackout!
        };

        float targetFar = Mth.lerp(smoothedIntensity, baseFar, stormFar);
        float targetNear = Math.max(2.0F, targetFar * 0.25F);

        event.setNearPlaneDistance(targetNear);
        event.setFarPlaneDistance(targetFar);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }

        // Base clear Martian butterscotch -> deep apocalyptic uniform ochre/terracotta
        float ochreR = 0.65F;
        float ochreG = 0.30F;
        float ochreB = 0.14F;

        float blend = smoothedIntensity;
        if (currentState == MarsWeatherState.DUST_DEVILS) {
            blend = Math.max(blend, 0.15F);
        }

        float r = Mth.lerp(blend, event.getRed(), ochreR);
        float g = Mth.lerp(blend, event.getGreen(), ochreG);
        float b = Mth.lerp(blend, event.getBlue(), ochreB);

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}

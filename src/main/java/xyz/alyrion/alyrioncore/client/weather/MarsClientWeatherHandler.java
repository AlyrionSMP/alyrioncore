package xyz.alyrion.alyrioncore.client.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.GuiGraphics;
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
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import net.neoforged.fml.ModList;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.compat.SodiumCompat;
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
    /** Tick clock used by the per-frame wind sway; only advances while on Mars. */
    private static float effectTime = 0.0F;

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

        boolean onMars = level.dimension().equals(ModDimensions.MARS_LEVEL);
        if (!onMars) {
            smoothedIntensity = 0.0F;
        }

        // Sodium's fog occlusion clamps its chunk search distance to the fog end, so storm fog makes
        // it unload sections around the player. Suspend it while visibility is low, and restore the
        // player's own value as soon as the storm ends or the player leaves Mars.
        if (ModList.get().isLoaded("sodium")) {
            boolean blinding = onMars && smoothedIntensity > 0.35F;
            if (blinding || SodiumCompat.isSuppressing()) {
                SodiumCompat.setFogOcclusionSuspended(blinding);
            }
        }

        if (!onMars) {
            return;
        }

        // Vanilla skips the particle engine and ambient block ticks while the game is paused (or the
        // level is frozen), but ClientTickEvent.Post still fires. Spawning here would push particles
        // into the engine's add-queue, which is only drained by its own tick — they would all appear
        // at once in a burst on unpause. Mirror vanilla's condition instead of feeding a frozen engine.
        if (mc.isPaused() || !level.tickRateManager().runsNormally()) {
            return;
        }

        clientTickCount++;

        // Smoothly interpolate intensity towards server target
        smoothedIntensity = Mth.lerp(0.05F, smoothedIntensity, stormIntensity);

        RandomSource random = level.random;
        effectTime++;

        // Electrostatic discharges: Martian dust storms spark, they never rain. A sky flash
        // (the vanilla lightning flash path) reads as a storm without spawning a single particle.
        if (currentState == MarsWeatherState.GLOBAL_DUST_STORM && smoothedIntensity > 0.8F
                && random.nextInt(400) == 0) {
            level.setSkyFlashTime(2 + random.nextInt(3));
        }

        // Particle budget: the storm's weight is carried by fog + the dust veil overlay, so the
        // particle layer is deliberately thin — and it honours the vanilla particle setting.
        float particleBudget = switch (mc.options.particles().get()) {
            case ALL -> 1.0F;
            case DECREASED -> 0.35F;
            case MINIMAL -> 0.0F;
        };
        // An unfocused window still ticks (~10 FPS) and the storm stays visible on a second monitor,
        // so only the spawns are dropped — the cheap field effects keep running and the dust field
        // refills within a second of refocusing.
        if (!mc.isWindowActive()) {
            particleBudget = 0.0F;
        }

        // 1. Ambient wind-borne dust: a handful of cheap DUST particles near the player.
        int particleCount = (int) ((smoothedIntensity * 7.0F
                + (currentState == MarsWeatherState.DUST_DEVILS ? 2.0F : 1.0F)) * particleBudget);
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        for (int i = 0; i < particleCount; i++) {
            double ox = px + (random.nextDouble() - 0.5) * 24.0;
            double oy = py + (random.nextDouble() - 0.3) * 12.0;
            double oz = pz + (random.nextDouble() - 0.5) * 24.0;

            double speed = (0.25 + smoothedIntensity * 0.95);
            double vx = Math.cos(windAngle) * speed;
            double vz = Math.sin(windAngle) * speed;
            double vy = -0.01 + (random.nextDouble() - 0.5) * 0.04;

            if (random.nextInt(6) == 0) {
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
            if (distSq < 4096.0) { // dust devils are a local detail: within 64 blocks
                int devilParticles = (int) (6.0F * particleBudget);
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

        // Atmospheric fog ramp. A global storm is a genuine blackout: you lose the horizon first,
        // then the terrain, then almost everything else.
        float stormFar = switch (currentState) {
            case CLEAR -> baseFar;
            case DUST_DEVILS -> Math.min(baseFar, 120.0F);
            case REGIONAL_STORM -> 14.0F + (1.0F - smoothedIntensity) * 42.0F;
            case GLOBAL_DUST_STORM -> 3.0F + (1.0F - smoothedIntensity) * 9.0F; // buried in dust
        };

        float targetFar = Mth.lerp(smoothedIntensity, baseFar, stormFar);
        float targetNear = Math.max(0.5F, targetFar * 0.35F);

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

        // Clear butterscotch -> uniform ochre -> darker storm brown: a storm does not merely tint
        // the view, it takes the light away with it.
        float ochreR = 0.58F;
        float ochreG = 0.26F;
        float ochreB = 0.12F;

        float blend = switch (currentState) {
            case CLEAR -> smoothedIntensity;
            case DUST_DEVILS -> Math.max(smoothedIntensity, 0.20F);
            default -> Math.max(smoothedIntensity, 0.40F);
        };
        blend = Mth.clamp(blend * 1.15F, 0.0F, 1.0F);
        float darken = 1.0F - 0.45F * blend;

        float r = Mth.lerp(blend, event.getRed(), ochreR) * darken;
        float g = Mth.lerp(blend, event.getGreen(), ochreG) * darken;
        float b = Mth.lerp(blend, event.getBlue(), ochreB) * darken;

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    /**
     * Dust veil: one full-screen ochre tint plus two vignette bands, all scaled by storm
     * intensity — three draw calls per frame. This is what replaces hundreds of particles as the
     * storm's visual weight, and it is what makes near-zero visibility read as dust rather than
     * as a black wall.
     */
    @SubscribeEvent
    public static void onRenderDustVeil(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.screen != null) {
            return; // never tint menus or the pause screen
        }
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }
        float intensity = smoothedIntensity;
        if (intensity <= 0.04F) {
            return;
        }
        // Ramp late (^0.7) so a regional storm reads as haze and a global one reads as blackout.
        float s = (float) Math.pow(intensity, 0.7D);

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float veilR = Mth.lerp(s, 0.62F, 0.36F);
        float veilG = Mth.lerp(s, 0.30F, 0.18F);
        float veilB = Mth.lerp(s, 0.14F, 0.09F);

        graphics.fill(0, 0, width, height, dustArgb(Math.min(0.40F, s * 0.40F), veilR, veilG, veilB));

        int edgeColor = dustArgb(Math.min(0.65F, s * 0.65F), veilR * 0.85F, veilG * 0.85F, veilB * 0.85F);
        int clearColor = dustArgb(0.0F, veilR, veilG, veilB);
        int band = (int) (height * 0.38F);
        graphics.fillGradient(0, 0, width, band, edgeColor, clearColor);
        graphics.fillGradient(0, height - band, width, height, clearColor, edgeColor);
    }

    /**
     * Wind sway: a few tenths of a degree of roll/wobble driven by the same clock as the dust,
     * so the storm is felt in the camera without any post-processing pass.
     */
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }
        float intensity = smoothedIntensity;
        if (intensity <= 0.02F) {
            return;
        }
        float t = effectTime + (float) event.getPartialTick();
        event.setRoll(event.getRoll() + Mth.sin(t * 0.35F) * 0.9F * intensity);
        event.setPitch(event.getPitch() + Mth.sin(t * 0.23F + 1.7F) * 0.25F * intensity);
        event.setYaw(event.getYaw() + Mth.cos(t * 0.31F) * 0.35F * intensity);
    }

    /** Gust pressure: up to ~2 degrees of extra FOV at the height of a global storm. */
    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return;
        }
        float intensity = smoothedIntensity;
        if (intensity <= 0.05F) {
            return;
        }
        event.setFOV(event.getFOV() * (1.0D + 0.035D * intensity));
    }

    private static int dustArgb(float alpha, float red, float green, float blue) {
        int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        return (a << 24)
                | ((int) (red * 255.0F) << 16)
                | ((int) (green * 255.0F) << 8)
                | (int) (blue * 255.0F);
    }
}

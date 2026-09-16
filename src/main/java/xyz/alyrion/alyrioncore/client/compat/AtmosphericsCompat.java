package xyz.alyrion.alyrioncore.client.compat;

import com.beash.atmospherics.Atmospherics;
import com.beash.atmospherics.config.AirHazeSettings;
import com.beash.atmospherics.config.BiomeFogSettings;
import com.beash.atmospherics.config.FogConfig;
import com.beash.atmospherics.config.HazeSettings;
import com.beash.atmospherics.config.WeatherSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.weather.MarsClientWeatherHandler;

/**
 * Soft integration with ATMOSPHERICS (mod id {@code atmospherics}, client only).
 *
 * <p>ATMOSPHERICS picks fog/sky/haze/cloud colours per biome out of its own
 * config ({@code config/ambientfog/biome_fog.json}) and overrides vanilla fog and sky
 * colour in {@code BackgroundRenderer}/{@code ClientLevel} through mixins. Mars biomes are
 * not in that config, so an unconfigured Mars would render with default terrestrial fog
 * while our own {@code MarsDimensionEffects} fog tint is superseded by their mixins.
 *
 * <p>To keep exactly one source of truth for the Martian atmosphere, this class pushes a
 * Mars profile into every AlyrionCore biome of the mod's live {@link FogConfig}:
 * the base day colours from the biome's own {@code effects} (via
 * {@link BiomeFogSettings#fromBiomeId(String)}), thin-air visibility, low ferric-dust haze
 * and suspended dust air-haze. It never writes the config file and never touches biomes it
 * does not own, so user edits survive; entries are re-applied if the config object is
 * replaced (preset load) or our biomes are missing from it.
 *
 * <p>Everything here is reached only when {@code atmospherics} is present, so the class is
 * never loaded without it.
 */
public final class AtmosphericsCompat {

    /** Physical AlyrionCore Mars biomes (the planet's sky sampler biomes). */
    private static final String[] MARS_BIOMES = {
            "alyrioncore:noachis_terra",
            "alyrioncore:olympus_mons",
            "alyrioncore:planum_boreum",
            "alyrioncore:tharsis_volcanic_plateau",
            "alyrioncore:valles_marineris",
            "alyrioncore:vastitas_borealis"
    };

    private static final int CHECK_INTERVAL_TICKS = 20;

    /** Storm colours pushed into the dusty-air profiles (day fog, sky, haze). */
    private static final int STORM_FOG = 0x5E2F14;
    private static final int STORM_SKY = 0x4A2410;
    private static final int STORM_HAZE = 0x7A3C18;

    /** Visibility in blocks at the peak of a planet-encircling storm — effectively a blackout. */
    private static final float STORM_VISIBILITY = 6.0F;

    /** The exact config instance we last injected into; a different instance means a reload. */
    private static FogConfig injectedInto;
    private static int ticks;

    /** Calm-weather values, captured when a storm starts so they can be restored when it ends. */
    private static final StormBase[] BASES = new StormBase[MARS_BIOMES.length];
    private static float lastStorm = -1.0F;

    private record StormBase(int fogColor, int skyColor, int nightFogColor, int nightSkyColor,
                             float endDistance, float nightEndDistance,
                             float fogDensity, float nightFogDensity,
                             int hazeColor, float hazeStrength,
                             int airHazeColor, float airHazeIntensity) {
    }

    private AtmosphericsCompat() {
    }

    /** Cheap no-op unless ATMOSPHERICS is loaded; call once per client tick. */
    public static void tick(Minecraft mc) {
        if (mc.level != null && ++ticks % CHECK_INTERVAL_TICKS == 0) {
            FogConfig config = Atmospherics.getConfig();
            if (config != null && config.biomes != null
                    && (config != injectedInto || !hasAllProfiles(config))) {
                for (String biomeId : MARS_BIOMES) {
                    config.biomes.put(biomeId, marsProfile(biomeId));
                }
                injectedInto = config;
                lastStorm = -1.0F; // fresh baseline: the calm values were just rewritten
                AlyrionCore.LOGGER.info(
                        "ATMOSPHERICS integration: Mars atmosphere profile applied to {} biomes",
                        MARS_BIOMES.length);
            }
        }
        // ATMOSPHERICS' mixins own fog colour *and* fog distance, so a Mars dust storm has to be
        // pushed into its values — otherwise a storm renders with clear-sky visibility.
        applyStorm(MarsClientWeatherHandler.getSmoothedIntensity());
    }

    private static boolean hasAllProfiles(FogConfig config) {
        for (String biomeId : MARS_BIOMES) {
            if (!config.biomes.containsKey(biomeId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds the Martian atmosphere profile for one biome. Day fog/sky come from the biome's
     * own registered {@code effects} so each region keeps its identity (dark Olympus Mons,
     * pale Planum Boreum, ...) while the rest models a 600 Pa ferric-dust atmosphere.
     */
    private static BiomeFogSettings marsProfile(String biomeId) {
        BiomeFogSettings settings = BiomeFogSettings.fromBiomeId(biomeId);
        int fog = settings.fogColor;
        int sky = settings.skyColor;

        settings.enabled = true;
        settings.intensity = 1.0F;
        // Thin air: less density falloff and much longer visibility than terrestrial biomes.
        settings.fogDensity = 0.85F;
        settings.startDistance = 24.0F;
        settings.endDistance = 420.0F;
        settings.nightFogDensity = 1.0F;
        settings.nightStartDistance = 8.0F;
        settings.nightEndDistance = 220.0F;
        settings.nightSkyColor = 0; // 0 = no night sky tint, stars stay visible
        settings.nightFogColor = scale(fog, 0.22F);

        // Low-lying dust layer hugging the horizon (dust devils and settling fines).
        HazeSettings haze = settings.haze;
        haze.enabled = true;
        haze.color = lighten(fog, 0.35F);
        haze.nightColor = scale(fog, 0.18F);
        haze.strength = 1.8F;
        haze.bottomY = -64.0F;
        haze.middleY = -24.0F;
        haze.topY = 24.0F;
        haze.radius = 56.0F;
        haze.nightStrength = 1.2F;
        haze.nightBottomY = -64.0F;
        haze.nightMiddleY = -28.0F;
        haze.nightTopY = 28.0F;
        haze.nightRadius = 56.0F;

        // Suspended fines in the air itself: sparse, because the atmosphere is 0.6% of Earth's.
        AirHazeSettings airHaze = settings.airHaze;
        airHaze.enabled = true;
        airHaze.intensity = 0.55F;
        airHaze.spawnRate = 0.8F;
        airHaze.size = 1.1F;
        airHaze.startDistance = 1.0F;
        airHaze.endDistance = 14.0F;
        airHaze.color = lighten(fog, 0.35F);

        // Mars has no rain; if vanilla weather ever runs here, render it as a dust storm
        // instead of blue-grey terrestrial rain.
        WeatherSettings weather = settings.weatherOverride != null
                ? settings.weatherOverride.copy()
                : new WeatherSettings();
        int dustHaze = lighten(fog, 0.40F);
        int dustStorm = scale(fog, 0.55F);
        weather.rainFogColor = dustHaze;
        weather.rainSkyColor = sky;
        weather.rainCloudColor = lighten(fog, 0.60F);
        weather.rainHazeColor = dustHaze;
        weather.thunderFogColor = dustStorm;
        weather.thunderSkyColor = scale(sky, 0.60F);
        weather.thunderCloudColor = dustStorm;
        weather.thunderHazeColor = dustStorm;
        weather.fogNightColorDarkening = 0.85F;
        weather.hazeNightColorDarkening = 0.85F;
        weather.cloudNightColorDarkening = 0.90F;
        weather.rain.fog = 0.55F;
        weather.rain.sky = 0.35F;
        weather.rain.haze = 0.60F;
        weather.rain.fogStart = 30.0F;
        weather.rain.fogEnd = 140.0F;
        weather.thunder.fog = 0.80F;
        weather.thunder.sky = 0.60F;
        weather.thunder.haze = 0.85F;
        weather.thunder.fogStart = 16.0F;
        weather.thunder.fogEnd = 90.0F;
        weather.sanitize();
        settings.weatherOverride = weather;

        settings.sanitize();
        return settings;
    }

    /**
     * Lays the live dust-storm intensity over the per-biome values: visibility collapses, the sky
     * browns out, haze and airborne fines thicken. The calm values are captured when a storm
     * starts (so anything tuned in their menu becomes the baseline) and written back when it ends,
     * so clear-weather configuration is never permanently altered.
     */
    private static void applyStorm(float intensity) {
        if (injectedInto == null) {
            return;
        }
        float curve = (float) Math.pow(Mth.clamp(intensity, 0.0F, 1.0F), 0.6D);
        if (Math.abs(curve - lastStorm) < 0.01F) {
            return;
        }
        boolean stormStarting = lastStorm <= 0.0F && curve > 0.0F;
        boolean stormEnding = lastStorm > 0.0F && curve <= 0.0F;
        lastStorm = curve;

        for (int i = 0; i < MARS_BIOMES.length; i++) {
            BiomeFogSettings live = injectedInto.biomes.get(MARS_BIOMES[i]);
            if (live == null) {
                continue;
            }
            if (stormStarting || BASES[i] == null) {
                BASES[i] = capture(live);
            }
            if (stormEnding) {
                restore(live, BASES[i]);
            } else if (curve > 0.0F) {
                modulate(live, BASES[i], curve);
            }
        }
    }

    private static StormBase capture(BiomeFogSettings s) {
        ensureNested(s);
        return new StormBase(s.fogColor, s.skyColor, s.nightFogColor, s.nightSkyColor,
                s.endDistance, s.nightEndDistance, s.fogDensity, s.nightFogDensity,
                s.haze.color, s.haze.strength, s.airHaze.color, s.airHaze.intensity);
    }

    /** Their loader nulls nested sections on hand-edited configs; never assume they exist. */
    private static void ensureNested(BiomeFogSettings s) {
        if (s.haze == null) {
            s.haze = new HazeSettings();
        }
        if (s.airHaze == null) {
            s.airHaze = new AirHazeSettings();
        }
    }

    private static void restore(BiomeFogSettings s, StormBase b) {
        ensureNested(s);
        s.fogColor = b.fogColor();
        s.skyColor = b.skyColor();
        s.nightFogColor = b.nightFogColor();
        s.nightSkyColor = b.nightSkyColor();
        s.endDistance = b.endDistance();
        s.nightEndDistance = b.nightEndDistance();
        s.fogDensity = b.fogDensity();
        s.nightFogDensity = b.nightFogDensity();
        s.haze.color = b.hazeColor();
        s.haze.strength = b.hazeStrength();
        s.airHaze.color = b.airHazeColor();
        s.airHaze.intensity = b.airHazeIntensity();
    }

    private static void modulate(BiomeFogSettings s, StormBase b, float k) {
        ensureNested(s);
        s.fogColor = blendColour(b.fogColor(), STORM_FOG, k);
        s.skyColor = blendColour(b.skyColor(), STORM_SKY, k);
        s.nightFogColor = blendColour(b.nightFogColor(), STORM_FOG, k * 0.8F);
        s.nightSkyColor = blendColour(b.nightSkyColor(), STORM_SKY, k * 0.8F);
        s.endDistance = Mth.lerp(k, b.endDistance(), STORM_VISIBILITY);
        s.nightEndDistance = Mth.lerp(k, b.nightEndDistance(), STORM_VISIBILITY * 1.5F);
        s.fogDensity = Mth.lerp(k, b.fogDensity(), b.fogDensity() * 3.5F);
        s.nightFogDensity = Mth.lerp(k, b.nightFogDensity(), b.nightFogDensity() * 3.5F);
        s.haze.color = blendColour(b.hazeColor(), STORM_HAZE, k);
        s.haze.strength = Mth.lerp(k, b.hazeStrength(), b.hazeStrength() * 2.4F);
        s.airHaze.color = blendColour(b.airHazeColor(), STORM_HAZE, k);
        s.airHaze.intensity = Mth.lerp(k, b.airHazeIntensity(), b.airHazeIntensity() * 1.8F);
    }

    /** Channel-wise blend between two 0xRRGGBB colours. */
    private static int blendColour(int from, int to, float k) {
        float f = Mth.clamp(k, 0.0F, 1.0F);
        int r = Math.round(Mth.lerp(f, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Math.round(Mth.lerp(f, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Math.round(Mth.lerp(f, from & 0xFF, to & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    /** Multiplies an 0xRRGGBB colour by {@code factor} (used for night variants). */
    private static int scale(int rgb, float factor) {
        int r = Math.round(((rgb >> 16) & 0xFF) * factor);
        int g = Math.round(((rgb >> 8) & 0xFF) * factor);
        int b = Math.round((rgb & 0xFF) * factor);
        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    /** Blends an 0xRRGGBB colour towards white by {@code factor}. */
    private static int lighten(int rgb, float factor) {
        int r = Math.round(((rgb >> 16) & 0xFF) + (255 - ((rgb >> 16) & 0xFF)) * factor);
        int g = Math.round(((rgb >> 8) & 0xFF) + (255 - ((rgb >> 8) & 0xFF)) * factor);
        int b = Math.round((rgb & 0xFF) + (255 - (rgb & 0xFF)) * factor);
        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    private static int channel(int value) {
        return Math.min(255, Math.max(0, value));
    }
}

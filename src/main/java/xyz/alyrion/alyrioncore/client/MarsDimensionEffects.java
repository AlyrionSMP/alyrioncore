package xyz.alyrion.alyrioncore.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import xyz.alyrion.alyrioncore.client.weather.MarsClientWeatherHandler;

public class MarsDimensionEffects extends DimensionSpecialEffects {
    private final float[] sunriseCol = new float[4];

    public MarsDimensionEffects() {
        // cloudHeight = Float.NaN (no clouds in thin atmosphere), hasGround = true, skyType = NORMAL, forceBrightLightmap = false, constantAmbientLight = false
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Mars atmosphere has fine suspended ferric oxide dust scattering light,
        // giving the ambient daytime fog an authentic butterscotch/rusty-amber tone.
        float intensity = MarsClientWeatherHandler.getSmoothedIntensity();
        float dimFactor = 1.0F - intensity * 0.55F;

        double r = (0.85D * (1.0D - intensity * 0.20D)) * (fogColor.x * 0.4D + 0.6D);
        double g = (0.52D * (1.0D - intensity * 0.40D)) * (fogColor.y * 0.4D + 0.6D);
        double b = (0.32D * (1.0D - intensity * 0.55D)) * (fogColor.z * 0.4D + 0.6D);
        return new Vec3(r * brightness * dimFactor, g * brightness * dimFactor, b * brightness * dimFactor);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        float intensity = MarsClientWeatherHandler.getSmoothedIntensity();
        if (intensity > 0.65F) {
            // Sun is obscured behind uniform ochre dust haze during severe storms
            return null;
        }

        // Scientifically authentic Martian Blue Sunset / Sunrise!
        // On Mars, fine dust particles cause forward Mie scattering, allowing blue wavelengths
        // to penetrate the atmosphere directly around the setting Sun while red is scattered away.
        float f = Mth.cos(timeOfDay * ((float) Math.PI * 2F)) - 0.0F;
        if (f >= -0.4F && f <= 0.4F) {
            float f1 = (f + 0.0F) / 0.4F * 0.5F + 0.5F;
            float f2 = 1.0F - (1.0F - Mth.sin(f1 * (float) Math.PI)) * 0.99F;
            f2 *= f2;
            this.sunriseCol[0] = (f1 * 0.25F + 0.15F) * (1.0F - intensity * 0.5F);
            this.sunriseCol[1] = (f1 * f1 * 0.55F + 0.35F) * (1.0F - intensity * 0.4F);
            this.sunriseCol[2] = (f1 * f1 * 0.95F + 0.55F) * (1.0F - intensity * 0.3F);
            this.sunriseCol[3] = f2 * (1.0F - intensity * 0.7F);
            return this.sunriseCol;
        } else {
            return null;
        }
    }
}

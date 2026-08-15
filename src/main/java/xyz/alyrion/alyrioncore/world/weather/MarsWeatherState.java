package xyz.alyrion.alyrioncore.world.weather;

import net.minecraft.util.StringRepresentable;

public enum MarsWeatherState implements StringRepresentable {
    CLEAR("clear", "Clear Skies", 0.0F, 0.05F),
    DUST_DEVILS("dust_devils", "Dust Devil Activity", 0.2F, 0.3F),
    REGIONAL_STORM("regional_storm", "Regional Dust Storm", 0.65F, 0.75F),
    GLOBAL_DUST_STORM("global_dust_storm", "Global Planet-Encircling Dust Storm", 1.0F, 1.0F);

    private final String name;
    private final String displayName;
    private final float baseIntensity;
    private final float maxWindSpeed;

    MarsWeatherState(String name, String displayName, float baseIntensity, float maxWindSpeed) {
        this.name = name;
        this.displayName = displayName;
        this.baseIntensity = baseIntensity;
        this.maxWindSpeed = maxWindSpeed;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public float getBaseIntensity() {
        return this.baseIntensity;
    }

    public float getMaxWindSpeed() {
        return this.maxWindSpeed;
    }

    public static MarsWeatherState byName(String name) {
        for (MarsWeatherState state : values()) {
            if (state.name.equalsIgnoreCase(name)) {
                return state;
            }
        }
        return CLEAR;
    }
}

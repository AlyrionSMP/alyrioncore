package xyz.alyrion.alyrioncore.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import xyz.alyrion.alyrioncore.AlyrionCore;

public class ModDimensions {
    // Dimension & Dimension Type
    public static final ResourceKey<Level> MARS_LEVEL =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars"));

    public static final ResourceKey<DimensionType> MARS_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars_type"));

    public static final ResourceKey<NoiseGeneratorSettings> MARS_NOISE_SETTINGS =
            ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars_noise_settings"));

    // Scientifically Correct Martian Biomes
    public static final ResourceKey<Biome> VASTITAS_BOREALIS =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "vastitas_borealis"));

    public static final ResourceKey<Biome> VALLES_MARINERIS =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "valles_marineris"));

    public static final ResourceKey<Biome> THARSIS_VOLCANIC_PLATEAU =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "tharsis_volcanic_plateau"));

    public static final ResourceKey<Biome> PLANUM_BOREUM =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "planum_boreum"));

    public static final ResourceKey<Biome> NOACHIS_TERRA =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "noachis_terra"));
}

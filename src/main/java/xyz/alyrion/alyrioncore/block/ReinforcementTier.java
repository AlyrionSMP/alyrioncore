package xyz.alyrion.alyrioncore.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import xyz.alyrion.alyrioncore.item.ReinforcementPlateItem;
import xyz.alyrion.alyrioncore.registry.ModItems;

import java.util.function.Supplier;

/**
 * The four reinforcement tiers. Each tier maps to its plate item, how many
 * full mining cycles a reinforced block must absorb before it really breaks,
 * and the sounds/particles used when a hit is absorbed.
 */
public enum ReinforcementTier implements StringRepresentable {
    IRON("iron", 3, () -> ModItems.IRON_REINFORCEMENT_PLATE.get(),
            SoundEvents.METAL_HIT, SoundEvents.METAL_PLACE, SoundType.METAL),
    DIAMOND("diamond", 10, () -> ModItems.DIAMOND_REINFORCEMENT_PLATE.get(),
            SoundEvents.METAL_HIT, SoundEvents.METAL_PLACE, SoundType.METAL),
    METEORIC_IRON("meteoric_iron", 30, () -> ModItems.METEORIC_IRON_REINFORCEMENT_PLATE.get(),
            SoundEvents.METAL_HIT, SoundEvents.METAL_PLACE, SoundType.METAL),
    NETHERITE("netherite", 100, () -> ModItems.NETHERITE_REINFORCEMENT_PLATE.get(),
            SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_PLACE, SoundType.NETHERITE_BLOCK);

    private final String name;
    private final int hits;
    private final Supplier<ReinforcementPlateItem> plate;
    private final SoundEvent hitSound;
    private final SoundEvent placeSound;
    private final SoundType soundType;

    ReinforcementTier(String name, int hits, Supplier<ReinforcementPlateItem> plate,
                      SoundEvent hitSound, SoundEvent placeSound, SoundType soundType) {
        this.name = name;
        this.hits = hits;
        this.plate = plate;
        this.hitSound = hitSound;
        this.placeSound = placeSound;
        this.soundType = soundType;
    }

    /** How many mining cycles a reinforced block absorbs before it breaks. */
    public int getHits() {
        return this.hits;
    }

    /** The plate item that applies this tier. */
    public ReinforcementPlateItem getPlate() {
        return this.plate.get();
    }

    /** Metallic "clang" played when a mining cycle is absorbed. */
    public SoundEvent getHitSound() {
        return this.hitSound;
    }

    /** Sound played when a plate is bolted onto a block. */
    public SoundEvent getPlaceSound() {
        return this.placeSound;
    }

    public SoundType getSoundType() {
        return this.soundType;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** The tier belonging to a plate item, or {@code null} if the item is not a plate. */
    public static ReinforcementTier fromItem(Item item) {
        for (ReinforcementTier tier : values()) {
            if (tier.getPlate() == item) {
                return tier;
            }
        }
        return null;
    }
}

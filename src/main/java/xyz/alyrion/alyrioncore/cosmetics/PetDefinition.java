package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xyz.alyrion.alyrioncore.AlyrionCore;

public enum PetDefinition {
    SATELLITE(
            "satellite",
            "Satellite Pet",
            "A little research satellite that orbits above your head!",
            15,
            false
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final int price;
    private final boolean unlockedByDefault;
    private final ResourceLocation textureLocation;

    PetDefinition(String id, String displayName, String description, int price, boolean unlockedByDefault) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.price = price;
        this.unlockedByDefault = unlockedByDefault;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "textures/pets/" + id + ".png");
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayNameComponent() {
        return Component.translatable("pet.alyrioncore." + id + ".name");
    }

    public String getDescription() {
        return description;
    }

    public Component getDescriptionComponent() {
        return Component.translatable("pet.alyrioncore." + id + ".desc");
    }

    public int getPrice() {
        return price;
    }

    public boolean isFree() {
        return price <= 0;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public static PetDefinition fromId(String id) {
        if (id == null) return null;
        for (PetDefinition pet : values()) {
            if (pet.id.equalsIgnoreCase(id)) {
                return pet;
            }
        }
        return null;
    }
}

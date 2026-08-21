package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xyz.alyrion.alyrioncore.AlyrionCore;

/**
 * A single purchasable / earnable cosmetic item (a cape, a pet, a trail, ...).
 *
 * This is the unified replacement for the old {@code CapeDefinition} and
 * {@code PetDefinition} enums: one class for every cosmetic kind. Instances
 * are registered in {@link CosmeticsRegistry}, which is the single place new
 * cosmetics are added.
 *
 * The preview texture convention is {@code textures/<typeId>/<cosmeticId>.png}
 * (e.g. {@code textures/capes/marsian.png}, {@code textures/pets/satellite.png}),
 * which keeps existing assets in place. Types without a texture (like trails)
 * simply never load it.
 */
public final class CosmeticDefinition {

    private final String id;
    private final CosmeticType type;
    private final String displayName;
    private final String description;
    private final int price;
    private final boolean unlockedByDefault;
    private final boolean purchasable;
    private final ResourceLocation textureLocation;

    public CosmeticDefinition(String id, CosmeticType type, String displayName, String description,
                              int price, boolean unlockedByDefault) {
        this(id, type, displayName, description, price, unlockedByDefault, true);
    }

    public CosmeticDefinition(String id, CosmeticType type, String displayName, String description,
                              int price, boolean unlockedByDefault, boolean purchasable) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.price = Math.max(0, price);
        this.unlockedByDefault = unlockedByDefault;
        this.purchasable = purchasable;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(
                AlyrionCore.MODID, "textures/" + type.getTextureFolder() + "/" + id + ".png");
    }

    public String getId() {
        return id;
    }

    public CosmeticType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayNameComponent() {
        return Component.translatable(type.getId() + ".alyrioncore." + id + ".name");
    }

    public String getDescription() {
        return description;
    }

    public Component getDescriptionComponent() {
        return Component.translatable(type.getId() + ".alyrioncore." + id + ".desc");
    }

    public int getPrice() {
        return price;
    }

    /** Whether this cosmetic can be bought with coins in the store (task-only ones cannot). */
    public boolean isPurchasable() {
        return purchasable;
    }

    public boolean isFree() {
        return purchasable && price <= 0;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }
}

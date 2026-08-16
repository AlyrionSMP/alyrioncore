package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xyz.alyrion.alyrioncore.AlyrionCore;

public enum CapeDefinition {
    TWO_YEAR_CELEBRATION(
            "2_year_celebration",
            "2 Year Celebration Cape",
            "Commemorating 2 years of Alyrion with a festive cake!",
            0,
            true,
            true
    ),
    SEASON_8(
            "season_8",
            "Season 8 Cape",
            "Exclusive crimson & gold cape celebrating Season 8!",
            0,
            true,
            true
    ),
    STARS(
            "stars",
            "Stars Cape",
            "Deep space starry sky with an orbiting research satellite.",
            5,
            false,
            true
    ),
    MOON(
            "moon",
            "Moon Cape",
            "Lunar surface overlooking planet Earth in deep space.",
            5,
            false,
            true
    ),
    MARSIAN(
            "marsian",
            "The Martian Cape",
            "Martian rust dunes and Olympus Mons with a green Martian.",
            5,
            false,
            true
    ),
    GRIM(
            "grim",
            "Grim Cape",
            "A black cape bearing a bleached skull, earned with 10 coins or 10 player kills.",
            10,
            false,
            true
    ),
    PRIDE(
            "pride",
            "Pride Cape",
            "A vibrant rainbow cape earned by partying up with at least 4 players (Open Parties and Claims).",
            0,
            false,
            false
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final int price;
    private final boolean unlockedByDefault;
    private final boolean purchasable;
    private final ResourceLocation textureLocation;

    CapeDefinition(String id, String displayName, String description, int price, boolean unlockedByDefault, boolean purchasable) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.price = price;
        this.unlockedByDefault = unlockedByDefault;
        this.purchasable = purchasable;
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "textures/capes/" + id + ".png");
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayNameComponent() {
        return Component.translatable("cape.alyrioncore." + id + ".name");
    }

    public String getDescription() {
        return description;
    }

    public Component getDescriptionComponent() {
        return Component.translatable("cape.alyrioncore." + id + ".desc");
    }

    public int getPrice() {
        return price;
    }

    /** Whether this cape can be bought with coins in the store (the Pride Cape cannot). */
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

    public static CapeDefinition fromId(String id) {
        if (id == null) return null;
        for (CapeDefinition cape : values()) {
            if (cape.id.equalsIgnoreCase(id)) {
                return cape;
            }
        }
        return null;
    }
}

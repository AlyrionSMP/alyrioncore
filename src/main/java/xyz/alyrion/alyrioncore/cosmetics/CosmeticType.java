package xyz.alyrion.alyrioncore.cosmetics;

/**
 * A cosmetic category — the bedrock of the store's "one slot per category"
 * model (like Bedrock marketplace categories / Essentials-style tags).
 *
 * Every player can equip <b>one</b> cosmetic per type at the same time
 * (cape + pet + trail + ...), and each type has its own store tab and its own
 * client-side renderer. Adding a brand-new cosmetic kind later is just:
 * <ol>
 *   <li>add a constant here (e.g. {@code HAT("hat", "Hats")}),</li>
 *   <li>register a {@link CosmeticRenderer} for it in
 *       {@code ClientCosmeticsRenderers},</li>
 *   <li>add {@link CosmeticDefinition}s of that type to the registry.</li>
 * </ol>
 * No data, networking, GUI or render-layer changes are needed.
 */
public enum CosmeticType {
    CAPE("cape", "Capes", "capes"),
    PET("pet", "Pets", "pets"),
    TRAIL("trail", "Trails", "trails");
    // Future categories, e.g.: HAT("hat", "Hats", "hats"), TITLE("title", "Titles", "titles"),
    // BALLOON("balloon", "Balloons", "balloons"), EMOTE("emote", "Emotes", "emotes")

    private final String id;
    private final String displayName;
    /** Asset sub-folder under textures/, e.g. "capes" -> textures/capes/<id>.png. */
    private final String textureFolder;

    CosmeticType(String id, String displayName, String textureFolder) {
        this.id = id;
        this.displayName = displayName;
        this.textureFolder = textureFolder;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTextureFolder() {
        return textureFolder;
    }

    public static CosmeticType fromId(String id) {
        if (id == null) return null;
        for (CosmeticType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}

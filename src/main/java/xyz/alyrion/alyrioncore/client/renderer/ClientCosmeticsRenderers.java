package xyz.alyrion.alyrioncore.client.renderer;

import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Maps every {@link CosmeticType} to its client-side {@link CosmeticRenderer}.
 *
 * This is the single place a new cosmetic kind is wired on the client: add the
 * type constant, register a renderer here, done. {@link CosmeticRenderLayer}
 * and the store GUI pick it up automatically.
 */
public final class ClientCosmeticsRenderers {

    private static final Map<CosmeticType, CosmeticRenderer> RENDERERS = new EnumMap<>(CosmeticType.class);

    static {
        RENDERERS.put(CosmeticType.CAPE, new CapeCosmeticRenderer());
        RENDERERS.put(CosmeticType.PET, new PetCosmeticRenderer());
        RENDERERS.put(CosmeticType.TRAIL, new TrailCosmeticRenderer());
    }

    private ClientCosmeticsRenderers() {
    }

    public static CosmeticRenderer get(CosmeticType type) {
        return type != null ? RENDERERS.get(type) : null;
    }

    public static boolean has(CosmeticType type) {
        return type != null && RENDERERS.containsKey(type);
    }
}

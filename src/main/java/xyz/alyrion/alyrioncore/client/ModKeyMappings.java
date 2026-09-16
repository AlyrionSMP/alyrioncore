package xyz.alyrion.alyrioncore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import xyz.alyrion.alyrioncore.AlyrionCore;

public class ModKeyMappings {
    public static final String CATEGORY_ALYRION = "key.categories.alyrioncore";

    // Unbound by default (GLFW_KEY_UNKNOWN -> InputConstants.UNKNOWN): vanilla Escape
    // handling stays untouched until the player binds a key or mouse button in Controls.
    public static final KeyMapping ESCAPE_KEY = new KeyMapping(
            "key.alyrioncore.escape",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY_ALYRION
    );

    public static final KeyMapping OPEN_STORE = new KeyMapping(
            "key.alyrioncore.open_store",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY_ALYRION
    );

    /**
     * Left mouse is not a valid binding for the escape action: the action fires on the bound key or
     * mouse button, and it consumes the click, so a left-click binding would hijack every
     * interaction and every GUI. Bindings to it are cleared instead of honoured.
     */
    private static final InputConstants.Key FORBIDDEN_ESCAPE_KEY =
            InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_LEFT);

    /**
     * Clears the escape binding if it is left mouse, wherever it came from (Controls screen, a
     * hand-edited {@code options.txt}, another mod).
     *
     * @return true when a forbidden binding was found and cleared
     */
    public static boolean refuseLeftMouseEscapeBinding() {
        if (!ESCAPE_KEY.getKey().equals(FORBIDDEN_ESCAPE_KEY)) {
            return false;
        }
        ESCAPE_KEY.setKey(InputConstants.UNKNOWN);
        Minecraft.getInstance().options.save();
        AlyrionCore.LOGGER.info(
                "Left mouse is not a valid binding for '{}' — binding cleared", ESCAPE_KEY.getName());
        return true;
    }
}

package xyz.alyrion.alyrioncore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final String CATEGORY_ALYRION = "key.categories.alyrioncore";

    public static final KeyMapping ESCAPE_KEY = new KeyMapping(
            "key.alyrioncore.escape",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_ESCAPE,
            CATEGORY_ALYRION
    );
}

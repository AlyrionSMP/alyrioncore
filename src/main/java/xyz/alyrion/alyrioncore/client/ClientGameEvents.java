package xyz.alyrion.alyrioncore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import xyz.alyrion.alyrioncore.AlyrionCore;

import net.neoforged.fml.ModList;
import xyz.alyrion.alyrioncore.client.compat.AtmosphericsCompat;
import xyz.alyrion.alyrioncore.client.compat.SodiumCompat;
import xyz.alyrion.alyrioncore.client.gui.CosmeticStoreScreen;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

@EventBusSubscriber(modid = AlyrionCore.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    /** One-shot guard: options are loaded before the first client tick. */
    private static boolean escapeBindingChecked;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!escapeBindingChecked) {
            escapeBindingChecked = true;
            // A hand-edited options.txt could have loaded with the forbidden binding.
            ModKeyMappings.refuseLeftMouseEscapeBinding();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (mc.screen == null) {
                while (ModKeyMappings.ESCAPE_KEY.consumeClick()) {
                    boolean pauseOnF3 = InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_F3);
                    mc.pauseGame(pauseOnF3);
                }

                while (ModKeyMappings.OPEN_STORE.consumeClick()) {
                    mc.setScreen(new CosmeticStoreScreen());
                }
            }
        }

        // Soft dependency: ATMOSPHERICS owns fog/sky colour in Mars, so hand it our biome profiles.
        if (ModList.get().isLoaded("atmospherics")) {
            AtmosphericsCompat.tick(mc);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Progress is server-side per-world: wipe the client mirror + cape cache
        // so nothing leaks across servers or worlds.
        CosmeticNetworking.clearClientData();

        // Never leave a storm override behind in the player's renderer settings.
        if (ModList.get().isLoaded("sodium")) {
            SodiumCompat.setFogOcclusionSuspended(false);
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (ModKeyMappings.ESCAPE_KEY.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
            if (event.getKeyCode() != GLFW.GLFW_KEY_ESCAPE) {
                Screen screen = event.getScreen();
                if (screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, event.getScanCode(), event.getModifiers())) {
                    event.setCanceled(true);
                } else if (screen.shouldCloseOnEsc()) {
                    screen.onClose();
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenMouseButtonReleased(ScreenEvent.MouseButtonReleased.Post event) {
        // The Controls screen assigns a binding on click; a left-mouse pick is cleared immediately,
        // in the same frame, so it can never be used or saved.
        ModKeyMappings.refuseLeftMouseEscapeBinding();
    }

    @SubscribeEvent
    public static void onScreenMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return; // defence in depth: a left click is never consumed by the escape action
        }
        if (ModKeyMappings.ESCAPE_KEY.isActiveAndMatches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) {
            Screen screen = event.getScreen();
            if (screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0)) {
                event.setCanceled(true);
            } else if (screen.shouldCloseOnEsc()) {
                screen.onClose();
                event.setCanceled(true);
            }
        }
    }
}

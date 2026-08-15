package xyz.alyrion.alyrioncore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import xyz.alyrion.alyrioncore.AlyrionCore;

import xyz.alyrion.alyrioncore.client.gui.CosmeticStoreScreen;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;

@EventBusSubscriber(modid = AlyrionCore.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            CosmeticsManager.get().onClientTick(mc);

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
    public static void onScreenMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
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

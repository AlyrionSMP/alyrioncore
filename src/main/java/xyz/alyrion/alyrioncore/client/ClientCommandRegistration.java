package xyz.alyrion.alyrioncore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.gui.CosmeticStoreScreen;

@EventBusSubscriber(modid = AlyrionCore.MODID, value = Dist.CLIENT)
public class ClientCommandRegistration {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("cosmetics")
                        .executes(ctx -> {
                            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CosmeticStoreScreen()));
                            return 1;
                        })
        );

        event.getDispatcher().register(
                Commands.literal("store")
                        .executes(ctx -> {
                            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CosmeticStoreScreen()));
                            return 1;
                        })
        );
    }
}

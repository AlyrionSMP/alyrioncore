package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.List;

/**
 * Server-side progression driver for the cosmetics & rewards system.
 *
 * All playtime accumulation, coin awards and task detection run here on the
 * server thread, so progression is identical no matter which client connects,
 * and it is persisted in the world's saved data.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID)
public class ServerCosmeticsEvents {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        ServerCosmeticsManager manager = ServerCosmeticsManager.get();

        // Track survival playtime every 20 ticks (1 second)
        if (tickCounter % 20 == 0) {
            for (ServerPlayer player : players) {
                manager.tickPlaytime(player);
            }
        }

        // Check natural task triggers every 5 ticks (instant responsiveness)
        if (tickCounter % 5 == 0) {
            for (ServerPlayer player : players) {
                manager.checkTasks(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerKill(LivingDeathEvent event) {
        // Only credit player-vs-player kills made directly in survival
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (killer == victim) return;
        if (killer.isCreative() || killer.isSpectator()) return;

        ServerCosmeticsManager.get().onPlayerKill(killer);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ServerCosmeticsManager manager = ServerCosmeticsManager.get();

        // Push the joining player their own full cosmetics state
        manager.syncToPlayer(serverPlayer);

        // Let everyone already online see the joiner's cape & pet
        manager.broadcastCape(serverPlayer);
        manager.broadcastPet(serverPlayer);

        // Let the joiner see the capes & pets of everyone already online
        for (ServerPlayer other : serverPlayer.server.getPlayerList().getPlayers()) {
            if (other != serverPlayer) {
                manager.sendCapeTo(serverPlayer, other);
                manager.sendPetTo(serverPlayer, other);
            }
        }
    }
}

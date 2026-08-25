package xyz.alyrion.alyrioncore.store;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticSound;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsSavedData;
import xyz.alyrion.alyrioncore.cosmetics.PlayerCosmeticsData;
import xyz.alyrion.alyrioncore.cosmetics.ServerCosmeticsManager;
import xyz.alyrion.alyrioncore.item.CrateItem;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Server-authoritative item pack fulfillment.
 *
 * Purchasing a pack deducts coins from the same wallet as cosmetics
 * ({@link PlayerCosmeticsData}) and hands out the pack as a {@link CrateItem}
 * — a portable chest whose contents live in the stack's CONTAINER component.
 * Any contents that don't fit inside the crate are given directly. Unlike
 * cosmetics, packs are consumables — there is no "owned" state to persist.
 */
public class ServerItemPackManager {

    private ServerItemPackManager() {
    }

    public static void purchase(ServerPlayer player, String packId) {
        ItemPackDefinition pack = ItemPacksRegistry.fromId(packId);
        if (pack == null) return;

        PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(player);
        if (data.getCoins() < pack.price()) {
            // Not enough coins: reject and re-sync the true state
            ServerCosmeticsManager.get().syncToPlayer(player);
            return;
        }

        data.setCoins(data.getCoins() - pack.price());
        CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
        if (savedData != null) {
            savedData.setDirty();
        }

        List<ItemStack> overflow = new ArrayList<>();
        ItemStack crate = CrateItem.createFilled(pack.contents(), overflow);
        ItemHandlerHelper.giveItemToPlayer(player, crate);
        for (ItemStack extra : overflow) {
            ItemHandlerHelper.giveItemToPlayer(player, extra.copy());
        }

        ServerCosmeticsManager.get().syncToPlayer(player);
        player.displayClientMessage(Component.literal(
                "§6§l[Alyrion SMP] §aPurchased " + pack.displayName()
                        + "! §7(§6-" + pack.price() + " Coins§7)"), false);
        PacketDistributor.sendToPlayer(player,
                new CosmeticNetworking.S2CPlaySoundPayload(CosmeticSound.SUCCESS.getId()));
    }
}

package xyz.alyrion.alyrioncore.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticSound;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsSavedData;
import xyz.alyrion.alyrioncore.cosmetics.PlayerCosmeticsData;
import xyz.alyrion.alyrioncore.cosmetics.ServerCosmeticsManager;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Soft-dependency integration with Create: Numismatics.
 *
 * Provides bidirectional 1:1 conversion between AlyrionCore Coins and Numismatics Spurs (numismatics:spur).
 * Never requires Create Numismatics at compile or runtime; lookups degrade gracefully when the mod is absent.
 */
public final class NumismaticsCompat {

    public static final String NUMISMATICS_MOD_ID = "numismatics";
    public static final ResourceLocation SPUR_ID = ResourceLocation.fromNamespaceAndPath(NUMISMATICS_MOD_ID, "spur");

    private NumismaticsCompat() {
    }

    /** True if Create: Numismatics is loaded on this instance. */
    public static boolean isNumismaticsInstalled() {
        return ModList.get().isLoaded(NUMISMATICS_MOD_ID);
    }

    /** Returns the Spur item instance, or Items.AIR if not registered/loaded. */
    public static Item getSpurItem() {
        return BuiltInRegistries.ITEM.get(SPUR_ID);
    }

    /** True if the Spur item is registered and accessible. */
    public static boolean isSpurAvailable() {
        Item item = getSpurItem();
        return item != null && item != Items.AIR;
    }

    /** Count total spurs currently in the player's main inventory. */
    public static int countSpurs(Player player) {
        if (player == null) return 0;
        Item spur = getSpurItem();
        if (spur == null || spur == Items.AIR) return 0;

        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(spur)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Converts AlyrionCore coins into physical Numismatics Spurs.
     * Deducts coins from PlayerCosmeticsData and hands spurs directly to the player's inventory.
     */
    public static boolean convertCoinsToSpurs(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;

        if (!isNumismaticsInstalled() || !isSpurAvailable()) {
            player.displayClientMessage(Component.literal("§cCreate: Numismatics is not installed on this server."), false);
            return false;
        }

        PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(player);
        if (data.getCoins() < amount) {
            player.displayClientMessage(Component.literal("§cYou do not have enough Alyrion Coins (have: " + data.getCoins() + ", need: " + amount + ")."), false);
            ServerCosmeticsManager.get().syncToPlayer(player);
            return false;
        }

        data.setCoins(data.getCoins() - amount);
        CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
        if (savedData != null) {
            savedData.setDirty();
        }

        Item spur = getSpurItem();
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(spur, amount));

        ServerCosmeticsManager.get().syncToPlayer(player);
        player.displayClientMessage(Component.literal(
                "§6§l[Alyrion SMP] §aConverted §6" + amount + " Coin" + (amount > 1 ? "s" : "")
                        + " §ainto §e" + amount + " Spur" + (amount > 1 ? "s" : "") + "!"), false);
        PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CPlaySoundPayload(CosmeticSound.SUCCESS.getId()));
        return true;
    }

    /**
     * Converts physical Numismatics Spurs from the player's inventory into AlyrionCore coins.
     * Takes spurs from inventory and credits coins to PlayerCosmeticsData.
     */
    public static boolean convertSpursToCoins(ServerPlayer player, int amount) {
        if (player == null || amount <= 0) return false;

        if (!isNumismaticsInstalled() || !isSpurAvailable()) {
            player.displayClientMessage(Component.literal("§cCreate: Numismatics is not installed on this server."), false);
            return false;
        }

        int available = countSpurs(player);
        if (available < amount) {
            player.displayClientMessage(Component.literal("§cYou do not have enough Spurs in your inventory (have: " + available + ", need: " + amount + ")."), false);
            return false;
        }

        Item spur = getSpurItem();
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(spur)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        player.containerMenu.broadcastChanges();

        PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(player);
        data.addCoins(amount);
        CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
        if (savedData != null) {
            savedData.setDirty();
        }

        ServerCosmeticsManager.get().syncToPlayer(player);
        player.displayClientMessage(Component.literal(
                "§6§l[Alyrion SMP] §aConverted §e" + amount + " Spur" + (amount > 1 ? "s" : "")
                        + " §ainto §6" + amount + " Coin" + (amount > 1 ? "s" : "") + "!"), false);
        PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CPlaySoundPayload(CosmeticSound.SUCCESS.getId()));
        return true;
    }
}

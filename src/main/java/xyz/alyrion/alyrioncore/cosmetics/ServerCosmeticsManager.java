package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Server-authoritative cosmetics & rewards manager.
 *
 * Every progression decision (purchasing a cosmetic, equipping it, completing a
 * task, earning playtime coins, dev overrides) is made here, on the server,
 * against the per-world {@link CosmeticsSavedData}. Clients never decide
 * anything themselves: they request an action via a C2S payload and receive the
 * resulting state back via the S2C sync payloads.
 *
 * All methods are type-agnostic — a "cosmetic" is any {@link CosmeticDefinition}
 * of any {@link CosmeticType}. New cosmetic kinds need zero changes here.
 */
public class ServerCosmeticsManager {
    private static final ServerCosmeticsManager INSTANCE = new ServerCosmeticsManager();

    private ServerCosmeticsManager() {
    }

    public static ServerCosmeticsManager get() {
        return INSTANCE;
    }

    public PlayerCosmeticsData getPlayerData(ServerPlayer player) {
        CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
        return savedData != null ? savedData.getOrCreate(player.getUUID()) : new PlayerCosmeticsData();
    }

    // --- Sync ---

    public void syncToPlayer(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        List<CosmeticNetworking.S2CSyncCosmeticsPayload.EquippedSlot> slots = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.getEquippedSlots().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                slots.add(new CosmeticNetworking.S2CSyncCosmeticsPayload.EquippedSlot(entry.getKey(), entry.getValue()));
            }
        }
        PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CSyncCosmeticsPayload(
                data.getCoins(),
                data.getSurvivalPlaytimeSeconds(),
                new HashSet<>(data.getUnlockedCosmetics()),
                slots,
                new HashSet<>(data.getCompletedTasks())
        ));
    }

    /** Broadcast one equipped slot of a player to everyone tracking them (and themselves). */
    public void broadcastSlot(ServerPlayer player, CosmeticType type) {
        if (type == null) return;
        PlayerCosmeticsData data = getPlayerData(player);
        String id = data.getEquippedSlot(type.getId());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new CosmeticNetworking.S2CSyncCosmeticPayload(player.getUUID(), type.getId(), id != null ? id : ""));
    }

    /** Send one equipped slot of a target player to a specific recipient (used on login). */
    public void sendSlotTo(ServerPlayer recipient, ServerPlayer target, CosmeticType type) {
        if (type == null) return;
        PlayerCosmeticsData data = getPlayerData(target);
        String id = data.getEquippedSlot(type.getId());
        PacketDistributor.sendToPlayer(recipient,
                new CosmeticNetworking.S2CSyncCosmeticPayload(target.getUUID(), type.getId(), id != null ? id : ""));
    }

    // --- Store actions ---

    public boolean purchase(ServerPlayer player, String cosmeticId) {
        CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
        if (cosmetic == null) return false;
        if (!cosmetic.isPurchasable()) {
            // Task-only cosmetics can never be bought, only earned
            syncToPlayer(player);
            return false;
        }

        PlayerCosmeticsData data = getPlayerData(player);
        if (data.isCosmeticUnlocked(cosmetic.getId())) {
            // Already owned: just equip it
            equip(player, cosmetic.getType().getId(), cosmetic.getId());
            return true;
        }

        if (data.getCoins() >= cosmetic.getPrice()) {
            data.setCoins(data.getCoins() - cosmetic.getPrice());
            data.unlockCosmetic(cosmetic.getId());
            data.setEquippedSlot(cosmetic.getType().getId(), cosmetic.getId());
            markDirty(player);

            syncToPlayer(player);
            broadcastSlot(player, cosmetic.getType());
            notify(player,
                    "§6§l[Alyrion SMP] §aUnlocked " + cosmetic.getDisplayName() + "! §7(§6-" + cosmetic.getPrice() + " Coins§7)",
                    CosmeticSound.SUCCESS);
            return true;
        }
        return false;
    }

    public void equip(ServerPlayer player, String typeId, String cosmeticId) {
        if (typeId == null || typeId.isEmpty()) return;
        if (cosmeticId == null || cosmeticId.isEmpty()) {
            unequipSlot(player, typeId);
            return;
        }

        CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
        if (cosmetic == null || !cosmetic.getType().getId().equalsIgnoreCase(typeId)) return;

        PlayerCosmeticsData data = getPlayerData(player);
        if (!data.isCosmeticUnlocked(cosmetic.getId())) {
            // Server rejects equipping cosmetics the player hasn't unlocked
            syncToPlayer(player);
            return;
        }

        data.setEquippedSlot(typeId, cosmetic.getId());
        markDirty(player);

        syncToPlayer(player);
        broadcastSlot(player, cosmetic.getType());
        notify(player, "§6§l[Alyrion SMP] §aEquipped " + cosmetic.getDisplayName() + ".", CosmeticSound.CLICK);
    }

    public void unequipSlot(ServerPlayer player, String typeId) {
        CosmeticType type = CosmeticType.fromId(typeId);
        if (type == null) return;

        PlayerCosmeticsData data = getPlayerData(player);
        data.setEquippedSlot(typeId, null);
        markDirty(player);

        syncToPlayer(player);
        broadcastSlot(player, type);
        notify(player, "§6§l[Alyrion SMP] §7" + type.getDisplayName() + " cosmetic unequipped.", CosmeticSound.CLICK);
    }

    // --- Progression ---

    /** Called once per second of server time per online player. */
    public void tickPlaytime(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        PlayerCosmeticsData data = getPlayerData(player);
        long prev = data.getSurvivalPlaytimeSeconds();
        data.incrementSurvivalPlaytime();
        long current = data.getSurvivalPlaytimeSeconds();

        if (current > 0 && current % CosmeticConfig.PLAYTIME_SECONDS_PER_COIN == 0) {
            data.addCoins(1);
            markDirty(player);
            syncToPlayer(player);
            notify(player,
                    "§6§l[Alyrion SMP] §e+1 Coin §fearned for 1 hour of survival playtime! (Total: §6" + data.getCoins() + " Coins§f)",
                    CosmeticSound.LEVEL_UP);
        } else if (current % 60 == 0) {
            // Periodic save every minute
            markDirty(player);
        }
    }

    /** Called when this player kills another player; feeds kill-count based tasks. */
    public void onPlayerKill(ServerPlayer killer) {
        PlayerCosmeticsData data = getPlayerData(killer);
        data.incrementPvpKills();
        markDirty(killer);
        syncToPlayer(killer);
        checkTasks(killer);
    }

    public void checkTasks(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        for (TaskDefinition task : TaskDefinition.values()) {
            boolean taskDone = data.isTaskCompleted(task.getId());
            boolean rewardUnlocked = task.getReward() == null || data.isCosmeticUnlocked(task.getReward().getId());

            if (!taskDone || !rewardUnlocked) {
                if (task.test(player)) {
                    completeTask(player, task, false);
                }
            }
        }
    }

    public void completeTask(ServerPlayer player, TaskDefinition task, boolean isManualDev) {
        if (task == null) return;

        PlayerCosmeticsData data = getPlayerData(player);
        boolean alreadyDone = data.isTaskCompleted(task.getId());

        if (!alreadyDone || isManualDev) {
            data.completeTask(task.getId());
            if (!alreadyDone || isManualDev) {
                data.addCoins(task.getCoinReward());
            }
            if (task.getReward() != null) {
                data.unlockCosmetic(task.getReward().getId());
            }
            data.sanitize();
            markDirty(player);

            syncToPlayer(player);
            if (task.getReward() != null) {
                broadcastSlot(player, task.getReward().getType());
            }

            String prefix = isManualDev ? "§d§l[DEV TASK COMPLETED] §f" : "§6§l[TASK COMPLETED] §f";
            String rewardNotice = task.getReward() != null ? " + §bUnlocked " + task.getReward().getDisplayName() + "!" : "";
            notify(player,
                    prefix + "§a" + task.getTitle() + " §7(§6+" + task.getCoinReward() + " Coins" + rewardNotice + "§7)",
                    CosmeticSound.LEVEL_UP);
        }
    }

    // --- Dev / admin overrides (ops only, see CosmeticsCommands) ---

    public void devAddCoins(ServerPlayer target, int amount) {
        PlayerCosmeticsData data = getPlayerData(target);
        data.addCoins(amount);
        markDirty(target);
        syncToPlayer(target);
        notify(target, "§d[DEV] §aAdded " + amount + " Coins. (Total: " + data.getCoins() + ")", CosmeticSound.CLICK);
    }

    public void devAddPlaytime(ServerPlayer target, long seconds) {
        PlayerCosmeticsData data = getPlayerData(target);
        long prev = data.getSurvivalPlaytimeSeconds();
        data.setSurvivalPlaytimeSeconds(prev + seconds);
        int coinsEarned = (int) ((data.getSurvivalPlaytimeSeconds() / CosmeticConfig.PLAYTIME_SECONDS_PER_COIN)
                - (prev / CosmeticConfig.PLAYTIME_SECONDS_PER_COIN));
        if (coinsEarned > 0) {
            data.addCoins(coinsEarned);
        }
        markDirty(target);
        syncToPlayer(target);
        notify(target, "§d[DEV] §aAdded " + (seconds / 60) + " minutes of survival playtime. (+" + coinsEarned + " coins)", CosmeticSound.LEVEL_UP);
    }

    public void devUnlock(ServerPlayer target, String cosmeticId) {
        CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
        if (cosmetic == null) return;
        PlayerCosmeticsData data = getPlayerData(target);
        data.unlockCosmetic(cosmetic.getId());
        markDirty(target);
        syncToPlayer(target);
        notify(target, "§d[DEV] §aUnlocked " + cosmetic.getDisplayName() + ".", CosmeticSound.CLICK);
    }

    public void devResetAllTasks(ServerPlayer target) {
        PlayerCosmeticsData data = getPlayerData(target);
        data.resetAllTasks();
        markDirty(target);
        syncToPlayer(target);
        notify(target, "§d[DEV] §cAll task progression has been reset.", CosmeticSound.CLICK);
    }

    public void devResetCosmetics(ServerPlayer target) {
        PlayerCosmeticsData data = getPlayerData(target);
        data.resetCosmetics();
        markDirty(target);
        syncToPlayer(target);
        for (CosmeticType type : CosmeticType.values()) {
            broadcastSlot(target, type);
        }
        notify(target, "§d[DEV] §cAll cosmetic unlocks have been reset.", CosmeticSound.CLICK);
    }

    // --- Helpers ---

    private void markDirty(ServerPlayer player) {
        CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
        if (savedData != null) {
            savedData.setDirty();
        }
    }

    private void notify(ServerPlayer player, String message, CosmeticSound sound) {
        player.displayClientMessage(Component.literal(message), false);
        if (sound != CosmeticSound.NONE) {
            PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CPlaySoundPayload(sound.getId()));
        }
    }
}

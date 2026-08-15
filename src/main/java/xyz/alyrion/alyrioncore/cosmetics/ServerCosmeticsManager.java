package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

import java.util.HashSet;

/**
 * Server-authoritative cosmetics & rewards manager.
 *
 * Every progression decision (purchasing a cape, equipping it, completing a
 * task, earning playtime coins, dev overrides) is made here, on the server,
 * against the per-world {@link CosmeticsSavedData}. Clients never decide
 * anything themselves: they request an action via a C2S payload and receive the
 * resulting state back via the S2C sync payloads.
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
        PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CSyncCosmeticsPayload(
                data.getCoins(),
                data.getSurvivalPlaytimeSeconds(),
                new HashSet<>(data.getUnlockedCapes()),
                data.getEquippedCapeId() != null ? data.getEquippedCapeId() : "",
                new HashSet<>(data.getCompletedTasks()),
                new CosmeticNetworking.S2CSyncCosmeticsPayload.PetState(
                        new HashSet<>(data.getUnlockedPets()),
                        data.getEquippedPetId() != null ? data.getEquippedPetId() : ""
                )
        ));
    }

    /** Broadcast a single player's equipped cape to everyone tracking them (and themselves). */
    public void broadcastCape(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        String capeId = data.getEquippedCapeId() != null ? data.getEquippedCapeId() : "";
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new CosmeticNetworking.S2CSyncCapePayload(player.getUUID(), capeId));
    }

    /** Send another player's equipped cape to a specific recipient (used when someone logs in). */
    public void sendCapeTo(ServerPlayer recipient, ServerPlayer target) {
        PlayerCosmeticsData data = getPlayerData(target);
        String capeId = data.getEquippedCapeId() != null ? data.getEquippedCapeId() : "";
        PacketDistributor.sendToPlayer(recipient, new CosmeticNetworking.S2CSyncCapePayload(target.getUUID(), capeId));
    }

    /** Broadcast a single player's equipped pet to everyone tracking them (and themselves). */
    public void broadcastPet(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        String petId = data.getEquippedPetId() != null ? data.getEquippedPetId() : "";
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new CosmeticNetworking.S2CSyncPetPayload(player.getUUID(), petId));
    }

    /** Send another player's equipped pet to a specific recipient (used when someone logs in). */
    public void sendPetTo(ServerPlayer recipient, ServerPlayer target) {
        PlayerCosmeticsData data = getPlayerData(target);
        String petId = data.getEquippedPetId() != null ? data.getEquippedPetId() : "";
        PacketDistributor.sendToPlayer(recipient, new CosmeticNetworking.S2CSyncPetPayload(target.getUUID(), petId));
    }

    // --- Store actions ---

    public boolean purchaseCape(ServerPlayer player, String capeId) {
        CapeDefinition cape = CapeDefinition.fromId(capeId);
        if (cape == null) return false;

        PlayerCosmeticsData data = getPlayerData(player);
        if (data.isCapeUnlocked(cape.getId())) {
            // Already owned: just equip it
            equipCape(player, cape.getId());
            return true;
        }

        if (data.getCoins() >= cape.getPrice()) {
            data.setCoins(data.getCoins() - cape.getPrice());
            data.unlockCape(cape.getId());
            data.setEquippedCapeId(cape.getId());
            markDirty(player);

            syncToPlayer(player);
            broadcastCape(player);
            notify(player,
                    "§6§l[Alyrion SMP] §aUnlocked " + cape.getDisplayName() + "! §7(§6-" + cape.getPrice() + " Coins§7)",
                    CosmeticSound.SUCCESS);
            return true;
        }
        return false;
    }

    public void equipCape(ServerPlayer player, String capeId) {
        if (capeId == null || capeId.isEmpty()) {
            unequipCape(player);
            return;
        }

        CapeDefinition cape = CapeDefinition.fromId(capeId);
        if (cape == null) return;

        PlayerCosmeticsData data = getPlayerData(player);
        if (!data.isCapeUnlocked(cape.getId())) {
            // Server rejects equipping capes the player hasn't unlocked
            syncToPlayer(player);
            return;
        }

        data.setEquippedCapeId(cape.getId());
        markDirty(player);

        syncToPlayer(player);
        broadcastCape(player);
        notify(player, "§6§l[Alyrion SMP] §aEquipped " + cape.getDisplayName() + ".", CosmeticSound.CLICK);
    }

    public void unequipCape(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        data.setEquippedCapeId(null);
        markDirty(player);

        syncToPlayer(player);
        broadcastCape(player);
        notify(player, "§6§l[Alyrion SMP] §7Cape unequipped.", CosmeticSound.CLICK);
    }

    // --- Pet store actions ---

    public boolean purchasePet(ServerPlayer player, String petId) {
        PetDefinition pet = PetDefinition.fromId(petId);
        if (pet == null) return false;

        PlayerCosmeticsData data = getPlayerData(player);
        if (data.isPetUnlocked(pet.getId())) {
            // Already owned: just equip it
            equipPet(player, pet.getId());
            return true;
        }

        if (data.getCoins() >= pet.getPrice()) {
            data.setCoins(data.getCoins() - pet.getPrice());
            data.unlockPet(pet.getId());
            data.setEquippedPetId(pet.getId());
            markDirty(player);

            syncToPlayer(player);
            broadcastPet(player);
            notify(player,
                    "§6§l[Alyrion SMP] §aUnlocked " + pet.getDisplayName() + "! §7(§6-" + pet.getPrice() + " Coins§7)",
                    CosmeticSound.SUCCESS);
            return true;
        }
        return false;
    }

    public void equipPet(ServerPlayer player, String petId) {
        if (petId == null || petId.isEmpty()) {
            unequipPet(player);
            return;
        }

        PetDefinition pet = PetDefinition.fromId(petId);
        if (pet == null) return;

        PlayerCosmeticsData data = getPlayerData(player);
        if (!data.isPetUnlocked(pet.getId())) {
            // Server rejects equipping pets the player hasn't unlocked
            syncToPlayer(player);
            return;
        }

        data.setEquippedPetId(pet.getId());
        markDirty(player);

        syncToPlayer(player);
        broadcastPet(player);
        notify(player, "§6§l[Alyrion SMP] §aEquipped " + pet.getDisplayName() + ".", CosmeticSound.CLICK);
    }

    public void unequipPet(ServerPlayer player) {
        PlayerCosmeticsData data = getPlayerData(player);
        data.setEquippedPetId(null);
        markDirty(player);

        syncToPlayer(player);
        broadcastPet(player);
        notify(player, "§6§l[Alyrion SMP] §7Pet unequipped.", CosmeticSound.CLICK);
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
            boolean capeUnlocked = task.getCapeReward() == null || data.isCapeUnlocked(task.getCapeReward().getId());

            if (!taskDone || !capeUnlocked) {
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
            if (task.getCapeReward() != null) {
                data.unlockCape(task.getCapeReward().getId());
            }
            data.sanitize();
            markDirty(player);

            syncToPlayer(player);
            broadcastCape(player);

            String prefix = isManualDev ? "§d§l[DEV TASK COMPLETED] §f" : "§6§l[TASK COMPLETED] §f";
            String capeNotice = task.getCapeReward() != null ? " + §bUnlocked " + task.getCapeReward().getDisplayName() + "!" : "";
            notify(player,
                    prefix + "§a" + task.getTitle() + " §7(§6+" + task.getCoinReward() + " Coins" + capeNotice + "§7)",
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
        broadcastCape(target);
        broadcastPet(target);
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

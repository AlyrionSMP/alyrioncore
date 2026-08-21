package xyz.alyrion.alyrioncore.cosmetics;

import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Client-side mirror of the player's cosmetics & rewards state.
 *
 * The client no longer owns any progression: coins, playtime, unlocks, task
 * completions and the equipped cosmetics are all decided and persisted by the
 * server. This manager only caches the latest state the server synced to us
 * (via {@code S2CSyncCosmeticsPayload}) and forwards store actions to the
 * server as C2S requests.
 *
 * Everything is type-agnostic: any {@link CosmeticDefinition} of any
 * {@link CosmeticType} works through the same four operations
 * (purchase / equip / unequip / get equipped).
 */
public class CosmeticsManager {
    private static final CosmeticsManager INSTANCE = new CosmeticsManager();

    private PlayerCosmeticsData data = new PlayerCosmeticsData();
    private boolean synced = false;
    private int revision = 0;

    private CosmeticsManager() {
    }

    public static CosmeticsManager get() {
        return INSTANCE;
    }

    /** Apply the authoritative state received from the server. */
    public void applySync(CosmeticNetworking.S2CSyncCosmeticsPayload payload) {
        PlayerCosmeticsData newData = new PlayerCosmeticsData();
        newData.setCoins(payload.coins());
        newData.setSurvivalPlaytimeSeconds(payload.survivalPlaytimeSeconds());
        newData.getUnlockedCosmetics().addAll(payload.unlockedCosmetics());
        for (CosmeticNetworking.S2CSyncCosmeticsPayload.EquippedSlot slot : payload.equippedSlots()) {
            newData.setEquippedSlot(slot.typeId(), slot.cosmeticId());
        }
        newData.getCompletedTasks().addAll(payload.completedTasks());
        newData.sanitize();

        this.data = newData;
        this.synced = true;
        this.revision++;
    }

    /** Clear the mirror when leaving a server so stale progress doesn't leak across worlds. */
    public void resetForDisconnect() {
        this.data = new PlayerCosmeticsData();
        this.synced = false;
        this.revision++;
    }

    /** Ask the server for the authoritative state if we haven't received it yet. */
    public void ensureSynced() {
        if (!synced) {
            CosmeticNetworking.sendRequestSync();
        }
    }

    /** Monotonic revision counter, bumped on every sync; lets GUIs refresh live. */
    public int getRevision() {
        return revision;
    }

    public PlayerCosmeticsData getData() {
        return data;
    }

    public int getCoins() {
        return data.getCoins();
    }

    public long getPlaytimeSeconds() {
        return data.getSurvivalPlaytimeSeconds();
    }

    // --- Generic cosmetic ops ---

    public boolean isUnlocked(CosmeticDefinition def) {
        if (def == null) return false;
        return data.isCosmeticUnlocked(def.getId());
    }

    public boolean isEquipped(CosmeticDefinition def) {
        if (def == null) return false;
        return def.getId().equalsIgnoreCase(data.getEquippedSlot(def.getType().getId()));
    }

    public CosmeticDefinition getEquipped(CosmeticType type) {
        if (type == null) return null;
        return CosmeticsRegistry.fromId(data.getEquippedSlot(type.getId()));
    }

    public boolean isSlotEquipped(CosmeticType type) {
        if (type == null) return false;
        String id = data.getEquippedSlot(type.getId());
        return id != null && !id.isEmpty();
    }

    /**
     * Request a purchase. Applies the change optimistically for instant UI
     * feedback; the server is authoritative and will send back the true state,
     * which replaces this mirror.
     */
    public boolean purchase(CosmeticDefinition def) {
        if (def == null) return false;
        if (isUnlocked(def)) {
            equip(def);
            return true;
        }

        if (!def.isPurchasable()) {
            // Task-only cosmetics cannot be bought; the server rejects any such
            // request and will sync back the true state.
            return false;
        }

        if (data.getCoins() >= def.getPrice()) {
            data.setCoins(data.getCoins() - def.getPrice());
            data.unlockCosmetic(def.getId());
            data.setEquippedSlot(def.getType().getId(), def.getId());
            revision++;
            CosmeticNetworking.sendPurchaseCosmetic(def.getId());
            return true;
        }
        return false;
    }

    public void equip(CosmeticDefinition def) {
        if (def != null && isUnlocked(def)) {
            data.setEquippedSlot(def.getType().getId(), def.getId());
            revision++;
            CosmeticNetworking.sendEquipCosmetic(def.getType().getId(), def.getId());
        }
    }

    public void unequip(CosmeticType type) {
        if (type == null) return;
        data.setEquippedSlot(type.getId(), null);
        revision++;
        CosmeticNetworking.sendEquipCosmetic(type.getId(), "");
    }

    public boolean isTaskCompleted(String taskId) {
        return data.isTaskCompleted(taskId);
    }
}

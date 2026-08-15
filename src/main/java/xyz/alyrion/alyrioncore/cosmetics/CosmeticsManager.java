package xyz.alyrion.alyrioncore.cosmetics;

import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Client-side mirror of the player's cosmetics & rewards state.
 *
 * The client no longer owns any progression: coins, playtime, cape unlocks,
 * task completions and the equipped cape are all decided and persisted by the
 * server. This manager only caches the latest state the server synced to us
 * (via {@link CosmeticNetworking.S2CSyncCosmeticsPayload}) and forwards store
 * actions to the server as C2S requests.
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
        newData.getUnlockedCapes().addAll(payload.unlockedCapes());
        newData.setEquippedCapeId(payload.equippedCapeId());
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

    public boolean isCapeUnlocked(CapeDefinition cape) {
        if (cape == null) return false;
        return data.isCapeUnlocked(cape.getId());
    }

    public boolean isCapeEquipped(CapeDefinition cape) {
        if (cape == null) return false;
        return cape.getId().equalsIgnoreCase(data.getEquippedCapeId());
    }

    public CapeDefinition getEquippedCape() {
        return CapeDefinition.fromId(data.getEquippedCapeId());
    }

    public boolean isTaskCompleted(String taskId) {
        return data.isTaskCompleted(taskId);
    }

    /**
     * Request a purchase. Applies the change optimistically for instant UI
     * feedback; the server is authoritative and will send back the true state,
     * which replaces this mirror.
     */
    public boolean purchaseCape(CapeDefinition cape) {
        if (cape == null) return false;
        if (isCapeUnlocked(cape)) {
            equipCape(cape);
            return true;
        }

        if (data.getCoins() >= cape.getPrice()) {
            data.setCoins(data.getCoins() - cape.getPrice());
            data.unlockCape(cape.getId());
            data.setEquippedCapeId(cape.getId());
            revision++;
            CosmeticNetworking.sendPurchaseCape(cape.getId());
            return true;
        }
        return false;
    }

    public void equipCape(CapeDefinition cape) {
        if (cape != null && isCapeUnlocked(cape)) {
            data.setEquippedCapeId(cape.getId());
            revision++;
            CosmeticNetworking.sendCapeEquipped(cape.getId());
        }
    }

    public void unequipCape() {
        data.setEquippedCapeId(null);
        revision++;
        CosmeticNetworking.sendCapeEquipped("");
    }
}

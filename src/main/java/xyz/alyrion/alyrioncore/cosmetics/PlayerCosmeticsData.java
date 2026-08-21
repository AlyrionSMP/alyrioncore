package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-player cosmetic & reward progression data.
 *
 * This class is the single source of truth for a player's cosmetics state and is
 * owned by the server (stored inside {@link CosmeticsSavedData} in the world save).
 * The client only ever holds a synchronized mirror of it, populated from the
 * {@code S2CSyncCosmeticsPayload} the server sends.
 *
 * Storage is fully generic: one set of unlocked cosmetic ids (all types) plus one
 * equipped slot per {@link CosmeticType}. Old saves written with the legacy
 * cape/pet split are migrated transparently in {@link #load(CompoundTag)}.
 */
public class PlayerCosmeticsData {
    private static final String KEY_COINS = "Coins";
    private static final String KEY_PLAYTIME = "PlaytimeSeconds";
    private static final String KEY_PVP_KILLS = "PvpKills";
    private static final String KEY_UNLOCKED_COSMETICS = "UnlockedCosmetics";
    private static final String KEY_EQUIPPED = "EquippedCosmetics";
    private static final String KEY_EQUIPPED_TYPE = "Type";
    private static final String KEY_EQUIPPED_ID = "Id";
    private static final String KEY_COMPLETED_TASKS = "CompletedTasks";

    // Legacy keys (pre-unified saves), read for one-time migration
    private static final String KEY_UNLOCKED_CAPES = "UnlockedCapes";
    private static final String KEY_EQUIPPED_CAPE = "EquippedCape";
    private static final String KEY_UNLOCKED_PETS = "UnlockedPets";
    private static final String KEY_EQUIPPED_PET = "EquippedPet";

    private int coins = 0;
    private long survivalPlaytimeSeconds = 0;
    private int pvpKills = 0;
    private Set<String> unlockedCosmetics = new HashSet<>();
    private Map<String, String> equipped = new HashMap<>(); // typeId -> cosmeticId
    private Set<String> completedTasks = new HashSet<>();

    public PlayerCosmeticsData() {
        sanitize();
    }

    public void sanitize() {
        CosmeticsRegistry.ensureLoaded();
        if (unlockedCosmetics == null) unlockedCosmetics = new HashSet<>();
        if (completedTasks == null) completedTasks = new HashSet<>();
        if (equipped == null) equipped = new HashMap<>();

        // Default cosmetics are always unlocked
        for (CosmeticDefinition def : CosmeticsRegistry.all()) {
            if (def.isUnlockedByDefault()) {
                unlockedCosmetics.add(def.getId());
            }
        }

        // Completing a task unlocks its reward cosmetic
        for (TaskDefinition task : TaskDefinition.values()) {
            if (task.getReward() != null && completedTasks.contains(task.getId())) {
                unlockedCosmetics.add(task.getReward().getId());
            }
        }

        // Drop ids that no longer exist (removed cosmetics) and equipped entries
        // that point at locked or mismatched cosmetics.
        unlockedCosmetics.removeIf(id -> CosmeticsRegistry.fromId(id) == null);
        equipped.entrySet().removeIf(entry -> {
            String id = entry.getValue();
            if (id == null || id.isEmpty()) return true;
            CosmeticDefinition def = CosmeticsRegistry.fromId(id);
            if (def == null) return true;                    // unknown cosmetic
            if (!def.getType().getId().equalsIgnoreCase(entry.getKey())) return true; // wrong slot
            return !isCosmeticUnlocked(id);                  // not unlocked
        });
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_COINS, coins);
        tag.putLong(KEY_PLAYTIME, survivalPlaytimeSeconds);
        tag.putInt(KEY_PVP_KILLS, pvpKills);

        ListTag unlocked = new ListTag();
        for (String id : unlockedCosmetics) {
            unlocked.add(StringTag.valueOf(id));
        }
        tag.put(KEY_UNLOCKED_COSMETICS, unlocked);

        ListTag equippedList = new ListTag();
        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
            CompoundTag slot = new CompoundTag();
            slot.putString(KEY_EQUIPPED_TYPE, entry.getKey());
            slot.putString(KEY_EQUIPPED_ID, entry.getValue());
            equippedList.add(slot);
        }
        tag.put(KEY_EQUIPPED, equippedList);

        ListTag tasks = new ListTag();
        for (String id : completedTasks) {
            tasks.add(StringTag.valueOf(id));
        }
        tag.put(KEY_COMPLETED_TASKS, tasks);
        return tag;
    }

    public static PlayerCosmeticsData load(CompoundTag tag) {
        PlayerCosmeticsData data = new PlayerCosmeticsData();
        data.coins = Math.max(0, tag.getInt(KEY_COINS));
        data.survivalPlaytimeSeconds = Math.max(0, tag.getLong(KEY_PLAYTIME));
        data.pvpKills = Math.max(0, tag.getInt(KEY_PVP_KILLS));

        // --- New unified format ---
        ListTag unlocked = tag.getList(KEY_UNLOCKED_COSMETICS, Tag.TAG_STRING);
        for (int i = 0; i < unlocked.size(); i++) {
            data.unlockedCosmetics.add(unlocked.getString(i));
        }

        ListTag equippedList = tag.getList(KEY_EQUIPPED, Tag.TAG_COMPOUND);
        for (int i = 0; i < equippedList.size(); i++) {
            CompoundTag slot = equippedList.getCompound(i);
            String typeId = slot.getString(KEY_EQUIPPED_TYPE);
            String cosmeticId = slot.getString(KEY_EQUIPPED_ID);
            if (!typeId.isEmpty() && !cosmeticId.isEmpty()) {
                data.equipped.put(typeId, cosmeticId);
            }
        }

        ListTag tasks = tag.getList(KEY_COMPLETED_TASKS, Tag.TAG_STRING);
        for (int i = 0; i < tasks.size(); i++) {
            data.completedTasks.add(tasks.getString(i));
        }

        // --- Legacy migration (old cape/pet split) ---
        boolean hasLegacy = tag.contains(KEY_UNLOCKED_CAPES) || tag.contains(KEY_UNLOCKED_PETS);
        if (hasLegacy) {
            ListTag capes = tag.getList(KEY_UNLOCKED_CAPES, Tag.TAG_STRING);
            for (int i = 0; i < capes.size(); i++) {
                data.unlockedCosmetics.add(capes.getString(i));
            }
            ListTag pets = tag.getList(KEY_UNLOCKED_PETS, Tag.TAG_STRING);
            for (int i = 0; i < pets.size(); i++) {
                data.unlockedCosmetics.add(pets.getString(i));
            }
            if (tag.contains(KEY_EQUIPPED_CAPE, Tag.TAG_STRING)) {
                String id = tag.getString(KEY_EQUIPPED_CAPE);
                if (!id.isEmpty()) data.equipped.put(CosmeticType.CAPE.getId(), id);
            }
            if (tag.contains(KEY_EQUIPPED_PET, Tag.TAG_STRING)) {
                String id = tag.getString(KEY_EQUIPPED_PET);
                if (!id.isEmpty()) data.equipped.put(CosmeticType.PET.getId(), id);
            }
        }

        data.sanitize();
        return data;
    }

    // --- Coins ---

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(int amount) {
        this.coins = Math.max(0, this.coins + amount);
    }

    // --- Playtime ---

    public long getSurvivalPlaytimeSeconds() {
        return survivalPlaytimeSeconds;
    }

    public void setSurvivalPlaytimeSeconds(long seconds) {
        this.survivalPlaytimeSeconds = Math.max(0, seconds);
    }

    public void incrementSurvivalPlaytime() {
        this.survivalPlaytimeSeconds++;
    }

    // --- Player Kills ---

    public int getPvpKills() {
        return pvpKills;
    }

    public void setPvpKills(int pvpKills) {
        this.pvpKills = Math.max(0, pvpKills);
    }

    public void incrementPvpKills() {
        this.pvpKills++;
    }

    // --- Cosmetics (all types) ---

    public Set<String> getUnlockedCosmetics() {
        if (unlockedCosmetics == null) unlockedCosmetics = new HashSet<>();
        return unlockedCosmetics;
    }

    public boolean isCosmeticUnlocked(String cosmeticId) {
        if (cosmeticId == null) return false;
        CosmeticDefinition def = CosmeticsRegistry.fromId(cosmeticId);
        if (def == null) return false;
        if (def.isUnlockedByDefault()) return true;
        if (getUnlockedCosmetics().contains(cosmeticId)) return true;

        // Reward of a completed task
        for (TaskDefinition task : TaskDefinition.values()) {
            if (task.getReward() != null && task.getReward().getId().equalsIgnoreCase(cosmeticId)
                    && isTaskCompleted(task.getId())) {
                return true;
            }
        }
        return false;
    }

    public void unlockCosmetic(String cosmeticId) {
        if (cosmeticId != null) {
            getUnlockedCosmetics().add(cosmeticId);
        }
    }

    /** The equipped cosmetic id for a slot type (may be null). */
    public String getEquippedSlot(String typeId) {
        if (equipped == null) equipped = new HashMap<>();
        return equipped.get(typeId);
    }

    /** Set the equipped cosmetic for a slot type; null/empty unequips. */
    public void setEquippedSlot(String typeId, String cosmeticId) {
        if (equipped == null) equipped = new HashMap<>();
        if (typeId == null || typeId.isEmpty()) return;
        if (cosmeticId == null || cosmeticId.isEmpty()) {
            equipped.remove(typeId);
        } else {
            equipped.put(typeId, cosmeticId);
        }
    }

    public Map<String, String> getEquippedSlots() {
        if (equipped == null) equipped = new HashMap<>();
        return equipped;
    }

    /** Number of distinct equipped slots (cap + pet + ...). */
    public int getEquippedSlotCount() {
        return equipped == null ? 0 : equipped.size();
    }

    // --- Tasks ---

    public Set<String> getCompletedTasks() {
        if (completedTasks == null) completedTasks = new HashSet<>();
        return completedTasks;
    }

    public boolean isTaskCompleted(String taskId) {
        if (taskId == null) return false;
        return getCompletedTasks().contains(taskId);
    }

    public void completeTask(String taskId) {
        if (taskId != null) {
            getCompletedTasks().add(taskId);
        }
    }

    public void resetAllTasks() {
        getCompletedTasks().clear();
        sanitize();
    }

    public void resetCosmetics() {
        getUnlockedCosmetics().clear();
        setPvpKills(0);
        if (equipped == null) equipped = new HashMap<>();
        equipped.clear();
        sanitize(); // re-adds default + task-reward unlocks
    }
}

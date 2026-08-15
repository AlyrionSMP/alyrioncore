package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

/**
 * Per-player cosmetic & reward progression data.
 *
 * This class is the single source of truth for a player's cosmetics state and is
 * owned by the server (stored inside {@link CosmeticsSavedData} in the world save).
 * The client only ever holds a synchronized mirror of it, populated from the
 * {@code S2CSyncCosmeticsPayload} the server sends.
 */
public class PlayerCosmeticsData {
    private static final String KEY_COINS = "Coins";
    private static final String KEY_PLAYTIME = "PlaytimeSeconds";
    private static final String KEY_UNLOCKED_CAPES = "UnlockedCapes";
    private static final String KEY_EQUIPPED_CAPE = "EquippedCape";
    private static final String KEY_COMPLETED_TASKS = "CompletedTasks";

    private int coins = 0;
    private long survivalPlaytimeSeconds = 0;
    private Set<String> unlockedCapes = new HashSet<>();
    private String equippedCapeId = null;
    private Set<String> completedTasks = new HashSet<>();

    public PlayerCosmeticsData() {
        sanitize();
    }

    public void sanitize() {
        if (unlockedCapes == null) unlockedCapes = new HashSet<>();
        if (completedTasks == null) completedTasks = new HashSet<>();

        // Ensure default free capes are always unlocked
        unlockedCapes.add(CapeDefinition.TWO_YEAR_CELEBRATION.getId());
        unlockedCapes.add(CapeDefinition.SEASON_8.getId());

        // Sync completed tasks to cape unlocks
        if (completedTasks.contains(TaskDefinition.GOING_TO_SPACE.getId())) {
            unlockedCapes.add(CapeDefinition.STARS.getId());
        }
        if (completedTasks.contains(TaskDefinition.GOING_TO_MOON.getId())) {
            unlockedCapes.add(CapeDefinition.MOON.getId());
        }
        if (completedTasks.contains(TaskDefinition.GOING_TO_MARS.getId())) {
            unlockedCapes.add(CapeDefinition.MARSIAN.getId());
        }

        // Never leave an equipped cape that isn't unlocked
        if (equippedCapeId != null && !isCapeUnlocked(equippedCapeId)) {
            equippedCapeId = null;
        }
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_COINS, coins);
        tag.putLong(KEY_PLAYTIME, survivalPlaytimeSeconds);

        ListTag capes = new ListTag();
        for (String id : unlockedCapes) {
            capes.add(StringTag.valueOf(id));
        }
        tag.put(KEY_UNLOCKED_CAPES, capes);

        if (equippedCapeId != null && !equippedCapeId.isEmpty()) {
            tag.putString(KEY_EQUIPPED_CAPE, equippedCapeId);
        }

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

        ListTag capes = tag.getList(KEY_UNLOCKED_CAPES, Tag.TAG_STRING);
        for (int i = 0; i < capes.size(); i++) {
            data.unlockedCapes.add(capes.getString(i));
        }

        if (tag.contains(KEY_EQUIPPED_CAPE, Tag.TAG_STRING)) {
            data.equippedCapeId = tag.getString(KEY_EQUIPPED_CAPE);
        }

        ListTag tasks = tag.getList(KEY_COMPLETED_TASKS, Tag.TAG_STRING);
        for (int i = 0; i < tasks.size(); i++) {
            data.completedTasks.add(tasks.getString(i));
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

    // --- Capes ---

    public Set<String> getUnlockedCapes() {
        if (unlockedCapes == null) unlockedCapes = new HashSet<>();
        return unlockedCapes;
    }

    public boolean isCapeUnlocked(String capeId) {
        if (capeId == null) return false;
        CapeDefinition def = CapeDefinition.fromId(capeId);
        if (def != null && def.isUnlockedByDefault()) return true;

        if (getUnlockedCapes().contains(capeId)) return true;

        // Check if unlocked via task
        if (capeId.equalsIgnoreCase(CapeDefinition.MARSIAN.getId()) && isTaskCompleted(TaskDefinition.GOING_TO_MARS.getId())) {
            return true;
        }
        if (capeId.equalsIgnoreCase(CapeDefinition.MOON.getId()) && isTaskCompleted(TaskDefinition.GOING_TO_MOON.getId())) {
            return true;
        }
        if (capeId.equalsIgnoreCase(CapeDefinition.STARS.getId()) && isTaskCompleted(TaskDefinition.GOING_TO_SPACE.getId())) {
            return true;
        }

        return false;
    }

    public void unlockCape(String capeId) {
        if (capeId != null) {
            getUnlockedCapes().add(capeId);
        }
    }

    public String getEquippedCapeId() {
        return equippedCapeId;
    }

    public void setEquippedCapeId(String equippedCapeId) {
        this.equippedCapeId = (equippedCapeId != null && !equippedCapeId.isEmpty()) ? equippedCapeId : null;
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
        getUnlockedCapes().clear();
        unlockedCapes.add(CapeDefinition.TWO_YEAR_CELEBRATION.getId());
        unlockedCapes.add(CapeDefinition.SEASON_8.getId());
        if (equippedCapeId != null && !isCapeUnlocked(equippedCapeId)) {
            equippedCapeId = null;
        }
        sanitize();
    }
}

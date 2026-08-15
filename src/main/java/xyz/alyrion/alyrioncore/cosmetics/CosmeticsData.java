package xyz.alyrion.alyrioncore.cosmetics;

import java.util.HashSet;
import java.util.Set;

public class CosmeticsData {
    private int coins = 0;
    private long survivalPlaytimeSeconds = 0;
    private Set<String> unlockedCapes = new HashSet<>();
    private String equippedCapeId = null;
    private Set<String> completedTasks = new HashSet<>();

    public CosmeticsData() {
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
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(int amount) {
        this.coins = Math.max(0, this.coins + amount);
    }

    public long getSurvivalPlaytimeSeconds() {
        return survivalPlaytimeSeconds;
    }

    public void setSurvivalPlaytimeSeconds(long seconds) {
        this.survivalPlaytimeSeconds = Math.max(0, seconds);
    }

    public void incrementSurvivalPlaytime() {
        this.survivalPlaytimeSeconds++;
    }

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
    }

    public void resetCosmetics() {
        getUnlockedCapes().clear();
        unlockedCapes.add(CapeDefinition.TWO_YEAR_CELEBRATION.getId());
        unlockedCapes.add(CapeDefinition.SEASON_8.getId());
        if (equippedCapeId != null && !isCapeUnlocked(equippedCapeId)) {
            equippedCapeId = null;
        }
    }
}

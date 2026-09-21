package xyz.alyrion.alyrioncore.cosmetics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class PlayerCosmeticsData {
   private static final String KEY_COINS = "Coins";
   private static final String KEY_PLAYTIME = "PlaytimeSeconds";
   private static final String KEY_PVP_KILLS = "PvpKills";
   private static final String KEY_UNLOCKED_COSMETICS = "UnlockedCosmetics";
   private static final String KEY_EQUIPPED = "EquippedCosmetics";
   private static final String KEY_EQUIPPED_TYPE = "Type";
   private static final String KEY_EQUIPPED_ID = "Id";
   private static final String KEY_COMPLETED_TASKS = "CompletedTasks";
   private static final String KEY_COLLECTED_DYES = "CollectedDyes";
   private static final String KEY_COLLECTED_ORES = "CollectedOres";
   private static final String KEY_HIDDEN_COINS = "HiddenCoins";
   private static final String KEY_UNLOCKED_CAPES = "UnlockedCapes";
   private static final String KEY_EQUIPPED_CAPE = "EquippedCape";
   private static final String KEY_UNLOCKED_PETS = "UnlockedPets";
   private static final String KEY_EQUIPPED_PET = "EquippedPet";
   private int coins = 0;
   private long survivalPlaytimeSeconds = 0L;
   private double hiddenCoins = 0.0;
   private int pvpKills = 0;
   private Set<String> unlockedCosmetics = new HashSet<>();
   private Map<String, String> equipped = new HashMap<>();
   private Set<String> completedTasks = new HashSet<>();
   private Set<String> collectedDyes = new HashSet<>();
   private Set<String> collectedOres = new HashSet<>();

   public PlayerCosmeticsData() {
      this.sanitize();
   }

   public void sanitize() {
      CosmeticsRegistry.ensureLoaded();
      if (this.unlockedCosmetics == null) {
         this.unlockedCosmetics = new HashSet<>();
      }

      if (this.completedTasks == null) {
         this.completedTasks = new HashSet<>();
      }

      if (this.collectedDyes == null) {
         this.collectedDyes = new HashSet<>();
      }

      if (this.collectedOres == null) {
         this.collectedOres = new HashSet<>();
      }

      if (this.equipped == null) {
         this.equipped = new HashMap<>();
      }

      for (CosmeticDefinition def : CosmeticsRegistry.all()) {
         if (def.isUnlockedByDefault()) {
            this.unlockedCosmetics.add(def.getId());
         }
      }

      for (TaskDefinition task : TaskDefinition.values()) {
         if (task.getReward() != null && this.completedTasks.contains(task.getId())) {
            this.unlockedCosmetics.add(task.getReward().getId());
         }
      }

      this.unlockedCosmetics.removeIf(id -> CosmeticsRegistry.fromId(id) == null);
      this.equipped.entrySet().removeIf(entry -> {
         String id = entry.getValue();
         if (id != null && !id.isEmpty()) {
            CosmeticDefinition defx = CosmeticsRegistry.fromId(id);
            if (defx == null) {
               return true;
            } else {
               return !defx.getType().getId().equalsIgnoreCase(entry.getKey()) ? true : !this.isCosmeticUnlocked(id);
            }
         } else {
            return true;
         }
      });
   }

   public CompoundTag save(CompoundTag tag) {
      tag.putInt("Coins", this.coins);
      tag.putLong("PlaytimeSeconds", this.survivalPlaytimeSeconds);
      tag.putDouble("HiddenCoins", this.hiddenCoins);
      tag.putInt("PvpKills", this.pvpKills);
      ListTag unlocked = new ListTag();

      for (String id : this.unlockedCosmetics) {
         unlocked.add(StringTag.valueOf(id));
      }

      tag.put("UnlockedCosmetics", unlocked);
      ListTag equippedList = new ListTag();

      for (Entry<String, String> entry : this.equipped.entrySet()) {
         if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            CompoundTag slot = new CompoundTag();
            slot.putString("Type", entry.getKey());
            slot.putString("Id", entry.getValue());
            equippedList.add(slot);
         }
      }

      tag.put("EquippedCosmetics", equippedList);
      ListTag tasks = new ListTag();

      for (String id : this.completedTasks) {
         tasks.add(StringTag.valueOf(id));
      }

      tag.put("CompletedTasks", tasks);
      ListTag dyes = new ListTag();

      for (String dye : this.collectedDyes) {
         dyes.add(StringTag.valueOf(dye));
      }

      tag.put("CollectedDyes", dyes);
      ListTag ores = new ListTag();

      for (String ore : this.collectedOres) {
         ores.add(StringTag.valueOf(ore));
      }

      tag.put("CollectedOres", ores);
      return tag;
   }

   public static PlayerCosmeticsData load(CompoundTag tag) {
      PlayerCosmeticsData data = new PlayerCosmeticsData();
      data.coins = Math.max(0, tag.getInt("Coins"));
      data.survivalPlaytimeSeconds = Math.max(0L, tag.getLong("PlaytimeSeconds"));
      if (tag.contains("HiddenCoins")) {
         data.hiddenCoins = Math.max(0.0, tag.getDouble("HiddenCoins"));
      } else {
         data.hiddenCoins = 0.0;
      }

      data.pvpKills = Math.max(0, tag.getInt("PvpKills"));
      ListTag unlocked = tag.getList("UnlockedCosmetics", 8);

      for (int i = 0; i < unlocked.size(); i++) {
         data.unlockedCosmetics.add(unlocked.getString(i));
      }

      ListTag equippedList = tag.getList("EquippedCosmetics", 10);

      for (int i = 0; i < equippedList.size(); i++) {
         CompoundTag slot = equippedList.getCompound(i);
         String typeId = slot.getString("Type");
         String cosmeticId = slot.getString("Id");
         if (!typeId.isEmpty() && !cosmeticId.isEmpty()) {
            data.equipped.put(typeId, cosmeticId);
         }
      }

      ListTag tasks = tag.getList("CompletedTasks", 8);

      for (int ix = 0; ix < tasks.size(); ix++) {
         data.completedTasks.add(tasks.getString(ix));
      }

      ListTag dyesList = tag.getList("CollectedDyes", 8);

      for (int ix = 0; ix < dyesList.size(); ix++) {
         data.collectedDyes.add(dyesList.getString(ix));
      }

      ListTag oresList = tag.getList("CollectedOres", 8);

      for (int ix = 0; ix < oresList.size(); ix++) {
         data.collectedOres.add(oresList.getString(ix));
      }

      boolean hasLegacy = tag.contains("UnlockedCapes") || tag.contains("UnlockedPets");
      if (hasLegacy) {
         ListTag capes = tag.getList("UnlockedCapes", 8);

         for (int ix = 0; ix < capes.size(); ix++) {
            data.unlockedCosmetics.add(capes.getString(ix));
         }

         ListTag pets = tag.getList("UnlockedPets", 8);

         for (int ix = 0; ix < pets.size(); ix++) {
            data.unlockedCosmetics.add(pets.getString(ix));
         }

         if (tag.contains("EquippedCape", 8)) {
            String id = tag.getString("EquippedCape");
            if (!id.isEmpty()) {
               data.equipped.put(CosmeticType.CAPE.getId(), id);
            }
         }

         if (tag.contains("EquippedPet", 8)) {
            String id = tag.getString("EquippedPet");
            if (!id.isEmpty()) {
               data.equipped.put(CosmeticType.PET.getId(), id);
            }
         }
      }

      data.sanitize();
      return data;
   }

   public int getCoins() {
      return this.coins;
   }

   public void setCoins(int coins) {
      this.coins = Math.max(0, coins);
   }

   public void addCoins(int amount) {
      long result = (long)this.coins + (long)amount;
      this.coins = (int)Math.max(0L, Math.min(2147483647L, result));
   }

   public long getSurvivalPlaytimeSeconds() {
      return this.survivalPlaytimeSeconds;
   }

   public void setSurvivalPlaytimeSeconds(long seconds) {
      this.survivalPlaytimeSeconds = Math.max(0L, seconds);
   }

   public void incrementSurvivalPlaytime() {
      this.survivalPlaytimeSeconds++;
   }

   public double getHiddenCoins() {
      return this.hiddenCoins;
   }

   public void setHiddenCoins(double coins) {
      this.hiddenCoins = Math.max(0.0, coins);
   }

   public void addHiddenCoins(double amount) {
      this.hiddenCoins = Math.max(0.0, this.hiddenCoins + amount);
   }

   public int getPvpKills() {
      return this.pvpKills;
   }

   public void setPvpKills(int pvpKills) {
      this.pvpKills = Math.max(0, pvpKills);
   }

   public void incrementPvpKills() {
      this.pvpKills++;
   }

   public Set<String> getUnlockedCosmetics() {
      if (this.unlockedCosmetics == null) {
         this.unlockedCosmetics = new HashSet<>();
      }

      return this.unlockedCosmetics;
   }

   public boolean isCosmeticUnlocked(String cosmeticId) {
      if (cosmeticId == null) {
         return false;
      } else {
         CosmeticDefinition def = CosmeticsRegistry.fromId(cosmeticId);
         if (def == null) {
            return false;
         } else if (def.isUnlockedByDefault()) {
            return true;
         } else if (this.getUnlockedCosmetics().contains(cosmeticId)) {
            return true;
         } else {
            for (TaskDefinition task : TaskDefinition.values()) {
               if (task.getReward() != null && task.getReward().getId().equalsIgnoreCase(cosmeticId) && this.isTaskCompleted(task.getId())) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public void unlockCosmetic(String cosmeticId) {
      if (cosmeticId != null) {
         this.getUnlockedCosmetics().add(cosmeticId);
      }
   }

   public String getEquippedSlot(String typeId) {
      if (this.equipped == null) {
         this.equipped = new HashMap<>();
      }

      return this.equipped.get(typeId);
   }

   public void setEquippedSlot(String typeId, String cosmeticId) {
      if (this.equipped == null) {
         this.equipped = new HashMap<>();
      }

      if (typeId != null && !typeId.isEmpty()) {
         if (cosmeticId != null && !cosmeticId.isEmpty()) {
            this.equipped.put(typeId, cosmeticId);
         } else {
            this.equipped.remove(typeId);
         }
      }
   }

   public Map<String, String> getEquippedSlots() {
      if (this.equipped == null) {
         this.equipped = new HashMap<>();
      }

      return this.equipped;
   }

   public int getEquippedSlotCount() {
      return this.equipped == null ? 0 : this.equipped.size();
   }

   public Set<String> getCompletedTasks() {
      if (this.completedTasks == null) {
         this.completedTasks = new HashSet<>();
      }

      return this.completedTasks;
   }

   public boolean isTaskCompleted(String taskId) {
      return taskId == null ? false : this.getCompletedTasks().contains(taskId);
   }

   public void completeTask(String taskId) {
      if (taskId != null) {
         this.getCompletedTasks().add(taskId);
      }
   }

   public void resetAllTasks() {
      this.getCompletedTasks().clear();
      this.getCollectedDyes().clear();
      this.getCollectedOres().clear();
      this.sanitize();
   }

   public Set<String> getCollectedDyes() {
      if (this.collectedDyes == null) {
         this.collectedDyes = new HashSet<>();
      }

      return this.collectedDyes;
   }

   public boolean addCollectedDye(String dye) {
      if (this.collectedDyes == null) {
         this.collectedDyes = new HashSet<>();
      }

      return dye != null && !dye.isEmpty() ? this.collectedDyes.add(dye.toLowerCase()) : false;
   }

   public Set<String> getCollectedOres() {
      if (this.collectedOres == null) {
         this.collectedOres = new HashSet<>();
      }

      return this.collectedOres;
   }

   public boolean addCollectedOre(String ore) {
      if (this.collectedOres == null) {
         this.collectedOres = new HashSet<>();
      }

      return ore != null && !ore.isEmpty() ? this.collectedOres.add(ore.toLowerCase()) : false;
   }

   public void resetCosmetics() {
      this.getUnlockedCosmetics().clear();
      this.setPvpKills(0);
      this.hiddenCoins = 0.0;
      if (this.equipped == null) {
         this.equipped = new HashMap<>();
      }

      this.equipped.clear();
      this.sanitize();
   }
}

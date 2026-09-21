package xyz.alyrion.alyrioncore.cosmetics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

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

   public void syncToPlayer(ServerPlayer player) {
      PlayerCosmeticsData data = this.getPlayerData(player);
      List<CosmeticNetworking.S2CSyncCosmeticsPayload.EquippedSlot> slots = new ArrayList<>();

      for (Entry<String, String> entry : data.getEquippedSlots().entrySet()) {
         if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            slots.add(new CosmeticNetworking.S2CSyncCosmeticsPayload.EquippedSlot(entry.getKey(), entry.getValue()));
         }
      }

      PacketDistributor.sendToPlayer(
         player,
         new CosmeticNetworking.S2CSyncCosmeticsPayload(
            data.getCoins(), data.getSurvivalPlaytimeSeconds(), new HashSet<>(data.getUnlockedCosmetics()), slots, new HashSet<>(data.getCompletedTasks())
         ),
         new CustomPacketPayload[0]
      );
   }

   public void broadcastSlot(ServerPlayer player, CosmeticType type) {
      if (type != null) {
         PlayerCosmeticsData data = this.getPlayerData(player);
         String id = data.getEquippedSlot(type.getId());
         PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            player, new CosmeticNetworking.S2CSyncCosmeticPayload(player.getUUID(), type.getId(), id != null ? id : ""), new CustomPacketPayload[0]
         );
      }
   }

   public void sendSlotTo(ServerPlayer recipient, ServerPlayer target, CosmeticType type) {
      if (type != null) {
         PlayerCosmeticsData data = this.getPlayerData(target);
         String id = data.getEquippedSlot(type.getId());
         PacketDistributor.sendToPlayer(
            recipient, new CosmeticNetworking.S2CSyncCosmeticPayload(target.getUUID(), type.getId(), id != null ? id : ""), new CustomPacketPayload[0]
         );
      }
   }

   public boolean purchase(ServerPlayer player, String cosmeticId) {
      CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
      if (cosmetic == null) {
         return false;
      } else if (!cosmetic.isPurchasable()) {
         this.syncToPlayer(player);
         return false;
      } else {
         PlayerCosmeticsData data = this.getPlayerData(player);
         if (data.isCosmeticUnlocked(cosmetic.getId())) {
            this.equip(player, cosmetic.getType().getId(), cosmetic.getId());
            return true;
         } else if (data.getCoins() >= cosmetic.getPrice()) {
            data.setCoins(data.getCoins() - cosmetic.getPrice());
            data.unlockCosmetic(cosmetic.getId());
            data.setEquippedSlot(cosmetic.getType().getId(), cosmetic.getId());
            this.markDirty(player);
            this.syncToPlayer(player);
            this.broadcastSlot(player, cosmetic.getType());
            this.notify(
               player, "§6§l[Alyrion SMP] §aUnlocked " + cosmetic.getDisplayName() + "! §7(§6-" + cosmetic.getPrice() + " Coins§7)", CosmeticSound.SUCCESS
            );
            return true;
         } else {
            return false;
         }
      }
   }

   public void equip(ServerPlayer player, String typeId, String cosmeticId) {
      if (typeId != null && !typeId.isEmpty()) {
         if (cosmeticId != null && !cosmeticId.isEmpty()) {
            CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
            if (cosmetic != null && cosmetic.getType().getId().equalsIgnoreCase(typeId)) {
               PlayerCosmeticsData data = this.getPlayerData(player);
               if (!data.isCosmeticUnlocked(cosmetic.getId())) {
                  this.syncToPlayer(player);
               } else {
                  data.setEquippedSlot(typeId, cosmetic.getId());
                  this.markDirty(player);
                  this.syncToPlayer(player);
                  this.broadcastSlot(player, cosmetic.getType());
                  this.notify(player, "§6§l[Alyrion SMP] §aEquipped " + cosmetic.getDisplayName() + ".", CosmeticSound.CLICK);
               }
            }
         } else {
            this.unequipSlot(player, typeId);
         }
      }
   }

   public void unequipSlot(ServerPlayer player, String typeId) {
      CosmeticType type = CosmeticType.fromId(typeId);
      if (type != null) {
         PlayerCosmeticsData data = this.getPlayerData(player);
         data.setEquippedSlot(typeId, null);
         this.markDirty(player);
         this.syncToPlayer(player);
         this.broadcastSlot(player, type);
         this.notify(player, "§6§l[Alyrion SMP] §7" + type.getDisplayName() + " cosmetic unequipped.", CosmeticSound.CLICK);
      }
   }

   public void tickPlaytime(ServerPlayer player) {
      if (!player.isCreative() && !player.isSpectator()) {
         PlayerCosmeticsData data = this.getPlayerData(player);
         data.incrementSurvivalPlaytime();
         long current = data.getSurvivalPlaytimeSeconds();
         if (current > 0L && current % (long)CosmeticConfig.PLAYTIME_SECONDS_PER_COIN == 0L) {
            boolean hasPalestine = "palestine".equalsIgnoreCase(data.getEquippedSlot(CosmeticType.CAPE.getId()));
            int coinsToAward = 1;
            boolean bonusAwarded = false;
            if (hasPalestine) {
               data.addHiddenCoins(0.1);
               if (data.getHiddenCoins() >= 0.9999) {
                  data.setHiddenCoins(Math.max(0.0, Math.round((data.getHiddenCoins() - 1.0) * 1000.0) / 1000.0));
                  coinsToAward += 1;
                  bonusAwarded = true;
               }
            }

            data.addCoins(coinsToAward);
            this.markDirty(player);
            this.syncToPlayer(player);
            this.checkTasks(player);
            if (bonusAwarded) {
               this.notify(
                  player,
                  "§6§l[Alyrion SMP] §e+2 Coins §fearned for survival playtime! §a(+1 Bonus Coin from Palestine Cape!) §f(Total: §6" + data.getCoins() + " Coins§f)",
                  CosmeticSound.LEVEL_UP
               );
            } else {
               String boostNotice = hasPalestine ? " §a(1.1x Multiplier!)" : "";
               this.notify(
                  player,
                  "§6§l[Alyrion SMP] §e+1 Coin §fearned for survival playtime!" + boostNotice + " (Total: §6" + data.getCoins() + " Coins§f)",
                  CosmeticSound.LEVEL_UP
               );
            }
         } else {
            if (current % (long)CosmeticConfig.PLAYTIME_SYNC_INTERVAL_SECONDS == 0L) {
               this.syncToPlayer(player);
            }

            if (current % 60L == 0L) {
               this.markDirty(player);
            }
         }
      }
   }

   public void onPlayerKill(ServerPlayer killer) {
      PlayerCosmeticsData data = this.getPlayerData(killer);
      data.incrementPvpKills();
      this.markDirty(killer);
      this.syncToPlayer(killer);
      this.checkTasks(killer);
   }

   public void checkTasks(ServerPlayer player) {
      PlayerCosmeticsData data = this.getPlayerData(player);

      for (TaskDefinition task : TaskDefinition.values()) {
         boolean taskDone = data.isTaskCompleted(task.getId());
         boolean rewardUnlocked = task.getReward() == null || data.isCosmeticUnlocked(task.getReward().getId());
         if ((!taskDone || !rewardUnlocked) && task.test(player)) {
            this.completeTask(player, task, false);
         }
      }
   }

   public void completeTask(ServerPlayer player, TaskDefinition task, boolean isManualDev) {
      if (task != null) {
         PlayerCosmeticsData data = this.getPlayerData(player);
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
            this.markDirty(player);
            this.syncToPlayer(player);
            if (task.getReward() != null) {
               this.broadcastSlot(player, task.getReward().getType());
            }

            String prefix = isManualDev ? "§d§l[DEV TASK COMPLETED] §f" : "§6§l[TASK COMPLETED] §f";
            String rewardNotice = task.getReward() != null ? " + §bUnlocked " + task.getReward().getDisplayName() + "!" : "";
            this.notify(player, prefix + "§a" + task.getTitle() + " §7(§6+" + task.getCoinReward() + " Coins" + rewardNotice + "§7)", CosmeticSound.LEVEL_UP);
         }
      }
   }

   public void devAddCoins(ServerPlayer target, int amount) {
      PlayerCosmeticsData data = this.getPlayerData(target);
      data.addCoins(amount);
      this.markDirty(target);
      this.syncToPlayer(target);
      this.checkTasks(target);
      this.notify(target, "§d[DEV] §aAdded " + amount + " Coins. (Total: " + data.getCoins() + ")", CosmeticSound.CLICK);
   }

   public void devAddPlaytime(ServerPlayer target, long seconds) {
      PlayerCosmeticsData data = this.getPlayerData(target);
      long prev = data.getSurvivalPlaytimeSeconds();
      data.setSurvivalPlaytimeSeconds(prev + seconds);
      int coinsEarned = (int)(data.getSurvivalPlaytimeSeconds() / (long)CosmeticConfig.PLAYTIME_SECONDS_PER_COIN - prev / (long)CosmeticConfig.PLAYTIME_SECONDS_PER_COIN);
      if (coinsEarned > 0) {
         boolean hasPalestine = "palestine".equalsIgnoreCase(data.getEquippedSlot(CosmeticType.CAPE.getId()));
         if (hasPalestine) {
            data.addHiddenCoins(0.1 * (double)coinsEarned);
            if (data.getHiddenCoins() >= 0.9999) {
               int bonus = (int)Math.floor(data.getHiddenCoins() + 0.0001);
               data.setHiddenCoins(Math.max(0.0, Math.round((data.getHiddenCoins() - (double)bonus) * 1000.0) / 1000.0));
               coinsEarned += bonus;
            }
         }
         data.addCoins(coinsEarned);
      }

      this.markDirty(target);
      this.syncToPlayer(target);
      this.checkTasks(target);
      this.notify(target, "§d[DEV] §aAdded " + seconds / 60L + " minutes of survival playtime. (+" + coinsEarned + " coins)", CosmeticSound.LEVEL_UP);
   }

   public void devUnlock(ServerPlayer target, String cosmeticId) {
      CosmeticDefinition cosmetic = CosmeticsRegistry.fromId(cosmeticId);
      if (cosmetic != null) {
         PlayerCosmeticsData data = this.getPlayerData(target);
         data.unlockCosmetic(cosmetic.getId());
         this.markDirty(target);
         this.syncToPlayer(target);
         this.notify(target, "§d[DEV] §aUnlocked " + cosmetic.getDisplayName() + ".", CosmeticSound.CLICK);
      }
   }

   public void devResetAllTasks(ServerPlayer target) {
      PlayerCosmeticsData data = this.getPlayerData(target);
      data.resetAllTasks();
      this.markDirty(target);
      this.syncToPlayer(target);
      this.notify(target, "§d[DEV] §cAll task progression has been reset.", CosmeticSound.CLICK);
   }

   public void devResetCosmetics(ServerPlayer target) {
      PlayerCosmeticsData data = this.getPlayerData(target);
      data.resetCosmetics();
      this.markDirty(target);
      this.syncToPlayer(target);

      for (CosmeticType type : CosmeticType.values()) {
         this.broadcastSlot(target, type);
      }

      this.notify(target, "§d[DEV] §cAll cosmetic unlocks have been reset.", CosmeticSound.CLICK);
   }

   private void markDirty(ServerPlayer player) {
      CosmeticsSavedData savedData = CosmeticsSavedData.get(player.server);
      if (savedData != null) {
         savedData.setDirty();
      }
   }

   private void notify(ServerPlayer player, String message, CosmeticSound sound) {
      player.displayClientMessage(Component.literal(message), false);
      if (sound != null && sound != CosmeticSound.NONE) {
         PacketDistributor.sendToPlayer(player, new CosmeticNetworking.S2CPlaySoundPayload(sound.getId()), new CustomPacketPayload[0]);
      }
   }
}

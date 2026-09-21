package xyz.alyrion.alyrioncore.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import xyz.alyrion.alyrioncore.block.MartianPotatoCropBlock;
import xyz.alyrion.alyrioncore.block.SleepingPodBlock;
import xyz.alyrion.alyrioncore.world.ModDimensions;
import xyz.alyrion.alyrioncore.world.habitat.HabitatSealManager;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherSavedData;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherState;

@EventBusSubscriber(
   modid = "alyrioncore"
)
public class ServerCosmeticsEvents {
   private static int tickCounter = 0;
   private static final List<ServerCosmeticsEvents.EggImpactRecord> recentEggImpacts = new ArrayList<>();

   @SubscribeEvent
   public static void onServerTick(Post event) {
      tickCounter++;
      if (!recentEggImpacts.isEmpty()) {
         long currentServerTime = event.getServer().overworld().getGameTime();
         recentEggImpacts.removeIf(record -> Math.abs(currentServerTime - record.gameTime()) > 10L);
      }

      List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
      if (!players.isEmpty()) {
         ServerCosmeticsManager manager = ServerCosmeticsManager.get();
         if (tickCounter % 20 == 0) {
            for (ServerPlayer player : players) {
               manager.tickPlaytime(player);
            }
         }

         if (tickCounter % 5 == 0) {
            for (ServerPlayer player : players) {
               manager.checkTasks(player);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer victim
         && event.getSource().getEntity() instanceof ServerPlayer killer
         && killer != victim
         && !killer.isCreative()
         && !killer.isSpectator()) {
         ServerCosmeticsManager.get().onPlayerKill(killer);
      }

      if (event.getEntity() instanceof WitherBoss && event.getSource().getEntity() instanceof ServerPlayer killer && !killer.isSpectator()) {
         ServerCosmeticsManager.get().completeTask(killer, TaskDefinition.WITHER_SLAYER, false);
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         ServerCosmeticsManager manager = ServerCosmeticsManager.get();
         manager.syncToPlayer(serverPlayer);

         for (CosmeticType type : CosmeticType.values()) {
            manager.broadcastSlot(serverPlayer, type);
         }

         for (ServerPlayer other : serverPlayer.server.getPlayerList().getPlayers()) {
            if (other != serverPlayer) {
               for (CosmeticType type : CosmeticType.values()) {
                  manager.sendSlotTo(serverPlayer, other, type);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getProjectile() instanceof ThrownEgg egg && egg.getOwner() instanceof ServerPlayer player) {
         recentEggImpacts.add(new ServerCosmeticsEvents.EggImpactRecord(player.getUUID(), egg.level().getGameTime(), egg.position()));
      }
   }

   @SubscribeEvent
   public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
      if (!event.loadedFromDisk()) {
         if (!recentEggImpacts.isEmpty()) {
            if (event.getEntity() instanceof Chicken chicken && chicken.isBaby()) {
               Level level = event.getLevel();
               long currentTime = level.getGameTime();
               Vec3 chickenPos = chicken.position();
               ServerCosmeticsEvents.EggImpactRecord matched = null;

               for (ServerCosmeticsEvents.EggImpactRecord record : recentEggImpacts) {
                  if (record.gameTime() == currentTime && chickenPos.distanceToSqr(record.pos()) < 16.0) {
                     matched = record;
                     break;
                  }
               }

               if (matched != null && level.getServer() != null) {
                  ServerPlayer player = level.getServer().getPlayerList().getPlayer(matched.playerUuid());
                  if (player != null && !player.isSpectator()) {
                     ServerCosmeticsManager.get().completeTask(player, TaskDefinition.CHICKEN, false);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onAnvilRepair(AnvilRepairEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (!player.isSpectator()) {
            ItemStack left = event.getLeft();
            ItemStack output = event.getOutput();
            boolean repaired = left.isDamageableItem() && output.getDamageValue() < left.getDamageValue() || !event.getRight().isEmpty() && left.isDamaged();
            if (repaired) {
               ServerCosmeticsManager.get().completeTask(player, TaskDefinition.COMMUNISM, false);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onBlockBreak(BreakEvent event) {
      if (event.getPlayer() instanceof ServerPlayer sp && !sp.isSpectator()) {
         if (event.getState().getBlock() instanceof MartianPotatoCropBlock crop
            && crop.isMaxAge(event.getState())
            && event.getLevel() instanceof ServerLevel sl
            && HabitatSealManager.isPositionSealed(sl, event.getPos())) {
            ServerCosmeticsManager.get().completeTask(sp, TaskDefinition.POTATO_BOTANIST, false);
         }

         ServerCosmeticsManager.get().checkTasks(sp);
      }
   }

   @SubscribeEvent
   public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer
         && !serverPlayer.isSpectator()
         && serverPlayer.level() instanceof ServerLevel serverLevel
         && serverLevel.dimension().equals(ModDimensions.MARS_LEVEL)) {
         BlockPos sleepingPos = serverPlayer.getSleepingPos().orElse(serverPlayer.blockPosition());
         if (serverLevel.getBlockState(sleepingPos).getBlock() instanceof SleepingPodBlock) {
            MarsWeatherSavedData weatherData = MarsWeatherSavedData.get(serverLevel);
            if (weatherData != null && weatherData.getCurrentState() == MarsWeatherState.GLOBAL_DUST_STORM) {
               ServerCosmeticsManager.get().completeTask(serverPlayer, TaskDefinition.STORM_SURVIVOR, false);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRightClickBlock(RightClickBlock event) {
      if (event.getLevel() instanceof ServerLevel sl && sl.dimension().equals(ModDimensions.MARS_LEVEL)) {
         boolean isProbeChest = false;
         if (sl.getBlockEntity(event.getPos()) instanceof RandomizableContainerBlockEntity chest) {
            ResourceKey<LootTable> lootTableKey = chest.getLootTable();
            if (lootTableKey != null && lootTableKey.location().getPath().contains("crashed_")) {
               isProbeChest = true;
            }
         }

         if (!isProbeChest) {
            StructureStart struct = sl.structureManager()
               .getStructureWithPieceAt(event.getPos(), holder -> holder.unwrapKey().map(k -> k.location().getPath().contains("crashed_")).orElse(false));
            if (struct.isValid() && sl.getBlockState(event.getPos()).is(Blocks.CHEST)) {
               isProbeChest = true;
            }
         }

         if (isProbeChest && event.getEntity() instanceof ServerPlayer sp && !sp.isSpectator()) {
            ServerCosmeticsManager.get().completeTask(sp, TaskDefinition.PROBE_SALVAGER, false);
         }
      }
   }

   @SubscribeEvent
   public static void onEntityInteract(EntityInteract event) {
      if (event.getEntity() instanceof ServerPlayer player && !player.isSpectator()) {
         if (event.getTarget() instanceof ZombieVillager zv
            && zv.hasEffect(MobEffects.WEAKNESS)
            && (event.getItemStack().is(Items.GOLDEN_APPLE) || event.getItemStack().is(Items.ENCHANTED_GOLDEN_APPLE))) {
            ServerCosmeticsManager.get().completeTask(player, TaskDefinition.CURE_VILLAGER, false);
         }

         if (event.getTarget() instanceof Villager villager && villager.getVillagerData().getLevel() >= 5) {
            ServerCosmeticsManager.get().completeTask(player, TaskDefinition.MASTER_TRADER, false);
         }

         return;
      }
   }

   private static record EggImpactRecord(UUID playerUuid, long gameTime, Vec3 pos) {
   }
}

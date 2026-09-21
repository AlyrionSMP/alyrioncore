package xyz.alyrion.alyrioncore.cosmetics;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.alyrion.alyrioncore.compat.OpacCompat;
import xyz.alyrion.alyrioncore.compat.VacuumAtmosphere;
import xyz.alyrion.alyrioncore.world.ModDimensions;
import xyz.alyrion.alyrioncore.world.habitat.HabitatSealManager;

public enum TaskDefinition {
   GOING_TO_SPACE("task_space", "Going to Space", "Launch into Space or Orbit (Cosmonautics / Orbit).", 5, CosmeticsRegistry.fromId("stars"), player -> {
      if (player.level() == null) {
         return false;
      } else {
         ResourceLocation dim = player.level().dimension().location();
         String dimStr = dim.toString().toLowerCase();
         String path = dim.getPath().toLowerCase();
         String namespace = dim.getNamespace().toLowerCase();
         if (dimStr.contains("space") || dimStr.contains("orbit") || dimStr.contains("asteroid")) {
            return true;
         } else if (namespace.contains("cosmonautics") && !path.contains("moon") && !path.contains("earth")) {
            return true;
         } else {
            try {
               Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
               String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
               if (biomeStr.contains("space") || biomeStr.contains("orbit") || biomeStr.contains("asteroid")) {
                  return true;
               }
            } catch (Throwable var7) {
            }

            return false;
         }
      }
   }),
   GOING_TO_MOON("task_moon", "Going to the Moon", "Touch down on the lunar surface (Cosmonautics Moon).", 5, CosmeticsRegistry.fromId("moon"), player -> {
      if (player.level() == null) {
         return false;
      } else {
         ResourceLocation dim = player.level().dimension().location();
         String dimStr = dim.toString().toLowerCase();
         if (!dimStr.contains("moon") && !dimStr.contains("luna")) {
            try {
               Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
               String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
               if (biomeStr.contains("moon") || biomeStr.contains("luna") || biomeStr.contains("lunar")) {
                  return true;
               }
            } catch (Throwable var5x) {
            }

            return false;
         } else {
            return true;
         }
      }
   }),
   GOING_TO_MARS(
      "task_mars",
      "Going to Mars",
      "Touch down on the red Martian surface.",
      5,
      CosmeticsRegistry.fromId("marsian"),
      player -> {
         if (player.level() == null) {
            return false;
         } else if (player.level().dimension().equals(ModDimensions.MARS_LEVEL)) {
            return true;
         } else {
            ResourceLocation dim = player.level().dimension().location();
            String dimStr = dim.toString().toLowerCase();
            if (!dimStr.contains("mars") && !dimStr.contains("martian")) {
               AttributeInstance gravityAttr = player.getAttribute(Attributes.GRAVITY);
               if (gravityAttr != null) {
                  ResourceLocation marsGravityId = ResourceLocation.fromNamespaceAndPath("alyrioncore", "mars_gravity");
                  if (gravityAttr.hasModifier(marsGravityId)) {
                     return true;
                  }
               }

               try {
                  Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
                  String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
                  if (biomeStr.contains("mars")
                     || biomeStr.contains("vastitas")
                     || biomeStr.contains("olympus")
                     || biomeStr.contains("tharsis")
                     || biomeStr.contains("valles")
                     || biomeStr.contains("planum_boreum")
                     || biomeStr.contains("noachis")) {
                     return true;
                  }
               } catch (Throwable var6x) {
               }

               return false;
            } else {
               return true;
            }
         }
      }
   ),
   OBTAINING_DRAGON_EGG(
      "task_dragon_egg",
      "Obtaining the Dragon Egg",
      "Slay the Ender Dragon and hold the Dragon Egg in your inventory.",
      10,
      CosmeticsRegistry.fromId("ender"),
      player -> {
         if (player instanceof ServerPlayer sp && ServerCosmeticsManager.get().getPlayerData(sp).isTaskCompleted("task_dragon_egg")) {
            return true;
         }

         if (player.getInventory() == null) {
            return false;
         } else {
            for (ItemStack stack : player.getInventory().items) {
               if (!stack.isEmpty() && stack.is(Items.DRAGON_EGG)) {
                  return true;
               }
            }

            for (ItemStack stackx : player.getInventory().offhand) {
               if (!stackx.isEmpty() && stackx.is(Items.DRAGON_EGG)) {
                  return true;
               }
            }

            return false;
         }
      }
   ),
   SLAYING_PLAYERS(
      "task_kills",
      "Grim Reaper",
      "Slay 10 players in Survival.",
      5,
      CosmeticsRegistry.fromId("grim"),
      player -> player instanceof ServerPlayer serverPlayer ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).getPvpKills() >= 10 : false
   ),
   PARTY_OF_FOUR(
      "task_party",
      "United We Stand",
      "Be a member of a party with at least 4 players (Open Parties and Claims).",
      5,
      CosmeticsRegistry.fromId("pride"),
      player -> player instanceof ServerPlayer serverPlayer ? OpacCompat.isPartySizeAtLeast(serverPlayer, 4) : false
   ),
   TRAVEL_10K(
      "task_travel_10k",
      "Wanderer (10k Blocks)",
      "Travel 10,000 blocks across the world.",
      10,
      null,
      player -> player instanceof ServerPlayer serverPlayer ? getTotalBlocksTraveled(serverPlayer) >= 10000L : false
   ),
   TRAVEL_100K("task_travel_100k", "Explorer (100k Blocks)", "Travel 100,000 blocks across the world (requires Wanderer task).", 50, null, player -> {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      } else {
         PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(serverPlayer);
         return data.isTaskCompleted("task_travel_10k") && getTotalBlocksTraveled(serverPlayer) >= 100000L;
      }
   }),
   TRAVEL_1M("task_travel_1m", "Globetrotter (1M Blocks)", "Travel 1,000,000 blocks across the world (requires Explorer task).", 100, null, player -> {
      if (!(player instanceof ServerPlayer serverPlayer)) {
         return false;
      } else {
         PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(serverPlayer);
         return data.isTaskCompleted("task_travel_100k") && getTotalBlocksTraveled(serverPlayer) >= 1000000L;
      }
   }),
   CHICKEN(
      "task_chicken",
      "Egg Hatching",
      "Spawn a chicken by throwing eggs.",
      5,
      CosmeticsRegistry.fromId("chicken"),
      player -> player instanceof ServerPlayer serverPlayer ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_chicken") : false
   ),
   TRANS_PRIDE("task_trans_pride", "True Colors", "Collect each vanilla dye.", 5, CosmeticsRegistry.fromId("trans_pride"), player -> {
      if (player instanceof ServerPlayer serverPlayer) {
         PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(serverPlayer);
         if (data.isTaskCompleted("task_trans_pride")) {
            return true;
         } else {
            boolean added = false;
            if (player.getInventory() != null) {
               for (ItemStack stack : player.getInventory().items) {
                  if (!stack.isEmpty() && stack.getItem() instanceof DyeItem dye) {
                     added |= data.addCollectedDye(dye.getDyeColor().getName());
                  }
               }

               for (ItemStack stackx : player.getInventory().offhand) {
                  if (!stackx.isEmpty() && stackx.getItem() instanceof DyeItem dye) {
                     added |= data.addCollectedDye(dye.getDyeColor().getName());
                  }
               }
            }

            if (added) {
               CosmeticsSavedData savedData = CosmeticsSavedData.get(serverPlayer.server);
               if (savedData != null) {
                  savedData.setDirty();
               }
            }

            return data.getCollectedDyes().size() >= 16;
         }
      } else {
         return false;
      }
   }),
   COMMUNISM(
      "task_communism",
      "Hammer and Sickle",
      "Repair an item in an anvil.",
      5,
      CosmeticsRegistry.fromId("communism"),
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_communism")
            : false
   ),
   POTATO_BOTANIST(
      "task_martian_potato",
      "Martian Botanist",
      "Harvest a mature Martian Potato inside a sealed habitat.",
      10,
      null,
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_martian_potato")
            : false
   ),
   LIFE_SUPPORT(
      "task_life_support",
      "Life Support",
      "Stand inside a fully sealed and oxygenated habitat.",
      10,
      null,
      player -> {
         if (player.level() == null) {
            return false;
         } else {
            return !VacuumAtmosphere.isVacuum(player.level(), (double)player.blockPosition().getY())
               ? false
               : HabitatSealManager.sealState(player.level(), player.blockPosition()).oxygen();
         }
      }
   ),
   STORM_SURVIVOR(
      "task_weather_storm",
      "Storm Survivor",
      "Sleep through a global Martian dust storm in a Sleeping Pod.",
      10,
      null,
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_weather_storm")
            : false
   ),
   PROBE_SALVAGER(
      "task_probe_salvager",
      "Probe Salvager",
      "Loot a crashed probe on the Martian surface.",
      15,
      null,
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_probe_salvager")
            : false
   ),
   FORTRESS(
      "task_netherite_reinforcement",
      "Impenetrable Fortress",
      "Reinforce a block with a Netherite Reinforcement Plate.",
      15,
      null,
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_netherite_reinforcement")
            : false
   ),
   STRIDER_TOUR("task_strider_tour", "Lava Ferry", "Ride a Strider over lava for at least 1,000 blocks.", 10, null, player -> {
      if (player instanceof ServerPlayer serverPlayer) {
         ServerStatsCounter stats = serverPlayer.getStats();
         return stats == null ? false : stats.getValue(Stats.CUSTOM, Stats.STRIDER_ONE_CM) >= 100000;
      } else {
         return false;
      }
   }),
   RELIC_HUNTER("task_relic_hunter", "Relic Hunter", "Discover a Decorated Pot Sherd or Sniffer Egg through archaeology.", 5, null, player -> {
      if (player.getInventory() == null) {
         return false;
      } else {
         for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && (stack.is(ItemTags.DECORATED_POT_SHERDS) || stack.is(Items.SNIFFER_EGG))) {
               return true;
            }
         }

         for (ItemStack stackx : player.getInventory().offhand) {
            if (!stackx.isEmpty() && (stackx.is(ItemTags.DECORATED_POT_SHERDS) || stackx.is(Items.SNIFFER_EGG))) {
               return true;
            }
         }

         return false;
      }
   }),
   WITHER_SLAYER(
      "task_wither_slayer",
      "Nether Star Ascendant",
      "Defeat the Wither in mortal combat.",
      15,
      CosmeticsRegistry.fromId("wither"),
      player -> {
         if (player instanceof ServerPlayer serverPlayer) {
            ServerStatsCounter stats = serverPlayer.getStats();
            return stats != null && stats.getValue(Stats.ENTITY_KILLED.get(EntityType.WITHER)) > 0
               ? true
               : ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_wither_slayer");
         } else {
            return false;
         }
      }
   ),
   MASTER_TRADER(
      "task_master_trader",
      "Master Negotiator",
      "Trade with a Master-level villager.",
      10,
      null,
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_master_trader")
            : false
   ),
   MACH_SPEED("task_mach_speed", "Mach Speed", "Reach a horizontal flight speed of 40 m/s with Elytra.", 10, null, player -> {
      if (player.isFallFlying()) {
         Vec3 delta = player.getDeltaMovement();
         double horizontalSpeed = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
         return horizontalSpeed >= 2.0;
      } else {
         return false;
      }
   }),
   CURE_VILLAGER(
      "task_cure_villager",
      "Miracle Worker",
      "Cure a Zombie Villager back into a normal villager.",
      5,
      CosmeticsRegistry.fromId("zombie_villager"),
      player -> player instanceof ServerPlayer serverPlayer
            ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).isTaskCompleted("task_cure_villager")
            : false
   ),
   COIN_OVERLOAD(
      "task_coin_overload",
      "Coin Overload",
      "Amass a fortune of 1,000 Alyrion Coins.",
      50,
      CosmeticsRegistry.fromId("overload"),
      player -> player instanceof ServerPlayer serverPlayer ? ServerCosmeticsManager.get().getPlayerData(serverPlayer).getCoins() >= 1000 : false
   ),
   DEATH_100("task_death_100", "Century of Demise", "Die 100 times in your journey.", 5, CosmeticsRegistry.fromId("creeper"), player -> {
      if (player instanceof ServerPlayer serverPlayer) {
         ServerStatsCounter stats = serverPlayer.getStats();
         return stats == null ? false : stats.getValue(Stats.CUSTOM, Stats.DEATHS) >= 100;
      } else {
         return false;
      }
   }),
   MINE_10K(
      "task_mine_10k",
      "Master Miner (10k Blocks)",
      "Mine 10,000 blocks across the world.",
      10,
      null,
      player -> player instanceof ServerPlayer serverPlayer ? getTotalBlocksMined(serverPlayer) >= 10000L : false
   ),
   EVERY_ORE(
      "task_all_ores",
      "Master Prospector",
      "Obtain every vanilla ore (Coal, Iron, Copper, Gold, Redstone, Emerald, Lapis, Diamond, Nether Quartz, Ancient Debris).",
      15,
      null,
      player -> {
         if (player instanceof ServerPlayer serverPlayer) {
            PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(serverPlayer);
            if (data.isTaskCompleted("task_all_ores")) {
               return true;
            } else {
               boolean added = checkMinedOres(serverPlayer, data);
               if (serverPlayer.getInventory() != null) {
                  for (ItemStack stack : serverPlayer.getInventory().items) {
                     String ore = identifyOre(stack);
                     if (ore != null) {
                        added |= data.addCollectedOre(ore);
                     }
                  }

                  for (ItemStack stackx : serverPlayer.getInventory().offhand) {
                     String ore = identifyOre(stackx);
                     if (ore != null) {
                        added |= data.addCollectedOre(ore);
                     }
                  }
               }

               if (added) {
                  CosmeticsSavedData savedData = CosmeticsSavedData.get(serverPlayer.server);
                  if (savedData != null) {
                     savedData.setDirty();
                  }
               }

               return data.getCollectedOres().size() >= 10;
            }
         } else {
            return false;
         }
      }
   ),
   DIAMONDS_64("task_diamonds_64", "Diamond Hoarder", "Amass 64 diamonds in your inventory.", 10, null, player -> {
      if (player == null) {
         return false;
      } else {
         if (player instanceof ServerPlayer serverPlayer) {
            ServerStatsCounter stats = serverPlayer.getStats();
            if (stats != null && stats.getValue(Stats.ITEM_PICKED_UP.get(Items.DIAMOND)) >= 64) {
               return true;
            }
         }

         if (player.getInventory() == null) {
            return false;
         } else {
            int count = 0;

            for (ItemStack stack : player.getInventory().items) {
               if (!stack.isEmpty() && stack.is(Items.DIAMOND)) {
                  count += stack.getCount();
                  if (count >= 64) {
                     return true;
                  }
               }
            }

            for (ItemStack stackx : player.getInventory().offhand) {
               if (!stackx.isEmpty() && stackx.is(Items.DIAMOND)) {
                  count += stackx.getCount();
                  if (count >= 64) {
                     return true;
                  }
               }
            }

            return count >= 64;
         }
      }
   });

   private final String id;
   private final String title;
   private final String description;
   private final int coinReward;
   private final CosmeticDefinition reward;
   private final Predicate<Player> condition;
   private static final Field STATS_FIELD;

   private TaskDefinition(String id, String title, String description, int coinReward, CosmeticDefinition reward, Predicate<Player> condition) {
      this.id = id;
      this.title = title;
      this.description = description;
      this.coinReward = coinReward;
      this.reward = reward;
      this.condition = condition;
   }

   public String getId() {
      return this.id;
   }

   public String getTitle() {
      return this.title;
   }

   public Component getTitleComponent() {
      return Component.translatable("task.alyrioncore." + this.id + ".title");
   }

   public String getDescription() {
      return this.description;
   }

   public Component getDescriptionComponent() {
      return Component.translatable("task.alyrioncore." + this.id + ".desc");
   }

   public int getCoinReward() {
      return this.coinReward;
   }

   public CosmeticDefinition getReward() {
      return this.reward;
   }

   public boolean test(Player player) {
      if (player == null) {
         return false;
      } else {
         try {
            return this.condition.test(player);
         } catch (Throwable var3) {
            return false;
         }
      }
   }

   public static TaskDefinition fromId(String id) {
      if (id == null) {
         return null;
      } else {
         for (TaskDefinition task : values()) {
            if (task.id.equalsIgnoreCase(id)) {
               return task;
            }
         }

         return null;
      }
   }

   public static TaskDefinition forReward(CosmeticDefinition cosmetic) {
      return cosmetic == null ? null : forReward(cosmetic.getId());
   }

   public static TaskDefinition forReward(String cosmeticId) {
      if (cosmeticId != null && !cosmeticId.isEmpty()) {
         for (TaskDefinition task : values()) {
            if (task.reward != null && cosmeticId.equalsIgnoreCase(task.reward.getId())) {
               return task;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static long getTotalBlocksTraveled(ServerPlayer player) {
      if (player == null) {
         return 0L;
      } else {
         ServerStatsCounter stats = player.getStats();
         if (stats == null) {
            return 0L;
         } else {
            long totalCm = 0L;
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.WALK_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.CROUCH_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.SWIM_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.WALK_ON_WATER_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.WALK_UNDER_WATER_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.CLIMB_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.FALL_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.AVIATE_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.BOAT_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.HORSE_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.MINECART_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.PIG_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.STRIDER_ONE_CM);
            totalCm += (long)stats.getValue(Stats.CUSTOM, Stats.FLY_ONE_CM);
            return totalCm / 100L;
         }
      }
   }

   public static long getTotalBlocksMined(ServerPlayer player) {
      if (player == null) {
         return 0L;
      } else {
         ServerStatsCounter stats = player.getStats();
         if (stats == null) {
            return 0L;
         } else {
            try {
               if (STATS_FIELD != null) {
                  Object2IntMap<Stat<?>> map = (Object2IntMap<Stat<?>>)STATS_FIELD.get(stats);
                  if (map != null) {
                     long total = 0L;
                     ObjectIterator var9 = map.object2IntEntrySet().iterator();

                     while (var9.hasNext()) {
                        Entry<Stat<?>> entry = (Entry<Stat<?>>)var9.next();
                        if (((Stat)entry.getKey()).getType() == Stats.BLOCK_MINED) {
                           total += (long)entry.getIntValue();
                        }
                     }

                     return total;
                  }
               }
            } catch (Throwable var7) {
            }

            long total = 0L;

            for (Block block : BuiltInRegistries.BLOCK) {
               total += (long)stats.getValue(Stats.BLOCK_MINED, block);
            }

            return total;
         }
      }
   }

   private static boolean checkMinedOres(ServerPlayer player, PlayerCosmeticsData data) {
      ServerStatsCounter stats = player.getStats();
      if (stats == null) {
         return false;
      } else {
         boolean added = false;
         if (stats.getValue(Stats.BLOCK_MINED, Blocks.COAL_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_COAL_ORE) > 0) {
            added |= data.addCollectedOre("coal");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.IRON_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_IRON_ORE) > 0) {
            added |= data.addCollectedOre("iron");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.COPPER_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_COPPER_ORE) > 0) {
            added |= data.addCollectedOre("copper");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.GOLD_ORE) > 0
            || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_GOLD_ORE) > 0
            || stats.getValue(Stats.BLOCK_MINED, Blocks.NETHER_GOLD_ORE) > 0) {
            added |= data.addCollectedOre("gold");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.REDSTONE_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_REDSTONE_ORE) > 0) {
            added |= data.addCollectedOre("redstone");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.EMERALD_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_EMERALD_ORE) > 0) {
            added |= data.addCollectedOre("emerald");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.LAPIS_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_LAPIS_ORE) > 0) {
            added |= data.addCollectedOre("lapis");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.DIAMOND_ORE) > 0 || stats.getValue(Stats.BLOCK_MINED, Blocks.DEEPSLATE_DIAMOND_ORE) > 0) {
            added |= data.addCollectedOre("diamond");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.NETHER_QUARTZ_ORE) > 0) {
            added |= data.addCollectedOre("quartz");
         }

         if (stats.getValue(Stats.BLOCK_MINED, Blocks.ANCIENT_DEBRIS) > 0) {
            added |= data.addCollectedOre("ancient_debris");
         }

         return added;
      }
   }

   private static String identifyOre(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return null;
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_COAL)
         || stack.is(Items.COAL_ORE)
         || stack.is(Items.DEEPSLATE_COAL_ORE)
         || stack.is(Items.COAL)) {
         return "coal";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_IRON)
         || stack.is(Items.IRON_ORE)
         || stack.is(Items.DEEPSLATE_IRON_ORE)
         || stack.is(Items.RAW_IRON)) {
         return "iron";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_COPPER)
         || stack.is(Items.COPPER_ORE)
         || stack.is(Items.DEEPSLATE_COPPER_ORE)
         || stack.is(Items.RAW_COPPER)) {
         return "copper";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_GOLD)
         || stack.is(Items.GOLD_ORE)
         || stack.is(Items.DEEPSLATE_GOLD_ORE)
         || stack.is(Items.NETHER_GOLD_ORE)
         || stack.is(Items.RAW_GOLD)) {
         return "gold";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_REDSTONE)
         || stack.is(Items.REDSTONE_ORE)
         || stack.is(Items.DEEPSLATE_REDSTONE_ORE)
         || stack.is(Items.REDSTONE)) {
         return "redstone";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_EMERALD)
         || stack.is(Items.EMERALD_ORE)
         || stack.is(Items.DEEPSLATE_EMERALD_ORE)
         || stack.is(Items.EMERALD)) {
         return "emerald";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_LAPIS)
         || stack.is(Items.LAPIS_ORE)
         || stack.is(Items.DEEPSLATE_LAPIS_ORE)
         || stack.is(Items.LAPIS_LAZULI)) {
         return "lapis";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_DIAMOND)
         || stack.is(Items.DIAMOND_ORE)
         || stack.is(Items.DEEPSLATE_DIAMOND_ORE)
         || stack.is(Items.DIAMOND)) {
         return "diamond";
      } else if (stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_QUARTZ) || stack.is(Items.NETHER_QUARTZ_ORE) || stack.is(Items.QUARTZ)) {
         return "quartz";
      } else {
         return !stack.is(net.neoforged.neoforge.common.Tags.Items.ORES_NETHERITE_SCRAP) && !stack.is(Items.ANCIENT_DEBRIS) ? null : "ancient_debris";
      }
   }

   static {
      Field f = null;

      try {
         f = StatsCounter.class.getDeclaredField("stats");
         f.setAccessible(true);
      } catch (Throwable var6) {
         for (Field field : StatsCounter.class.getDeclaredFields()) {
            if (Object2IntMap.class.isAssignableFrom(field.getType())) {
               field.setAccessible(true);
               f = field;
               break;
            }
         }
      }

      STATS_FIELD = f;
   }
}

package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.compat.OpacCompat;
import xyz.alyrion.alyrioncore.world.ModDimensions;

import java.util.function.Predicate;

/**
 * Tasks grant coins and a cosmetic reward (any {@link CosmeticType}, not just
 * capes). Rewards are looked up from {@link CosmeticsRegistry} so a task can
 * reward a cape, a pet, a trail or any future cosmetic kind.
 */
public enum TaskDefinition {
    GOING_TO_SPACE(
            "task_space",
            "Going to Space",
            "Launch into Space or Orbit (Cosmonautics / Orbit).",
            5,
            CosmeticsRegistry.fromId("stars"),
            player -> {
                if (player.level() == null) return false;
                ResourceLocation dim = player.level().dimension().location();
                String dimStr = dim.toString().toLowerCase();
                String path = dim.getPath().toLowerCase();
                String namespace = dim.getNamespace().toLowerCase();

                if (dimStr.contains("space") || dimStr.contains("orbit") || dimStr.contains("asteroid")) {
                    return true;
                }
                if (namespace.contains("cosmonautics") && !path.contains("moon") && !path.contains("earth")) {
                    return true;
                }

                try {
                    Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
                    String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
                    if (biomeStr.contains("space") || biomeStr.contains("orbit") || biomeStr.contains("asteroid")) {
                        return true;
                    }
                } catch (Throwable ignored) {}

                return false;
            }
    ),
    GOING_TO_MOON(
            "task_moon",
            "Going to the Moon",
            "Touch down on the lunar surface (Cosmonautics Moon).",
            5,
            CosmeticsRegistry.fromId("moon"),
            player -> {
                if (player.level() == null) return false;
                ResourceLocation dim = player.level().dimension().location();
                String dimStr = dim.toString().toLowerCase();

                if (dimStr.contains("moon") || dimStr.contains("luna")) {
                    return true;
                }

                try {
                    Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
                    String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
                    if (biomeStr.contains("moon") || biomeStr.contains("luna") || biomeStr.contains("lunar")) {
                        return true;
                    }
                } catch (Throwable ignored) {}

                return false;
            }
    ),
    GOING_TO_MARS(
            "task_mars",
            "Going to Mars",
            "Touch down on the red Martian surface.",
            5,
            CosmeticsRegistry.fromId("marsian"),
            player -> {
                if (player.level() == null) return false;

                // 1. Direct dimension equality
                if (player.level().dimension().equals(ModDimensions.MARS_LEVEL)) {
                    return true;
                }

                // 2. Dimension ID inspection
                ResourceLocation dim = player.level().dimension().location();
                String dimStr = dim.toString().toLowerCase();
                if (dimStr.contains("mars") || dimStr.contains("martian")) {
                    return true;
                }

                // 3. Martian Physics / Gravity modifier check
                AttributeInstance gravityAttr = player.getAttribute(Attributes.GRAVITY);
                if (gravityAttr != null) {
                    ResourceLocation marsGravityId = ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars_gravity");
                    if (gravityAttr.hasModifier(marsGravityId)) {
                        return true;
                    }
                }

                // 4. Martian Biome check
                try {
                    Holder<Biome> biomeHolder = player.level().getBiome(player.blockPosition());
                    String biomeStr = biomeHolder.unwrapKey().map(k -> k.location().toString().toLowerCase()).orElse("");
                    if (biomeStr.contains("mars") || biomeStr.contains("vastitas") || biomeStr.contains("olympus")
                            || biomeStr.contains("tharsis") || biomeStr.contains("valles")
                            || biomeStr.contains("planum_boreum") || biomeStr.contains("noachis")) {
                        return true;
                    }
                } catch (Throwable ignored) {}

                return false;
            }
    ),
    OBTAINING_DRAGON_EGG(
            "task_dragon_egg",
            "Obtaining the Dragon Egg",
            "Slay the Ender Dragon and hold the Dragon Egg in your inventory.",
            10,
            null,
            player -> {
                if (player.getInventory() == null) return false;
                for (ItemStack stack : player.getInventory().items) {
                    if (!stack.isEmpty() && stack.is(Items.DRAGON_EGG)) return true;
                }
                for (ItemStack stack : player.getInventory().offhand) {
                    if (!stack.isEmpty() && stack.is(Items.DRAGON_EGG)) return true;
                }
                return false;
            }
    ),
    SLAYING_PLAYERS(
            "task_kills",
            "Grim Reaper",
            "Slay 10 players in Survival.",
            5,
            CosmeticsRegistry.fromId("grim"),
            player -> {
                if (!(player instanceof ServerPlayer serverPlayer)) return false;
                return ServerCosmeticsManager.get().getPlayerData(serverPlayer).getPvpKills() >= 10;
            }
    ),
    PARTY_OF_FOUR(
            "task_party",
            "United We Stand",
            "Be a member of a party with at least 4 players (Open Parties and Claims).",
            5,
            CosmeticsRegistry.fromId("pride"),
            player -> {
                if (!(player instanceof ServerPlayer serverPlayer)) return false;
                return OpacCompat.isPartySizeAtLeast(serverPlayer, 4);
            }
    );

    private final String id;
    private final String title;
    private final String description;
    private final int coinReward;
    private final CosmeticDefinition reward;
    private final Predicate<Player> condition;

    TaskDefinition(String id, String title, String description, int coinReward, CosmeticDefinition reward, Predicate<Player> condition) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coinReward = coinReward;
        this.reward = reward;
        this.condition = condition;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Component getTitleComponent() {
        return Component.translatable("task.alyrioncore." + id + ".title");
    }

    public String getDescription() {
        return description;
    }

    public Component getDescriptionComponent() {
        return Component.translatable("task.alyrioncore." + id + ".desc");
    }

    public int getCoinReward() {
        return coinReward;
    }

    /** The cosmetic unlocked by completing this task (any type), or null. */
    public CosmeticDefinition getReward() {
        return reward;
    }

    public boolean test(Player player) {
        if (player == null) return false;
        try {
            return condition.test(player);
        } catch (Throwable e) {
            return false;
        }
    }

    public static TaskDefinition fromId(String id) {
        if (id == null) return null;
        for (TaskDefinition task : values()) {
            if (task.id.equalsIgnoreCase(id)) {
                return task;
            }
        }
        return null;
    }
}

package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.world.ModDimensions;

import java.util.function.Predicate;

public enum TaskDefinition {
    GOING_TO_SPACE(
            "task_space",
            "Going to Space",
            "Launch into Space or Orbit (Cosmonautics / Orbit).",
            5,
            CapeDefinition.STARS,
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
            CapeDefinition.MOON,
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
            CapeDefinition.MARSIAN,
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
    );

    private final String id;
    private final String title;
    private final String description;
    private final int coinReward;
    private final CapeDefinition capeReward;
    private final Predicate<Player> condition;

    TaskDefinition(String id, String title, String description, int coinReward, CapeDefinition capeReward, Predicate<Player> condition) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coinReward = coinReward;
        this.capeReward = capeReward;
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

    public CapeDefinition getCapeReward() {
        return capeReward;
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

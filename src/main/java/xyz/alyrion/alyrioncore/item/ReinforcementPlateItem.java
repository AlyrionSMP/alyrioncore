package xyz.alyrion.alyrioncore.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import xyz.alyrion.alyrioncore.block.ReinforcementTier;

import java.util.List;

/**
 * A reinforcement plate. Right-clicking a breakable, block-entity-free block
 * with one in hand replaces the block with a {@code reinforced_block} wrapper
 * (see {@link xyz.alyrion.alyrioncore.event.ReinforcementEvents}) that must be
 * mined {@code tier.getHits()} times before it really breaks.
 */
public class ReinforcementPlateItem extends Item {

    private final ReinforcementTier tier;

    public ReinforcementPlateItem(ReinforcementTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public ReinforcementTier getTier() {
        return this.tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Reinforces a block:"));
        tooltip.add(Component.literal("§7requires §f" + this.tier.getHits() + "§7 breaks to destroy"));
        tooltip.add(Component.literal("§8Right-click a block to apply"));
    }
}

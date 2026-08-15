package xyz.alyrion.alyrioncore.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.block.AirlockBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AlyrionCore.MODID);

    public static final Supplier<BlockEntityType<AirlockBlockEntity>> AIRLOCK =
            BLOCK_ENTITY_TYPES.register("airlock",
                    () -> BlockEntityType.Builder.of(AirlockBlockEntity::new, ModBlocks.AIRLOCK.get()).build(null));
}

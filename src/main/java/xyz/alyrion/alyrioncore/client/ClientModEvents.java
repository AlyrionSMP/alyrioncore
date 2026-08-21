package xyz.alyrion.alyrioncore.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.renderer.AirlockBlockEntityRenderer;
import xyz.alyrion.alyrioncore.client.renderer.CosmeticRenderLayer;
import xyz.alyrion.alyrioncore.client.renderer.OxygenGeneratorBlockEntityRenderer;
import xyz.alyrion.alyrioncore.client.renderer.ReinforcedBlockEntityRenderer;
import xyz.alyrion.alyrioncore.client.renderer.SatellitePetModel;
import xyz.alyrion.alyrioncore.client.renderer.UshankaModel;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;
import xyz.alyrion.alyrioncore.registry.ModItems;

@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars"),
                new MarsDimensionEffects()
        );
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.ESCAPE_KEY);
        event.register(ModKeyMappings.OPEN_STORE);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : event.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer = event.getSkin(model);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                // One generic layer dispatches to every cosmetic type's renderer
                // (capes, pets, trails, ... future types).
                playerRenderer.addLayer(new CosmeticRenderLayer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SatellitePetModel.LAYER, SatellitePetModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // The Ushanka replaces the vanilla helmet box with its own fur-cap model.
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return UshankaModel.getInstance();
            }
        }, ModItems.USHANKA.get());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIRLOCK.get(), AirlockBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OXYGEN_GENERATOR.get(), OxygenGeneratorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REINFORCED.get(), ReinforcedBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(AirlockBlockEntityRenderer.LEAF_BOTTOM);
        event.register(AirlockBlockEntityRenderer.LEAF_TOP);
        event.register(AirlockBlockEntityRenderer.WINDOW);
        event.register(AirlockBlockEntityRenderer.LED_GREEN);
        event.register(AirlockBlockEntityRenderer.LED_RED);
        event.register(OxygenGeneratorBlockEntityRenderer.FAN);
        for (int stage = 0; stage < 8; stage++) {
            event.register(ReinforcedBlockEntityRenderer.crackModel(stage));
        }
    }
}


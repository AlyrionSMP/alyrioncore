package xyz.alyrion.alyrioncore.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.client.renderer.AirlockBlockEntityRenderer;
import xyz.alyrion.alyrioncore.client.renderer.AlyrionCapeLayer;
import xyz.alyrion.alyrioncore.client.renderer.SatellitePetLayer;
import xyz.alyrion.alyrioncore.client.renderer.SatellitePetModel;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

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
                playerRenderer.addLayer(new AlyrionCapeLayer(playerRenderer));
                playerRenderer.addLayer(new SatellitePetLayer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SatellitePetModel.LAYER, SatellitePetModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIRLOCK.get(), AirlockBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(AirlockBlockEntityRenderer.LEAF_BOTTOM);
        event.register(AirlockBlockEntityRenderer.LEAF_TOP);
        event.register(AirlockBlockEntityRenderer.WINDOW);
        event.register(AirlockBlockEntityRenderer.LED_GREEN);
        event.register(AirlockBlockEntityRenderer.LED_RED);
    }
}


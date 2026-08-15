package xyz.alyrion.alyrioncore.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;

@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MarsPhysicsHandler {

    private static final ResourceLocation MARS_GRAVITY_ID =
            ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "mars_gravity");

    // Mars surface gravity is ~38% of Earth gravity (0.38g).
    // An ADD_MULTIPLIED_BASE of -0.62 reduces gravity to 38% of normal.
    private static final AttributeModifier MARS_GRAVITY_MODIFIER =
            new AttributeModifier(MARS_GRAVITY_ID, -0.62D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living) {
            AttributeInstance gravityAttr = living.getAttribute(Attributes.GRAVITY);
            if (gravityAttr == null) return;

            boolean inMars = living.level().dimension().equals(ModDimensions.MARS_LEVEL);

            if (inMars) {
                if (!gravityAttr.hasModifier(MARS_GRAVITY_ID)) {
                    gravityAttr.addTransientModifier(MARS_GRAVITY_MODIFIER);
                }
            } else {
                if (gravityAttr.hasModifier(MARS_GRAVITY_ID)) {
                    gravityAttr.removeModifier(MARS_GRAVITY_ID);
                }
            }
        }
    }
}

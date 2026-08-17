package xyz.alyrion.alyrioncore.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.registry.ModBlocks;
import xyz.alyrion.alyrioncore.registry.ModItems;
import xyz.alyrion.alyrioncore.world.ModDimensions;
import xyz.alyrion.alyrioncore.compat.VacuumAtmosphere;
import xyz.alyrion.alyrioncore.world.habitat.HabitatSealManager;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherSavedData;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = AlyrionCore.MODID)
public class CommonGameEvents {

    /** Last known seal state per player (for the diagnostic actionbar message). */
    private static final Map<UUID, Boolean> LAST_SEAL_STATE = new HashMap<>();

    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(ModItems.SULFUR_DUST.get())) {
            event.setBurnTime(1600); // 8 items smelted (80 seconds)
        } else if (event.getItemStack().is(ModBlocks.SULFUR_BLOCK.asItem())) {
            event.setBurnTime(16000); // 80 items smelted (800 seconds)
        }
    }

    /**
     * Bulletproof air refill for sealed habitats: after every entity tick on a
     * vacuum world, any entity inside a sealed room has its air supply restored
     * to maximum. This does not depend on the LivingBreatheEvent outcome, so it
     * works even if another mod (e.g. Rocketnautics) denies breathing for the
     * dimension — the sealed habitat always wins.
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living
                && living.level() instanceof ServerLevel serverLevel) {
            boolean vacuum = VacuumAtmosphere.isVacuum(serverLevel, living.getY());
            boolean sealed = vacuum && HabitatSealManager.isPositionSealed(serverLevel, living.blockPosition());
            if (sealed) {
                living.setAirSupply(living.getMaxAirSupply());
            }
            // Diagnostics (Mars / Moon): log the seal + air state every 5s; warn the
            // player on screen only when the air is dropping while in vacuum.
            if (living instanceof ServerPlayer player && vacuum
                    && serverLevel.getGameTime() % 100 == 0) {
                int air = living.getAirSupply();
                int max = living.getMaxAirSupply();
                if (!sealed) {
                    BlockPos leak = HabitatSealManager.getLastLeakPos();
                    Direction leakDir = HabitatSealManager.getLastLeakDir();
                    AlyrionCore.LOGGER.info("[habitat] {} sealed=false air={}/{} at {} (leak: {} via {})",
                            player.getName().getString(), air, max, living.blockPosition(),
                            leak == null ? "?" : leak, leakDir == null ? "?" : leakDir);
                } else {
                    AlyrionCore.LOGGER.info("[habitat] {} sealed=true air={}/{} at {}",
                            player.getName().getString(), air, max, living.blockPosition());
                }

            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingBreathe(LivingBreatheEvent event) {
        // Works on BOTH logical sides: the client's local air bar must agree with
        // the server so bubbles don't drain inside a sealed habitat.
        net.minecraft.world.level.Level level = event.getEntity().level();
        if (VacuumAtmosphere.isVacuum(level, event.getEntity().getY())) {
            // AlyrionCore owns the atmosphere rule on vacuum worlds (Mars, the Moon,
            // deep space...): sealed habitats grant breathable air, the open surface
            // is vacuum. Determined cooperatively from Rocketnautics' atmosphere API,
            // so no hardcoded dimension lists and no priority fight.
            boolean sealed = HabitatSealManager.isPositionSealed(level, event.getEntity().blockPosition());
            if (sealed) {
                event.setCanBreathe(true);
                event.setRefillAirAmount(Math.max(event.getRefillAirAmount(), 4));
                event.setConsumeAirAmount(0);
            } else if (!(event.getEntity() instanceof Player player) || !player.getAbilities().invulnerable) {
                // Open vacuum: deny breathing (creative is exempt).
                event.setCanBreathe(false);
            }
            // Diagnostic feedback: show the seal state whenever it changes,
            // so players can tell why they are (or aren't) breathing.
            if (level instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer player) {
                Boolean last = LAST_SEAL_STATE.get(player.getUUID());
                if (last == null || last != sealed) {
                    boolean wasSealed = last != null && last;
                    LAST_SEAL_STATE.put(player.getUUID(), sealed);
                    if (sealed) {
                        AlyrionCore.LOGGER.info("[habitat] {} pressurized habitat detected at {}",
                                player.getName().getString(), player.blockPosition());
                        player.displayClientMessage(Component.literal(
                                "§a✔ Pressurized habitat detected — breathing"), true);
                    } else if (wasSealed) {
                        BlockPos leak = HabitatSealManager.getLastLeakPos();
                        Direction leakDir = HabitatSealManager.getLastLeakDir();
                        AlyrionCore.LOGGER.info("[habitat] {} habitat BREACHED at {} (leak: {} via {})",
                                player.getName().getString(), player.blockPosition(),
                                leak == null ? "?" : leak, leakDir == null ? "?" : leakDir);
                        player.displayClientMessage(Component.literal(
                                "§c⚠ Habitat breached — depressurizing!"), true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() == ItemAbilities.HOE_TILL) {
            BlockState originalState = event.getState();
            if (originalState.is(ModBlocks.MARTIAN_REGOLITH.get()) || originalState.is(ModBlocks.MARTIAN_SAND.get())) {
                event.setFinalState(ModBlocks.REGOLITH_FARMLAND.get().defaultBlockState());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            HabitatSealManager.onBlockBreak(serverLevel, event.getPos(), event.getState());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Intercept vanilla BedBlock interaction on Mars so it does NOT explode when inside a sealed habitat
        if (state.getBlock() instanceof BedBlock && !event.getEntity().isSpectator()) {
            if (level instanceof ServerLevel serverLevel && VacuumAtmosphere.isVacuum(serverLevel, pos.getY())) {
                if (HabitatSealManager.isPositionSealed(serverLevel, pos)) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));

                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        BlockPos headPos = (state.getValue(BedBlock.PART) == BedPart.HEAD)
                                ? pos
                                : pos.relative(state.getValue(BedBlock.FACING));
                        // Guard against a half-broken bed: never start sleeping if the head block is gone
                        if (!level.getBlockState(headPos).is(state.getBlock())) {
                            return;
                        }
                        serverPlayer.startSleepInBed(headPos).ifLeft(problem -> {
                            if (problem.getMessage() != null) {
                                serverPlayer.displayClientMessage(problem.getMessage(), true);
                            }
                        });
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCanPlayerSleep(CanPlayerSleepEvent event) {
        BlockState state = event.getState();
        ServerPlayer player = event.getEntity();
        Level level = player.level();

        boolean isPod = state.is(ModBlocks.SLEEPING_POD.get());
        boolean isBedInSealedHabitat = false;

        if (state.getBlock() instanceof BedBlock && level instanceof ServerLevel serverLevel) {
            if (VacuumAtmosphere.isVacuum(serverLevel, event.getPos().getY()) && HabitatSealManager.isPositionSealed(serverLevel, event.getPos())) {
                isBedInSealedHabitat = true;
            }
        }

        if (isPod || isBedInSealedHabitat) {
            // When sleeping in a sleeping pod or inside a pressurized sealed habitat, override NOT_POSSIBLE_HERE
            if (event.getProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_HERE) {
                // Vanilla's Level.isDay() relies on the server's cached skyDarken field, which is not
                // reliably maintained for custom dimensions such as Mars. Recompute daylight directly from
                // the dimension clock (plus current rain/thunder) instead, and treat Martian dust storms as
                // the local equivalent of a thunderstorm: the pod may be used while one is raging.
                if (isDayTime(level) && !isMarsDustStorm(level)) {
                    event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
                } else if (!player.isCreative()) {
                    Vec3 vec3 = Vec3.atBottomCenterOf(event.getPos());
                    List<Monster> list = level.getEntitiesOfClass(
                            Monster.class,
                            new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0),
                            monster -> monster.isPreventingPlayerRest(player)
                    );
                    if (!list.isEmpty()) {
                        event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
                    } else {
                        event.setProblem(null);
                    }
                } else {
                    event.setProblem(null);
                }
            }
        }
    }

    /**
     * Replicates vanilla's {@link Level#isDay()} computation ("skyDarken < 4") directly from the
     * dimension clock and the current rain/thunder levels, instead of trusting the server's cached
     * {@code skyDarken} field. On custom dimensions such as Mars that field can lag or never reflect
     * the actual time of day, which made the sleeping pod refuse to work at night.
     */
    private static boolean isDayTime(Level level) {
        if (level.dimensionType().hasFixedTime()) {
            return true; // Fixed-time dimensions (Nether/End) always report "day" for sleeping
        }
        double rainDim = 1.0 - (double) level.getRainLevel(1.0F) * 5.0 / 16.0;
        double thunderDim = 1.0 - (double) level.getThunderLevel(1.0F) * 5.0 / 16.0;
        double d = Mth.frac((double) level.getDayTime() / 24000.0 - 0.25);
        double d1 = 0.5 - Math.cos(d * Math.PI) / 2.0;
        float timeOfDay = (float) (d * 2.0 + d1) / 3.0F;
        double d2 = 0.5 + 2.0 * Mth.clamp((double) Mth.cos(timeOfDay * (float) (Math.PI * 2)), -0.25, 0.25);
        return (int) ((1.0 - d2 * rainDim * thunderDim) * 11.0) < 4;
    }

    /**
     * Martian dust storms are the local analogue of an Earth thunderstorm: they darken the sky and
     * make the environment look like night (a global storm is a near-total blackout). Like vanilla's
     * "night or thunderstorms" rule, the sleeping pod (and sealed-habitat beds) may be used while a
     * regional or planet-encircling dust storm is raging, even during the day.
     */
    private static boolean isMarsDustStorm(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || !serverLevel.dimension().equals(ModDimensions.MARS_LEVEL)) {
            return false;
        }
        MarsWeatherState state = MarsWeatherSavedData.get(serverLevel).getCurrentState();
        return state == MarsWeatherState.REGIONAL_STORM || state == MarsWeatherState.GLOBAL_DUST_STORM;
    }

    @SubscribeEvent
    public static void onCanContinueSleeping(CanContinueSleepingEvent event) {
        if (event.getEntity() instanceof Player player) {
            Level level = player.level();
            // Dawn auto-wake: when daylight returns (and no dust storm is darkening the sky), do NOT
            // force continue sleeping so players wake up naturally at sunrise!
            if (isDayTime(level) && !isMarsDustStorm(level)) {
                return;
            }

            Optional<BlockPos> sleepPos = player.getSleepingPos();
            if (sleepPos.isPresent()) {
                BlockState state = level.getBlockState(sleepPos.get());
                if (state.is(ModBlocks.SLEEPING_POD.get())) {
                    event.setContinueSleeping(true);
                } else if (state.getBlock() instanceof BedBlock && level instanceof ServerLevel serverLevel) {
                    if (VacuumAtmosphere.isVacuum(serverLevel, sleepPos.get().getY()) && HabitatSealManager.isPositionSealed(serverLevel, sleepPos.get())) {
                        event.setContinueSleeping(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        // Custom dimensions such as Mars share the Overworld's time-of-day clock through
        // DerivedLevelData, whose setDayTime/setGameTime are intentional no-ops (Mojang MC-190731:
        // "Sleep doesn't advance to day in custom dimensions"). The vanilla skip-to-morning in
        // ServerLevel.tick therefore never advances the clock for them: it wakes the players but
        // leaves the night running. Apply the computed morning time to the Overworld clock, which
        // the Mars level reads back, so the night is actually skipped.
        if (event.getLevel() instanceof ServerLevel level
                && level.dimension().equals(ModDimensions.MARS_LEVEL)) {
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                overworld.setDayTime(event.getNewTime());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension().equals(ModDimensions.MARS_LEVEL)) {
                MarsWeatherSavedData.get(serverLevel).tick(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTo().equals(ModDimensions.MARS_LEVEL)) {
            MarsWeatherSavedData.get(serverPlayer.serverLevel()).broadcastWeather(serverPlayer.serverLevel());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.level().dimension().equals(ModDimensions.MARS_LEVEL)) {
            MarsWeatherSavedData.get(serverPlayer.serverLevel()).broadcastWeather(serverPlayer.serverLevel());
        }
    }
}

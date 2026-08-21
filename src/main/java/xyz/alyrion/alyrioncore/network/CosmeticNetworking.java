package xyz.alyrion.alyrioncore.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.ServerCosmeticsManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Networking for the generic cosmetics framework.
 *
 * Every payload is type-agnostic: clients ask to equip/purchase a cosmetic by
 * its registry id, the server answers with the full player state, and other
 * players' equipped slots are broadcast per {@code (player, slot type)}. Adding
 * a new {@code CosmeticType} requires no networking changes at all.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CosmeticNetworking {

    /**
     * Client-side payload receivers. Defaults are no-ops so this class stays
     * loadable on a dedicated server; {@code client.ClientNetworkHandlers}
     * (Dist.CLIENT) replaces them with the real implementations at startup,
     * well before any payload can arrive.
     */
    public static final class ClientHandlers {
        public java.util.function.Consumer<S2CSyncCosmeticsPayload> onSyncCosmetics = p -> {};
        public java.util.function.Consumer<S2CSyncCosmeticPayload> onSyncCosmeticSlot = p -> {};
        public java.util.function.IntConsumer onPlayUiSound = soundId -> {};
        public java.util.function.Consumer<MarsWeatherPayload> onMarsWeather = p -> {};
    }

    /** Set once by {@code client.ClientNetworkHandlers} during mod construction. */
    public static volatile ClientHandlers clientHandlers = new ClientHandlers();

    /** Cache of other players' equipped cosmetics on the client: UUID -> (typeId -> cosmeticId). */
    private static final Map<UUID, Map<String, String>> CLIENT_COSMETIC_MAP = new ConcurrentHashMap<>();

    public static String getClientPlayerCosmetic(UUID playerUuid, String typeId) {
        if (playerUuid == null || typeId == null) return null;
        Map<String, String> slots = CLIENT_COSMETIC_MAP.get(playerUuid);
        return slots != null ? slots.get(typeId) : null;
    }

    public static void setClientPlayerCosmetic(UUID playerUuid, String typeId, String cosmeticId) {
        if (playerUuid == null || typeId == null) return;
        Map<String, String> slots = CLIENT_COSMETIC_MAP.computeIfAbsent(playerUuid, u -> new ConcurrentHashMap<>());
        if (cosmeticId == null || cosmeticId.isEmpty()) {
            slots.remove(typeId);
            if (slots.isEmpty()) {
                CLIENT_COSMETIC_MAP.remove(playerUuid);
            }
        } else {
            slots.put(typeId, cosmeticId);
        }
    }

    /** Wipe all client-side cosmetics state when leaving a server. */
    public static void clearClientData() {
        CLIENT_COSMETIC_MAP.clear();
        CosmeticsManager.get().resetForDisconnect();
    }

    // Packet: Client -> Server: "equip/unequip cosmetic X in slot type T"
    // (empty cosmeticId = unequip that slot)
    public record C2SEquipCosmeticPayload(String typeId, String cosmeticId) implements CustomPacketPayload {
        public static final Type<C2SEquipCosmeticPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_equip_cosmetic"));

        public static final StreamCodec<ByteBuf, C2SEquipCosmeticPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, C2SEquipCosmeticPayload::typeId,
                ByteBufCodecs.STRING_UTF8, C2SEquipCosmeticPayload::cosmeticId,
                C2SEquipCosmeticPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Client -> Server: "I want to buy cosmetic X"
    public record C2SPurchaseCosmeticPayload(String cosmeticId) implements CustomPacketPayload {
        public static final Type<C2SPurchaseCosmeticPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_purchase_cosmetic"));

        public static final StreamCodec<ByteBuf, C2SPurchaseCosmeticPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(C2SPurchaseCosmeticPayload::new, C2SPurchaseCosmeticPayload::cosmeticId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Client -> Server: "Resend my full cosmetics state" (fallback sync request)
    public record C2SRequestCosmeticsPayload() implements CustomPacketPayload {
        public static final Type<C2SRequestCosmeticsPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_request_cosmetics"));

        public static final StreamCodec<ByteBuf, C2SRequestCosmeticsPayload> STREAM_CODEC =
                StreamCodec.unit(new C2SRequestCosmeticsPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Here is your full cosmetics state"
    public record S2CSyncCosmeticsPayload(
            int coins,
            long survivalPlaytimeSeconds,
            Set<String> unlockedCosmetics,
            List<EquippedSlot> equippedSlots,
            Set<String> completedTasks
    ) implements CustomPacketPayload {
        public static final Type<S2CSyncCosmeticsPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_sync_cosmetics"));

        public record EquippedSlot(String typeId, String cosmeticId) {
            public static final StreamCodec<ByteBuf, EquippedSlot> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, EquippedSlot::typeId,
                    ByteBufCodecs.STRING_UTF8, EquippedSlot::cosmeticId,
                    EquippedSlot::new
            );
        }

        public static final StreamCodec<ByteBuf, S2CSyncCosmeticsPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, S2CSyncCosmeticsPayload::coins,
                ByteBufCodecs.VAR_LONG, S2CSyncCosmeticsPayload::survivalPlaytimeSeconds,
                ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), S2CSyncCosmeticsPayload::unlockedCosmetics,
                ByteBufCodecs.collection(ArrayList::new, EquippedSlot.STREAM_CODEC), S2CSyncCosmeticsPayload::equippedSlots,
                ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), S2CSyncCosmeticsPayload::completedTasks,
                S2CSyncCosmeticsPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Player UUID has equipped cosmetic X in slot T"
    public record S2CSyncCosmeticPayload(UUID playerUuid, String typeId, String cosmeticId) implements CustomPacketPayload {
        public static final Type<S2CSyncCosmeticPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_sync_cosmetic"));

        public static final StreamCodec<ByteBuf, S2CSyncCosmeticPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                S2CSyncCosmeticPayload::playerUuid,
                ByteBufCodecs.STRING_UTF8,
                S2CSyncCosmeticPayload::typeId,
                ByteBufCodecs.STRING_UTF8,
                S2CSyncCosmeticPayload::cosmeticId,
                S2CSyncCosmeticPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Play a UI sound" (rewards are decided server-side)
    public record S2CPlaySoundPayload(int soundId) implements CustomPacketPayload {
        public static final Type<S2CPlaySoundPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_play_sound"));

        public static final StreamCodec<ByteBuf, S2CPlaySoundPayload> STREAM_CODEC =
                ByteBufCodecs.VAR_INT.map(S2CPlaySoundPayload::new, S2CPlaySoundPayload::soundId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2.0.0").optional();

        // Client -> Server: equip / unequip request (validated against server data)
        registrar.playToServer(
                C2SEquipCosmeticPayload.TYPE,
                C2SEquipCosmeticPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().equip(serverPlayer, payload.typeId(), payload.cosmeticId());
                        }
                    });
                }
        );

        // Client -> Server: purchase request (coins deducted server-side)
        registrar.playToServer(
                C2SPurchaseCosmeticPayload.TYPE,
                C2SPurchaseCosmeticPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().purchase(serverPlayer, payload.cosmeticId());
                        }
                    });
                }
        );

        // Client -> Server: full state request (fallback sync)
        registrar.playToServer(
                C2SRequestCosmeticsPayload.TYPE,
                C2SRequestCosmeticsPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().syncToPlayer(serverPlayer);
                        }
                    });
                }
        );

        // Server -> Client: full cosmetics state for the local player
        registrar.playToClient(
                S2CSyncCosmeticsPayload.TYPE,
                S2CSyncCosmeticsPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> clientHandlers.onSyncCosmetics.accept(payload));
                }
        );

        // Server -> Client: equipped cosmetic (one slot) of another player
        registrar.playToClient(
                S2CSyncCosmeticPayload.TYPE,
                S2CSyncCosmeticPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> clientHandlers.onSyncCosmeticSlot.accept(payload));
                }
        );

        // Server -> Client: UI sound for rewards
        registrar.playToClient(
                S2CPlaySoundPayload.TYPE,
                S2CPlaySoundPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> clientHandlers.onPlayUiSound.accept(payload.soundId()));
                }
        );

        registrar.playToClient(
                MarsWeatherPayload.TYPE,
                MarsWeatherPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> clientHandlers.onMarsWeather.accept(payload));
                }
        );
    }

    // --- Client -> Server send helpers ---

    public static void sendEquipCosmetic(String typeId, String cosmeticId) {
        sendToServer(new C2SEquipCosmeticPayload(typeId != null ? typeId : "", cosmeticId != null ? cosmeticId : ""));
    }

    public static void sendPurchaseCosmetic(String cosmeticId) {
        sendToServer(new C2SPurchaseCosmeticPayload(cosmeticId != null ? cosmeticId : ""));
    }

    public static void sendRequestSync() {
        sendToServer(new C2SRequestCosmeticsPayload());
    }

    private static void sendToServer(CustomPacketPayload payload) {
        try {
            if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
                PacketDistributor.sendToServer(payload);
            }
        } catch (Throwable t) {
            AlyrionCore.LOGGER.debug("Cosmetics packet could not be sent: {}", t.getMessage());
        }
    }
}

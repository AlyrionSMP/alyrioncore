package xyz.alyrion.alyrioncore.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CosmeticNetworking {

    // Cache of other players' equipped capes on the client
    private static final Map<UUID, String> CLIENT_CAPE_MAP = new ConcurrentHashMap<>();

    public static String getClientPlayerCape(UUID playerUuid) {
        if (playerUuid == null) return null;
        return CLIENT_CAPE_MAP.get(playerUuid);
    }

    public static void setClientPlayerCape(UUID playerUuid, String capeId) {
        if (playerUuid == null) return;
        if (capeId == null || capeId.isEmpty()) {
            CLIENT_CAPE_MAP.remove(playerUuid);
        } else {
            CLIENT_CAPE_MAP.put(playerUuid, capeId);
        }
    }

    // Packet: Client -> Server: "I equipped cape X"
    public record C2SEquipCapePayload(String capeId) implements CustomPacketPayload {
        public static final Type<C2SEquipCapePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_equip_cape"));

        public static final StreamCodec<ByteBuf, C2SEquipCapePayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(C2SEquipCapePayload::new, C2SEquipCapePayload::capeId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Player UUID has equipped cape X"
    public record S2CSyncCapePayload(UUID playerUuid, String capeId) implements CustomPacketPayload {
        public static final Type<S2CSyncCapePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_sync_cape"));

        public static final StreamCodec<ByteBuf, S2CSyncCapePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                S2CSyncCapePayload::playerUuid,
                ByteBufCodecs.STRING_UTF8,
                S2CSyncCapePayload::capeId,
                S2CSyncCapePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0").optional();

        registrar.playToServer(
                C2SEquipCapePayload.TYPE,
                C2SEquipCapePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            // Broadcast to all players tracking this player
                            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                    serverPlayer,
                                    new S2CSyncCapePayload(serverPlayer.getUUID(), payload.capeId())
                            );
                        }
                    });
                }
        );

        registrar.playToClient(
                S2CSyncCapePayload.TYPE,
                S2CSyncCapePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        setClientPlayerCape(payload.playerUuid(), payload.capeId());
                    });
                }
        );
    }

    public static void sendCapeEquipped(String capeId) {
        try {
            if (Minecraft.getInstance().getConnection() != null) {
                PacketDistributor.sendToServer(new C2SEquipCapePayload(capeId != null ? capeId : ""));
            }
        } catch (Throwable t) {
            AlyrionCore.LOGGER.debug("Multiplayer packet could not be sent (singleplayer or vanilla server): {}", t.getMessage());
        }
    }
}

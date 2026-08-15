package xyz.alyrion.alyrioncore.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticSound;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.ServerCosmeticsManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AlyrionCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CosmeticNetworking {

    // Cache of other players' equipped capes on the client
    private static final Map<UUID, String> CLIENT_CAPE_MAP = new ConcurrentHashMap<>();

    // Cache of other players' equipped pets on the client
    private static final Map<UUID, String> CLIENT_PET_MAP = new ConcurrentHashMap<>();

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

    public static String getClientPlayerPet(UUID playerUuid) {
        if (playerUuid == null) return null;
        return CLIENT_PET_MAP.get(playerUuid);
    }

    public static void setClientPlayerPet(UUID playerUuid, String petId) {
        if (playerUuid == null) return;
        if (petId == null || petId.isEmpty()) {
            CLIENT_PET_MAP.remove(playerUuid);
        } else {
            CLIENT_PET_MAP.put(playerUuid, petId);
        }
    }

    /** Wipe all client-side cosmetics state when leaving a server. */
    public static void clearClientData() {
        CLIENT_CAPE_MAP.clear();
        CLIENT_PET_MAP.clear();
        CosmeticsManager.get().resetForDisconnect();
    }

    // Packet: Client -> Server: "I want to equip/unequip cape X"
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

    // Packet: Client -> Server: "I want to buy cape X"
    public record C2SPurchaseCapePayload(String capeId) implements CustomPacketPayload {
        public static final Type<C2SPurchaseCapePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_purchase_cape"));

        public static final StreamCodec<ByteBuf, C2SPurchaseCapePayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(C2SPurchaseCapePayload::new, C2SPurchaseCapePayload::capeId);

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

    // Packet: Client -> Server: "I want to equip/unequip pet X"
    public record C2SEquipPetPayload(String petId) implements CustomPacketPayload {
        public static final Type<C2SEquipPetPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_equip_pet"));

        public static final StreamCodec<ByteBuf, C2SEquipPetPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(C2SEquipPetPayload::new, C2SEquipPetPayload::petId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Client -> Server: "I want to buy pet X"
    public record C2SPurchasePetPayload(String petId) implements CustomPacketPayload {
        public static final Type<C2SPurchasePetPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "c2s_purchase_pet"));

        public static final StreamCodec<ByteBuf, C2SPurchasePetPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(C2SPurchasePetPayload::new, C2SPurchasePetPayload::petId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Here is your full cosmetics state"
    public record S2CSyncCosmeticsPayload(
            int coins,
            long survivalPlaytimeSeconds,
            Set<String> unlockedCapes,
            String equippedCapeId,
            Set<String> completedTasks,
            PetState petState
    ) implements CustomPacketPayload {
        public static final Type<S2CSyncCosmeticsPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_sync_cosmetics"));

        public record PetState(Set<String> unlockedPets, String equippedPetId) {
            public static final StreamCodec<ByteBuf, PetState> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), PetState::unlockedPets,
                    ByteBufCodecs.STRING_UTF8, PetState::equippedPetId,
                    PetState::new
            );
        }

        public static final StreamCodec<ByteBuf, S2CSyncCosmeticsPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, S2CSyncCosmeticsPayload::coins,
                ByteBufCodecs.VAR_LONG, S2CSyncCosmeticsPayload::survivalPlaytimeSeconds,
                ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), S2CSyncCosmeticsPayload::unlockedCapes,
                ByteBufCodecs.STRING_UTF8, S2CSyncCosmeticsPayload::equippedCapeId,
                ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), S2CSyncCosmeticsPayload::completedTasks,
                PetState.STREAM_CODEC, S2CSyncCosmeticsPayload::petState,
                S2CSyncCosmeticsPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Packet: Server -> Client: "Player UUID has equipped pet X"
    public record S2CSyncPetPayload(UUID playerUuid, String petId) implements CustomPacketPayload {
        public static final Type<S2CSyncPetPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_sync_pet"));

        public static final StreamCodec<ByteBuf, S2CSyncPetPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                S2CSyncPetPayload::playerUuid,
                ByteBufCodecs.STRING_UTF8,
                S2CSyncPetPayload::petId,
                S2CSyncPetPayload::new
        );

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
        final PayloadRegistrar registrar = event.registrar("1.0.0").optional();

        // Client -> Server: equip / unequip request (validated against server data)
        registrar.playToServer(
                C2SEquipCapePayload.TYPE,
                C2SEquipCapePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().equipCape(serverPlayer, payload.capeId());
                        }
                    });
                }
        );

        // Client -> Server: purchase request (coins deducted server-side)
        registrar.playToServer(
                C2SPurchaseCapePayload.TYPE,
                C2SPurchaseCapePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().purchaseCape(serverPlayer, payload.capeId());
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

        // Client -> Server: equip / unequip pet request (validated against server data)
        registrar.playToServer(
                C2SEquipPetPayload.TYPE,
                C2SEquipPetPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().equipPet(serverPlayer, payload.petId());
                        }
                    });
                }
        );

        // Client -> Server: pet purchase request (coins deducted server-side)
        registrar.playToServer(
                C2SPurchasePetPayload.TYPE,
                C2SPurchasePetPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            ServerCosmeticsManager.get().purchasePet(serverPlayer, payload.petId());
                        }
                    });
                }
        );

        // Server -> Client: full cosmetics state for the local player
        registrar.playToClient(
                S2CSyncCosmeticsPayload.TYPE,
                S2CSyncCosmeticsPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        CosmeticsManager.get().applySync(payload);
                    });
                }
        );

        // Server -> Client: equipped cape of another player
        registrar.playToClient(
                S2CSyncCapePayload.TYPE,
                S2CSyncCapePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        setClientPlayerCape(payload.playerUuid(), payload.capeId());
                    });
                }
        );

        // Server -> Client: equipped pet of another player
        registrar.playToClient(
                S2CSyncPetPayload.TYPE,
                S2CSyncPetPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        setClientPlayerPet(payload.playerUuid(), payload.petId());
                    });
                }
        );

        // Server -> Client: UI sound for rewards
        registrar.playToClient(
                S2CPlaySoundPayload.TYPE,
                S2CPlaySoundPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        playUiSound(CosmeticSound.byId(payload.soundId()));
                    });
                }
        );

        registrar.playToClient(
                MarsWeatherPayload.TYPE,
                MarsWeatherPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        xyz.alyrion.alyrioncore.client.weather.MarsClientWeatherHandler.updateFromServer(payload);
                    });
                }
        );
    }

    // --- Client -> Server send helpers ---

    public static void sendCapeEquipped(String capeId) {
        sendToServer(new C2SEquipCapePayload(capeId != null ? capeId : ""));
    }

    public static void sendPurchaseCape(String capeId) {
        sendToServer(new C2SPurchaseCapePayload(capeId != null ? capeId : ""));
    }

    public static void sendPetEquipped(String petId) {
        sendToServer(new C2SEquipPetPayload(petId != null ? petId : ""));
    }

    public static void sendPurchasePet(String petId) {
        sendToServer(new C2SPurchasePetPayload(petId != null ? petId : ""));
    }

    public static void sendRequestSync() {
        sendToServer(new C2SRequestCosmeticsPayload());
    }

    private static void sendToServer(CustomPacketPayload payload) {
        try {
            if (Minecraft.getInstance().getConnection() != null) {
                PacketDistributor.sendToServer(payload);
            }
        } catch (Throwable t) {
            AlyrionCore.LOGGER.debug("Cosmetics packet could not be sent: {}", t.getMessage());
        }
    }

    private static void playUiSound(CosmeticSound sound) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) return;
        switch (sound) {
            case CLICK -> mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            case SUCCESS -> mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F));
            case LEVEL_UP -> mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
            default -> {
            }
        }
    }
}

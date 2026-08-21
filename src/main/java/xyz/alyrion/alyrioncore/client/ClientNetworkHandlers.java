package xyz.alyrion.alyrioncore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticSound;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsManager;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

/**
 * Client-side implementations for the cosmetics & weather payload receivers.
 *
 * Payload registration itself stays in {@link CosmeticNetworking} (it must be
 * bilateral so dedicated servers can send these payloads), but every handler
 * body that touches client-only classes ({@code Minecraft}, the sound manager,
 * the client weather handler) lives here. The common class dispatches through
 * {@code CosmeticNetworking.clientHandlers}, which defaults to no-ops;
 * {@link #init()} swaps in the real implementations and is called from the mod
 * constructor on the client only — long before any payload can arrive.
 */
public final class ClientNetworkHandlers {

    public static void init() {
        CosmeticNetworking.ClientHandlers handlers = new CosmeticNetworking.ClientHandlers();
        handlers.onSyncCosmetics = payload -> CosmeticsManager.get().applySync(payload);
        handlers.onSyncCosmeticSlot = payload ->
                CosmeticNetworking.setClientPlayerCosmetic(payload.playerUuid(), payload.typeId(), payload.cosmeticId());
        handlers.onPlayUiSound = soundId -> playUiSound(CosmeticSound.byId(soundId));
        handlers.onMarsWeather = payload ->
                xyz.alyrion.alyrioncore.client.weather.MarsClientWeatherHandler.updateFromServer(payload);
        CosmeticNetworking.clientHandlers = handlers;
    }

    private ClientNetworkHandlers() {
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

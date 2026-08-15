package xyz.alyrion.alyrioncore.cosmetics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-level (per-server) saved data holding every player's cosmetic & reward
 * progression, keyed by player UUID. Stored inside the world save folder, so a
 * player's coins, unlocked capes, playtime and completed tasks are entirely
 * server-side and travel with the world, not with the client.
 */
public class CosmeticsSavedData extends SavedData {
    public static final String DATA_NAME = "alyrion_cosmetics";
    public static final SavedData.Factory<CosmeticsSavedData> FACTORY =
            new SavedData.Factory<>(CosmeticsSavedData::new, CosmeticsSavedData::load);

    private static final String KEY_PLAYERS = "Players";
    private static final String KEY_UUID = "UUID";
    private static final String KEY_DATA = "Data";

    private final Map<UUID, PlayerCosmeticsData> players = new HashMap<>();

    public static CosmeticsSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static CosmeticsSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld != null ? get(overworld) : null;
    }

    public PlayerCosmeticsData getOrCreate(UUID playerUuid) {
        if (playerUuid == null) return null;
        return players.computeIfAbsent(playerUuid, uuid -> new PlayerCosmeticsData());
    }

    public static CosmeticsSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CosmeticsSavedData data = new CosmeticsSavedData();
        ListTag playersTag = tag.getList(KEY_PLAYERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < playersTag.size(); i++) {
            try {
                CompoundTag entry = playersTag.getCompound(i);
                UUID uuid = UUID.fromString(entry.getString(KEY_UUID));
                CompoundTag playerTag = entry.getCompound(KEY_DATA);
                data.players.put(uuid, PlayerCosmeticsData.load(playerTag));
            } catch (Exception ignored) {
                // Skip malformed entries; sanitized defaults will be created on demand
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag playersTag = new ListTag();
        for (Map.Entry<UUID, PlayerCosmeticsData> entry : players.entrySet()) {
            CompoundTag playerEntry = new CompoundTag();
            playerEntry.putString(KEY_UUID, entry.getKey().toString());
            playerEntry.put(KEY_DATA, entry.getValue().save(new CompoundTag()));
            playersTag.add(playerEntry);
        }
        tag.put(KEY_PLAYERS, playersTag);
        return tag;
    }
}

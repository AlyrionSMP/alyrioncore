package xyz.alyrion.alyrioncore.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Soft-dependency integration with Open Parties and Claims (OPAC).
 *
 * The OPAC server API is reached reflectively so AlyrionCore compiles and runs
 * perfectly fine without OPAC installed: every lookup degrades gracefully
 * when the mod is missing, the API can't be resolved, or the player simply
 * isn't in a large enough party. OPAC's own javadoc lists the entry points used
 * here (OpenPACServerAPI -> IPartyManagerAPI -> IServerPartyAPI, and
 * PlayerConfigManager -> PlayerConfig).
 *
 * All methods must only be called on the server thread.
 */
public final class OpacCompat {
    public static final String OPAC_MOD_ID = "openpartiesandclaims";
    private static final String OPAC_SERVER_API_CLASS = "xaero.pac.common.server.api.OpenPACServerAPI";
    private static final Logger LOGGER = AlyrionCore.LOGGER;

    private static boolean resolved = false;
    private static boolean available = false;
    private static Method apiGet;
    private static Method apiGetPartyManager;
    private static Method managerGetPartyByMember;
    private static Method partyGetMemberCount;
    private static Method apiGetPlayerConfigManager;
    private static Method managerGetLoadedConfig;
    private static Object bonusChunkClaimsSpec;
    private static Method configGetEffective;
    private static Method configTryToSet;
    private static Method configForceSet;
    private static Method configSetDirty;
    private static Method managerGetSynchronizer;
    private static Method syncOptionToClients;

    private OpacCompat() {
    }

    /** True if Open Parties and Claims is loaded on this instance. */
    public static boolean isOpacInstalled() {
        return ModList.get().isLoaded(OPAC_MOD_ID);
    }

    /** True if the player is a member of an OPAC party with at least {@code minMembers} members. */
    public static boolean isPartySizeAtLeast(ServerPlayer player, int minMembers) {
        if (player == null || player.getServer() == null || minMembers < 1) return false;
        resolve(player.getServer());
        if (!available) return false;
        try {
            Object api = apiGet.invoke(null, player.getServer());
            if (api == null) return false;
            Object partyManager = apiGetPartyManager.invoke(api);
            if (partyManager == null) return false;
            Object party = managerGetPartyByMember.invoke(partyManager, player.getUUID());
            if (party == null) return false;
            return (Integer) partyGetMemberCount.invoke(party) >= minMembers;
        } catch (Throwable t) {
            LOGGER.warn("OPAC party lookup failed for {}: {}", player.getGameProfile().getName(), t.toString());
            return false;
        }
    }

    /** Returns current bonus claim chunks granted to the player in OPAC. */
    public static int getBonusClaimChunks(ServerPlayer player) {
        if (player == null || player.getServer() == null) return 0;
        resolve(player.getServer());
        if (!available || configGetEffective == null || bonusChunkClaimsSpec == null) return 0;
        try {
            Object api = apiGet.invoke(null, player.getServer());
            if (api == null) return 0;
            Object configManager = apiGetPlayerConfigManager.invoke(api);
            if (configManager == null) return 0;
            Object config = managerGetLoadedConfig.invoke(configManager, player.getUUID());
            if (config == null) return 0;
            Object res = configGetEffective.invoke(config, bonusChunkClaimsSpec);
            return res instanceof Integer i ? i : 0;
        } catch (Throwable t) {
            LOGGER.warn("Failed to get OPAC bonus claim chunks for {}: {}", player.getGameProfile().getName(), t.toString());
            return 0;
        }
    }

    /** Grants {@code amount} additional bonus claim chunks to the player's OPAC limit. */
    public static boolean addBonusClaimChunks(ServerPlayer player, int amount) {
        if (player == null || player.getServer() == null || amount <= 0) return false;
        resolve(player.getServer());
        if (!available || bonusChunkClaimsSpec == null) return false;
        try {
            Object api = apiGet.invoke(null, player.getServer());
            if (api == null) return false;
            Object configManager = apiGetPlayerConfigManager.invoke(api);
            if (configManager == null) return false;
            Object config = managerGetLoadedConfig.invoke(configManager, player.getUUID());
            if (config == null) return false;

            int current = 0;
            if (configGetEffective != null) {
                Object res = configGetEffective.invoke(config, bonusChunkClaimsSpec);
                if (res instanceof Integer i) {
                    current = i;
                }
            }

            int newTotal = current + amount;
            boolean success = false;
            if (configTryToSet != null) {
                Object setResult = configTryToSet.invoke(config, bonusChunkClaimsSpec, newTotal);
                if (setResult != null && "SUCCESS".equals(setResult.toString())) {
                    success = true;
                }
            }

            if (!success && configForceSet != null) {
                configForceSet.invoke(config, bonusChunkClaimsSpec, newTotal);
                if (configSetDirty != null) {
                    configSetDirty.invoke(config, true);
                }
                if (managerGetSynchronizer != null && syncOptionToClients != null) {
                    Object synchronizer = managerGetSynchronizer.invoke(configManager);
                    if (synchronizer != null) {
                        syncOptionToClients.invoke(synchronizer, config, bonusChunkClaimsSpec);
                    }
                }
                success = true;
            }

            if (success) {
                LOGGER.info("Granted OPAC bonus claim chunks to {}: {} -> {}", player.getGameProfile().getName(), current, newTotal);
            }
            return success;
        } catch (Throwable t) {
            LOGGER.warn("Failed to add OPAC bonus claim chunks for {}: {}", player.getGameProfile().getName(), t.toString());
            return false;
        }
    }

    private static void resolve(MinecraftServer server) {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> apiClass = Class.forName(OPAC_SERVER_API_CLASS);
            apiGet = findGetMethod(apiClass, server);
            if (apiGet == null) return;
            apiGetPartyManager = apiClass.getMethod("getPartyManager");
            Class<?> partyManagerClass = apiGetPartyManager.getReturnType();
            managerGetPartyByMember = partyManagerClass.getMethod("getPartyByMember", UUID.class);
            Class<?> partyClass = managerGetPartyByMember.getReturnType();
            partyGetMemberCount = partyClass.getMethod("getMemberCount");

            try {
                apiGetPlayerConfigManager = apiClass.getMethod("getPlayerConfigManager");
                Class<?> playerConfigManagerClass = apiGetPlayerConfigManager.getReturnType();
                managerGetLoadedConfig = playerConfigManagerClass.getMethod("getLoadedConfig", UUID.class);
                Class<?> optionsClass = Class.forName("xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions");
                bonusChunkClaimsSpec = optionsClass.getField("BONUS_CHUNK_CLAIMS").get(null);
                Class<?> specClass = Class.forName("xaero.pac.common.server.player.config.api.v2.IPlayerConfigOptionSpecAPI");
                Class<?> configClass = Class.forName("xaero.pac.common.server.player.config.api.v2.IPlayerConfigAPI");
                configGetEffective = configClass.getMethod("getEffective", specClass);
                configTryToSet = configClass.getMethod("tryToSet", specClass, Object.class);

                try {
                    Class<?> playerConfigImplClass = Class.forName("xaero.pac.common.server.player.config.PlayerConfig");
                    configForceSet = playerConfigImplClass.getMethod("forceSet", specClass, Object.class);
                    configSetDirty = playerConfigImplClass.getMethod("setDirty", boolean.class);
                    managerGetSynchronizer = playerConfigManagerClass.getMethod("getSynchronizer");
                    Class<?> syncClass = managerGetSynchronizer.getReturnType();
                    syncOptionToClients = syncClass.getMethod("syncOptionToClients", playerConfigImplClass, Class.forName("xaero.pac.common.server.player.config.PlayerConfigOptionSpec"));
                } catch (Throwable ignored) {
                }
            } catch (Throwable t) {
                LOGGER.warn("OPAC player config reflection setup failed: {}", t.toString());
            }

            available = true;
            LOGGER.info("Open Parties and Claims integration enabled.");
        } catch (Throwable t) {
            LOGGER.info("Open Parties and Claims integration unavailable ({}).", t.toString());
            available = false;
        }
    }

    private static Method findGetMethod(Class<?> apiClass, MinecraftServer server) {
        try {
            return apiClass.getMethod("get", MinecraftServer.class);
        } catch (NoSuchMethodException e) {
            try {
                return apiClass.getMethod("get");
            } catch (NoSuchMethodException e2) {
                return null;
            }
        }
    }
}
package xyz.alyrion.alyrioncore.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Soft-dependency integration with Open Parties and Claims (OPAC).
 *
 * The OPAC server API is reached reflectively so AlyrionCore compiles and runs
 * perfectly fine without OPAC installed: every lookup degrades to {@code false}
 * when the mod is missing, the API can't be resolved, or the player simply
 * isn't in a large enough party. OPAC's own javadoc lists the entry points used
 * here (OpenPACServerAPI -> IPartyManagerAPI -> IServerPartyAPI).
 *
 * All methods must only be called on the server thread.
 */
public final class OpacCompat {
    private static final String OPAC_SERVER_API_CLASS = "xaero.pac.common.server.api.OpenPACServerAPI";
    private static final Logger LOGGER = AlyrionCore.LOGGER;

    private static boolean resolved = false;
    private static boolean available = false;
    private static Method apiGet;
    private static Method apiGetPartyManager;
    private static Method managerGetPartyByMember;
    private static Method partyGetMemberCount;

    private OpacCompat() {
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
            Object party = managerGetPartyByMember.invoke(partyManager, (UUID) player.getUUID());
            if (party == null) return false;
            return (int) partyGetMemberCount.invoke(party) >= minMembers;
        } catch (Throwable t) {
            LOGGER.warn("OPAC party lookup failed for {}: {}", player.getGameProfile().getName(), t.toString());
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
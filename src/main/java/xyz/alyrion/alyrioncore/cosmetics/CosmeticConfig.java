package xyz.alyrion.alyrioncore.cosmetics;

public class CosmeticConfig {
    /**
     * Number of seconds in survival mode required to earn 1 coin.
     * Default: 3600 seconds (1 hour).
     *
     * This is enforced by the server ({@link ServerCosmeticsManager#tickPlaytime}),
     * not by the client.
     */
    public static final int PLAYTIME_SECONDS_PER_COIN = 3600;

    /**
     * How often (in seconds of tracked playtime) the running total is pushed to the
     * client while a player is online. The store's playtime card and its "next coin"
     * bar read the client's mirror, so without a regular push they would keep showing
     * the value from login until the next coin is earned — a whole hour of a frozen
     * bar. 5 seconds keeps it live at negligible traffic.
     */
    public static final int PLAYTIME_SYNC_INTERVAL_SECONDS = 5;
}

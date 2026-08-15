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
}

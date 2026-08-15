package xyz.alyrion.alyrioncore.cosmetics;

public class CosmeticConfig {
    /**
     * Dev Mode flag.
     * When TRUE, displays the Dev Tools panel in the Cosmetic Store GUI,
     * allowing instant task triggers and progression resets on any world/server.
     */
    public static boolean DEV_MODE = true;

    /**
     * Number of seconds in survival mode required to earn 1 coin.
     * Default: 3600 seconds (1 hour).
     */
    public static final int PLAYTIME_SECONDS_PER_COIN = 3600;
}

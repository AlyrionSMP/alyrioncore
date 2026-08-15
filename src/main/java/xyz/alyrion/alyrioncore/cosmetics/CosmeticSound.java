package xyz.alyrion.alyrioncore.cosmetics;

/**
 * Sound effects the server can ask the client to play, piggybacked on reward
 * notifications. The server is authoritative for *when* rewards happen, so it
 * sends a small sound request over the network and the client plays the sound.
 */
public enum CosmeticSound {
    NONE(0),
    CLICK(1),
    SUCCESS(2),
    LEVEL_UP(3);

    private final int id;

    CosmeticSound(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CosmeticSound byId(int id) {
        for (CosmeticSound sound : values()) {
            if (sound.id == id) return sound;
        }
        return NONE;
    }
}

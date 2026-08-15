package xyz.alyrion.alyrioncore.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLPaths;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.network.CosmeticNetworking;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class CosmeticsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final CosmeticsManager INSTANCE = new CosmeticsManager();

    private CosmeticsData data = new CosmeticsData();
    private File saveFile;
    private int tickCounter = 0;

    private CosmeticsManager() {
        initFile();
        load();
    }

    public static CosmeticsManager get() {
        return INSTANCE;
    }

    private void initFile() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            this.saveFile = configPath.resolve("alyrion_cosmetics.json").toFile();
        } catch (Exception e) {
            this.saveFile = new File("alyrion_cosmetics.json");
        }
    }

    public synchronized void load() {
        if (saveFile != null && saveFile.exists()) {
            try (FileReader reader = new FileReader(saveFile)) {
                CosmeticsData loaded = GSON.fromJson(reader, CosmeticsData.class);
                if (loaded != null) {
                    this.data = loaded;
                    this.data.sanitize();
                }
            } catch (Exception e) {
                AlyrionCore.LOGGER.error("Failed to load alyrion_cosmetics.json", e);
            }
        } else {
            this.data = new CosmeticsData();
            save();
        }
    }

    public synchronized void save() {
        if (saveFile != null) {
            try {
                File parent = saveFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                try (FileWriter writer = new FileWriter(saveFile)) {
                    GSON.toJson(this.data, writer);
                }
            } catch (Exception e) {
                AlyrionCore.LOGGER.error("Failed to save alyrion_cosmetics.json", e);
            }
        }
    }

    public CosmeticsData getData() {
        return data;
    }

    public int getCoins() {
        return data.getCoins();
    }

    public long getPlaytimeSeconds() {
        return data.getSurvivalPlaytimeSeconds();
    }

    public boolean isCapeUnlocked(CapeDefinition cape) {
        if (cape == null) return false;
        return data.isCapeUnlocked(cape.getId());
    }

    public boolean isCapeEquipped(CapeDefinition cape) {
        if (cape == null) return false;
        return cape.getId().equalsIgnoreCase(data.getEquippedCapeId());
    }

    public CapeDefinition getEquippedCape() {
        return CapeDefinition.fromId(data.getEquippedCapeId());
    }

    public boolean purchaseCape(CapeDefinition cape) {
        if (cape == null) return false;
        if (isCapeUnlocked(cape)) {
            equipCape(cape);
            return true;
        }

        if (data.getCoins() >= cape.getPrice()) {
            data.setCoins(data.getCoins() - cape.getPrice());
            data.unlockCape(cape.getId());
            data.setEquippedCapeId(cape.getId());
            save();
            sendEquipPacket(cape.getId());
            playSuccessSound();
            return true;
        }
        return false;
    }

    public void equipCape(CapeDefinition cape) {
        if (cape != null && isCapeUnlocked(cape)) {
            data.setEquippedCapeId(cape.getId());
            save();
            sendEquipPacket(cape.getId());
            playClickSound();
        }
    }

    public void unequipCape() {
        data.setEquippedCapeId(null);
        save();
        sendEquipPacket("");
        playClickSound();
    }

    public void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        tickCounter++;

        // Track survival playtime every 20 ticks (1 second)
        if (tickCounter % 20 == 0) {
            Player player = mc.player;
            // Only count survival/adventure mode (not creative or spectator)
            if (!player.isCreative() && !player.isSpectator()) {
                long prev = data.getSurvivalPlaytimeSeconds();
                data.incrementSurvivalPlaytime();
                long current = data.getSurvivalPlaytimeSeconds();

                // Check 1 hour threshold (3600 seconds)
                if (current > 0 && current % CosmeticConfig.PLAYTIME_SECONDS_PER_COIN == 0) {
                    data.addCoins(1);
                    save();
                    notifyReward(
                            Component.literal("§6§l[Alyrion SMP] §e+1 Coin §fearned for 1 hour of survival playtime! (Total: §6" + data.getCoins() + " Coins§f)")
                    );
                    playLevelUpSound();
                } else if (current % 60 == 0) {
                    // Periodic save every minute
                    save();
                }
            }
        }

        // Check natural task triggers every 5 ticks (instant responsiveness)
        if (tickCounter % 5 == 0) {
            checkTasks(mc.player);
        }
    }

    public void checkTasks(Player player) {
        if (player == null) return;
        for (TaskDefinition task : TaskDefinition.values()) {
            boolean taskDone = data.isTaskCompleted(task.getId());
            boolean capeUnlocked = task.getCapeReward() == null || data.isCapeUnlocked(task.getCapeReward().getId());

            if (!taskDone || !capeUnlocked) {
                if (task.test(player)) {
                    completeTask(task, false);
                }
            }
        }
    }

    public void completeTask(TaskDefinition task, boolean isManualDev) {
        if (task == null) return;
        boolean alreadyDone = data.isTaskCompleted(task.getId());

        if (!alreadyDone || isManualDev) {
            data.completeTask(task.getId());
            if (!alreadyDone || isManualDev) {
                data.addCoins(task.getCoinReward());
            }
            if (task.getCapeReward() != null) {
                data.unlockCape(task.getCapeReward().getId());
            }
            save();

            String prefix = isManualDev ? "§d§l[DEV TASK COMPLETED] §f" : "§6§l[TASK COMPLETED] §f";
            String capeNotice = task.getCapeReward() != null ? " + §bUnlocked " + task.getCapeReward().getDisplayName() + "!" : "";
            notifyReward(
                    Component.literal(prefix + "§a" + task.getTitle() + " §7(§6+" + task.getCoinReward() + " Coins" + capeNotice + "§7)")
            );
            playLevelUpSound();
        }
    }

    // --- Dev Mode Actions ---

    public void devAddCoins(int amount) {
        data.addCoins(amount);
        save();
        notifyReward(Component.literal("§d[DEV] §aAdded " + amount + " Coins. (Total: " + data.getCoins() + ")"));
        playClickSound();
    }

    public void devAddPlaytime(long seconds) {
        long prev = data.getSurvivalPlaytimeSeconds();
        data.setSurvivalPlaytimeSeconds(prev + seconds);
        // Award coins for hours passed
        int coinsEarned = (int) ((data.getSurvivalPlaytimeSeconds() / CosmeticConfig.PLAYTIME_SECONDS_PER_COIN)
                - (prev / CosmeticConfig.PLAYTIME_SECONDS_PER_COIN));
        if (coinsEarned > 0) {
            data.addCoins(coinsEarned);
        }
        save();
        notifyReward(Component.literal("§d[DEV] §aAdded " + (seconds / 60) + " minutes of survival playtime. (+" + coinsEarned + " coins)"));
        playLevelUpSound();
    }

    public void devResetAllTasks() {
        data.resetAllTasks();
        save();
        notifyReward(Component.literal("§d[DEV] §cAll task progression has been reset."));
        playClickSound();
    }

    public void devResetCosmetics() {
        data.resetCosmetics();
        save();
        sendEquipPacket(data.getEquippedCapeId() != null ? data.getEquippedCapeId() : "");
        notifyReward(Component.literal("§d[DEV] §cAll cosmetic unlocks have been reset."));
        playClickSound();
    }

    private void sendEquipPacket(String capeId) {
        try {
            CosmeticNetworking.sendCapeEquipped(capeId);
        } catch (Throwable ignored) {
        }
    }

    private void notifyReward(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(message, false);
        }
    }

    private void playLevelUpSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.2F));
        }
    }

    private void playSuccessSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F));
        }
    }

    private void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}

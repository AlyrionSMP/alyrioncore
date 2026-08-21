package xyz.alyrion.alyrioncore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticsRegistry;
import xyz.alyrion.alyrioncore.cosmetics.PlayerCosmeticsData;
import xyz.alyrion.alyrioncore.cosmetics.ServerCosmeticsManager;
import xyz.alyrion.alyrioncore.cosmetics.TaskDefinition;

import java.util.Arrays;

/**
 * Server-side admin commands for the cosmetics & rewards economy.
 *
 * These replace the old client-side "Dev Controls" tab: all economy overrides
 * are ops-only (permission level 2) and mutate the server-authoritative saved
 * data directly.
 */
@EventBusSubscriber(modid = AlyrionCore.MODID)
public class CosmeticsCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("alyrioncosmetics")
                        // Any player can check their own balance / progress
                        .then(Commands.literal("coins")
                                .executes(ctx -> showSelf(ctx.getSource()))
                        )
                        // --- Ops-only dev overrides ---
                        .then(Commands.literal("addcoins")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                    ServerCosmeticsManager.get().devAddCoins(target, amount);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§d[DEV] §aAdded §6" + amount + " Coins §ato " + target.getName().getString() + "."), true);
                                                    return 1;
                                                })))
                        )
                        .then(Commands.literal("addplaytime")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("seconds", LongArgumentType.longArg(1))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    long seconds = LongArgumentType.getLong(ctx, "seconds");
                                                    ServerCosmeticsManager.get().devAddPlaytime(target, seconds);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§d[DEV] §aAdded §e" + (seconds / 60) + " minutes §aof playtime to " + target.getName().getString() + "."), true);
                                                    return 1;
                                                })))
                        )
                        .then(Commands.literal("completetask")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("task", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(TaskDefinition.values()).map(TaskDefinition::getId),
                                                        builder
                                                ))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    TaskDefinition task = TaskDefinition.fromId(StringArgumentType.getString(ctx, "task"));
                                                    if (task == null) {
                                                        ctx.getSource().sendFailure(Component.literal("Unknown task id. Valid ids: "
                                                                + Arrays.stream(TaskDefinition.values()).map(TaskDefinition::getId).reduce((a, b) -> a + ", " + b).orElse("")));
                                                        return 0;
                                                    }
                                                    ServerCosmeticsManager.get().completeTask(target, task, true);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§d[DEV] §aCompleted task §e" + task.getTitle() + " §afor " + target.getName().getString() + "."), true);
                                                    return 1;
                                                })))
                        )
                        .then(Commands.literal("unlock")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("cosmetic", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        CosmeticsRegistry.all().stream().map(def -> def.getId()),
                                                        builder
                                                ))
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String cosmeticId = StringArgumentType.getString(ctx, "cosmetic");
                                                    if (CosmeticsRegistry.fromId(cosmeticId) == null) {
                                                        ctx.getSource().sendFailure(Component.literal("Unknown cosmetic id. Valid ids: "
                                                                + CosmeticsRegistry.all().stream().map(def -> def.getId())
                                                                .reduce((a, b) -> a + ", " + b).orElse("")));
                                                        return 0;
                                                    }
                                                    ServerCosmeticsManager.get().devUnlock(target, cosmeticId);
                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "§d[DEV] §aUnlocked §e" + cosmeticId + " §afor " + target.getName().getString() + "."), true);
                                                    return 1;
                                                })))
                        )
                        .then(Commands.literal("resettasks")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            ServerCosmeticsManager.get().devResetAllTasks(target);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§d[DEV] §cReset all task progression for " + target.getName().getString() + "."), true);
                                            return 1;
                                        }))
                        )
                        .then(Commands.literal("resetcosmetics")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            ServerCosmeticsManager.get().devResetCosmetics(target);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    "§d[DEV] §cReset all cosmetic unlocks for " + target.getName().getString() + "."), true);
                                            return 1;
                                        }))
                        )
        );
    }

    private static int showSelf(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerCosmeticsData data = ServerCosmeticsManager.get().getPlayerData(player);
        long seconds = data.getSurvivalPlaytimeSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        source.sendSuccess(() -> Component.literal(
                String.format("§6[Alyrion] §eCoins: §6%d §7| Survival Playtime: §e%dh %02dm %02ds §7| Unlocked Cosmetics: §b%d §7| Equipped Slots: §b%d",
                        data.getCoins(), hours, minutes, secs,
                        data.getUnlockedCosmetics().size(), data.getEquippedSlotCount())), false);
        return 1;
    }
}

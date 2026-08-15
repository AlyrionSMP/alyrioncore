package xyz.alyrion.alyrioncore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.alyrion.alyrioncore.AlyrionCore;
import xyz.alyrion.alyrioncore.world.ModDimensions;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherSavedData;
import xyz.alyrion.alyrioncore.world.weather.MarsWeatherState;

import java.util.Arrays;

@EventBusSubscriber(modid = AlyrionCore.MODID)
public class MarsWeatherCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("marsweather")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("query")
                    .executes(ctx -> queryWeather(ctx.getSource()))
                )
                .then(Commands.literal("set")
                    .then(Commands.argument("state", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(MarsWeatherState.values()).map(MarsWeatherState::getSerializedName),
                                builder
                        ))
                        .executes(ctx -> setWeather(ctx.getSource(), StringArgumentType.getString(ctx, "state"), 24000))
                        .then(Commands.argument("duration", IntegerArgumentType.integer(100, 2000000))
                            .executes(ctx -> setWeather(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "state"),
                                    IntegerArgumentType.getInteger(ctx, "duration")
                            ))
                        )
                    )
                )
        );
    }

    private static int queryWeather(CommandSourceStack source) {
        ServerLevel marsLevel = source.getServer().getLevel(ModDimensions.MARS_LEVEL);
        if (marsLevel == null) {
            source.sendFailure(Component.literal("Mars dimension is not loaded."));
            return 0;
        }

        MarsWeatherSavedData data = MarsWeatherSavedData.get(marsLevel);
        int sol = data.getSeasonSol(marsLevel);
        boolean perihelion = (sol >= 420 && sol <= 580);
        String seasonStr = perihelion ? "Perihelion / Southern Summer (Storm Season)" : "Aphelion / Northern Summer";

        source.sendSuccess(() -> Component.literal(String.format(
                "§6[Mars Weather]§r State: §e%s§r | Intensity: §a%.2f§r | Sol: §b%d/668§r (%s) | Duration: §e%d ticks§r",
                data.getCurrentState().getDisplayName(),
                data.getCurrentIntensity(),
                sol,
                seasonStr,
                data.getStateDuration()
        )), false);
        return 1;
    }

    private static int setWeather(CommandSourceStack source, String stateName, int duration) {
        ServerLevel marsLevel = source.getServer().getLevel(ModDimensions.MARS_LEVEL);
        if (marsLevel == null) {
            source.sendFailure(Component.literal("Mars dimension is not loaded."));
            return 0;
        }

        MarsWeatherState state = MarsWeatherState.byName(stateName);
        MarsWeatherSavedData data = MarsWeatherSavedData.get(marsLevel);
        data.setWeather(state, duration);
        data.broadcastWeather(marsLevel);

        source.sendSuccess(() -> Component.literal(String.format(
                "§6[Mars Weather]§r Set weather state to §e%s§r for §b%d ticks§r.",
                state.getDisplayName(), duration
        )), true);
        return 1;
    }
}

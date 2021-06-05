package waidesoper.vaultaddons.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import waidesoper.vaultaddons.VaultAddons;
import waidesoper.vaultaddons.commands.*;
import net.minecraft.command.Commands;
import static net.minecraft.command.Commands.literal;
import java.util.function.Supplier;

public class ModCommands{
    public static addonsCommand ADDONS;

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env){
        ADDONS = registerCommand(addonsCommand::new, dispatcher, env);
    }

    public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env) {
        T command = supplier.get();

        if (!command.isDedicatedServerOnly() || env == Commands.EnvironmentType.DEDICATED || env == Commands.EnvironmentType.ALL) {
            LiteralArgumentBuilder<CommandSource> builder = literal(command.getName());
            builder.requires((sender) -> sender.hasPermissionLevel(command.getRequiredPermissionLevel()));
            command.build(builder);
            dispatcher.register(literal(VaultAddons.MOD_ID).then(builder));
        }

        return command;
    }

}

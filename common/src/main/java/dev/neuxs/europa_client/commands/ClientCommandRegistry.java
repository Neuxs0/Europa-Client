package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.commands.misc.HelpCommand;
import dev.neuxs.europa_client.commands.misc.SayCommand;
import dev.neuxs.europa_client.commands.modules.NoClipCommand;
import dev.neuxs.europa_client.commands.modules.SetNoClipSpeedCommand;
import dev.neuxs.europa_client.commands.utils.DisconnectCommand;
import dev.neuxs.europa_client.commands.utils.FullbrightCommand;
import dev.neuxs.europa_client.commands.utils.PlayerListCommand;

public class ClientCommandRegistry {
    public static void registerClientCommands() {
        // Misc
        ClientCommandManager.registerCommand("say", SayCommand::new);
        ClientCommandManager.registerCommand("help", HelpCommand::new, "?", "h");

        // Utils
        ClientCommandManager.registerCommand("disconnect", DisconnectCommand::new, "dc", "quit", "exit");
        ClientCommandManager.registerCommand("fullbright", FullbrightCommand::new, "fb");
        ClientCommandManager.registerCommand("playerList", PlayerListCommand::new, "pl");

        // Modules
        ClientCommandManager.registerCommand("noclip", NoClipCommand::new, "nc");
        ClientCommandManager.registerCommand("setNoClipSpeed", SetNoClipSpeedCommand::new, "sncs", "setncspeed");
    }
}

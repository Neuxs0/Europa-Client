package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.commands.misc.TypeCommand;
import dev.neuxs.europa_client.commands.misc.HelpCommand;
import dev.neuxs.europa_client.commands.misc.SayCommand;
import dev.neuxs.europa_client.commands.misc.VersionCommand;
import dev.neuxs.europa_client.commands.utils.DisconnectCommand;
import dev.neuxs.europa_client.commands.modules.FullbrightCommand;
import dev.neuxs.europa_client.commands.utils.PlayerListCommand;
import dev.neuxs.europa_client.commands.utils.QuitGameCommand;

public class ClientCommandRegistry {
    public static void registerClientCommands() {
        // Misc
        ClientCommandManager.registerCommand("say", SayCommand::new);
        ClientCommandManager.registerCommand("help", HelpCommand::new, "?", "h");
        ClientCommandManager.registerCommand("type", TypeCommand::new, "clientType");
        ClientCommandManager.registerCommand("version", VersionCommand::new, "clientVersion");

        // Utils
        ClientCommandManager.registerCommand("disconnect", DisconnectCommand::new, "dc", "quit", "exit");
        ClientCommandManager.registerCommand("quitGame", QuitGameCommand::new, "gameQuit", "closeGame", "exitGame");
        ClientCommandManager.registerCommand("playerList", PlayerListCommand::new, "pl");

        // Modules
        ClientCommandManager.registerCommand("fullbright", FullbrightCommand::new, "fb");
    }
}

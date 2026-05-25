package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.misc.HelpCommand;
import dev.neuxs.europa_client.commands.misc.ProfileCommand;
import dev.neuxs.europa_client.commands.misc.SayCommand;
import dev.neuxs.europa_client.commands.misc.TypeCommand;
import dev.neuxs.europa_client.commands.misc.VersionCommand;
import dev.neuxs.europa_client.commands.modules.utils.NoFogCommand;
import dev.neuxs.europa_client.commands.utils.DisconnectCommand;
import dev.neuxs.europa_client.commands.modules.utils.FullbrightCommand;
import dev.neuxs.europa_client.commands.modules.utils.FreecamCommand;
import dev.neuxs.europa_client.commands.modules.utils.PacketInspectorCommand;
import dev.neuxs.europa_client.commands.utils.PlayerListCommand;
import dev.neuxs.europa_client.commands.utils.QuitGameCommand;

public class ClientCommandRegistry {
    private static boolean registered;

    public static void registerClientCommands() {
        if (registered) {
            return;
        }
        registered = true;

        // Misc
        ClientCommandManager.registerCommand("say", SayCommand::new);
        ClientCommandManager.registerCommand("help", HelpCommand::new, "?", "h");
        ClientCommandManager.registerCommand("type", TypeCommand::new, "clientType");
        ClientCommandManager.registerCommand("version", VersionCommand::new, "clientVersion");
        ClientCommandManager.registerCommand("profile", ProfileCommand::new, "profiles");

        // Utils
        ClientCommandManager.registerCommand("disconnect", DisconnectCommand::new, "dc", "quit", "exit");
        ClientCommandManager.registerCommand("quitGame", QuitGameCommand::new, "gameQuit", "closeGame", "exitGame");
        ClientCommandManager.registerCommand("playerList", PlayerListCommand::new, "pl");

        // Modules - Utils
        ClientCommandManager.registerCommand("fullbright", FullbrightCommand::new, "fb");
        ClientCommandManager.registerCommand("nofog", NoFogCommand::new, "nf");
        ClientCommandManager.registerCommand("packetInspector", PacketInspectorCommand::new);
        ClientCommandManager.registerCommand("freecam", FreecamCommand::new, "fc");

        Client.getVariant().registerCommands();
    }
}

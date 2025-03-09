package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.commands.misc.HelpCommand;
import dev.neuxs.europa_client.commands.misc.SayCommand;
import dev.neuxs.europa_client.commands.modules.NoClipCommand;
import dev.neuxs.europa_client.commands.modules.SetNoClipSpeedCommand;

public class ClientCommandRegistry {
    public static void registerClientCommands() {
        // Misc
        ClientCommandManager.registerCommand("say", SayCommand::new);
        ClientCommandManager.registerCommand("help", HelpCommand::new, "?", "h");

        // Modules
        ClientCommandManager.registerCommand("noclip", NoClipCommand::new, "nc");
        ClientCommandManager.registerCommand("setNoClipSpeed", SetNoClipSpeedCommand::new, "sncs", "setncspeed");
    }
}

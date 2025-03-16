package dev.neuxs.europa_client.commands.misc;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.commands.ClientCommandManager;

public class HelpCommand extends ClientCommand {

    @Override
    public void run() {
        ClientCommandManager.printHelp(Client.clientChat);
    }

    @Override
    public String getDescription() {
        return "Displays help information for all Europa Client commands.";
    }
}

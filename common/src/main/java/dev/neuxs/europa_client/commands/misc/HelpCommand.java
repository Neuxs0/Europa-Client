package dev.neuxs.europa_client.commands.misc;

import finalforeach.cosmicreach.chat.IChat;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.commands.ClientCommandManager;

public class HelpCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        ClientCommandManager.printHelp(chat);
    }

    @Override
    public String getDescription() {
        return "Displays help information for all Europa Client commands.";
    }
}

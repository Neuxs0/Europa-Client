package dev.neuxs.europa_client.commands.misc;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.utils.Chat;
import dev.neuxs.europa_client.commands.ClientCommand;

public class TypeCommand extends ClientCommand {

    @Override
    public void run() {
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + "You are running the no-cheats Client.");
    }

    @Override
    public String getDescription() {
        return "Tells you what type of Europa Client you are running.";
    }
}
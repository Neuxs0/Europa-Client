package dev.neuxs.europa_client.commands.misc;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.utils.Chat;

public class VersionCommand extends ClientCommand {

    @Override
    public void run() {
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + "You are running Europa Client v" + Client.VERSION);
    }

    @Override
    public String getDescription() {
        return "Tells you what version of Europa Client you are running.";
    }
}
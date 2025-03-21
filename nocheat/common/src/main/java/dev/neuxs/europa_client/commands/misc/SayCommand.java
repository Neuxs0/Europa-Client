package dev.neuxs.europa_client.commands.misc;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;

public class SayCommand extends ClientCommand {

    @Override
    public void run() {
        if (args.length < 2 || args[1].trim().isEmpty()) {
            Client.clientChat.addMessage(null, "Usage: #say <message>");
            return;
        }

        String message = args[1].trim();
        Client.clientChat.addMessage(account, message);
    }

    @Override
    public String getDescription() {
        return "Sends a public chat message.";
    }
}
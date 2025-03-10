package dev.neuxs.europa_client.commands.misc;

import finalforeach.cosmicreach.chat.IChat;
import dev.neuxs.europa_client.commands.ClientCommand;

public class SayCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        if (args.length < 2 || args[1].trim().isEmpty()) {
            chat.addMessage(null, "Usage: #say <message>");
            return;
        }

        String message = args[1].trim();
        chat.addMessage(account, message);
    }

    @Override
    public String getDescription() {
        return "Sends a public chat message.";
    }
}
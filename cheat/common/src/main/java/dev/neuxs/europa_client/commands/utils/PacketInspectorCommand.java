package dev.neuxs.europa_client.commands.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.Chat;

public class PacketInspectorCommand extends ClientCommand {
    @Override
    public void run() {
        Modules.packetInspectorEnabled = !Modules.packetInspectorEnabled;
        if (Modules.packetInspectorEnabled) Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Enabled packet inspector.");
        else Client.clientChat.addMessage(null, Chat.getClientPrefix() + "Disabled packet inspector.");
    }

    @Override
    public String getDescription() {
        return "Outputs all sent and received packets to the console.";
    }
}

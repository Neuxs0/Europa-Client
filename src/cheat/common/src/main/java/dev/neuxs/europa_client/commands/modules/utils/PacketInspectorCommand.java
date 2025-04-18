package dev.neuxs.europa_client.commands.modules.utils;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;

public class PacketInspectorCommand extends ClientCommand {
    @Override
    public void run() {
        Modules.packetInspector.toggle(true);
    }

    @Override
    public String getDescription() {
        return "Outputs all sent and received packets to the console.";
    }
}

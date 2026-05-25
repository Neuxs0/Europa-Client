package dev.neuxs.europa_client.commands.modules.cheats;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;

public class XrayCommand extends ClientCommand {
    @Override
    public void run() {
        Modules.xray.toggle(true);
    }

    @Override
    public String getDescription() {
        return "Toggles xray.";
    }
}

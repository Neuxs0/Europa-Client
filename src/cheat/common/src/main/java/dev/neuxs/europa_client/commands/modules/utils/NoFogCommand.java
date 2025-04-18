package dev.neuxs.europa_client.commands.modules.utils;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;

public class NoFogCommand extends ClientCommand {
    @Override
    public void run() {
        Modules.noFog.toggle(true);
    }

    @Override
    public String getDescription() {
        return "Toggles no-fog.";
    }
}

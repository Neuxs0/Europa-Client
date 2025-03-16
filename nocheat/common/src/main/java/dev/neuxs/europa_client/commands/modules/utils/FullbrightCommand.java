package dev.neuxs.europa_client.commands.modules.utils;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.Modules;
import finalforeach.cosmicreach.gamestates.InGame;

public class FullbrightCommand extends ClientCommand {
    @Override
    public void run() {
        Modules.fullbright.toggle(InGame.getWorld(), true);
    }

    @Override
    public String getDescription() {
        return "Toggles fullbright.";
    }
}

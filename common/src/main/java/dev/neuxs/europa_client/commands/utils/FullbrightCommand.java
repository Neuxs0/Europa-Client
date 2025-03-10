package dev.neuxs.europa_client.commands.utils;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.utils.Fullbright;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.gamestates.InGame;

public class FullbrightCommand extends ClientCommand {
    @Override
    public void run(IChat chat) {
        Fullbright.toggle(InGame.getWorld(), true);
    }

    @Override
    public String getDescription() {
        return "Toggles fullbright and forces immediate chunk regeneration.";
    }
}

package dev.neuxs.europa_client.commands.modules;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import finalforeach.cosmicreach.chat.IChat;

public class NoClipCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        NoClip.toggle(false);
    }

    @Override
    public String getDescription() {
        return "Toggles the no-clip cheat. (Works on every server)";
    }
}


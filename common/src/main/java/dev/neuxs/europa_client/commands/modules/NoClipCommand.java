package dev.neuxs.europa_client.commands.modules;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import finalforeach.cosmicreach.chat.IChat;

import static dev.neuxs.europa_client.modules.cheats.NoClip.toggleNoClip;
import static dev.neuxs.europa_client.utils.ChatFormater.getClientPrefix;

public class NoClipCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        toggleNoClip();
        if (NoClip.enabled) chat.addMessage(null, getClientPrefix() + "No-clip Enabled");
        else chat.addMessage(null, getClientPrefix() + "No-clip Disabled");
    }

    @Override
    public String getDescription() {
        return "Toggles the no-clip cheat. (Works on every server)";
    }
}


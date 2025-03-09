package dev.neuxs.europa_client.commands.modules;

import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.modules.cheats.NoClip;
import finalforeach.cosmicreach.chat.IChat;

import static dev.neuxs.europa_client.utils.ChatFormater.getClientPrefix;

public class SetNoClipSpeedCommand extends ClientCommand {

    @Override
    public void run(IChat chat) {
        // Expect the speed value after the command (args[0] is the command name).
        if (args.length < 2 || args[1].trim().isEmpty()) {
            chat.addMessage(null, "Usage: #setNoClipSpeed <speed>");
            return;
        }

        try {
            float speedValue = Float.parseFloat(args[1].trim());
            if (Float.isNaN(speedValue) || Float.isInfinite(speedValue)) {
                chat.addMessage(null, getClientPrefix() + "Invalid speed value; please provide a finite number.");
                return;
            }
            // Clamp and set the speed value
            NoClip.setSpeed(speedValue);
            chat.addMessage(null, getClientPrefix() + "No-clip speed set to " + NoClip.getSpeed());
        } catch (NumberFormatException ex) {
            chat.addMessage(null, getClientPrefix() + "Invalid number format. Use a valid float value.");
        }
    }

    @Override
    public String getDescription() {
        return "Sets the speed for no-clip. 1.0 is default. Acceptable range: 0.1 to 10.0.";
    }
}

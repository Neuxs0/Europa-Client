package dev.neuxs.europa_client.commands.utils;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.mixins.GameSingletonsInterface;
import dev.neuxs.europa_client.utils.Chat;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.entities.player.Player;
import java.util.WeakHashMap;

public class PlayerListCommand extends ClientCommand {

    @Override
    public void run() {
        // Use the mixin accessor to get the mapping of players to accounts.
        WeakHashMap<Player, Account> playersToAccounts =
                GameSingletonsInterface.getPlayersToAccounts();

        if (playersToAccounts == null || playersToAccounts.isEmpty()) {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "No players online.");
            return;
        }

        // Build a comma-separated list of player display names.
        StringBuilder playerList = new StringBuilder();
        int count = 0;
        for (Account account : playersToAccounts.values()) {
            if (count > 0) {
                playerList.append(", ");
            }
            playerList.append(account.getDisplayName());
            count++;
        }

        // If only one player is online, let them know they're alone!
        if (count == 1) {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "You are the only player online!");
        } else {
            Client.clientChat.addMessage(null,
                    Chat.getClientPrefix() + "Online players: " + playerList);
        }
    }

    @Override
    public String getDescription() {
        return "Shows all the online players on the server.";
    }
}

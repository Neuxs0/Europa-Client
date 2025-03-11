package dev.neuxs.europa_client.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.neuxs.europa_client.Client;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.accounts.Account;

public class ClientCommandManager {

    private static final Map<String, Supplier<ClientCommand>> COMMANDS =
            new HashMap<>();
    private static final Map<String, Supplier<ClientCommand>> ALIASES =
            new HashMap<>();

    public static void registerCommand(
            String name,
            Supplier<ClientCommand> supplier,
            String... aliases
    ) {
        name = name.toLowerCase();
        if (COMMANDS.containsKey(name)) {
            System.err.println("ClientCommand `" + name + "` already registered!");
        }
        COMMANDS.put(name, supplier);

        for (String alias : aliases) {
            alias = alias.toLowerCase();
            if (COMMANDS.containsKey(alias) || ALIASES.containsKey(alias)) {
                System.err.println("Alias `" + alias + "` already registered!");
            } else {
                ALIASES.put(alias, supplier);
            }
        }
    }

    public static void triggerCommand(
            Account account,
            String messageText
    ) {
        String withoutPrefix = messageText.substring(1);

        // For the special case of "say", we want to preserve the rest of the message as a single argument.
        String[] args;
        String commandStr;
        if (withoutPrefix.toLowerCase().startsWith("say ")) {
            commandStr = "say";
            String rest = withoutPrefix.substring(4);
            args = new String[]{commandStr, rest};
        } else {
            // Otherwise split by whitespace (first token is the command name)
            String[] parts = withoutPrefix.split(" ");
            commandStr = parts[0].toLowerCase();
            args = withoutPrefix.split(" ");
        }

        Supplier<ClientCommand> supplier = COMMANDS.get(commandStr);
        if (supplier == null) {
            supplier = ALIASES.get(commandStr);
        }
        if (supplier == null) {
            Client.clientChat.addMessage(null, "Unknown command: " + commandStr);
            return;
        }
        ClientCommand command = supplier.get();
        command.setup(account, args);
        try {
            command.run();
        } catch (Exception e) {
            Client.clientChat.addMessage(null, "Error running command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void printHelp(IChat chat) {
        StringBuilder sb = new StringBuilder("Available commands:\n");
        for (String cmd : COMMANDS.keySet()) {
            Supplier<ClientCommand> supplier = COMMANDS.get(cmd);
            ClientCommand command = supplier.get();
            sb.append("#")
                    .append(cmd)
                    .append(" - ")
                    .append(command.getDescription())
                    .append("\n");
        }
        Client.clientChat.addMessage(null, sb.toString());
    }
}

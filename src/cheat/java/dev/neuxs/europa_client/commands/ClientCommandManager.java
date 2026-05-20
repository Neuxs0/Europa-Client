package dev.neuxs.europa_client.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.settings.ClientSettings;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.accounts.Account;

@SuppressWarnings("unused")
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
        if (COMMANDS.isEmpty()) {
            ClientCommandRegistry.registerClientCommands();
        }

        String normalizedMessage = normalizeCommandMessage(messageText);
        String prefix = ClientSettings.getMatchingCommandPrefix(normalizedMessage);
        if (prefix.isEmpty()) {
            return;
        }

        if (!ClientSettings.areCommandsEnabled()) {
            return;
        }

        String withoutPrefix = normalizedMessage.substring(prefix.length()).trim();
        if (withoutPrefix.isEmpty()) {
            Client.clientChat.addMessage(null, "Enter a command after " + prefix);
            return;
        }

        String[] args;
        String commandStr;
        if (withoutPrefix.toLowerCase().startsWith("say ")) {
            commandStr = "say";
            String rest = withoutPrefix.substring(4);
            args = new String[]{commandStr, rest};
        } else {
            String[] parts = withoutPrefix.split("\\s+");
            commandStr = parts[0].toLowerCase();
            args = parts;
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
            Client.LOGGER.error("Error running command: {}", e.getMessage());
        }
    }

    public static void printHelp(IChat chat) {
        StringBuilder sb = new StringBuilder("Available commands:\n");
        String prefix = ClientSettings.getCommandPrefix();
        for (String cmd : COMMANDS.keySet()) {
            Supplier<ClientCommand> supplier = COMMANDS.get(cmd);
            ClientCommand command = supplier.get();
            sb.append(prefix)
                    .append(cmd)
                    .append(" - ")
                    .append(command.getDescription())
                    .append("\n");
        }
        Client.clientChat.addMessage(null, sb.toString());
    }

    private static String normalizeCommandMessage(String messageText) {
        if (messageText == null) {
            return "";
        }

        String normalized = messageText.trim();
        if (normalized.startsWith("/") && ClientSettings.isCommandMessage(normalized.substring(1))) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}

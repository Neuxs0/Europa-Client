package dev.neuxs.europa_client.commands.misc;

import dev.neuxs.europa_client.Client;
import dev.neuxs.europa_client.commands.ClientCommand;
import dev.neuxs.europa_client.settings.ProfileManager;
import dev.neuxs.europa_client.settings.ProfileSection;
import dev.neuxs.europa_client.utils.Chat;

import java.util.EnumSet;
import java.util.List;

public class ProfileCommand extends ClientCommand {
    @Override
    public void run() {
        if (args.length < 2) {
            sendUsage();
            return;
        }

        try {
            switch (args[1].toLowerCase()) {
                case "list" -> listProfiles();
                case "save" -> saveProfile();
                case "load" -> loadProfile();
                case "delete" -> deleteProfile();
                default -> sendUsage();
            }
        } catch (Exception e) {
            reply(e.getMessage());
        }
    }

    private void listProfiles() {
        List<String> profiles = ProfileManager.listProfiles();
        String profileText = profiles.isEmpty() ? "No profiles saved." : String.join(", ", profiles);
        reply("Profiles: " + profileText + " Active: " + ProfileManager.getActiveProfileName());
    }

    private void saveProfile() throws Exception {
        if (args.length < 3) {
            reply("Usage: " + commandPrefix() + "profile save <name> [all|modules|settings]");
            return;
        }

        String name = ProfileManager.saveProfile(args[2], parseSections(3));
        reply("Saved profile " + name);
    }

    private void loadProfile() throws Exception {
        if (args.length < 3) {
            reply("Usage: " + commandPrefix() + "profile load <name> [all|modules|settings]");
            return;
        }

        ProfileManager.applyProfile(args[2], parseSections(3));
        reply("Loaded profile " + args[2]);
    }

    private void deleteProfile() throws Exception {
        if (args.length < 3) {
            reply("Usage: " + commandPrefix() + "profile delete <name>");
            return;
        }

        ProfileManager.deleteProfile(args[2]);
        reply("Deleted profile " + args[2]);
    }

    private EnumSet<ProfileSection> parseSections(int startIndex) {
        if (args.length <= startIndex) {
            return ProfileManager.allSections();
        }

        EnumSet<ProfileSection> sections = EnumSet.noneOf(ProfileSection.class);
        for (int i = startIndex; i < args.length; i++) {
            String token = args[i].toLowerCase();
            switch (token) {
                case "all" -> {
                    return ProfileManager.allSections();
                }
                case "modules" -> sections.add(ProfileSection.MODULES);
                case "settings", "module-settings", "modules-settings" -> sections.add(ProfileSection.MODULE_SETTINGS);
                default -> throw new IllegalArgumentException("Unknown profile section: " + args[i]);
            }
        }

        return sections.isEmpty() ? ProfileManager.allSections() : sections;
    }

    private void sendUsage() {
        reply("Usage: " + commandPrefix() + "profile list|save|load|delete");
    }

    private void reply(String message) {
        Client.clientChat.addMessage(null, Chat.getClientPrefix() + message);
    }

    @Override
    public String getDescription() {
        return "Manages profiles. Sections: all, modules, settings.";
    }
}

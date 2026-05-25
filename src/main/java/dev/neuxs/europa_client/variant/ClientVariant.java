package dev.neuxs.europa_client.variant;

import dev.neuxs.europa_client.settings.ModuleSettingsSection;
import dev.neuxs.europa_client.ui.pages.Page;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

import java.util.List;

public interface ClientVariant {
    String clientType();

    default void registerModules() {
    }

    default void registerCommands() {
    }

    default List<ModuleSettingsSection> moduleSettingsSections() {
        return List.of();
    }

    default List<Page> createExtraPages(BoxRenderer contentMenu) {
        return List.of();
    }

    default void syncModules() {
    }
}

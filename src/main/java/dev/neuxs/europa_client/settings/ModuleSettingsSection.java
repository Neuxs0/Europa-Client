package dev.neuxs.europa_client.settings;

import dev.neuxs.europa_client.modules.Module;

import java.util.List;
import java.util.function.Supplier;

public record ModuleSettingsSection(
        String id,
        String displayName,
        String fileName,
        Supplier<List<Module>> modules
) {
}

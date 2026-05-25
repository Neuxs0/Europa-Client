package dev.neuxs.europa_client.variant;

import dev.neuxs.europa_client.commands.ClientCommandManager;
import dev.neuxs.europa_client.commands.modules.cheats.FlyCommand;
import dev.neuxs.europa_client.commands.modules.cheats.HClipCommand;
import dev.neuxs.europa_client.commands.modules.cheats.NoClipCommand;
import dev.neuxs.europa_client.commands.modules.cheats.NoFallCommand;
import dev.neuxs.europa_client.commands.modules.cheats.ReachCommand;
import dev.neuxs.europa_client.commands.modules.cheats.SpeedCommand;
import dev.neuxs.europa_client.commands.modules.cheats.VClipCommand;
import dev.neuxs.europa_client.commands.modules.cheats.XrayCommand;
import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.settings.ModuleSettingsSection;
import dev.neuxs.europa_client.ui.pages.CheatsPage;
import dev.neuxs.europa_client.ui.pages.Page;
import dev.neuxs.europa_client.utils.CheatSyncModules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

import java.util.List;

public class CheatVariant implements ClientVariant {
    @Override
    public String clientType() {
        return "Cheat";
    }

    @Override
    public void registerModules() {
        CheatModules.register();
    }

    @Override
    public void registerCommands() {
        ClientCommandManager.registerCommand("noclip", NoClipCommand::new, "nc");
        ClientCommandManager.registerCommand("nofall", NoFallCommand::new, "nf");
        ClientCommandManager.registerCommand("fly", FlyCommand::new, "f");
        ClientCommandManager.registerCommand("speed", SpeedCommand::new, "s");
        ClientCommandManager.registerCommand("reach", ReachCommand::new);
        ClientCommandManager.registerCommand("xray", XrayCommand::new, "xr");
        ClientCommandManager.registerCommand("hclip", HClipCommand::new, "hc");
        ClientCommandManager.registerCommand("vclip", VClipCommand::new, "vc");
    }

    @Override
    public List<ModuleSettingsSection> moduleSettingsSections() {
        return List.of(new ModuleSettingsSection(
                "cheat",
                "cheat",
                "cheat-settings.json",
                () -> CheatModules.cheatModuleList
        ));
    }

    @Override
    public List<Page> createExtraPages(BoxRenderer contentMenu) {
        return List.of(new CheatsPage(contentMenu));
    }

    @Override
    public void syncModules() {
        CheatSyncModules.syncNoClip();
    }
}

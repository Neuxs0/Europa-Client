package dev.neuxs.europa_client.ui.pages;

import dev.neuxs.europa_client.modules.CheatModules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

@SuppressWarnings("unused")
public class CheatsPage extends ModuleListPage {
    public CheatsPage(BoxRenderer pageContainer) {
        super("Cheats", pageContainer, () -> CheatModules.cheatModuleList);
    }
}

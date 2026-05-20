package dev.neuxs.europa_client.ui.pages;

import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

@SuppressWarnings("unused")
public class CheatsPage extends ModuleListPage {
    public CheatsPage(BoxRenderer pageContainer) {
        super("Cheats", pageContainer, () -> Modules.cheatModuleList);
    }
}

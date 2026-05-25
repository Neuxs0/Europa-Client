package dev.neuxs.europa_client.ui.pages;

import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

@SuppressWarnings("unused")
public class UtilitiesPage extends ModuleListPage {
    public UtilitiesPage(BoxRenderer pageContainer) {
        super("Utilities", pageContainer, () -> Modules.utilModuleList);
    }
}

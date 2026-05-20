package dev.neuxs.europa_client.ui.pages;

import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

@SuppressWarnings("unused")
public class UIPage extends ModuleListPage {
    public UIPage(BoxRenderer pageContainer) {
        super("UI", pageContainer, () -> Modules.uiModuleList);
    }
}

package dev.neuxs.europa_client.ui.pages;

import dev.neuxs.europa_client.modules.Module;
import dev.neuxs.europa_client.modules.Modules;
import dev.neuxs.europa_client.utils.rendering.BoxRenderer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class UIPage extends ModuleListPage {
    public UIPage(BoxRenderer pageContainer) {
        super("UI", pageContainer, UIPage::getListedUiModules);
    }

    private static List<Module> getListedUiModules() {
        List<Module> modules = new ArrayList<>(Modules.uiModuleList);
        modules.remove(Modules.vanillaHealthbar);
        modules.remove(Modules.vanillaHotbar);
        return modules;
    }
}

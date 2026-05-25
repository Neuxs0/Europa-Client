package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import dev.neuxs.europa_client.settings.ClientSettings;
import dev.neuxs.europa_client.settings.SettingsManager;
import dev.neuxs.europa_client.ui.widgets.ClientMenuKeybindButton;
import dev.neuxs.europa_client.utils.KeybindUtil;
import finalforeach.cosmicreach.gamestates.KeybindsMenu;
import finalforeach.cosmicreach.ui.widgets.CRButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;

@SuppressWarnings("unused")
@Mixin(KeybindsMenu.class)
public abstract class KeybindsMenuMixin implements EventListener, ClientMenuKeybindButton.Action {
    @Shadow
    private boolean keybindJustSet;

    @Unique
    private CRButton europaClient$menuKeybindButton;

    @Unique
    private boolean europaClient$listeningForMenuKeybind;

    @Unique
    private final LinkedHashSet<Integer> europaClient$capturedMenuKeys = new LinkedHashSet<>();

    @Inject(method = "create", at = @At("TAIL"))
    private void europaClient$addMenuKeybindButton(CallbackInfo ci) {
        var menuStage = ((GameStateAccessor) this).europa_client$getStage();

        europaClient$menuKeybindButton = new ClientMenuKeybindButton("", (ClientMenuKeybindButton.Action) (Object) this);
        europaClient$menuKeybindButton.setSize(250f, 50f);
        europaClient$menuKeybindButton.setPosition(16f, 16f);
        europaClient$syncMenuKeybindButton();
        menuStage.addActor(europaClient$menuKeybindButton);

        menuStage.addListener((EventListener) (Object) this);
    }

    @Override
    public boolean handle(Event event) {
        if (!europaClient$listeningForMenuKeybind || !(event instanceof InputEvent inputEvent)) {
            return false;
        }

        if (inputEvent.getType() != InputEvent.Type.keyDown) {
            return true;
        }

        return europaClient$handleMenuKeybindKeyDown(inputEvent.getKeyCode());
    }

    @Override
    public void run() {
        europaClient$startListeningForMenuKeybind();
    }

    @Unique
    private boolean europaClient$handleMenuKeybindKeyDown(int keycode) {
        keybindJustSet = true;

        if (keycode == Input.Keys.ESCAPE) {
            europaClient$cancelListeningForMenuKeybind();
            return true;
        }
        if (keycode == Input.Keys.BACKSPACE || keycode == Input.Keys.FORWARD_DEL) {
            europaClient$saveClientMenuKeybind(ClientSettings.DEFAULT_CLIENT_MENU_KEYBIND);
            europaClient$cancelListeningForMenuKeybind();
            return true;
        }

        europaClient$capturedMenuKeys.clear();
        europaClient$capturedMenuKeys.addAll(KeybindUtil.captureCurrentCombination(keycode));
        europaClient$menuKeybindButton.setText("Client Menu: " + KeybindUtil.format(KeybindUtil.serialize(europaClient$capturedMenuKeys)));

        if (KeybindUtil.containsNonModifier(europaClient$capturedMenuKeys)) {
            String keybind = KeybindUtil.serialize(europaClient$capturedMenuKeys);
            europaClient$saveClientMenuKeybind(keybind);
            europaClient$listeningForMenuKeybind = false;
            europaClient$capturedMenuKeys.clear();
            europaClient$syncMenuKeybindButton();
        }
        return true;
    }

    @Unique
    private void europaClient$startListeningForMenuKeybind() {
        europaClient$listeningForMenuKeybind = true;
        europaClient$capturedMenuKeys.clear();
        keybindJustSet = true;
        europaClient$menuKeybindButton.setText("Client Menu: [???]");
    }

    @Unique
    private void europaClient$cancelListeningForMenuKeybind() {
        europaClient$listeningForMenuKeybind = false;
        europaClient$capturedMenuKeys.clear();
        europaClient$syncMenuKeybindButton();
    }

    @Unique
    private void europaClient$syncMenuKeybindButton() {
        if (!europaClient$listeningForMenuKeybind && europaClient$menuKeybindButton != null) {
            europaClient$menuKeybindButton.setText("Client Menu: [" + KeybindUtil.format(ClientSettings.getClientMenuKeybind()) + "]");
        }
    }

    @Unique
    private void europaClient$saveClientMenuKeybind(String keybind) {
        boolean previousAutoSave = SettingsManager.isAutoSaveEnabled();
        SettingsManager.setAutoSaveEnabled(false);
        try {
            ClientSettings.CLIENT_MENU_KEYBIND.setValue(keybind);
        } finally {
            SettingsManager.setAutoSaveEnabled(previousAutoSave);
        }
        SettingsManager.saveClientSettings();
    }
}

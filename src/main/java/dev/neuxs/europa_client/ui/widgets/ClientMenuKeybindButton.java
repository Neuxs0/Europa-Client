package dev.neuxs.europa_client.ui.widgets;

import finalforeach.cosmicreach.ui.widgets.CRButton;

public class ClientMenuKeybindButton extends CRButton {
    public interface Action {
        void run();
    }

    private final Action onClickAction;

    public ClientMenuKeybindButton(String text, Action onClickAction) {
        super(text);
        this.onClickAction = onClickAction;
    }

    @Override
    public void onClick() {
        super.onClick();
        if (onClickAction != null) {
            onClickAction.run();
        }
    }
}

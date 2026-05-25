package dev.neuxs.europa_client.modules;

import finalforeach.cosmicreach.settings.Controls;

public class ControlsMovementInput implements MovementInput {
    @Override
    public float forward() {
        return Controls.forwardPressed();
    }

    @Override
    public float backward() {
        return Controls.backwardPressed();
    }

    @Override
    public float left() {
        return Controls.leftPressed();
    }

    @Override
    public float right() {
        return Controls.rightPressed();
    }

    @Override
    public boolean jump() {
        return Controls.jumpPressed();
    }

    @Override
    public boolean crouch() {
        return Controls.crouchPressed();
    }

    @Override
    public boolean sprint() {
        return Controls.sprintPressed();
    }
}

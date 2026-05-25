package dev.neuxs.europa_client.modules;

public interface MovementInput {
    float forward();

    float backward();

    float left();

    float right();

    boolean jump();

    boolean crouch();

    boolean sprint();
}

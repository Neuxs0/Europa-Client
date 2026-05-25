package dev.neuxs.europa_client.mixins;

import com.badlogic.gdx.scenes.scene2d.Stage;
import finalforeach.cosmicreach.gamestates.GameState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameState.class)
public interface GameStateAccessor {
    @Accessor("stage")
    Stage europa_client$getStage();
}

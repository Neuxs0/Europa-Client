package dev.neuxs.europa_client.mixins;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.entities.player.Player;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameSingletons.class)
public interface GameSingletonsInterface {
    @Accessor("playersToAccounts")
    static WeakHashMap<Player, Account> getPlayersToAccounts() {
        throw new AssertionError();
    }
}

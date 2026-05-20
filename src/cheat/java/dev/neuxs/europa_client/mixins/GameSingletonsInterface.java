package dev.neuxs.europa_client.mixins;

import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.singletons.GameSingletonPlayers;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameSingletonPlayers.class)
public interface GameSingletonsInterface {
    @Accessor("playersToAccounts")
    static WeakHashMap<Player, Account> europa_client$getPlayersToAccounts() {
        throw new AssertionError();
    }
}

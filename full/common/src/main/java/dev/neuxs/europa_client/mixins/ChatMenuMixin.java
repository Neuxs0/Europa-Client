package dev.neuxs.europa_client.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.networking.client.ChatSender;
import dev.neuxs.europa_client.commands.ClientCommandManager;

@Mixin(ChatMenu.class)
public abstract class ChatMenuMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/networking/client/ChatSender;sendMessageOrCommand(Lfinalforeach/cosmicreach/chat/Chat;Lfinalforeach/cosmicreach/accounts/Account;Ljava/lang/String;)V"
            )
    )
    private static void interceptSendMessageOrCommand(Chat chat, Account account, String messageText) {
        if (messageText.startsWith("#")) {
            ClientCommandManager.triggerCommand(account, messageText);
        } else {
            ChatSender.sendMessageOrCommand(chat, account, messageText);
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/chat/Chat;addMessage(Lfinalforeach/cosmicreach/accounts/Account;Ljava/lang/String;)V"
            )
    )
    private void interceptAddMessage(Chat instance, Account account, String messageText) {
        if (messageText.startsWith("#")) {
            return;
        }
        instance.addMessage(account, messageText);
    }
}

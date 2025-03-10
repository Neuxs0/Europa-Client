package dev.neuxs.europa_client.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.networking.client.ChatSender;
import dev.neuxs.europa_client.commands.ClientCommandManager;

@Mixin(ChatMenu.class)
public abstract class ChatMenuMixin {

    /**
     * Redirects the call to ChatSender.sendMessageOrCommand within ChatMenu.render.
     * If the message starts with '#' the command manager handles it;
     * otherwise the original call is executed.
     *
     * Note the adjusted target descriptor: the first parameter is now Chat.
     *
     * @param chat        the Chat instance (implements IChat)
     * @param account     the account sending the message
     * @param messageText the message text
     */
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/networking/client/ChatSender;sendMessageOrCommand(Lfinalforeach/cosmicreach/chat/Chat;Lfinalforeach/cosmicreach/accounts/Account;Ljava/lang/String;)V"
            )
    )
    private static void interceptSendMessageOrCommand(Chat chat, Account account,
                                                      String messageText) {
        if (messageText.startsWith("#")) {
            // Process the message as a client-side command.
            ClientCommandManager.triggerCommand((IChat) chat, account, messageText);
        } else {
            // Otherwise, send normally.
            ChatSender.sendMessageOrCommand(chat, account, messageText);
        }
    }

    /**
     * Redirects the call to Chat.addMessage inside ChatMenu.render.
     * If the text starts with '#' (a client command), we do not add it to the chat UI.
     *
     * @param instance    the Chat instance (myChat)
     * @param account     the sender’s account
     * @param messageText the message text
     */
    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lfinalforeach/cosmicreach/chat/Chat;addMessage(Lfinalforeach/cosmicreach/accounts/Account;Ljava/lang/String;)V"
            )
    )
    private void interceptAddMessage(Chat instance, Account account,
                                     String messageText) {
        if (messageText.startsWith("#")) {
            // Do not add command messages to the chat display.
            return;
        }
        instance.addMessage(account, messageText);
    }
}

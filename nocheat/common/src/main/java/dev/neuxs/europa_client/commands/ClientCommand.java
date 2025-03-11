package dev.neuxs.europa_client.commands;

import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.accounts.Account;

public abstract class ClientCommand {
    protected Account account;
    protected String[] args;

    public ClientCommand() {}

    /**
     * Called before running the command.
     *
     * @param account the sender’s account
     * @param args    the command arguments split by whitespace
     */
    public void setup(Account account, String[] args) {
        this.account = account;
        this.args = args;
    }

    public abstract void run();

    /**
     * Returns a description for this command used in help menus.
     *
     * @return the description string
     */
    public abstract String getDescription();
}

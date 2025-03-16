package dev.neuxs.europa_client.commands;

import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.accounts.Account;

public abstract class ClientCommand {
    protected Account account;
    protected String[] args;

    public ClientCommand() {}

    public void setup(Account account, String[] args) {
        this.account = account;
        this.args = args;
    }

    public abstract void run();

    public abstract String getDescription();
}

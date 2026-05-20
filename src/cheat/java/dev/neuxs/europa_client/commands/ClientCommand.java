package dev.neuxs.europa_client.commands;

import dev.neuxs.europa_client.settings.ClientSettings;
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

    protected String commandPrefix() {
        return ClientSettings.getCommandPrefix();
    }
}

package dev.neuxs.europa_client.settings;

public enum ProfileSection {
    MODULES("Modules", "modulesEnabled"),
    MODULE_SETTINGS("Module Settings", "moduleSettings");

    private final String displayName;
    private final String jsonKey;

    ProfileSection(String displayName, String jsonKey) {
        this.displayName = displayName;
        this.jsonKey = jsonKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJsonKey() {
        return jsonKey;
    }
}

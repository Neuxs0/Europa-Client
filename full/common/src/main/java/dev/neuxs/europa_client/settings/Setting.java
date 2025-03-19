package dev.neuxs.europa_client.settings;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final T defaultValue;
    private final Predicate<T> validator;
    private T value;

    public Setting(String name, T defaultValue) {
        this(name, defaultValue, t -> true);
    }

    public Setting(String name, T defaultValue, Predicate<T> validator) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        // If the new value is equal to the current value, do nothing.
        if (this.value != null && this.value.equals(newValue)) {
            return;
        }
        if (!validator.test(newValue)) {
            throw new IllegalArgumentException("Invalid value for setting " + name);
        }
        this.value = newValue;
        // Only auto-save if we are not reloading settings.
        if (!dev.neuxs.europa_client.settings.SettingsManager.isReloading()) {
            dev.neuxs.europa_client.settings.SettingsManager.autoSaveIfEnabled();
        }
    }
}

package dev.neuxs.europa_client.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Setting<T> {
    private final String name;
    private final T defaultValue;
    private final Predicate<T> validator;
    private final List<T> options;
    private T value;
    private String displayName;
    private String description;
    private Float minValue;
    private Float maxValue;
    private Float step;

    public Setting(String name, T defaultValue) {
        this(name, defaultValue, t -> true);
    }

    public Setting(String name, T defaultValue, Predicate<T> validator) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.options = new ArrayList<>();
        this.value = defaultValue;
        this.displayName = name;
        this.description = "";
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

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getStep() {
        return step;
    }

    public boolean hasRange() {
        return minValue != null && maxValue != null;
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public List<T> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setValue(T newValue) {
        if (this.value != null && this.value.equals(newValue)) {
            return;
        }
        if (!options.isEmpty() && !options.contains(newValue)) {
            throw new IllegalArgumentException("Invalid option for setting " + name);
        }
        if (!validator.test(newValue)) {
            throw new IllegalArgumentException("Invalid value for setting " + name);
        }
        this.value = newValue;

        if (!dev.neuxs.europa_client.settings.SettingsManager.isReloading()) {
            dev.neuxs.europa_client.settings.SettingsManager.autoSaveIfEnabled();
        }
    }

    @SuppressWarnings("unchecked")
    public void setValueFromObject(Object rawValue) {
        setValue((T) convertValue(rawValue));
    }

    public Object convertValue(Object rawValue) {
        if (rawValue == null || defaultValue == null) {
            return rawValue;
        }

        if (defaultValue instanceof Float) {
            return toFloat(rawValue);
        } else if (defaultValue instanceof Double) {
            return toDouble(rawValue);
        } else if (defaultValue instanceof Integer) {
            return toInteger(rawValue);
        } else if (defaultValue instanceof Long) {
            return toLong(rawValue);
        } else if (defaultValue instanceof Short) {
            return toShort(rawValue);
        } else if (defaultValue instanceof Byte) {
            return toByte(rawValue);
        } else if (defaultValue instanceof Boolean) {
            return toBoolean(rawValue);
        } else if (defaultValue instanceof String) {
            return rawValue.toString();
        } else if (defaultValue instanceof Enum<?>) {
            return toEnum(rawValue);
        }

        return rawValue;
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    public Setting<T> withDisplayName(String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
        return this;
    }

    public Setting<T> withDescription(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public Setting<T> withRange(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (this.maxValue < this.minValue) {
            float previousMin = this.minValue;
            this.minValue = this.maxValue;
            this.maxValue = previousMin;
        }
        return this;
    }

    public Setting<T> withStep(float step) {
        this.step = step > 0f ? step : null;
        return this;
    }

    public Setting<T> withOptions(Collection<T> options) {
        this.options.clear();
        if (options != null) {
            for (T option : options) {
                if (option != null && !this.options.contains(option)) {
                    this.options.add(option);
                }
            }
        }
        return this;
    }

    private Float toFloat(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.floatValue();
        }
        return Float.parseFloat(requireTextValue(rawValue));
    }

    private Double toDouble(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(requireTextValue(rawValue));
    }

    private Integer toInteger(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(requireTextValue(rawValue));
    }

    private Long toLong(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(requireTextValue(rawValue));
    }

    private Short toShort(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.shortValue();
        }
        return Short.parseShort(requireTextValue(rawValue));
    }

    private Byte toByte(Object rawValue) {
        if (rawValue instanceof Number number) {
            return number.byteValue();
        }
        return Byte.parseByte(requireTextValue(rawValue));
    }

    private String requireTextValue(Object rawValue) {
        String text = rawValue.toString().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Invalid numeric value for setting " + name);
        }
        return text;
    }

    private Boolean toBoolean(Object rawValue) {
        if (rawValue instanceof Boolean value) {
            return value;
        }

        String normalized = rawValue.toString().trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true", "on", "yes", "1" -> true;
            case "false", "off", "no", "0" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean value for setting " + name);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Enum<?> toEnum(Object rawValue) {
        if (defaultValue.getClass().isInstance(rawValue)) {
            return (Enum<?>) rawValue;
        }

        String requestedName = rawValue.toString().trim();
        Class<? extends Enum> enumClass = ((Enum<?>) defaultValue).getDeclaringClass();
        for (Enum<?> constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(requestedName)) {
                return constant;
            }
        }

        throw new IllegalArgumentException("Invalid option for setting " + name);
    }
}

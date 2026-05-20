package dev.neuxs.europa_client.utils;

public class ClientLogger {
    private final String name;

    public ClientLogger(String name) {
        this.name = name;
    }

    public void trace(String message, Object... args) {
        log("TRACE", message, args);
    }

    public void debug(String message, Object... args) {
        log("DEBUG", message, args);
    }

    public void info(String message, Object... args) {
        log("INFO", message, args);
    }

    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }

    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }

    private void log(String level, String message, Object... args) {
        Throwable throwable = extractThrowable(args);
        Object[] formatArgs = throwable == null ? args : copyWithoutLast(args);
        String formattedMessage = format(message, formatArgs);
        System.out.println("[" + level + "] [" + name + "] " + formattedMessage);
        if (throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }

    private static Throwable extractThrowable(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object last = args[args.length - 1];
        return last instanceof Throwable throwable ? throwable : null;
    }

    private static Object[] copyWithoutLast(Object[] args) {
        Object[] copy = new Object[args.length - 1];
        System.arraycopy(args, 0, copy, 0, copy.length);
        return copy;
    }

    private static String format(String message, Object[] args) {
        if (message == null) {
            return "null";
        }
        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int searchStart = 0;
        int placeholder;
        while ((placeholder = message.indexOf("{}", searchStart)) >= 0) {
            result.append(message, searchStart, placeholder);
            if (argIndex < args.length) {
                result.append(String.valueOf(args[argIndex++]));
            } else {
                result.append("{}");
            }
            searchStart = placeholder + 2;
        }
        result.append(message.substring(searchStart));

        while (argIndex < args.length) {
            result.append(' ').append(String.valueOf(args[argIndex++]));
        }

        return result.toString();
    }
}

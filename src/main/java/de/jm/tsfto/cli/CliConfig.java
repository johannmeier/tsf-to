package de.jm.tsfto.cli;

import de.jm.tsfto.cli.exceptions.CliRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CliConfig {
    private final Map<String, String> appProperties = new HashMap<>();
    private final Properties fileProperties = new Properties();
    List<String> remainingArguments = Collections.emptyList();

    public void init(String[] args) {
        remainingArguments = processArguments(args);
        String filename = getProperty("cliConfig");
        if (filename == null) {
            if (new File("cli.properties").exists()) {
                filename = "cli.properties";
            }

            if (filename == null) {
                filename = "easy.properties";
            }
        }
        readFileArguments(filename);
    }

    private List<String> processArguments(String[] args) {
        remainingArguments = new ArrayList<>();
        for (String arg : args) {
            if (isConfigArgument(arg)) {
                appProperties.put(getKey(arg), getValue(arg));
            } else {
                remainingArguments.add(arg);
            }
        }
        return remainingArguments;
    }

    private static String getKey(String arg) {
        int posOfColon = arg.indexOf(":");
        if (posOfColon < 0) {
            return arg.substring("--".length());
        } else {
            return arg.substring("--".length(), posOfColon);
        }
    }

    private static String getValue(String arg) {
        int posOfColon = arg.indexOf(":");
        if (posOfColon < 0) {
            return null;
        } else {
            return arg.substring(posOfColon + 1);
        }
    }

    private static boolean isConfigArgument(String arg) {
        return arg != null && arg.startsWith("--");
    }

    private void readFileArguments(String filename) {
        try {
            if (new File(filename).exists()) {
                try (InputStreamReader fileReader = new InputStreamReader(Files.newInputStream(Paths.get(filename)), Charset.defaultCharset())) {
                    fileProperties.load(fileReader);
                }
            }
        } catch (IOException e) {
            throw new CliRuntimeException(e);
        }
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        String value;
        value = appProperties.get(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            value = fileProperties.getProperty(key);
        }
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }


    public boolean containsKey(String key) {
        boolean contains;
        contains = appProperties.containsKey(key);
        if (!contains) {
            contains = System.getProperties().contains(key);
        }
        if (!contains) {
            contains = System.getenv().containsKey(key);
        }
        if (!contains) {
            contains = fileProperties.containsKey(key);
        }
        return contains;
    }

    public Boolean getPropertyAsBoolean(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        } else {
            return Boolean.valueOf(value);
        }
    }

    public Integer getPropertyAsInteger(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        } else {
            return Integer.valueOf(value);
        }
    }

    public Long getPropertyAsLong(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        } else {
            return Long.valueOf(value);
        }
    }

    public List<String> getPropertyAsStringList(String key) {
        String separator = getSeparator(key);
        String listValue = getProperty(key);
        if (listValue == null) {
            return new ArrayList<>();
        } else {
            String[] values = listValue.split(separator);
            return new ArrayList<>(Arrays.asList(values));
        }
    }

    String getSeparator(String key) {
        String separatorKey = key;
        String separator;
        do {
            separator = getProperty(separatorKey + ".separator");
            if (separator == null) {
                int lastIndex =  separatorKey.lastIndexOf('.');
                if (lastIndex < 0) {
                    separatorKey = "";
                } else {
                    separatorKey = separatorKey.substring(0, lastIndex);
                }
            }
        } while (!separatorKey.isEmpty() && separator == null);
        return separator == null ? getProperty("separator",",") : separator;
    }

    public List<String> getRemainingArguments() {
        return remainingArguments;
    }
}

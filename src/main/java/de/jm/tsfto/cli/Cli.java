package de.wsc.tsfto.cli;

import de.wsc.tsfto.cli.annotations.App;
import de.wsc.tsfto.cli.annotations.Argument;
import de.wsc.tsfto.cli.annotations.CliConfiguration;
import de.wsc.tsfto.cli.annotations.CliService;
import de.wsc.tsfto.cli.annotations.ErrorCode;
import de.wsc.tsfto.cli.annotations.Flag;
import de.wsc.tsfto.cli.exceptions.CliRuntimeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public final class Cli {

    @ErrorCode(description = "Mandatory parameters are missing")
    private static final int MANDATORY_PARAMETER_MISSING = 8;

    @ErrorCode(description = "Ein unerwarteter Fehler ist aufgetreten")
    private static final int RUNTIME_ERROR = 12;

    private static class CliParameter {
        String name;
        String description;
        boolean mandatory;
        boolean missingValue;

        public CliParameter(String name, String description, boolean mandatory, boolean missingValue) {
            this.name = name;
            this.description = description;
            this.mandatory = mandatory;
            this.missingValue = missingValue;
        }
    }

    private static class CliFlag extends CliParameter implements Comparable<CliFlag> {
        public CliFlag(String name, String description, boolean mandatory, boolean missingValue) {
            super(name, description, mandatory, missingValue);
        }

        @Override
        public int compareTo(CliFlag other) {
            return other == null ? 1 : name.compareTo(other.name);
        }
    }

    private static class CliArgument extends CliParameter implements Comparable<CliArgument> {
        final int index;

        public CliArgument(int index, String name, String description, boolean mandatory, boolean missingValue) {
            super(name, description, mandatory, missingValue);
            this.index = index;
        }

        @Override
        public int compareTo(CliArgument other) {
            return other == null ? 1 : (index - other.index);
        }
    }

    private static class CliErrorCode {
        int code;
        String description;

        public CliErrorCode(int code, String description) {
            this.code = code;
            this.description = description;
        }
    }

    private static final ThreadLocal<Map<String, Object>> classInstances = new ThreadLocal<>();
    private static final TreeSet<CliArgument> cliArguments = new TreeSet<>();
    private static final TreeSet<CliFlag> cliFlags = new TreeSet<>();

    static {
        cliFlags.add(new CliFlag("cliConfig", "Konfigurationsdatei, default 'cli.properties'", false, false));
    }

    private static final Logger LOGGER = CliLogger.getLogger(Cli.class.getName());

    private static final List<CliErrorCode> cliErrorCodes = new ArrayList<>();

    private Cli() {
    }

    public static <T extends CliApp> void run(Class<T> cliMainClass, String[] args) {
        try {
            classInstances.set(new HashMap<>());
            CliConfig config = new CliConfig();
            config.init(args);
            addInstance(config);
            T tApp = createInstance(cliMainClass, config);
            processAnnotations(tApp, config);
            collectErrorCodes(tApp);

            if (config.containsKey("help")) {
                printUsage(cliMainClass);
                return;
            }

            String mandatory = checkMandatory();
            if (mandatory != null) {
                LOGGER.info(mandatory);
                printUsage(cliMainClass);
                System.exit(MANDATORY_PARAMETER_MISSING);
            }

            int rc = tApp.cliMain(config.getRemainingArguments());
            if (rc > 0) {
                System.exit(rc);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new CliRuntimeException(e);
        } catch (RuntimeException e) {
            LOGGER.severe(e.getMessage());
            System.exit(RUNTIME_ERROR);
        } finally {
            classInstances.remove();
        }
    }

    private static void addInstance(Object instance) {
        classInstances.get().put(instance.getClass().getName(), instance);
    }

    private static Object getInstance(String key) {
        return classInstances.get().get(key);
    }

    private static <T> T createInstance(Class<T> tClass, CliConfig config)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Constructor<?>[] constructors = tClass.getDeclaredConstructors();
        if (constructors.length > 1) {
            throw new CliRuntimeException(tClass + " has more than one constructor");
        }

        Class<?>[] parameterTypes = constructors[0].getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> pClass = parameterTypes[i];
            String classname = pClass.getName();
            if (classInstances.get().containsKey(classname)) {
                parameters[i] = getInstance(classname);
            } else if (isCliService(pClass) || isCliConfiguration(pClass)) {
                parameters[i] = createInstance(pClass, config);
                processAnnotations(parameters[i], (CliConfig) getInstance(CliConfig.class.getName()));
                addInstance(parameters[i]);
            } else {
                if (pClass == String.class) {
                    Flag flag = getFlagAnnotation(constructors[0].getParameterAnnotations()[i]);
                    if (flag != null && !flag.name().isEmpty()) {
                        parameters[i] = config.getProperty(flag.name());
                        continue;
                    }

                    Argument argument = getArgumentAnnotation(constructors[0].getParameterAnnotations()[i]);
                    if (argument != null) {
                        parameters[i] = config.getRemainingArguments().get(argument.index());
                        continue;
                    }
                }
                throw new CliRuntimeException(pClass + " is no CliService");
            }
        }

        Constructor<T> constructor = tClass.getDeclaredConstructor(parameterTypes);
        return constructor.newInstance(parameters);
    }

    private static Argument getArgumentAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Argument) {
                return (Argument) annotation;
            }
        }
        return null;
    }

    private static Flag getFlagAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Flag) {
                return (Flag) annotation;
            }
        }
        return null;
    }

    private static boolean isCliService(Class<?> pClass) {
        return pClass.isAnnotationPresent(CliService.class);
    }

    private static boolean isCliConfiguration(Class<?> pClass) {
        return pClass.isAnnotationPresent(CliConfiguration.class);
    }

    private static void collectErrorCodes(Object cliApp) {
        for (Field field : Cli.class.getDeclaredFields()) {
            processErrorCode(field);
        }

        App app = cliApp.getClass().getAnnotation(App.class);
        if (app != null) {
            Class<?>[] additionalClasses = app.classes();
            for (Class<?> clazz : additionalClasses) {
                for (Field field : clazz.getDeclaredFields()) {
                    processErrorCode(field);
                }
            }
        }
    }

    private static void processAnnotations(Object appOrService, CliConfig config) {
        for (Field field : appOrService.getClass().getDeclaredFields()) {
            processFlag(appOrService, config, field);
            processArgument(appOrService, config, field);
            processErrorCode(field);
        }
    }

    private static String getPrefix(Class<?> clazz) {
        CliConfiguration configAnnotation = clazz.getAnnotation(CliConfiguration.class);
        if (configAnnotation == null) {
            return "";
        } else {
            return configAnnotation.value();
        }
    }

    private static String getKey(Flag flag, Field field, Class<?> clazz) {
        if (flag == null) {
            return null;
        } else {
            String key = flag.name();
            if (key.isEmpty()) {
                key = field.getName();
            }
            return getPrefix(clazz) + key;
        }
    }

    private static void processFlag(Object tApp, CliConfig config, Field field) {
        Flag flag = field.getAnnotation(Flag.class);
        if (flag != null) {
            String key = getKey(flag, field, tApp.getClass());
            if (config.containsKey(key)) {
                if (isStringArray(field) || isStringList(field)) {
                    List<String> values = config.getPropertyAsStringList(key);
                    setFieldValue(tApp, field, values.toArray(new String[0]));

                    cliFlags.add(new CliFlag(key, flag.description(), flag.mandatory(), values.isEmpty()));
                } else {
                    String value = config.getProperty(key);
                    if ((field.getType() == boolean.class || field.getType() == Boolean.class) && value == null) {
                        value = config.containsKey(key) ? "true" : "false";
                    }
                    setFieldValue(tApp, field, value);

                    cliFlags.add(new CliFlag(key, flag.description(), flag.mandatory(), value == null));
                }
            } else {
                cliFlags.add(new CliFlag(key, flag.description(), flag.mandatory(), true));
            }
        }
    }

    private static void processArgument(Object tApp, CliConfig config, Field field) {
        Argument argument = field.getAnnotation(Argument.class);
        if (argument != null) {
            String key = argument.name();
            if (key.isEmpty()) {
                key = field.getName();
            }
            int index = argument.index();
            List<String> remainingArguments = config.getRemainingArguments();
            if (index >= 0) {
                if (remainingArguments.size() > index) {
                    if (isStringArray(field) || isStringList(field)) {
                        int size = remainingArguments.size();
                        setFieldValue(tApp, field, remainingArguments.subList(index, size).toArray(new String[0]));
                    } else {
                        setFieldValue(tApp, field, remainingArguments.get(index));
                    }
                }
                cliArguments.add(new CliArgument(index, key, argument.description(), argument.mandatory(),
                        remainingArguments.size() <= index));
            }
        }
    }

    private static void processErrorCode(Field field) {
        try {
            ErrorCode errorCode = field.getAnnotation(ErrorCode.class);
            if (errorCode != null) {
                field.setAccessible(true);
                int code = field.getInt(null);
                String description = errorCode.description();
                cliErrorCodes.add(new CliErrorCode(code, description));
            }
        } catch (IllegalAccessException e) {
            throw new CliRuntimeException(e);
        }
    }

    private static void setFieldValue(Object appOrService, Field field, String... values) {
        try {
            if (values[0] == null) {
                return;
            }
            Class<?> fieldType = field.getType();

            field.setAccessible(true);

            if (fieldType == String.class) {
                field.set(appOrService, values[0]);
            }
            if (fieldType == int.class || fieldType == Integer.class) {
                field.set(appOrService, Integer.parseInt(values[0]));
            }
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                field.set(appOrService, Boolean.valueOf(values[0]));
            }

            if (isStringList(field)) {
                field.set(appOrService, Arrays.asList(values));
            }
            if (isStringArray(field)) {
                field.set(appOrService, values);
            }
        } catch (IllegalAccessException e) {
            throw new CliRuntimeException(e);
        }
    }

    private static boolean isStringList(Field field) {
        Type fieldGenericType = field.getGenericType();
        ParameterizedType parameterizedType = null;
        if (fieldGenericType instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) fieldGenericType;
        }
        if (parameterizedType != null && parameterizedType.getRawType() == List.class) {
            return parameterizedType.getActualTypeArguments().length == 1 && parameterizedType.getActualTypeArguments()[0] == String.class;
        }
        return false;
    }

    private static boolean isStringArray(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType.isArray() && fieldType.getComponentType() == String.class;
    }

    private static <T extends CliApp> void printUsage(Class<T> cliApp) {
        StringBuilder description = new StringBuilder("\nUsage: ");
        description.append("java ").append(cliApp.getName()).append(' ');

        for (CliFlag cliFlag : cliFlags) {
            description.append(toString(cliFlag)).append(' ');
        }

        for (CliArgument cliArgument : cliArguments) {
            description.append(toString(cliArgument)).append(' ');
        }

        description.append("\n\n");

        App app = cliApp.getAnnotation(App.class);
        if (app != null) {
            description.append(app.description()).append("\n\n");
        }

        for (CliArgument cliArgument : cliArguments) {
            description.append(toString(cliArgument)).append(": ")
                    .append(cliArgument.description).append(cliArgument.mandatory ? " (MANDATORY)" : "")
                    .append('\n');
        }

        description.append("\n");
        for (CliFlag cliFlag : cliFlags) {
            description.append("--").append(cliFlag.name).append(": ")
                    .append(cliFlag.description).append(cliFlag.mandatory ? " (MANDATORY)" : "")
                    .append('\n');
        }

        if (!cliErrorCodes.isEmpty()) {
            description.append("\nERROR CODES:\n");

            for (CliErrorCode cliErrorCode : cliErrorCodes) {
                description.append(cliErrorCode.code).append(": ")
                        .append(cliErrorCode.description)
                        .append('\n');
            }
        }

        LOGGER.info(description::toString);
    }

    private static String toString(CliParameter parameter) {
        String prefix = parameter instanceof CliFlag ? "--" : "";
        if (parameter.mandatory) {
            return '<' + prefix + parameter.name + ">";
        } else {
            return '[' + prefix + parameter.name + "]";
        }
    }

    private static String checkMandatory() {
        StringBuilder result = new StringBuilder();
        for (CliFlag cliFlag : cliFlags) {
            if (cliFlag.mandatory && cliFlag.missingValue) {
                result.append("--").append(cliFlag.name).append(' ');
            }
        }
        for (CliArgument cliArgument : cliArguments) {
            if (cliArgument.mandatory && cliArgument.missingValue) {
                result.append('<').append(cliArgument.name).append('>').append(' ');
            }
        }
        if (result.length() == 0) {
            return null;
        } else {
            return "Missing mandatory parameter(s):\n" + result + '\n';
        }
    }

    public static CliConfig getPlainCliConfig() {
        CliConfig config = new CliConfig();
        config.init(new String[0]);
        return config;
    }
}

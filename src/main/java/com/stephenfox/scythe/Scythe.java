package com.stephenfox.scythe;

import static com.stephenfox.scythe.ReflectionUtil.getFieldAnnotations;
import static com.stephenfox.scythe.ReflectionUtil.getMethodAnnotations;
import static java.lang.System.exit;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.stephenfox.scythe.annotation.Option;

import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Scythe command line parser.
 *
 * @author Stephen Fox.
 */
public class Scythe {

  private static final Comparator<Option> OPTION_COMPARATOR =
      Comparator.comparingInt(Option::order);
  private final String[] cliArgs;
  private final Class<?> mainClass;

  public static Scythe cli(String[] cliArgs, Class<?> mainClass) {
    return new Scythe(cliArgs, mainClass);
  }

  private Scythe(String[] cliArgs, Class<?> mainClass) {
    this.cliArgs = sanitize(cliArgs);
    this.mainClass = mainClass;
  }

  public Map<String, Object> parse() {
    if (cliArgs.length > 0 && (cliArgs[0].equals("-h") || cliArgs[0].equals("--help"))) {
      final List<Option> fieldAnnotations = getFieldAnnotations(Option.class, mainClass);
      if (fieldAnnotations.size() > 0) {
        printHelpMessage(fieldAnnotations);
      }

      final Optional<ReflectionUtil.MethodAnnotationPair<Option>> methodAnnotations =
          getMethodAnnotations(Option.class, mainClass);
      methodAnnotations.ifPresent(
          optionMethodAnnotationPair -> printHelpMessage(optionMethodAnnotationPair.annotations));
    }

    // If annotations were declared via a field, parse them.
    final List<Option> fieldAnnotations = getFieldAnnotations(Option.class, mainClass);
    if (fieldAnnotations.size() > 0) {
      final Map<Option, Object> parsedOptions = parseOptions(fieldAnnotations, FIELD);
      final Map<String, Object> map = new HashMap<>(parsedOptions.size());
      for (Map.Entry<Option, Object> entry : parsedOptions.entrySet()) {
        // Put for the name.
        map.put(entry.getKey().name(), entry.getValue());
        // Now put for each of its alias.
        for (String alias : entry.getKey().aliases()) {
          map.put(alias, entry.getValue());
        }
      }
      return map;
    }

    // If annotations were declared via a method, parse them.
    final Optional<ReflectionUtil.MethodAnnotationPair<Option>> methodAnnotations =
        getMethodAnnotations(Option.class, mainClass);
    if (methodAnnotations.isPresent()) {
      final ReflectionUtil.MethodAnnotationPair<Option> methodAnnotationPair =
          methodAnnotations.get();
      final Map<Option, Object> parsedOptions =
          parseOptions(methodAnnotationPair.annotations, METHOD);
      final Object[] values = parsedOptions.values().toArray();

      try {
        methodAnnotationPair.method.invoke(null, values);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private static void printHelpMessage(List<Option> options) {
    final StringBuilder prefix = new StringBuilder();
    final StringBuilder help = new StringBuilder();

    for (Option option : options) {
      prefix.append(option.name());
      prefix.append(", ");
      for (String alias : option.aliases()) {
        prefix.append(alias);
        prefix.append(", ");
      }

      prefix.deleteCharAt(prefix.length() - 1);
      prefix.deleteCharAt(prefix.length() - 1);
      prefix.append(" ");
      final String[] typeSplit = option.type().getName().split("\\.");
      prefix.append(typeSplit[typeSplit.length - 1 < 0 ? 0 : typeSplit.length - 1]);
      System.out.printf("%-50s%s%n", prefix.toString(), option.help());
      prefix.setLength(0);
    }

    exit(0);
  }

  /**
   * Parse the command line arguments for the option annotations declared.
   *
   * @param options The option annotations declared.
   * @return A mapping of an {@code Option} to the value found in the command line arguments.
   */
  private Map<Option, Object> parseOptions(List<Option> options, ElementType declaredAt) {
    final Map<Option, Object> mappings;
    if (declaredAt == METHOD) {
      mappings = new TreeMap<>(OPTION_COMPARATOR);
      for (Option option : options) {
        if (option.order() == -1) {
          throw new SortOrderException(
              "No sort order defined for option: "
                  + option.name()
                  + ". Please ensure all options defined at method level have "
                  + "an order set.");
        } else if (option.order() < -1) {
          throw new SortOrderException("Invalid order " + option.order() + ", orders must be >= 0");
        }
        mappings.put(option, parseOption(cliArgs, option));
      }

    } else {
      mappings = new HashMap<>(options.size());
      for (Option option : options) {
        mappings.put(option, parseOption(cliArgs, option));
      }
    }

    return mappings;
  }

  @SuppressWarnings("unchecked")
  private static Object parseOption(String[] cliArgs, Option option) {
    if (option.multiple()) {
      final List<String> optionValueStrings = getMultipleOptionValuesFromCliArgs(cliArgs, option);
      if (optionValueStrings.size() > 0) {
        final List<Object> optionValues = new ArrayList<>();
        final Class<?> type = option.type();
        if (Number.class.isAssignableFrom(type)) {
          for (String value : optionValueStrings) {
            optionValues.add(parseNumber((Class<? extends Number>) type, value));
          }
        } else {
          optionValues.addAll(optionValueStrings);
        }
        return optionValues;
      }
    } else {
      final String optionValue = getSingleOptionValueFromCliArgs(cliArgs, option);
      if (optionValue != null) {
        if (option.isFlag()) {
          return parseBoolean(optionValue);
        }

        // TODO: Add support for custom type parsing.
        final Class<?> type = option.type();
        if (Number.class.isAssignableFrom(type)) {

          if (option.nargs() > 0) {
            final String[] numberStrings = optionValue.split(" ");

            correctNargs(option, numberStrings);
            final List<Number> numbers = new ArrayList<>();

            for (String numberString : numberStrings) {
              numbers.add(parseNumber((Class<? extends Number>) type, numberString));
            }

            return numbers;
          }
          return parseNumber((Class<? extends Number>) type, optionValue);
        } else { // Just fall back to string.
          if (option.nargs() > 0) {
            final String[] strings = optionValue.split(" ");
            correctNargs(option, strings);

            return Arrays.asList(strings);
          }
          return optionValue;
        }
      }
    }

    return null;
  }

  /**
   * Get the value for a single option from the passed command line arguments, this also includes
   * options that are multi valued.
   *
   * @param args The command line arguments.
   * @param option The option to get from the cli args.
   * @return The string representation of the option value.
   */
  private static String getSingleOptionValueFromCliArgs(String[] args, Option option) {
    final String optionName = option.name();

    for (int i = 0; i < args.length; i++) {
      final String argName = args[i];
      if (argName.equals(optionName) || equalsAnyAlias(argName, option)) {
        if (option.isFlag()) {
          return "true";
        }

        if (i + 2 > args.length) {
          throw new IllegalArgumentException("Option values must appear after the option name");
        }

        return args[i + 1];
      }
    }
    if (option.isFlag()) {
      return "false"; // No flag was found. Therefore the flag is false.
    }
    if (option.required()) {
      throw new RequiredOptionException("Required option " + optionName + " not found");
    }
    return null;
  }

  private static boolean equalsAnyAlias(String argName, Option option) {
    for (String alias : option.aliases()) {
      if (alias.equals(argName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieve multiple option value pairs. For example --env HOST --env PORT
   *
   * @param args The command line args.
   * @param option The option to retrieve.
   * @return A list of all values that appeared for each occurence of the option.
   */
  private static List<String> getMultipleOptionValuesFromCliArgs(String[] args, Option option) {
    final String optionName = option.name();
    final List<String> values = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(optionName)) {
        if (option.isFlag()) {
          throw new IllegalArgumentException("Cannot have multiple values for flags.");
        }

        if (i + 2 > args.length) {
          throw new IllegalArgumentException("Option values must appear after the option name");
        }
        values.add(args[i + 1]);
      }
    }

    if (option.required() && values.size() < 1) {
      throw new RequiredOptionException("Required option " + optionName + " not found");
    }

    return values;
  }

  /**
   * Parse any value which extends {@link Number}.
   *
   * @param numberClass The concrete number class.
   * @param optionValue The value passed from the cli.
   */
  private static Number parseNumber(Class<? extends Number> numberClass, String optionValue) {
    final Number numberValue;
    if (numberClass.equals(Byte.class)) {
      numberValue = Byte.valueOf(optionValue);
    } else if (numberClass.equals(Short.class)) {
      numberValue = Short.valueOf(optionValue);
    } else if (numberClass.equals(Integer.class)) {
      numberValue = Integer.valueOf(optionValue);
    } else if (numberClass.equals(Long.class)) {
      numberValue = Long.valueOf(optionValue);
    } else if (numberClass.equals(Float.class)) {
      numberValue = Float.valueOf(optionValue);
    } else if (numberClass.equals(Double.class)) {
      numberValue = Double.valueOf(optionValue);
    } else {
      throw new IllegalArgumentException("Cannot parse " + numberClass);
    }
    return numberValue;
  }

  private static Boolean parseBoolean(String optionValue) {
    return Boolean.valueOf(optionValue);
  }

  /**
   * Remove any leading or trailing spaces.
   *
   * @param args The args to sanitize.
   */
  private static String[] sanitize(String[] args) {
    return Arrays.stream(args).map(String::trim).collect(Collectors.toList()).toArray(args);
  }

  private static void correctNargs(Option option, String[] args) {
    if (args.length != option.nargs()) {
      // For the case where nothing was passed and args is just an empty string.
      if (args.length == 1 && args[0].isEmpty()) {
        throw new IllegalArgumentException(
            option.name() + " requires " + option.nargs() + " values, received 0");
      }
      throw new IllegalArgumentException(
          option.name() + " requires " + option.nargs() + " values, received " + args.length);
    }
  }
}

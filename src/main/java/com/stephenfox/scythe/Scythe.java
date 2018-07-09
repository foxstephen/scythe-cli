package com.stephenfox.scythe;

import com.stephenfox.scythe.annotation.Option;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Scythe {

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public static Map<String, Object> cli(Class<?> clazz, String[] args) {
    if (args.length < 1) {
      return Collections.emptyMap();
    }

    args = sanitize(args);

    final Map<String, Object> mappings = new HashMap<>();

    for (Field field : clazz.getDeclaredFields()) {
      final Option option = field.getAnnotation(Option.class);
      if (option != null) {
        parseOptionValue(args, option, mappings);
      }

      final Option[] options = field.getAnnotationsByType(Option.class);
      if (options != null) {
        for (Option opt : options) {
          parseOptionValue(args, opt, mappings);
        }
      }
    }

    for (Method method : clazz.getDeclaredMethods()) {
      final Option option = method.getAnnotation(Option.class);
      if (option != null) {
        try {
          parseOptionValue(args, option, mappings);
          method.invoke(null, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }

      final Option[] options = method.getAnnotationsByType(Option.class);
      if (options != null) {}
    }

    return mappings;
  }

  @SuppressWarnings("unchecked")
  private static void parseOptionValue(
      String[] cliArgs, Option option, Map<String, Object> mappings) {
    final String optionValue = getOptionFromCliArgs(cliArgs, option);

    final Class<?> type = option.type();
    if (Number.class.isAssignableFrom(type)) {
      parseNumber(mappings, (Class<? extends Number>) type, option.name(), optionValue);
    } else { // Just fall back to string.
      mappings.put(option.name(), optionValue);
    }
  }

  /**
   * Get the value for an option from the passed command line arguments.
   *
   * @param args The command line arguments.
   * @param option The option to get from the cli args.
   * @return The string representation of the option value.
   */
  private static String getOptionFromCliArgs(String[] args, Option option) {
    final String optionName = option.name();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(optionName)) {
        if (i + 1 > args.length) {
          throw new IllegalArgumentException("Option values must appear after the option name");
        }
        return args[i + 1];
      }
    }
    return null;
  }

  /**
   * Parse any value which extends {@link Number}.
   *
   * @param mappings The mappings containing the optionName -> optionValue.
   * @param numberClass The concrete number class.
   * @param optionName The name of the option.
   * @param optionValue The value passed from the cli.
   */
  private static void parseNumber(
      Map<String, Object> mappings,
      Class<? extends Number> numberClass,
      String optionName,
      String optionValue) {
    final Number numberValue;
    if (numberClass.equals(Short.class)) {
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
    mappings.put(optionName, numberValue);
  }

  /**
   * Remove any leading or trailing spaces.
   *
   * @param args The args to sanitize.
   */
  private static String[] sanitize(String[] args) {
    return Arrays.stream(args).map(String::trim).collect(Collectors.toList()).toArray(args);
  }
}

package com.stephenfox.scythe;

import static com.stephenfox.scythe.ReflectionUtil.getFieldAnnotations;
import static com.stephenfox.scythe.ReflectionUtil.getMethodAnnotations;

import com.stephenfox.scythe.annotation.Option;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Scythe {

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
    if (cliArgs.length < 1) {
      return Collections.emptyMap();
    }

    // If annotations were declared via a field, parse them.
    final List<Option> fieldAnnotations = getFieldAnnotations(Option.class, mainClass);
    if (fieldAnnotations.size() > 0) {
      final Map<Option, Object> parsedOptions = parseOptions(fieldAnnotations);
      final Map<String, Object> map = new HashMap<>(parsedOptions.size());
      for (Map.Entry<Option, Object> entry : parsedOptions.entrySet()) {
        map.put(entry.getKey().name(), entry.getValue());
      }
      return map;
    }

    // If annotations were declared via a method, parse them.
    final Optional<ReflectionUtil.MethodAnnotationPair<Option>> methodAnnotations =
        getMethodAnnotations(Option.class, mainClass);
    if (methodAnnotations.isPresent()) {
      ReflectionUtil.MethodAnnotationPair<Option> methodAnnotationPair = methodAnnotations.get();
      final Map<Option, Object> parsedOptions = parseOptions(methodAnnotationPair.annotations);
      final Object[] values = parsedOptions.values().toArray();

      try {
        methodAnnotationPair.method.invoke(null, values);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * Parse the command line arguments for the option annotations declared.
   *
   * @param options The option annotations declared.
   * @return A mapping of an {@code Option} to the value found in the command line arguments.
   */
  private Map<Option, Object> parseOptions(List<Option> options) {
    final Map<Option, Object> mappings = new LinkedHashMap<>();
    for (Option option : options) {
      mappings.put(option, getOptionValue(cliArgs, option));
    }
    return mappings;
  }

  @SuppressWarnings("unchecked")
  private static Object getOptionValue(String[] cliArgs, Option option) {
    final String optionValue = getOptionFromCliArgs(cliArgs, option);

    if (option.isFlag()) {
      return parseBoolean(optionValue);
    }

    final Class<?> type = option.type();
    if (Number.class.isAssignableFrom(type)) {
      return parseNumber((Class<? extends Number>) type, optionValue);
    } else { // Just fall back to string.
      return optionValue;
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
        if (option.isFlag()) {
          return "true";
        }

        if (i + 2 > args.length) {
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
   * @param numberClass The concrete number class.
   * @param optionValue The value passed from the cli.
   */
  private static Number parseNumber(Class<? extends Number> numberClass, String optionValue) {
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
}

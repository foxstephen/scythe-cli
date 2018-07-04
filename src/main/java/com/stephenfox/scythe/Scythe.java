package com.stephenfox.scythe;

import com.stephenfox.scythe.annotation.Option;
import com.stephenfox.scythe.annotation.Options;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Scythe {

  public static Map<String, Object> cli(Class<?> clazz, String[] args) {
    if (args.length < 1) {
      return Collections.emptyMap();
    }

    args = sanitize(args);

    final Map<String, Object> mappings = new HashMap<>();

    for (Field field : clazz.getDeclaredFields()) {
      final Option option = field.getAnnotation(Option.class);
      if (option != null) {
        final Object optionValue = parseOption(option, args);
        mappings.put(option.name(), optionValue);
      }

      final Option[] options = field.getAnnotationsByType(Option.class);
      if (options != null) {
        for (Option option1 : options) {
          final Object optionValue = parseOption(option1, args);
          mappings.put(option1.name(), optionValue);
        }
      }
    }
    return mappings;
  }

  private static Object parseOption(Option option, String[] args) {
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
   * Remove any leading or trailing spaces.
   *
   * @param args The args to sanitize.
   */
  private static String[] sanitize(String[] args) {
    return Arrays.stream(args).map(String::trim).collect(Collectors.toList()).toArray(args);
  }
}

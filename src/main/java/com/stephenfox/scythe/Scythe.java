package com.stephenfox.scythe;

import com.stephenfox.scythe.annotation.Option;

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
        final Map<String, Object> parsedOption = parseOption(option, args);
        System.out.println(parsedOption);
      }

    }
    return mappings;
  }

  private static Map<String, Object> parseOption(Option option, String[] args) {
    final Map<String, Object> mapping = new HashMap<>(1);
    final String optionName = option.name();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(optionName)) {
        final Object optionValue = args[i + 1]; // TODO: Raise exception if option value is not straight after it.
        mapping.put(optionName, optionValue);
        break;
      }
    }
    return mapping;

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

package com.stephenfox.scythe;

import com.stephenfox.scythe.annotation.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class ReflectionUtil {

  /**
   * Attempts to retrieve annotations from the first annotated {@link Field} found with the passed
   * annotation type. If multiple fields are annotated with this type, the first field found with
   * the annotation from a call to {@link Class#getDeclaredFields()} will be used.
   *
   * @param annotationClass The class of the annotation to search for.
   * @param clazz The class containing the field.
   * @param <T> Upper bounded {@link Annotation} type.
   * @return If annotation(s) where found they are returned in this list, otherwise an empty list.
   */
  static <T extends Annotation> List<T> getFieldAnnotations(
      Class<T> annotationClass, Class<?> clazz) {

    final List<T> foundAnnotations = new ArrayList<>();

    for (Field field : clazz.getDeclaredFields()) {
      final T annotation = field.getAnnotation(annotationClass);
      if (annotation != null) {
        foundAnnotations.add(annotation);
      }

      final T[] annotations = field.getAnnotationsByType(annotationClass);
      foundAnnotations.addAll(Arrays.asList(annotations));

      if (foundAnnotations.size() > 0) {
        return foundAnnotations;
      }
    }
    return foundAnnotations;
  }

  static class MethodAnnotationPair<T extends Annotation> {
    Method method;
    List<T> annotations;

    MethodAnnotationPair(Method method, List<T> annotations) {
      this.method = method;
      this.annotations = annotations;
    }
  }

  /**
   * Attempts to retreive annotations from the first annotated {@link Method} found with the passed
   * annotation type.
   *
   * @param annotationClass The class of the annotation to search for.
   * @param clazz The class containing the method.
   * @param <T> Upper bounded {@link Annotation} type.
   * @return If annotation(s) where found they are returned in this list, otherwise an empty list.
   */
  static <T extends Annotation> Optional<MethodAnnotationPair<T>> getMethodAnnotations(
      Class<T> annotationClass, Class<?> clazz) {
    final List<T> foundAnnotations = new ArrayList<>();

    for (Method m : clazz.getDeclaredMethods()) {
      final T annotation = m.getAnnotation(annotationClass);

      if (annotation != null) {
        foundAnnotations.add(annotation);
      }

      final T[] annotations = m.getAnnotationsByType(annotationClass);
      foundAnnotations.addAll(Arrays.asList(annotations));

      if (foundAnnotations.size() > 0) {
        if (!Modifier.isStatic(m.getModifiers())) {
          throw new InvalidMethodException("Annotations declared at methods must be static.");
        }
        return Optional.of(new MethodAnnotationPair<>(m, foundAnnotations));
      }
    }
    return Optional.empty();
  }

  /**
   * Attempts to the default values for a field, given some possible names for the field. (This is
   * required as its possible for each option to have many aliases, all of which could be chosen for
   * the field name)
   *
   * @param clazz The class where the field is declared.
   * @param possibleNames A list of possible names for the field name.
   * @return If the field is found, its value will be returned, otherwise null. It could be noted
   *     that default fields could be set to null as their default value, however it makes sense to
   *     advise users against this. If default values are to be null then their field shouldn't be
   *     declared. (This may also need to be investigated, what if users want to have fields with
   *     the same name as some option and it happens to be null. Maybe an annotation will suffice to
   *     solve this problem so we only look at annotated fields.)
   */
  static Object getDefaultFieldValue(Class<?> clazz, List<String> possibleNames) {
    for (String possibleName : possibleNames) {
      if (possibleName.startsWith("--") && possibleName.length() > 2) {
        possibleName = possibleName.substring(2, possibleName.length());
      } else if (possibleName.startsWith("-") && possibleName.length() > 1) {
        possibleName = possibleName.substring(1, possibleName.length());
      }

      try {
        final Field declaredField = clazz.getDeclaredField(possibleName);
        declaredField.setAccessible(true);
        final Object value = declaredField.get(null);
        if (value != null) {
          return value;
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        if (possibleNames.indexOf(possibleName) == possibleNames.size() - 1) {
          return null;
        }
      }
    }
    return null;
  }
}

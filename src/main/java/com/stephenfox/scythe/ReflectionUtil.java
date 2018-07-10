package com.stephenfox.scythe;

import com.stephenfox.scythe.annotation.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        return Optional.of(new MethodAnnotationPair<>(m, foundAnnotations));
      }
    }
    return Optional.empty();
  }
}

package com.stephenfox.scythe.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Options.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Option {
  String name() default "";
  String help() default "No description provided.";
  Class type() default Object.class;
  boolean isFlag() default false;
}

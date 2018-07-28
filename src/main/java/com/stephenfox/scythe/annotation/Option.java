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

  String name();
  String help() default "";
  Class type() default String.class;
  boolean isFlag() default false;
  boolean required() default true;
  int order() default -1;
  boolean multiple() default false;
  int nargs() default 0;
}

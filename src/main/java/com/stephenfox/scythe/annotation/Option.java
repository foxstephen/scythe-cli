package com.stephenfox.scythe.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Options.class)
public @interface Option {
  String name() default "";
  String help() default "No description provided.";
}

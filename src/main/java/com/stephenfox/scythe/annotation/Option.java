package com.stephenfox.scythe.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
  String name() default "";
  String help() default "No description provided.";
}

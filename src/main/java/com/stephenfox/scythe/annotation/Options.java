package com.stephenfox.scythe.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * As many options can be declared this is the container type for such occurrences.
 *
 * @author Stephen Fox.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Options {
  Option[] value();
}

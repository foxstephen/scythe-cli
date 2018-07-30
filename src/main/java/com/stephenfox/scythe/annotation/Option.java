package com.stephenfox.scythe.annotation;

import com.stephenfox.scythe.Scythe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Options.class)
@Target({ElementType.FIELD, ElementType.METHOD}) // TODO: Add support for Type target level.
public @interface Option {

  /**
   * The name of an option. This will typically be the name the option will most likely be used by
   * e.g `--port`, `--host` etc.
   */
  String name();

  /**
   * Options can have aliases for it's name. For example the name of an option may be `--port` but
   * could have an alias as `-p` for brevity. An option can have many aliases. If an option does
   * have many aliases and the returned map is being used from a call to {@link Scythe#parse()} then
   * each alias will have an entry within the map.
   *
   * <pre>{@code
   * @Option(name="--environment", aliases={"--env", "-e"})
   * Object field;
   *
   * public static void main(String[] args) {
   *   Map<String, Object> parsed = Scythe.cli(Main.class).parse(args);
   *   parsed.get("--environment"); // Returns value.
   *   parsed.get("--env"); // Returns value.
   *   parsed.get("-e"); // Returns value.
   * }
   * }</pre>
   */
  String[] aliases() default {};

  /**
   * The help message is displayed anytime a user runs your application with `-h` or `--help`. This
   * should describe to purpose of the option.
   *
   * <p>TODO: Add an example of how a help message will be displayed.
   */
  String help() default "";

  /**
   * Every option must have a type in order for it to be correctly parsed from the command line
   * arguments or via environment variables.
   *
   * <p>Custom types are supported, however they must have a default constructor that will take a
   * {@code String} as an argument, where the value from the cli args will be passed.
   */
  Class type() default String.class;

  /**
   * Setting `isFlag` to true indicates to Scythe that the presence of an option indicates its
   * value. For example if there is a flag option called `--isReady` passed to the application the
   * presence of this flag indicates `true`. The absence of the flag indicated `false`.
   */
  boolean isFlag() default false;

  /**
   * Not all options are created equally, some options may be required and some may not be required.
   * By default all options are required and failure for the user to supply them will lead to an
   * error message. If the option is not required then this field should be marked false to stop
   * Scythe from throwing an error at the user.
   */
  boolean required() default true;

  /**
   * For options declared at a method level - indicating that the parsed options will be passed
   * through that method, it is necessary for those options to have an order. The order indicates
   * which sequence the options will be passed to the method. In the example below `--forename` will
   * be passed as the first argument and `--surname` will be passed as the second argument.
   *
   * <pre>{@code
   * @Option(name="--forename", order=1)
   * @Option(name="--surname", order=2)
   * public static void main(String forename, String surname) {
   *   System.out.println(forename);
   *   System.out.println(surname);
   * }
   *
   * }</pre>
   *
   * It is important to note that any method that has {@code Option} declared, must be static and
   * greater than or equal to 0.
   */
  int order() default -1;

  /**
   * A given option can be declared multiple times within the command line arguments. For example it
   * is often desirable to declare environment variables multiple times.
   *
   * <p>For example `--env DOCKER_HOST=127.0.0.1 --env DOCKER_PORT=2375`
   */
  boolean multiple() default false;

  /**
   * A given options can have multiple values or n arguments. Each argument is separated by a space.
   *
   * <p>For example `--dimensions 2.0 3.0`
   */
  int nargs() default 0;
}

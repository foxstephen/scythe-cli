package com.stephenfox.scythe;

import static org.junit.Assert.*;

import com.stephenfox.scythe.annotation.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ScytheTest {

  private static String[] args(String... args) {
    return args;
  }

  // --------------------------
  // Test flag.
  // --------------------------
  @Test
  public void testOptionFlagWithFlag() {
    final Object clazz =
        new Object() {
          @Option(name = "--is_robot", isFlag = true)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args("--is_robot"), clazz.getClass()).parse();
    assertTrue((Boolean) parse.get("--is_robot"));
  }

  @Test
  public void testOptionFlagWithNoFlag() {
    final Object clazz =
        new Object() {
          @Option(name = "--is_robot", isFlag = true)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args(), clazz.getClass()).parse();
    assertFalse((Boolean) parse.get("--is_robot"));
  }

  @Test
  public void testOptionFlagWithNothingPassed() {
    final Object clazz =
        new Object() {
          @Option(name = "--is_robot", isFlag = true)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args(), clazz.getClass()).parse();
    assertFalse((Boolean) parse.get("--is_robot"));
  }

  // --------------------------
  // Test multiple args.
  // --------------------------
  @SuppressWarnings("unchecked")
  @Test
  public void testOptionMultiple() {
    final Object clazz =
        new Object() {
          @Option(name = "--env", multiple = true)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--env", "HOST", "--env", "PORT"), clazz.getClass()).parse();

    assertEquals("HOST", ((List<String>) parse.get("--env")).get(0));
    assertEquals("PORT", ((List<String>) parse.get("--env")).get(1));
  }

  @SuppressWarnings("unchecked")
  @Test(expected = RequiredOptionException.class)
  public void testOptionMultipleNothingPassed() {
    final Object clazz =
        new Object() {
          @Option(name = "--env", multiple = true)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args(), clazz.getClass()).parse();

    assertEquals("HOST", ((List<String>) parse.get("--env")).get(0));
    assertEquals("PORT", ((List<String>) parse.get("--env")).get(1));
  }

  // --------------------------
  // Test nargs.
  // --------------------------
  @SuppressWarnings("unchecked")
  @Test
  public void testOptionNargsWithCorrectNumberOfArgs() {
    final Object clazz =
        new Object() {
          @Option(name = "--names", nargs = 2)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--names", "stephen steve"), clazz.getClass()).parse();
    assertEquals("stephen", ((List<String>) parse.get("--names")).get(0));
    assertEquals("steve", ((List<String>) parse.get("--names")).get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOptionNargsWithBlankArgs() {
    final Object clazz =
        new Object() {
          @Option(name = "--names", nargs = 2)
          private Object field;
        };

    Scythe.cli(args("--names", ""), clazz.getClass()).parse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOptionNargsWithTooManyArgs() {
    final Object clazz =
        new Object() {
          @Option(name = "--names", nargs = 2)
          private Object field;
        };

    Scythe.cli(args("--names", "stephen steve john"), clazz.getClass()).parse();
  }

  // ---------------------------------------------
  // Test required options.
  // ---------------------------------------------
  @Test
  public void testOptionRequiredNotRequired() {
    final Object clazz =
        new Object() {
          @Option(name = "--port", required = false)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args(), clazz.getClass()).parse();
    assertNull(parse.get("--port"));
  }

  @Test(expected = RequiredOptionException.class)
  public void testOptionRequiredWithNothingPassed() {
    final Object clazz =
        new Object() {
          @Option(name = "--names")
          private Object field;
        };

    Scythe.cli(args(), clazz.getClass()).parse();
  }

  // ---------------------------------------------
  // Test passing parsed args through method call.
  // ---------------------------------------------
  private static final List<String> parsedArgsFromMethodCall = new ArrayList<>(2);

  @Before
  public void before() {
    parsedArgsFromMethodCall.clear();
  }

  private static class Main1 {
    @Option(name = "--forename", order = 1)
    @Option(name = "--surname", order = 2)
    public static void main(String forename, String surname) {
      parsedArgsFromMethodCall.add(forename);
      parsedArgsFromMethodCall.add(surname);
    }
  }

  private static class Main2 {
    @Option(name = "--host")
    public static void mainWithNoSortOrder(String host) {
      parsedArgsFromMethodCall.add(host);
    }
  }

  private static class Main3 {
    @Option(name = "--host", order = -2)
    public static void mainWithNoSortOrder(String host) {
      parsedArgsFromMethodCall.add(host);
    }
  }

  @Test
  public void testParsedArgsMethodCall() {
    Scythe.cli(args("--forename", "Stephen", "--surname", "Fox"), Main1.class).parse();

    assertEquals(2, parsedArgsFromMethodCall.size());
    assertEquals("Stephen", parsedArgsFromMethodCall.get(0));
    assertEquals("Fox", parsedArgsFromMethodCall.get(1));
  }

  @Test(expected = InvalidMethodException.class)
  public void testParsedArgsMethodCallWithNonStaticMethod() {
    final Object clazz =
        new Object() {
          @Option(name = "--forename", order = 1)
          @Option(name = "--surname", order = 2)
          public void main(String forename, String surname) {}
        };
    Scythe.cli(args("--forename", "Stephen", "--surname", "Fox"), clazz.getClass()).parse();
  }

  @Test(expected = SortOrderException.class)
  public void testParsedArgsMethodCallDefinedSortOrder() {
    Scythe.cli(args("--host", "127.0.0.1"), Main2.class).parse();
  }

  @Test(expected = SortOrderException.class)
  public void testParsedArgsMethodCallInvalidSortOrder() {
    Scythe.cli(args("--host", "127.0.0.1"), Main3.class).parse();
  }
}

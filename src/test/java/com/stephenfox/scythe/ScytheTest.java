package com.stephenfox.scythe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.stephenfox.scythe.annotation.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
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

  // ---------------------------------------------
  // Test types.
  // ---------------------------------------------
  @Test
  public void testTypeString() {
    final Object clazz =
        new Object() {
          @Option(name = "--forename")
          @Option(name = "--surname")
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--forename", "Stephen", "--surname", "Fox"), clazz.getClass()).parse();
    assertEquals("Stephen", parse.get("--forename"));
    assertEquals("Fox", parse.get("--surname"));
  }

  @Test
  public void testTypeByte() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Byte.class)
          @Option(name = "--b", type = Byte.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1", "--b", "2"), clazz.getClass()).parse();
    assertEquals(((byte) 1), parse.get("--a"));
    assertEquals(((byte) 2), parse.get("--b"));
  }

  @Test
  public void testTypeShort() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Short.class)
          @Option(name = "--b", type = Short.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1", "--b", "2"), clazz.getClass()).parse();
    assertEquals(((short) 1), parse.get("--a"));
    assertEquals(((short) 2), parse.get("--b"));
  }

  @Test
  public void testTypeInteger() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Integer.class)
          @Option(name = "--b", type = Integer.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1", "--b", "2"), clazz.getClass()).parse();
    assertEquals(1, parse.get("--a"));
    assertEquals(2, parse.get("--b"));
  }

  @Test
  public void testTypeLong() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Long.class)
          @Option(name = "--b", type = Long.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1", "--b", "2"), clazz.getClass()).parse();
    assertEquals(1L, parse.get("--a"));
    assertEquals(2L, parse.get("--b"));
  }

  @Test
  public void testTypeFloat() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Float.class)
          @Option(name = "--b", type = Float.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1.0", "--b", "2"), clazz.getClass()).parse();
    assertEquals(1F, parse.get("--a"));
    assertEquals(2F, parse.get("--b"));
  }

  @Test
  public void testTypeDouble() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = Double.class)
          @Option(name = "--b", type = Double.class)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(args("--a", "1.0", "--b", "2"), clazz.getClass()).parse();
    assertEquals(1D, parse.get("--a"));
    assertEquals(2D, parse.get("--b"));
  }

  private static class CustomClass {
    private final String string;

    CustomClass(String string) {
      this.string = string;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CustomClass that = (CustomClass) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }
  }

  @Test
  public void testCustomType() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = CustomClass.class)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args("--a", "hello"), clazz.getClass()).parse();
    assertEquals(new CustomClass("hello"), parse.get("--a"));
  }

  // Currently this is not supported, make sure the correct exception is tested for.
  @SuppressWarnings("unchecked")
  @Test(expected = UnsupportedOperationException.class)
  public void testCustomTypeWithNargs() {
    final Object clazz =
        new Object() {
          @Option(name = "--a", type = CustomClass.class, nargs = 2)
          private Object field;
        };

    final Map<String, Object> parse = Scythe.cli(args("--a", "hello"), clazz.getClass()).parse();
    Scythe.cli(args("--a", "hello world"), clazz.getClass()).parse();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCustomTypeWithMultiOptions() {
    final Object clazz =
        new Object() {
          @Option(name = "--env", type = CustomClass.class, multiple = true)
          private Object field;
        };

    final Map<String, Object> parse =
        Scythe.cli(
                args("--env", "DOCKER_HOST=127.0.0.1", "--env", "DOCKER_PORT=2375"),
                clazz.getClass())
            .parse();
    assertEquals(
        new CustomClass("DOCKER_HOST=127.0.0.1"), ((List<CustomClass>) parse.get("--env")).get(0));
    assertEquals(
        new CustomClass("DOCKER_PORT=2375"), ((List<CustomClass>) parse.get("--env")).get(1));
  }

  // ---------------------------------------------
  // Test aliases.
  // ---------------------------------------------
  @Test
  public void testAliasesWithReturnedMap() {
    final Object clazz =
        new Object() {
          @Option(
              name = "--environment",
              aliases = {"--env", "-e"})
          private Object field;
        };

    final Map<String, Object> parse1 =
        Scythe.cli(args("--env", "DOCKER_HOST=127.0.0.1"), clazz.getClass()).parse();
    assertEquals("DOCKER_HOST=127.0.0.1", parse1.get("--environment"));
    assertEquals("DOCKER_HOST=127.0.0.1", parse1.get("--env"));
    assertEquals("DOCKER_HOST=127.0.0.1", parse1.get("-e"));
  }

  // ---------------------------------------------
  // Test default values.
  // ---------------------------------------------

  private static class Default {
    @Option(
        name = "--names",
        aliases = {"-n"},
        nargs = 2)
    @Option(
        name = "--environment",
        aliases = {"--env", "-e"})
    @Option(name = "-kv", type = HashMap.class)
    private Object options;

    private static String environment = "127.0.0.1";
    private static List<String> n =
        new ArrayList<String>() {
          {
            add("Stephen");
            add("John");
          }
        };
    private static Map<String, String> kv =
        new HashMap<String, String>() {
          {
            put("a", "1");
            put("b", "2");
          }
        };
  }

  @Test
  public void testDefaultValues() {
    final Map<String, Object> parse = Scythe.cli(args(), Default.class).parse();

    final List<String> n =
        new ArrayList<String>() {
          {
            add("Stephen");
            add("John");
          }
        };

    final Map<String, String> kv =
        new HashMap<String, String>() {
          {
            put("a", "1");
            put("b", "2");
          }
        };

    assertEquals(n, parse.get("-n"));
    assertEquals("127.0.0.1", parse.get("--environment"));
    assertEquals(kv, parse.get("-kv"));
  }

  private static class DefaultWrongNames {
    @Option(name = "--environment")
    private Object options;

    private static String wrongName = "127.0.0.1";
  }

  @Test(expected = RequiredOptionException.class)
  public void tetDefaultValuesWithIncorrectName() {
    final Map<String, Object> parse = Scythe.cli(args(), DefaultWrongNames.class).parse();
  }
}

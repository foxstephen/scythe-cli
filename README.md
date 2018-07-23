# scythe-cli

Scythe is a command line argument parser for Java.

### Documentation
- Commands
- Options
    - Supported types
    - Single value options
    - Multi value options
    - Multiple options
    - Flags
    - Required options
    - Default options
    - Option Ordering

The basic building block of the Scythe parser are options. To create an option use the `Option` annotation, the `Option` annotation can be declared at fields and methods. Depending on declaration site of the `Option` the option values parsed from the cli will be passed to your application in different ways. If the annotations are declared at a field then a call to Scyth will return a mapping of option names to the corresponding values. 

For example:

```java
class Main {

  @Option(name="--name")
  @Option(name="--age", type=Integer.class)
  Object options;
  
  // args = ["--name", "Stephen", "--age", "100"]
  public static void main(String[] args) {
    final Map<String, Object> values = Scyth.cli(args, Main.class).parse();        
    values.get("--name"); // Stephen
    values.get("--age"); // 100
  }
}
```

Alternatively the options can be declared at a method level, if options are declared at 
method level then the method they are declared at will be invoked with the options.

For example:

```java
class Main {
  
  // args = ["--name", "Stephen", "--age", "100"]
  public static void main(String[] args) {
    Scyth.cli(args, Main.class).parse();
  }
  
  @Option(name="--name")
  @Option(name="--age", type=Integer.class)
  public static void main(String name, Integer age) {
    System.out.println(name); // Stephen
    System.out.println(age); // 100
  }
}
```

### Supported type
Scythe supports `String` and all `Number` subtypes i.e. `Integer`, `Float`, `Double` etc.

### Single Value Options
Single value options are options that take a single value.

```java
@Option(name="--host")
@Option(name="--port", type=Integer.class)

$ opts --host 127.0.0.1 --port 8080
```

### Multi Value Options
```
TODO
```

### Multiple Options
Multiple options allow for the same option to be declared multiple times with different values. Scythe will pass values of these to your application inside a `java.util.List` class.
```java
@Option(name="--env", multiple=true)

$ multi --env DOCKER_HOST --env DOCKER_PORT
```

### Flags
Flags are determined either by the presence or absence of the value.

```java
@Option(name="isMethod", isFlag=true)
```

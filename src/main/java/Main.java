import com.stephenfox.scythe.Scythe;
import com.stephenfox.scythe.annotation.Option;

public class Main {
  //
  //  @Option(name = "--age", help = "The age of a person", type = Integer.class)
  //  @Option(name = "--gender", help = "The gender of the person")
  //    @Option(name = "--boobs", isFlag = true)
  //  Object cliOptions;

  public static void main(String[] args) {
    final String[] arrrgs =
        new String[] {" --age", "1", "--gender", "male", "--base", "100", "--boobs"};
    Scythe.cli(arrrgs, Main.class).parse();
  }

  @Option(name = "--age", help = "The age of a person", type = Integer.class)
  @Option(name = "--gender", help = "The gender of the person")
  @Option(name = "--base", type = Integer.class)
  @Option(name = "--boobs", isFlag = true)
  public static void main(Integer age, String gender, Integer base, Boolean flag) {
    System.out.println(age);
    System.out.println(gender);
    System.out.println(base);
    System.out.println(flag);
  }
}

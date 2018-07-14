import com.stephenfox.scythe.Scythe;
import com.stephenfox.scythe.annotation.Option;

public class Main {

  @Option(name = "-e", nargs = 3)
  Object cliOptions;

  public static void main(String[] args) {
    final String[] arrrgs = new String[] {"-e", "1 3 2"};
    System.out.println(Scythe.cli(arrrgs, Main.class).parse());
  }

  //
  //  @Option(name = "--age",     help = "The age of a person", type = Integer.class, order = 0)
  //  @Option(name = "--gender", help = "The gender of the person", order = 1)
  //  @Option(name = "--base", type = Integer.class, order = 2)
  //  @Option(name = "--isHuman", isFlag = true, order = 3)
  //  @Option(name = "--app", required = false, isFlag = true, order = 4)
  //  public static void main(Integer age, String gender, Integer base, Boolean isHuman, Boolean
  // app) {
  //    System.out.println(age);
  //    System.out.println(gender);
  //    System.out.println(base);
  //    System.out.println(isHuman);
  //    System.out.println(app);
  //  }
}

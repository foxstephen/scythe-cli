import com.stephenfox.scythe.Scythe;
import com.stephenfox.scythe.annotation.Option;

import java.util.Map;

public class Main {


  @Option(name="--age", help = "The age of a person")
  @Option(name="--gender", help = "The gender of the person")
  Object cliOptions;

  public static void main(String[] args) {
    final String[] arrrrgs = new String[]{" --age", " stephen ", "--gender", "male"};
    final Map<String, Object> cli = Scythe.cli(Main.class, arrrrgs);
    System.out.println(cli);
  }
}

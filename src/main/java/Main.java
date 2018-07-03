import com.stephenfox.scythe.Scythe;
import com.stephenfox.scythe.annotation.Option;

import java.util.Map;

public class Main {


  @Option(name="--age", help = "The age of a person")
  Object cliOptions;

  public static void main(String[] args) {
    final String[] arrrrgs = new String[]{" --age", " stephen "};
    final Map<String, Object> cli = Scythe.cli(Main.class, arrrrgs);

  }
}

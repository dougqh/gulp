import com.azul.gulp.text.GulpText;
import com.azul.gulp.text.GulpTextLog;

public class TextDemo {
  @GulpText.LineMatcher(regex="Hello, (.+)")
  static final class Hello {
	private final String person;
	
	public Hello(String person) {
	  this.person = person;
	}
	
	@Override
	public final String toString() {
	  return this.person;
	}
  }
  public static void main(String[] args) {
	GulpTextLog log = GulpText.gulp(TextDemo.class.getResource("demo.txt"));
	
	log.select(Hello.class).print();
  }
}

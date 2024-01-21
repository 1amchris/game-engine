package shared.toolbox;

import java.io.File;
import java.util.regex.Pattern;

public class Directories {
    public static String fromPath(String... elements) {
        return (String.join(File.separator, elements) + File.separator)
                .replaceAll(Pattern.quote(File.separator) + "+", File.separator);
    }
}

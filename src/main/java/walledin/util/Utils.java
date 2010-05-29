package walledin.util;

import java.net.URL;


public class Utils {
	public static URL getClasspathURL(String filename) {
		return ClassLoader.getSystemResource(filename);
	}
}

package walledin.util;


public class Utils {
	public static String getClasspathFilename(String filename) {
		return ClassLoader.getSystemResource(filename).getPath();
	}
}

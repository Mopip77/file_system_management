package experiment.os.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class GlobalProperties {
    private GlobalProperties(){}

    public static Properties properties = new Properties();

    static {
        // load config file
        FileInputStream in = null;
        try {
            in = new FileInputStream("src/system.properties");
            properties.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public static Integer getInt(String propertyName) {
        return Integer.valueOf(properties.getProperty(propertyName));
    }
}

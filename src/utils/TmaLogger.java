package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TmaLogger {

    private static TmaLogger instance;
    private final Logger logger;

    public TmaLogger() {
        logger = Logger.getLogger(this.getClass());
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(Values.LOG4J_PROPERTIES.getValue()));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Values.class.getName()).log(Level.SEVERE, null, ex);
        }
        PropertyConfigurator.configure(props);
    }

    public static Logger get() {
        if (instance == null) {
            instance = new TmaLogger();
        }
        return instance.logger;
    }

}
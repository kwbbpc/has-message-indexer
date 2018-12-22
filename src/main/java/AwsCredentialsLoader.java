
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsCredentialsLoader {


    private final String accessKey;
    private final String secretKey;


    private static final Logger logger = LoggerFactory.getLogger(AwsCredentialsLoader.class);

    public AwsCredentialsLoader(String path) throws IOException{
        InputStream keys = this.getClass().getResourceAsStream(path);
        Properties props = new Properties();

        if(keys == null){
            logger.error("No aws keys found at {}", path);
        }

        logger.info("Loading aws keys from {}: {}", path, props.toString());
        props.load(keys);


        this.accessKey = props.getProperty("aws.access.key.id");
        this.secretKey = props.getProperty("aws.access.key.secret");

    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}

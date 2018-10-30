
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsCredentialsLoader {


    private final String accessKey;
    private final String secretKey;

    public AwsCredentialsLoader(String path) throws IOException{
        InputStream keys = this.getClass().getResourceAsStream(path);
        Properties props = new Properties();

        if(keys == null){
            System.err.println("No aws keys found!");
        }

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

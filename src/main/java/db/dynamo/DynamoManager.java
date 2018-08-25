package db.dynamo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import db.DataPipe;
import db.HumidityDao;
import db.TemperatureDao;
import org.joda.time.DateTime;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class DynamoManager implements DataPipe{


    private DynamoDB db;
    private Table temperatureTable;
    private Table humidityTable;


    public DynamoManager(){


        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

        this.db = new DynamoDB(client);

        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("NodeId").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("Timestamp").withAttributeType("N"));

        List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement().withAttributeName("NodeId").withKeyType(KeyType.HASH));
        keySchema.add(new KeySchemaElement().withAttributeName("Timestamp").withKeyType(KeyType.RANGE));

        ProvisionedThroughput pt = new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L);

        try {
            CreateTableRequest tempTableRequest = new CreateTableRequest()
                    .withTableName("temperatures").withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchema).withProvisionedThroughput(pt);
            this.temperatureTable = this.db.createTable(tempTableRequest);
        }catch (Exception e){
            System.err.println(e);
            this.temperatureTable = this.db.getTable("temperatures");
        }

        try{
            CreateTableRequest humidityTableRequest = new CreateTableRequest().withTableName("humidity")
                    .withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchema).withProvisionedThroughput(pt);
            this.humidityTable = this.db.createTable(humidityTableRequest);
        }catch (Exception e){
            System.err.println(e);
            this.humidityTable = this.db.getTable("humidity");
        }

    }

    public void saveTemperature(TemperatureDao temperature) throws JsonProcessingException {
        Item temp = new Item();

        temp.withPrimaryKey("nodeId", temperature.getSensorInfo().getAddress64bit());
        temp.withPrimaryKey("timestamp", new DateTime(temperature.getTimestamp()).toDateTimeISO().toString());
        temp.with("nodeInfo", JsonUtils.MAPPER.writeValueAsString(temperature.getSensorInfo()));


        String tempJson = JsonUtils.MAPPER.writeValueAsString(temperature);

        temp.withJSON("sensor", tempJson);

        UpdateItemSpec update = new UpdateItemSpec().withPrimaryKey(
                "nodeId", temperature.getSensorInfo().getAddress64bit(),
                "daystamp", new DateTime(temperature.getTimestamp()))
                .withUpdate

        temperatureTable.putItem(temp);
    }

    public void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException {
        Item humidity = new Item();
        humidity.withPrimaryKey("NodeId", humidityDao.getSensorInfo().getAddress64bit());
        humidity.withPrimaryKey("Timestamp", humidityDao.getTimestamp().toInstant().toEpochMilli());

        String humJson = JsonUtils.MAPPER.writeValueAsString(humidityDao);

        humidity.withJSON("sensor", humJson);

        humidityTable.putItem(humidity);
    }
}

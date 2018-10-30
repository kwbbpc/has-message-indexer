package db.dynamo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import db.DatabaseManager;
import db.model.humidity.HumidityDao;
import db.model.temp.TemperatureDao;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class DynamoManager implements DatabaseManager {


    private DynamoDB db;
    private Table temperatureTable;
    private Table humidityTable;


    public DynamoManager(BasicAWSCredentials creds){


        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(
                creds)).build();

        this.db = new DynamoDB(client);

        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("nodeId").withAttributeType("S"));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("createdDate").withAttributeType("S"));

        List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement().withAttributeName("nodeId").withKeyType(KeyType.HASH));
        keySchema.add(new KeySchemaElement().withAttributeName("createdDate").withKeyType(KeyType.RANGE));

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
        temp.withPrimaryKey("nodeId", temperature.getNodeId());
        temp.withPrimaryKey("createdDate", temperature.getCreatedDate().toDateTimeISO().toString());


        temp.withString("indexDate", temperature.getIndexedDate().toDateTimeISO().toString());
        temp.withJSON("nodeInfo", JsonUtils.MAPPER.writeValueAsString(temperature.getNodeInfo()));
        temp.withJSON("temperature", JsonUtils.MAPPER.writeValueAsString(temperature.getTemperature()));
        temperatureTable.putItem(temp);
    }

    public void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException {
        Item humidity = new Item();
        humidity.withPrimaryKey("nodeId", humidityDao.getNodeId());
        humidity.withPrimaryKey("createdDate", humidityDao.getCreatedDate().toDateTimeISO().toString());

        humidity.withString("indexDate", humidityDao.getIndexedDate().toDateTimeISO().toString());
        humidity.withJSON("nodeInfo", JsonUtils.MAPPER.writeValueAsString(humidityDao.getNodeInfo()));
        humidity.withJSON("humidity", JsonUtils.MAPPER.writeValueAsString(humidityDao.getHumidity()));

        humidityTable.putItem(humidity);
    }
}

package db.dynamo;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import db.DatabaseManager;
import db.model.humidity.HumidityDao;
import db.model.motion.MotionDetectionDao;
import db.model.temp.TemperatureDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class DynamoManager implements DatabaseManager {


    private DynamoDB db;
    private Table temperatureTable;
    private Table humidityTable;
    private Table motionDetectionTable;
    private Table sensorRegistrarTable;

    private static final Logger logger = LoggerFactory.getLogger(DynamoManager.class);

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
            logger.error("Error creating the temperature table: {}", e);
            this.temperatureTable = this.db.getTable("temperatures");
        }

        try{
            CreateTableRequest humidityTableRequest = new CreateTableRequest().withTableName("humidity")
                    .withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchema).withProvisionedThroughput(pt);
            this.humidityTable = this.db.createTable(humidityTableRequest);
        }catch (Exception e){
            logger.error("Error creating the humidity table: {}", e);
            this.humidityTable = this.db.getTable("humidity");
        }

        try{
            CreateTableRequest motionDetectionTableRequest = new CreateTableRequest().withTableName("motionDetection")
                    .withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchema).withProvisionedThroughput(pt);
            this.motionDetectionTable = this.db.createTable(motionDetectionTableRequest);
        }catch (Exception e){
            logger.error("Error creating the motion detection table: {}", e);
            this.motionDetectionTable = this.db.getTable("motionDetection");
        }

        try{
            CreateTableRequest tableRequest = new CreateTableRequest().withTableName("sensor-names")
                    .withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchema).withProvisionedThroughput(pt);
            this.sensorRegistrarTable = this.db.createTable(tableRequest);
        }catch (Exception e){
            logger.error("Error creating the sensor name table: {}", e);
            this.sensorRegistrarTable = this.db.getTable("sensor-names");
        }

    }


    @Override
    public void registerSensor(String nodeId, DateTime updateDate, final Integer messageId){

        try {
            {
                try {
                    //Set the created date conditionally
                    Map<String, String> expressionAttributeNames = new HashMap<String, String>();
                    expressionAttributeNames.put("#cd", "registeredDate");

                    Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
                    expressionAttributeValues.put(":registeredDate", updateDate.toDateTimeISO().toString());

                    UpdateItemOutcome outcome = sensorRegistrarTable.updateItem(
                            new PrimaryKey("nodeId", nodeId),
                            "set #cd = :registeredDate",
                            "attribute_not_exists(#cd)",
                            expressionAttributeNames,
                            expressionAttributeValues
                    );
                }catch (ConditionalCheckFailedException e){
                    logger.info("Got a new check in for sensor {}", nodeId);
                }catch (Exception e){
                    logger.error("Unexpected exception while updating the registered date for sensor {}: {}", nodeId, e);
                }

            }

            {
                ///Set the last updated time and insert messages
                Map<String, String> expressionAttributeNames = new HashMap<String, String>();
                expressionAttributeNames.put("#ts", "lastUpdated");
                expressionAttributeNames.put("#mt", "messageTypes");

                Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
                expressionAttributeValues.put(":messageTypes", new HashSet<Integer>(Arrays.asList(messageId)));
                expressionAttributeValues.put(":lastUpdated", updateDate.toDateTimeISO().toString());

                UpdateItemOutcome outcome = sensorRegistrarTable.updateItem(
                        new PrimaryKey("nodeId", nodeId),
                        "set #ts = :lastUpdated add #mt :messageTypes",
                        expressionAttributeNames,
                        expressionAttributeValues
                );
            }
        }catch (Exception e){
            logger.error("Error while registering sensor update: {}", e);
        }

    }



    @Override
    public void saveTemperature(TemperatureDao temperature) throws JsonProcessingException {
        Item temp = new Item();
        temp.withPrimaryKey("nodeId", temperature.getNodeId());
        temp.withPrimaryKey("createdDate", temperature.getCreatedDate().toDateTimeISO().toString());


        temp.withString("indexDate", temperature.getIndexedDate().toDateTimeISO().toString());
        temp.withJSON("nodeInfo", JsonUtils.MAPPER.writeValueAsString(temperature.getNodeInfo()));
        temp.withJSON("temperature", JsonUtils.MAPPER.writeValueAsString(temperature.getTemperature()));
        temperatureTable.putItem(temp);
    }

    @Override
    public void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException {
        Item humidity = new Item();
        humidity.withPrimaryKey("nodeId", humidityDao.getNodeId());
        humidity.withPrimaryKey("createdDate", humidityDao.getCreatedDate().toDateTimeISO().toString());

        humidity.withString("indexDate", humidityDao.getIndexedDate().toDateTimeISO().toString());
        humidity.withJSON("nodeInfo", JsonUtils.MAPPER.writeValueAsString(humidityDao.getNodeInfo()));
        humidity.withJSON("humidity", JsonUtils.MAPPER.writeValueAsString(humidityDao.getHumidity()));

        humidityTable.putItem(humidity);
    }

    @Override
    public void saveMotionDetection(MotionDetectionDao motionDetectionDao) throws JsonProcessingException {
        Item motion = new Item();
        motion.withPrimaryKey("nodeId", motionDetectionDao.getNodeId());
        motion.withPrimaryKey("createdDate", motionDetectionDao.getCreatedDate().toDateTimeISO().toString());

        motion.withString("indexDate", motionDetectionDao.getIndexedDate().toDateTimeISO().toString());
        motion.withJSON("nodeInfo", JsonUtils.MAPPER.writeValueAsString(motionDetectionDao.getNodeInfo()));
        motion.withJSON("expectedDistanceCm", JsonUtils.MAPPER.writeValueAsString(motionDetectionDao.getExpectedDistanceCm()));
        motion.withJSON("triggeredDistanceCm", JsonUtils.MAPPER.writeValueAsString(motionDetectionDao.getTriggeredDistanceCm()));

        motionDetectionTable.putItem(motion);
    }


}

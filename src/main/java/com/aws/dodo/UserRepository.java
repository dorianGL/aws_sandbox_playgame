package com.aws.dodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class UserRepository {

    private final CloudWatchLogService cloudWatchLogService;
    private static final String TABLE_NAME = "User";
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final DynamoDbTable<User> usersTable;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, CloudWatchLogService cloudWatchLogService) {
        this.cloudWatchLogService = cloudWatchLogService;
        this.usersTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(User.class));
    }

    public User save(User user, String requestId) {
        long startTime = System.currentTimeMillis();
        logger.info("Saving user with ID: {}", user.getUserId());
        try {
            usersTable.putItem(user);
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("PutItem", TABLE_NAME, user.getUserId(), true, duration);
            return user;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("PutItem", TABLE_NAME, user.getUserId(), false, duration);
            throw e;
        }
    }

    public Optional<User> findById(String userId, String requestId) {
        long startTime = System.currentTimeMillis();
        try {
            User user = usersTable.getItem(r -> r.key(k -> k.partitionValue(userId)));
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("GetItem", TABLE_NAME, userId, user != null, duration);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("GetItem", TABLE_NAME, userId, false, duration);
            throw e;
        }
    }

    public void delete(String userId, String requestId) {
        long startTime = System.currentTimeMillis();
        try {
            usersTable.deleteItem(r -> r.key(k -> k.partitionValue(userId)));
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("DeleteItem", TABLE_NAME, userId, true, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("DeleteItem", TABLE_NAME, userId, false, duration);
            throw e;
        }
    }

    public void update(User user, String requestId) {
        long startTime = System.currentTimeMillis();
        try {
            usersTable.updateItem(user);
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("UpdateItem", TABLE_NAME, user.getUserId(), true, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logDynamoDBOperation("UpdateItem", TABLE_NAME, user.getUserId(), false, duration);
            throw e;
        }
    }
}

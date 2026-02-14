package com.aws.dodo;

import com.aws.dodo.dto.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final CloudWatchLogService cloudWatchLogService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserRepository userRepository, CloudWatchLogService cloudWatchLogService) {
        this.userRepository = userRepository;
        this.cloudWatchLogService = cloudWatchLogService;
    }

    public User createUser(UserDto userDto, String requestId) {
        long startTime = System.currentTimeMillis();
        logger.info("Creating user with name: {} and email: {}", userDto.name(), userDto.email());
        cloudWatchLogService.logOperationStart("CREATE_USER", "NEW", requestId);

        try {
            User user = userDto.toEntity();
            User createdUser = userRepository.save(user, requestId);

            SnsClient snsClient = SnsClient.builder()
                    .region(Region.EU_WEST_3)
                    .build();
            String message = objectMapper.writeValueAsString(createdUser);
            logger.info("envoi du message dans SQS : {}", message);
            pubTopic(snsClient, message, "arn:aws:sns:eu-west-3:225578988341:userTopic");
            snsClient.close();
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logOperationSuccess("CREATE_USER", createdUser.getUserId(), requestId, duration);
            return createdUser;
        } catch (JsonProcessingException e) {
            cloudWatchLogService.logOperationError("CREATE_USER", "UNKNOWN", requestId, e);
            throw new RuntimeException(e);
        }
    }

    public static void pubTopic(SnsClient snsClient, String message, String topicArn) {

        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .topicArn(topicArn)
                    .build();

            PublishResponse result = snsClient.publish(request);
            System.out.println(result.messageId() + " Message sent. Status is " + result.sdkHttpResponse().statusCode());

        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public Optional<User> getUserById(String userId, String requestId) {
        long startTime = System.currentTimeMillis();
        cloudWatchLogService.logOperationStart("GET_USER", userId, requestId);

        try {
            Optional<User> user = userRepository.findById(userId, requestId);
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logOperationSuccess("GET_USER", userId, requestId, duration);
            return user;
        } catch (Exception e) {
            cloudWatchLogService.logOperationError("GET_USER", userId, requestId, e);
            throw e;
        }
    }

    public User updateUser(@NonNull String userId, UserDto userDto, String requestId) {
        long startTime = System.currentTimeMillis();
        cloudWatchLogService.logOperationStart("UPDATE_USER", userId, requestId);

        try {
            User userDetails = userDto.toEntity();
            return userRepository.findById(userId, requestId)
                    .map(user -> {
                        User newUser = new User(userId, userDetails.getName(), userDetails.getEmail(), userDetails.getPhone(),
                                user.getCreatedAt(), System.currentTimeMillis());
                        userRepository.update(newUser, requestId);
                        return user;
                    })
                    .orElseThrow(() -> {
                        cloudWatchLogService.logValidationError("userId", "User not found", requestId);
                        return new RuntimeException("Utilisateur non trouvé : " + userId);
                    });
        } catch (Exception e) {
            cloudWatchLogService.logOperationError("UPDATE_USER", userId, requestId, e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Update duration: {}ms", duration);
        }
    }

    public void deleteUser(String userId, String requestId) {
        long startTime = System.currentTimeMillis();
        cloudWatchLogService.logOperationStart("DELETE_USER", userId, requestId);

        try {
            userRepository.delete(userId, requestId);
            long duration = System.currentTimeMillis() - startTime;
            cloudWatchLogService.logOperationSuccess("DELETE_USER", userId, requestId, duration);
        } catch (Exception e) {
            cloudWatchLogService.logOperationError("DELETE_USER", userId, requestId, e);
            throw e;
        }
    }
}

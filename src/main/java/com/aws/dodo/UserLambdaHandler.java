package com.aws.dodo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.aws.dodo.dto.CreateUserRequestDto;
import com.aws.dodo.dto.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class UserLambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(UserLambdaHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConfigurableApplicationContext applicationContext;
    private static final UserService userService;

    static {
        applicationContext = SpringApplication.run(FirstLambdaApplication.class);
        userService = applicationContext.getBean(UserService.class);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        logger.info("Requête reçue - ID: {}", context.getAwsRequestId());
        logger.info("Liste des champs du JSON : {}", input.keySet());
        logger.info("Liste des valeurs du JSON : {}", input.values());

        try {
            CreateUserRequestDto request = objectMapper.convertValue(input, CreateUserRequestDto.class);
            Optional<UserDto> body = Optional.ofNullable(request.body());

            return routeRequest(request.httpMethod(), request.path(), body, context);
        } catch (Exception e) {
            logger.error("Erreur lors du traitement", e);
            return errorResponse(500, "Erreur interne du serveur");
        }
    }

    private Map<String, Object> routeRequest(String method, String path, Optional<UserDto> body, Context context) {
        String[] pathParts = path.split("/");

        return switch (method) {
            case "POST" -> body.map(userBody -> createUser(userBody, context.getAwsRequestId())).orElseGet(() -> errorResponse(400, "Body manquant"));
            case "GET" -> getUser(pathParts[2], context.getAwsRequestId());
            case "PUT" -> body.map(userBody ->  updateUser(userBody, context.getAwsRequestId())).orElseGet(() -> errorResponse(400, "Body manquant"));
            case "DELETE" -> deleteUser(pathParts[2], context.getAwsRequestId());
            default -> errorResponse(404, "Endpoint non trouvé");
        };
    }

    private Map<String, Object> createUser(UserDto userDto, String requestId) {
        String userAsString = null;
        try {
            userAsString = objectMapper.writeValueAsString(userDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        logger.info("Création d'un utilisateur: {}", userAsString);
        User createdUser = userService.createUser(userDto, requestId);
        return successResponse(201, createdUser);
    }

    private Map<String, Object> getUser(String userId, String requestId) {
        return userService.getUserById(userId, requestId)
                .map(user -> successResponse(200, user))
                .orElseGet(() -> errorResponse(404, "Utilisateur non trouvé"));
    }

    private Map<String, Object> updateUser(UserDto userDetails, String requestId) {
        User updatedUser = userService.updateUser(userDetails.userId(), userDetails, requestId);
        return successResponse(200, updatedUser);
    }

    private Map<String, Object> deleteUser(String userId, String requestId) {
        userService.deleteUser(userId, requestId);
        return successResponse(204, null);
    }

    private Map<String, Object> successResponse(int statusCode, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("body", data != null ? objectMapper.convertValue(data, Map.class) : null);
        return response;
    }

    private Map<String, Object> errorResponse(int statusCode, String message) {
        return Map.of("statusCode", statusCode, "body", Map.of("message", message));
    }
}


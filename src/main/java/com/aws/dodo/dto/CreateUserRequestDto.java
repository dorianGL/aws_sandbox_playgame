package com.aws.dodo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record CreateUserRequestDto(
        @JsonProperty("body")
        UserDto body,

        @JsonProperty("resource")
        String resource,

        @JsonProperty("path")
        String path,

        @JsonProperty("httpMethod")
        String httpMethod,

        @JsonProperty("isBase64Encoded")
        boolean isBase64Encoded,

        @JsonProperty("queryStringParameters")
        Map<String, String> queryStringParameters,

        @JsonProperty("multiValueQueryStringParameters")
        Map<String, List<String>> multiValueQueryStringParameters,

        @JsonProperty("pathParameters")
        Map<String, String> pathParameters,

        @JsonProperty("stageVariables")
        Map<String, String> stageVariables,

        @JsonProperty("headers")
        Map<String, String> headers,

        @JsonProperty("multiValueHeaders")
        Map<String, List<String>> multiValueHeaders,

        @JsonProperty("requestContext")
        RequestContextDto requestContext
) {
}


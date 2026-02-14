package com.aws.dodo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestContextDto(
        @JsonProperty("accountId")
        String accountId,

        @JsonProperty("resourceId")
        String resourceId,

        @JsonProperty("stage")
        String stage,

        @JsonProperty("requestId")
        String requestId,

        @JsonProperty("requestTime")
        String requestTime,

        @JsonProperty("requestTimeEpoch")
        long requestTimeEpoch,

        @JsonProperty("identity")
        IdentityDto identity,

        @JsonProperty("path")
        String path,

        @JsonProperty("resourcePath")
        String resourcePath,

        @JsonProperty("httpMethod")
        String httpMethod,

        @JsonProperty("apiId")
        String apiId,

        @JsonProperty("protocol")
        String protocol
) {
}

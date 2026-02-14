package com.aws.dodo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IdentityDto(
        @JsonProperty("cognitoIdentityPoolId")
        String cognitoIdentityPoolId,

        @JsonProperty("accountId")
        String accountId,

        @JsonProperty("cognitoIdentityId")
        String cognitoIdentityId,

        @JsonProperty("caller")
        String caller,

        @JsonProperty("accessKey")
        String accessKey,

        @JsonProperty("sourceIp")
        String sourceIp,

        @JsonProperty("cognitoAuthenticationType")
        String cognitoAuthenticationType,

        @JsonProperty("cognitoAuthenticationProvider")
        String cognitoAuthenticationProvider,

        @JsonProperty("userArn")
        String userArn,

        @JsonProperty("userAgent")
        String userAgent,

        @JsonProperty("user")
        String user
) {
}

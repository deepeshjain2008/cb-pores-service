package com.igot.cb.pores.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.exceptions.CustomException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayloadValidationTest {

    private final PayloadValidation payloadValidation = new PayloadValidation();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validatePayload_validPayload_doesNotThrow() {
        JsonNode payload = objectMapper.createObjectNode().put("designation", "Engineer");

        assertDoesNotThrow(() ->
                payloadValidation.validatePayload(Constants.DESIGNATION_PAYLOAD_VALIDATION, payload));
    }

    @Test
    void validatePayload_missingRequiredField_throwsCustomException() {
        JsonNode payload = objectMapper.createObjectNode().put("description", "missing designation field");

        CustomException exception = assertThrows(CustomException.class, () ->
                payloadValidation.validatePayload(Constants.DESIGNATION_PAYLOAD_VALIDATION, payload));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatusCode());
    }
}

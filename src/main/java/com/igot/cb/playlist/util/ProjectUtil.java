package com.igot.cb.playlist.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.igot.cb.pores.util.ApiRespParam;
import com.igot.cb.pores.util.ApiResponse;
import com.igot.cb.pores.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

public class ProjectUtil {

  private ProjectUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static ApiResponse createDefaultResponse(String api) {
    ApiResponse response = new ApiResponse();
    response.setId(api);
    response.setVer(Constants.API_VERSION_1);
    response.setParams(new ApiRespParam(UUID.randomUUID().toString()));
    response.getParams().setStatus(Constants.SUCCESS);
    response.setResponseCode(HttpStatus.OK);
    response.setTs(DateTime.now().toString());
    return response;
  }

  public static Map<String, String> getDefaultHeadrs(String userAuthToken) {
    return Map.of(
            Constants.X_AUTH_TOKEN, userAuthToken,
            Constants.CONTENT_TYPE, Constants.APPLICATION_JSON
    );
  }
  public static void errorResponse(ApiResponse response, String errorMessage, HttpStatus httpStatus) {
        response.setResponseCode(httpStatus);
        response.getParams().setErrMsg(errorMessage);
        response.getParams().setStatus(Constants.FAILED);
    }

  public static Object toJavaObject(JsonNode jsonNode) {
    if (jsonNode.isObject()) {
      Map<String, Object> map = new HashMap<>();
      jsonNode.fields().forEachRemaining(entry -> map.put(entry.getKey(), toJavaObject(entry.getValue())));
      return map;
    } else if (jsonNode.isArray()) {
      List<Object> list = new ArrayList<>();
      jsonNode.forEach(element -> list.add(toJavaObject(element)));
      return list;
    } else {
      return jsonNode.asText();
    }
  }
}
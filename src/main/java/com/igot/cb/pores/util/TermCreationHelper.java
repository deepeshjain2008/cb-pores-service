package com.igot.cb.pores.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.Service.OutboundRequestHandlerServiceImpl;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.playlist.util.ProjectUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TermCreationHelper {

  private final CbServerProperties cbServerProperties;
  private final OutboundRequestHandlerServiceImpl outboundRequestHandlerServiceImpl;
  private final ObjectMapper objectMapper;

  public void setTermFailure(ApiResponse response, String errMsg, HttpStatus status) {
    response.getParams().setErr(errMsg);
    response.setResponseCode(status);
    response.getParams().setStatus(Constants.FAILED);
  }

  public void handleTermReadResponse(ApiResponse response, ApiResponse readResponse, JsonNode request,
          String name, String refId, String framework, String category, String notFoundMessage,
          String createLabel, String updateLabel, String titleFieldKey,
          Function<JsonNode, CustomResponse> updateFn) {
    if (readResponse == null) {
      setTermFailure(response, notFoundMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    } else if (HttpStatus.NOT_FOUND.equals(readResponse.getResponseCode())) {
      createTermViaOdcs(response, request, name, refId, framework, category, createLabel, updateLabel,
              titleFieldKey, updateFn);
    } else if (HttpStatus.CONFLICT.equals(readResponse.getResponseCode())) {
      String errMsg = Constants.TERM_CREATION_NOT_POSSIBLE + refId;
      log.error(errMsg);
      setTermFailure(response, errMsg, HttpStatus.BAD_REQUEST);
    } else {
      log.error("Failed to create the {} with name: {}", createLabel, refId);
      setTermFailure(response, "Failed to create.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  private void createTermViaOdcs(ApiResponse response, JsonNode request, String name, String refId,
          String framework, String category, String createLabel, String updateLabel, String titleFieldKey,
          Function<JsonNode, CustomResponse> updateFn) {
    Map<String, Object> reqBody = new HashMap<>();
    request.fields().forEachRemaining(entry -> reqBody.put(entry.getKey(), ProjectUtil.toJavaObject(entry.getValue())));
    Map<String, Object> termReq = new HashMap<>();
    termReq.put(Constants.TERM, reqBody);
    Map<String, Object> createReq = new HashMap<>();
    createReq.put(Constants.REQUEST, termReq);
    StringBuilder strUrl = new StringBuilder(cbServerProperties.getKnowledgeMS());
    strUrl.append(cbServerProperties.getOdcsTermCrete()).append("?framework=")
            .append(framework).append("&category=")
            .append(category);
    Map<String, Object> termResponse = (Map<String, Object>) outboundRequestHandlerServiceImpl.fetchResultUsingPost(strUrl.toString(),
            createReq);
    if (termResponse == null
            || !Constants.OK.equalsIgnoreCase((String) termResponse.get(Constants.RESPONSE_CODE))) {
      log.error("Failed to create the {} with name: {}", createLabel, refId);
      setTermFailure(response, "Failed to create the " + createLabel, HttpStatus.INTERNAL_SERVER_ERROR);
      return;
    }
    Map<String, Object> resultMap = (Map<String, Object>) termResponse.get(Constants.RESULT);
    List<String> termIdentifier = (List<String>) resultMap.getOrDefault(Constants.NODE_ID, "");
    log.info("Created {} successfully with name: {}", createLabel, refId);
    log.info("termIdentifier : {}", termIdentifier);
    Map<String, Object> reqBodyMap = new HashMap<>();
    reqBodyMap.put(Constants.ID, refId);
    reqBodyMap.put(titleFieldKey, name);
    reqBodyMap.put(Constants.REF_NODES, termIdentifier);
    CustomResponse updateResponse = updateFn.apply(objectMapper.valueToTree(reqBodyMap));
    if (updateResponse.getResponseCode() != HttpStatus.OK) {
      log.error("Failed to update {}: {}", updateLabel, response.getParams().getErr());
      setTermFailure(response, "Failed to update " + updateLabel + ".", HttpStatus.INTERNAL_SERVER_ERROR);
    } else {
      response.getResult().put(Constants.NODE_ID, termIdentifier);
    }
  }
}

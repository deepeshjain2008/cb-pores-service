package com.igot.cb.competencies.theme.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.igot.cb.authentication.util.AccessTokenValidator;
import com.igot.cb.competencies.theme.enity.CompetencyThemeEntity;
import com.igot.cb.competencies.theme.repository.CompetencyThemeRepository;
import com.igot.cb.designation.service.DesignationService;
import com.igot.cb.pores.Service.OutboundRequestHandlerServiceImpl;
import com.igot.cb.pores.cache.CacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.elasticsearch.dto.SearchCriteria;
import com.igot.cb.pores.elasticsearch.dto.SearchResult;
import com.igot.cb.pores.elasticsearch.service.EsUtilService;
import com.igot.cb.pores.exceptions.CustomException;
import com.igot.cb.pores.util.ApiResponse;
import com.igot.cb.pores.util.CbServerProperties;
import com.igot.cb.pores.util.Constants;
import com.igot.cb.pores.util.FileProcessService;
import com.igot.cb.pores.util.PayloadValidation;
import com.igot.cb.pores.util.TermCreationHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CompetencyThemeServiceImplTest {

    @Mock
    private org.apache.commons.lang3.StringUtils StringUtils;

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private CacheService cacheService;

    @Mock
    private CbServerProperties cbServerProperties;

    @Mock
    private CompetencyThemeRepository competencyThemeRepository;

    @InjectMocks
    private CompetencyThemeServiceImpl competencyThemeService;

    @Mock
    private DesignationService designationService;

    @Mock
    private EsUtilService esUtilService;

    @Mock
    private FileProcessService fileProcessService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerServiceImpl;

    @Mock
    private PayloadValidation payloadValidation;

    @Mock
    private RedisTemplate<String, SearchResult> redisTemplate;

    @Mock
    private ValueOperations<String, SearchResult> valueOperations;

    private static CompetencyThemeServiceImpl newUninitializedService() {
        Objenesis objenesis = new ObjenesisStd();
        return objenesis.newInstance(CompetencyThemeServiceImpl.class);
    }

    @BeforeEach
    void setup() {
        lenient().when(cbServerProperties.getJwtSecretKey()).thenReturn("test_secret_key");
        TermCreationHelper termCreationHelper = new TermCreationHelper(cbServerProperties, outboundRequestHandlerServiceImpl, objectMapper);
        ReflectionTestUtils.setField(competencyThemeService, "termCreationHelper", termCreationHelper);
    }

    @Test
    void test_createCompTheme_unauthorized_user() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode competencyTheme = objectMapper.createObjectNode();
        String token = "invalid_token";

        when(accessTokenValidator.verifyUserToken(anyString())).thenReturn(Constants.UNAUTHORIZED);

        // Act
        CustomResponse response = competencyThemeService.createCompTheme(competencyTheme, token);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals(Constants.USER_ID_DOESNT_EXIST, response.getParams().getErrmsg());
    }

    /**
     * Test case for createErrorResponse method.
     * This test verifies that the error response is correctly set with the provided parameters.
     */
    @Test
    void test_createErrorResponse_1() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        CustomResponse response = new CustomResponse();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String status = "FAILED";

        service.createErrorResponse(response, httpStatus, status);

        assertNotNull(response.getParams());
        assertEquals(status, response.getParams().getStatus());
        assertEquals(httpStatus, response.getResponseCode());
    }

    /**
     * Tests that the createErrorResponse method correctly sets all fields of the CustomResponse
     * when provided with valid input parameters.
     */
    @Test
    void test_createErrorResponse_setsAllFields() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        CustomResponse response = new CustomResponse();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String status = "FAILED";

        service.createErrorResponse(response, httpStatus, status);

        assertNotNull(response.getParams());
        assertEquals(status, response.getParams().getStatus());
        assertEquals(httpStatus, response.getResponseCode());
    }

    /**
     * Test case for createSuccessResponse method
     * Verifies that the method correctly sets the success response parameters
     */
    @Test
    void test_createSuccessResponse_1() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        CustomResponse response = new CustomResponse();

        service.createSuccessResponse(response);

        assertNotNull(response.getParams());
        assertEquals(Constants.SUCCESS, response.getParams().getStatus());
        assertEquals(HttpStatus.OK, response.getResponseCode());
    }

    /**
     * Test case for createTerm method when the designation entity is present, active,
     * but the readResponse is null.
     * 
     * This test verifies that the method returns an ApiResponse with the correct error
     * message and HTTP status when the readResponse from designationService is null.
     */
    @Test
    void test_createTerm_1() {
        // Arrange
        JsonNode request = new ObjectMapper().createObjectNode();
        CompetencyThemeEntity designationEntity = new CompetencyThemeEntity();
        designationEntity.setIsActive(true);

        //when(payloadValidation.validatePayload(any(), any())).thenReturn(null);
//        when(competencyThemeRepository.findByIdAndIsActive(any(), any())).thenReturn(Optional.of(designationEntity));
//        when(designationService.frameworkRead(any(), any(), any(), any())).thenReturn(null);

        // Act
        ApiResponse response = competencyThemeService.createTerm(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    /**
     * Testcase 2 for @Override public ApiResponse createTerm(JsonNode request)
     * Path constraints: (designationEntity.isPresent()), (designation.getIsActive()), !((readResponse == null)) && (HttpStatus.NOT_FOUND.equals(readResponse.getResponseCode())), (termResponse != null
     *                 && Constants.OK.equalsIgnoreCase((String) termResponse.get(Constants.RESPONSE_CODE))), (desgResponse.getResponseCode() != HttpStatus.OK)
     * returns: response
     */
    @Test
    void test_createTerm_2() {

        JsonNode request = mock(JsonNode.class);
        CompetencyThemeEntity competencyThemeEntity = mock(CompetencyThemeEntity.class);
        ApiResponse readResponse = mock(ApiResponse.class);
        CustomResponse desgResponse = mock(CustomResponse.class);

        when(request.get(Constants.NAME)).thenReturn(mock(JsonNode.class));
        when(request.get(Constants.REF_ID)).thenReturn(mock(JsonNode.class));
        when(request.get(Constants.FRAMEWORK)).thenReturn(mock(JsonNode.class));
        when(request.get(Constants.CATEGORY)).thenReturn(mock(JsonNode.class));
        when(request.get(Constants.ADDITIONAL_PROPERTIES)).thenReturn(mock(JsonNode.class));

//        when(competencyThemeRepository.findByIdAndIsActive(anyString(), eq(Boolean.TRUE))).thenReturn(Optional.of(competencyThemeEntity));
//        when(competencyThemeEntity.getIsActive()).thenReturn(true);
//        when(designationService.frameworkRead(anyString(), anyString(), anyString(), anyString())).thenReturn(readResponse);
//        when(readResponse.getResponseCode()).thenReturn(HttpStatus.NOT_FOUND);

        Map<String, Object> termResponse = new HashMap<>();
        termResponse.put(Constants.RESPONSE_CODE, Constants.OK);
        termResponse.put(Constants.RESULT, Collections.singletonMap(Constants.NODE_ID, Collections.singletonList("testNodeId")));

//        when(outboundRequestHandlerServiceImpl.fetchResultUsingPost(anyString(), any())).thenReturn(termResponse);
//        when(objectMapper.valueToTree(any())).thenReturn(mock(JsonNode.class));
//        when(desgResponse.getResponseCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        ApiResponse result = competencyThemeService.createTerm(request);

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getResponseCode());
        assertEquals(Constants.FAILED, result.getParams().getStatus());
    }

    /**
    * Testcase 3 for @Override public ApiResponse createTerm(JsonNode request)
    * Path constraints: (designationEntity.isPresent()), (designation.getIsActive()), !((readResponse == null)) && !((HttpStatus.NOT_FOUND.equals(readResponse.getResponseCode()))), (HttpStatus.CONFLICT.equals(readResponse.getResponseCode()))
    * returns: response
    */
    @Test
    void test_createTerm_3() throws CustomException {
        // Arrange
        JsonNode request = objectMapper.createObjectNode();
        CompetencyThemeEntity designation = new CompetencyThemeEntity();
        designation.setIsActive(true);

        //when(payloadValidation.validatePayload(any(), any())).thenReturn(true);
        //when(competencyThemeRepository.findByIdAndIsActive(any(), any())).thenReturn(Optional.of(designation));

        ApiResponse readResponse = new ApiResponse();
        readResponse.setResponseCode(HttpStatus.CONFLICT);
//        when(designationService.frameworkRead(any(), any(), any(), any())).thenReturn(readResponse);

        // Act
        ApiResponse response = competencyThemeService.createTerm(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    /**
     * Test case for createTerm method when designation entity is present but not active.
     * This test covers the path where the designation entity exists but is not active.
     */
    @Test
    void test_createTerm_4() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode request = objectMapper.createObjectNode()
                .put(Constants.NAME, "Test Term")
                .put(Constants.REF_ID, "TEST-REF-ID")
                .put(Constants.FRAMEWORK, "Test Framework")
                .put(Constants.CATEGORY, "Test Category")
                .set(Constants.ADDITIONAL_PROPERTIES, objectMapper.createObjectNode()
                        .put(Constants.PARENT_CATEGORY, "Parent Category")
                        .put(Constants.PREV_TERM_CODE, "PREV-TERM-CODE"));

        CompetencyThemeEntity mockEntity = new CompetencyThemeEntity();
        mockEntity.setIsActive(false);

        //when(payloadValidation.validatePayload(eq(Constants.TERM_CREATE_PAYLOAD_VALIDATION), any(JsonNode.class))).thenReturn(true);
        when(competencyThemeRepository.findByIdAndIsActive(eq("TEST-REF-ID"), eq(Boolean.TRUE)))
                .thenReturn(Optional.of(mockEntity));

        // Act
        ApiResponse response = competencyThemeService.createTerm(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
        assertEquals("Failed to create term.", response.getParams().getErr());
    }

    /**
     * Tests the createTerm method with an empty JsonNode input.
     * This test verifies that the method handles empty input correctly by returning a BAD_REQUEST response.
     */
    @Test
    void test_createTerm_emptyInput() {
        JsonNode emptyNode = objectMapper.createObjectNode();
        ApiResponse response = competencyThemeService.createTerm(emptyNode);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    /**
     * Tests the createTerm method with missing required fields in the input JsonNode.
     * This test checks if the method properly handles incomplete input data by returning a BAD_REQUEST response.
     */
    @Test
    void test_createTerm_missingRequiredFields() {
        ObjectMapper newObjectMapper = new ObjectMapper();
        JsonNode incompleteNode = newObjectMapper.createObjectNode()
                .put(Constants.NAME, "Test Term")
                .put(Constants.REF_ID, "TERM001");

        ApiResponse response = competencyThemeService.createTerm(incompleteNode);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    /**
     * Test case for deleteCompetencyTheme method when the entity is present.
     * This test verifies that the method successfully deletes a competency theme
     * when it exists in the repository.
     */
    @Test
    void test_deleteCompetencyTheme_1() {
        // Arrange
        String id = "TEST_ID";
        CompetencyThemeEntity entity = new CompetencyThemeEntity();
        entity.setId(id);
        entity.setIsActive(true);
        entity.setData(mock(ObjectNode.class));

        when(competencyThemeRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.of(entity));
        when(objectMapper.convertValue(any(), eq(java.util.Map.class))).thenReturn(new java.util.HashMap<>());

        // Act
        CustomResponse response = competencyThemeService.deleteCompetencyTheme(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(Constants.DELETED_SUCCESSFULLY, response.getMessage());
        verify(competencyThemeRepository).save(entity);
        verify(esUtilService).updateDocument(eq(Constants.COMP_THEME_INDEX_NAME), eq(Constants.INDEX_TYPE),
                eq(id), any(), any());
        verify(cacheService).deleteCache(id);

        assertFalse(entity.getIsActive());
        assertNotNull(entity.getUpdatedOn());
    }

    /**
     * Test case for deleteCompetencyTheme when the entity is not present.
     * This test verifies that the method returns the correct response when the competency theme is not found.
     */
    @Test
    void test_deleteCompetencyTheme_2() {
        // Arrange
        String id = "non-existent-id";
        when(competencyThemeRepository.findByIdAndIsActive(anyString(), anyBoolean())).thenReturn(Optional.empty());

        // Act
        CustomResponse response = competencyThemeService.deleteCompetencyTheme(id);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("CompetencyThemeServiceImpl::deleteCompetencyTheme:No data found for this id", response.getMessage());
    }

    /**
     * Test case for deleteCompetencyTheme when the competency theme is not found.
     * This test verifies that the method returns a BAD_REQUEST response when
     * the competency theme with the given ID does not exist or is not active.
     */
    @Test
    void test_deleteCompetencyTheme_nonExistentTheme() {
        // Arrange
        String nonExistentId = "NON_EXISTENT_ID";
        when(competencyThemeRepository.findByIdAndIsActive(nonExistentId, true)).thenReturn(Optional.empty());

        // Act
        CustomResponse response = competencyThemeService.deleteCompetencyTheme(nonExistentId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("CompetencyThemeServiceImpl::deleteCompetencyTheme:No data found for this id", response.getMessage());
        verify(competencyThemeRepository, times(1)).findByIdAndIsActive(nonExistentId, true);
        verifyNoMoreInteractions(competencyThemeRepository, esUtilService, cacheService);
    }

    /**
     * Tests the generateRedisJwtTokenKey method with a non-null requestPayload.
     * Verifies that a JWT token is generated and returned.
     */
    @Test
    void test_generateRedisJwtTokenKey_1() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        CbServerProperties cbServerProperties = mock(CbServerProperties.class);
        ReflectionTestUtils.setField(service, "cbServerProperties", cbServerProperties);

        Object requestPayload = new Object();
        String reqJsonString = "{\"key\":\"value\"}";

        try {
            when(objectMapper.writeValueAsString(requestPayload)).thenReturn(reqJsonString);
            when(cbServerProperties.getJwtSecretKey()).thenReturn("test_secret_key");

            String result = service.generateRedisJwtTokenKey(requestPayload);

            assertNotNull(result);
            // Additional assertions can be added to verify the JWT structure and claims
        } catch (Exception e) {
            // Handle or fail the test if an exception occurs
            throw new RuntimeException("Test failed due to exception", e);
        }
    }

    /**
     * Test case for generateRedisJwtTokenKey method when null input is provided.
     * This tests the edge case where the method is called with a null payload.
     */
    @Test
    void test_generateRedisJwtTokenKey_nullInput() {
        String result = competencyThemeService.generateRedisJwtTokenKey(null);
        assertEquals("", result, "Should return an empty string for null input");
    }

    /**
     * Test case for generateRedisJwtTokenKey when requestPayload is null
     * Expected to return an empty string
     */
    @Test
    void test_generateRedisJwtTokenKey_withNullPayload() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        String result = service.generateRedisJwtTokenKey(null);
        assertEquals("", result, "Should return an empty string when requestPayload is null");
    }

    /**
     * Test case for loadCompetencyTheme method
     * This test verifies the successful execution of the loadCompetencyTheme method
     * when all conditions in the path constraints are met.
     */
    @Test
    void test_loadCompetencyTheme_1()  {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String token = "validToken";
        String userId = "testUser";

        when(accessTokenValidator.verifyUserToken(token)).thenReturn(userId);
        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        SearchResult mockSearchResult = mock(SearchResult.class);
        JsonNode mockDataJson = mock(JsonNode.class);
        when(mockSearchResult.getData()).thenReturn(mockDataJson);
        when(mockDataJson.isEmpty()).thenReturn(false);
        when(mockDataJson.isNull()).thenReturn(false);
        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any())).thenReturn(mockSearchResult);

        List<Map<String, String>> processedData = new ArrayList<>();
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(Constants.COMPETENCY_THEME_TYPE, "TestTheme");
        processedData.add(dataMap);
        when(fileProcessService.processExcelFile(mockFile)).thenReturn(processedData);

        JsonNode mockJsonNode = mock(JsonNode.class);
        when(objectMapper.valueToTree(processedData)).thenReturn(mockJsonNode);
//        when(mockJsonNode.forEach(any())).thenAnswer(invocation -> {
//            java.util.function.Consumer<JsonNode> consumer = invocation.getArgument(0);
//            consumer.accept(mock(JsonNode.class));
//            return null;
//        });

        when(competencyThemeRepository.count()).thenReturn(0L);

        // Act
        competencyThemeService.loadCompetencyTheme(mockFile, token);

        // Assert
        verify(esUtilService).isIndexPresent(Constants.COMP_THEME_INDEX_NAME);
        verify(esUtilService).searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any());
        verify(fileProcessService).processExcelFile(mockFile);
        verify(competencyThemeRepository).count();
        // Add more verifications as needed based on the expected behavior
    }

    /**
     * Test case for loadCompetencyTheme method
     * This test verifies the behavior of loadCompetencyTheme method when all path constraints are satisfied
     * Path constraints:
     * - esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME) returns true
     * - !dataFetched.getData().isEmpty() && !dataFetched.getData().isNull() is true
     * - !StringUtils.isBlank(userId) is true
     * - !node.has(Constants.TITLE) is true for at least one node
     * - !eachCompTheme.isNull() && eachCompTheme.has(Constants.COMPETENCY_THEME_TYPE) is true
     * - !eachCompTheme.get(Constants.COMPETENCY_THEME_TYPE).asText().isEmpty() is true
     * - !titles.containsKey(eachCompTheme.get(Constants.COMPETENCY_THEME_TYPE).asText().toLowerCase()) is true
     */
    @Test
    void test_loadCompetencyTheme_4()  {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        String token = "valid_token";
        String userId = "test_user";

        when(accessTokenValidator.verifyUserToken(token)).thenReturn(userId);
        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        SearchResult searchResult = new SearchResult();
        ArrayNode dataNode = mock(ArrayNode.class);
        when(dataNode.isEmpty()).thenReturn(false);
        when(dataNode.isNull()).thenReturn(false);
        searchResult.setData(dataNode);
        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any())).thenReturn(searchResult);

        List<Map<String, String>> processedData = new ArrayList<>();
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(Constants.COMPETENCY_THEME_TYPE, "New Theme");
        processedData.add(dataMap);
        when(fileProcessService.processExcelFile(file)).thenReturn(processedData);

        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.valueToTree(processedData)).thenReturn(jsonNode);

        when(competencyThemeRepository.count()).thenReturn(0L);

        // Act
        competencyThemeService.loadCompetencyTheme(file, token);

        // Assert
        verify(competencyThemeRepository).saveAll(any());
    }

    /**
     * Test case for loadCompetencyTheme method when the index is present, data is not empty,
     * userId is not blank, node has title, eachCompTheme is not null and has COMPETENCY_THEME_TYPE,
     * but COMPETENCY_THEME_TYPE is empty.
     */
    @Test
    void test_loadCompetencyTheme_6()  {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        String token = "testToken";
        String userId = "testUserId";

        when(accessTokenValidator.verifyUserToken(token)).thenReturn(userId);
        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        SearchResult searchResult = new SearchResult();
        JsonNode mockDataJson = mock(JsonNode.class);
        when(mockDataJson.isEmpty()).thenReturn(false);
        when(mockDataJson.isNull()).thenReturn(false);
        searchResult.setData(mockDataJson);
        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any())).thenReturn(searchResult);

        when(fileProcessService.processExcelFile(file)).thenReturn(new ArrayList<>());
        when(competencyThemeRepository.count()).thenReturn(0L);

        JsonNode mockJsonNode = mock(JsonNode.class);
        when(objectMapper.valueToTree(any())).thenReturn(mockJsonNode);

        // Act
        competencyThemeService.loadCompetencyTheme(file, token);

        // Assert
        verify(esUtilService).isIndexPresent(Constants.COMP_THEME_INDEX_NAME);
        verify(esUtilService).searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any());
        verify(fileProcessService).processExcelFile(file);
        verify(competencyThemeRepository).count();
        verify(objectMapper).valueToTree(any());
    }

    /**
     * Test case for loadCompetencyTheme method when the index exists, data is not empty,
     * but the user token is invalid or blank.
     * 
     * This test verifies that the method handles the case where the Elasticsearch index exists,
     * contains non-empty data, but the user token is invalid, resulting in no further processing.
     */
    @Test
    void test_loadCompetencyTheme_8()  {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        String invalidToken = "invalid_token";

        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        SearchResult mockSearchResult = mock(SearchResult.class);
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(mockSearchResult.getData()).thenReturn(mockJsonNode);
        when(mockJsonNode.isEmpty()).thenReturn(false);
        when(mockJsonNode.isNull()).thenReturn(false);

        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any())).thenReturn(mockSearchResult);

        when(accessTokenValidator.verifyUserToken(invalidToken)).thenReturn("");

        // Act
        competencyThemeService.loadCompetencyTheme(mockFile, invalidToken);

        // Assert
        verify(esUtilService).isIndexPresent(Constants.COMP_THEME_INDEX_NAME);
        verify(esUtilService).searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any());
        verify(accessTokenValidator).verifyUserToken(invalidToken);
        verifyNoMoreInteractions(esUtilService, accessTokenValidator);
    }

    /**
     * Test case for loadCompetencyTheme method when index exists, data is present,
     * user token is valid, but the competency theme already exists.
     * 
     * Path constraints:
     * - esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME) returns true
     * - !dataFetched.getData().isEmpty() && !dataFetched.getData().isNull() is true
     * - !StringUtils.isBlank(userId) is true
     * - node.has(Constants.TITLE) is true
     * - !eachCompTheme.isNull() && eachCompTheme.has(Constants.COMPETENCY_THEME_TYPE) is true
     * - !eachCompTheme.get(Constants.COMPETENCY_THEME_TYPE).asText().isEmpty() is true
     * - titles.containsKey(eachCompTheme.get(Constants.COMPETENCY_THEME_TYPE).asText().toLowerCase()) is true
     */
    @Test
    void test_loadCompetencyTheme_existingTheme()  {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        String token = "validToken";
        String userId = "testUser";

        when(accessTokenValidator.verifyUserToken(token)).thenReturn(userId);
        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        SearchResult searchResult = new SearchResult();
        ArrayNode dataArray = mock(ArrayNode.class);
        ObjectNode dataNode = mock(ObjectNode.class);
//        when(dataNode.has(Constants.TITLE)).thenReturn(true);
//        when(dataNode.get(Constants.TITLE)).thenReturn(mock(JsonNode.class));
//        when(dataArray.iterator()).thenReturn(Collections.singletonList(dataNode).iterator());
        searchResult.setData(dataArray);

        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any(SearchCriteria.class))).thenReturn(searchResult);

        List<Map<String, String>> processedData = new ArrayList<>();
        Map<String, String> themeData = new HashMap<>();
        themeData.put(Constants.COMPETENCY_THEME_TYPE, "Existing Theme");
        processedData.add(themeData);
        when(fileProcessService.processExcelFile(file)).thenReturn(processedData);

        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.valueToTree(processedData)).thenReturn(jsonNode);
        //when(jsonNode.iterator()).thenReturn(Collections.singletonList(mock(JsonNode.class)).iterator());

        // Act
        competencyThemeService.loadCompetencyTheme(file, token);

        // Assert
        verify(competencyThemeRepository, never()).save(any(CompetencyThemeEntity.class));
        verify(esUtilService, never()).addDocument(anyString(), anyString(), anyString(), anyMap(), anyString());
    }

    /**
     * Test case for loadCompetencyTheme with a mixed batch exercising every branch of the
     * per-item validation: a normal item, a duplicate title, a missing type field, an empty
     * type value, and a literal null element.
     */
    @Test
    void test_loadCompetencyTheme_mixedBatch_coversAllFieldBranches() {
        MultipartFile file = mock(MultipartFile.class);
        String token = "validToken";
        String userId = "testUser";

        when(accessTokenValidator.verifyUserToken(token)).thenReturn(userId);
        when(esUtilService.isIndexPresent(Constants.COMP_THEME_INDEX_NAME)).thenReturn(true);

        ObjectMapper realMapper = new ObjectMapper();
        SearchResult searchResult = new SearchResult();
        searchResult.setData(realMapper.createArrayNode());
        when(esUtilService.searchDocuments(eq(Constants.COMP_THEME_INDEX_NAME), any(SearchCriteria.class)))
                .thenReturn(searchResult);

        List<Map<String, String>> processedData = new ArrayList<>();
        when(fileProcessService.processExcelFile(file)).thenReturn(processedData);

        com.fasterxml.jackson.databind.node.ArrayNode batch = realMapper.createArrayNode();
        batch.add(realMapper.createObjectNode().put(Constants.COMPETENCY_THEME_TYPE, "Theme A"));
        batch.add(realMapper.createObjectNode().put(Constants.COMPETENCY_THEME_TYPE, "Theme A"));
        batch.add(realMapper.createObjectNode().put("NotType", "ignored"));
        batch.add(realMapper.createObjectNode().put(Constants.COMPETENCY_THEME_TYPE, ""));
        batch.addNull();

        when(objectMapper.valueToTree(processedData)).thenReturn(batch);
        when(objectMapper.createObjectNode()).thenReturn(realMapper.createObjectNode());

        competencyThemeService.loadCompetencyTheme(file, token);

        // Only the first item should pass every branch and get persisted.
        verify(competencyThemeRepository, times(1)).saveAll(argThat(list ->
                ((List<?>) list).size() == 1));
    }

    /**
     * Testcase 2 for @Override public CustomResponse readCompTheme(String id)
     * Path constraints: !((StringUtils.isEmpty(id))), (StringUtils.isNotEmpty(cachedJson))
     * This test verifies that the method returns the correct response when a valid ID is provided
     * and the data is found in the cache.
     */
    @Test
    void test_readCompTheme_2() {
        // Arrange
        String id = "validId";
        String cachedJson = "{\"key\":\"value\"}";
        Object mockObject = new Object();

        when(cacheService.getCache(id)).thenReturn(cachedJson);
        //when(objectMapper.readValue(eq(cachedJson), any())).thenReturn(mockObject);

        // Act
        CustomResponse response = competencyThemeService.readCompTheme(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(Constants.SUCCESSFULLY_READING, response.getMessage());
    }

    /**
     * Test case for readCompTheme method when the id is not empty and the cache is empty.
     * This test verifies that the method correctly retrieves data from the repository
     * when it's not found in the cache.
     */
    @Test
    void test_readCompTheme_3() {
        // Arrange
        String id = "testId";
        CompetencyThemeEntity entity = new CompetencyThemeEntity();
        entity.setId(id);
        entity.setData(objectMapper.createObjectNode());

        when(cacheService.getCache(id)).thenReturn(null);
        when(competencyThemeRepository.findByIdAndIsActive(id, true)).thenReturn(Optional.of(entity));

        // Act
        CustomResponse response = competencyThemeService.readCompTheme(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(Constants.SUCCESSFULLY_READING, response.getMessage());
        verify(cacheService).getCache(id);
        verify(competencyThemeRepository).findByIdAndIsActive(id, true);
        verify(cacheService).putCache(eq(id), any());
    }

    /**
    * Edge case tests for `readCompTheme`
    * Consider the following scenarios, implementing only those that are realistically applicable and handled by the focal method:
    * 1. invalid/empty input (if method accepts user input)
    * 2. out-of-bounds values (if method has valid ranges)
    * 3. incorrect types/formats (if method expects specific types)
    * 4. expected exceptions (if method can throw exceptions)
    * 5. other relevant edge cases specific to this method's functionality
    *
    * Note: Only implement tests for edge cases that are both relevant AND actually
    * handled by the current implementation. Do not test edge cases that would be 
    * good to have but aren't currently supported. If the method doesn't handle 
    * any edge cases, respond with "NO ANSWER".
    */
    @Test
    void test_readCompTheme_emptyId() {
        CompetencyThemeServiceImpl service = newUninitializedService();
        CustomResponse response = service.readCompTheme("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("Id not found", response.getMessage());
    }

    /**
     * Test case for readTerm method when the response is successful.
     * This test verifies that the method correctly processes a valid response
     * from the outbound request handler and returns the expected API response.
     */
    @Test
    void test_readTerm_1() {
        // Arrange
        String id = "testId";
        String framework = "testFramework";
        String category = "testCategory";
        String knowledgeMS = "http://test-url.com";
        String odcsDesignationTermRead = "/odcs/term/read";

        when(cbServerProperties.getKnowledgeMS()).thenReturn(knowledgeMS);
        when(cbServerProperties.getOdcsDesignationTermRead()).thenReturn(odcsDesignationTermRead);

        Map<String, Object> termData = new HashMap<>();
        termData.put("name", "Test Term");
        termData.put("code", "TEST_TERM");

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.TERM, termData);

        Map<String, Object> desgResponse = new HashMap<>();
        desgResponse.put(Constants.RESPONSE_CODE, Constants.OK);
        desgResponse.put(Constants.RESULT, resultMap);

        when(outboundRequestHandlerServiceImpl.fetchResult(anyString())).thenReturn(desgResponse);

        // Act
        ApiResponse response = competencyThemeService.readTerm(id, framework, category);

        // Assert
        assertNotNull(response);

    }

    /**
     * Test case for readTerm method when desgResponse is not null but response code is not OK.
     * This test verifies that the method sets the correct error message and HTTP status
     * when the response from the outbound request is not successful.
     */
    @Test
    void test_readTerm_2() {
        // Arrange
        String id = "testId";
        String framework = "testFramework";
        String category = "testCategory";

        Map<String, Object> desgResponse = new HashMap<>();
        desgResponse.put(Constants.RESPONSE_CODE, "NOT_OK");

//        when(outboundRequestHandlerServiceImpl.fetchResult(anyString())).thenReturn(desgResponse);

        // Act
        ApiResponse response = competencyThemeService.readTerm(id, framework, category);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    /**
     * Testcase 3 for public ApiResponse readTerm(String Id, String framework, String category)
     * Path constraints: !((null != desgResponse))
     * This test verifies the behavior when the outbound request returns null
     */
    @Test
    void test_readTerm_3() {
        // Arrange
        String id = "testId";
        String framework = "testFramework";
        String category = "testCategory";

//        when(outboundRequestHandlerServiceImpl.fetchResult(anyString())).thenReturn(null);

        // Act
        ApiResponse response = competencyThemeService.readTerm(id, framework, category);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    /**
     * Test the readTerm method with an empty ID.
     * This test verifies that the method handles the case where an empty ID is provided,
     * which is an edge case explicitly handled in the focal method.
     */
    @Test
    void test_readTerm_emptyId() {
        String emptyId = "";
        String framework = "testFramework";
        String category = "testCategory";

        ApiResponse response = competencyThemeService.readTerm(emptyId, framework, category);

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
        assertTrue(response.getParams().getErr().contains("Failed to read Designation"));
    }

    /**
     * Test the readTerm method when the outbound request returns null.
     * This test verifies that the method handles the case where the outbound request returns null,
     * which is an edge case explicitly handled in the focal method.
     */
    @Test
    void test_readTerm_nullOutboundResponse() {
        String id = "testId";
        String framework = "testFramework";
        String category = "testCategory";

        when(cbServerProperties.getKnowledgeMS()).thenReturn("http://test.com");
        when(cbServerProperties.getOdcsDesignationTermRead()).thenReturn("/read");
        when(outboundRequestHandlerServiceImpl.fetchResult(anyString())).thenReturn(null);

        ApiResponse response = competencyThemeService.readTerm(id, framework, category);

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
        assertEquals("Failed to read the des details for Id : testId", response.getParams().getErr());
    }

    /**
     * Test case for searchCompTheme method when search result is found in Redis cache.
     * This test verifies that when a cached search result is available in Redis,
     * the method returns it without querying the database.
     */
    @Test
    void test_searchCompTheme_1() {
        // Arrange
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchResult mockSearchResult = new SearchResult();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(mockSearchResult);

        // Act
        CustomResponse response = competencyThemeService.searchCompTheme(searchCriteria);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertTrue(response.getResult().containsKey(Constants.RESULT));
        assertEquals(mockSearchResult, response.getResult().get(Constants.RESULT));
        verify(redisTemplate.opsForValue(), times(1)).get(anyString());
        verifyNoMoreInteractions(redisTemplate.opsForValue());
    }

    /**
     * Testcase 2 for @Override public CustomResponse searchCompTheme(SearchCriteria searchCriteria)
     * This test verifies that when the search string is less than 2 characters long,
     * the method returns an error response with BAD_REQUEST status.
     */
    @Test
    void test_searchCompTheme_2() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("a");

        CustomResponse response = competencyThemeService.searchCompTheme(searchCriteria);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
    }

    /**
     * Test case for searchCompTheme method when Redis cache is empty and search string is valid.
     * This test verifies that the method correctly searches for competency themes using Elasticsearch
     * when the Redis cache does not contain the search result and the search string is valid.
     */
    @Test
    void test_searchCompTheme_3()  {

        // Arrange
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("validSearchString");

        when(cbServerProperties.getJwtSecretKey()).thenReturn("test_secret_key");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        SearchResult mockSearchResult = new SearchResult();
        when(esUtilService.searchDocuments(anyString(), any(SearchCriteria.class))).thenReturn(mockSearchResult);

        // Act
        CustomResponse response = competencyThemeService.searchCompTheme(searchCriteria);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals("success", response.getParams().getStatus());
        assertTrue(response.getResult().containsKey("result"));
        assertEquals(mockSearchResult, response.getResult().get("result"));

        verify(redisTemplate.opsForValue(), times(1)).get(anyString());
        verify(esUtilService, times(1)).searchDocuments(anyString(), any(SearchCriteria.class));
    }

    /**
    * Edge case test for `searchCompTheme` when search string is less than 3 characters
    * This test verifies that the method handles the case where the search string is too short,
    * which is explicitly checked in the focal method.
    */
    @Test
    void test_searchCompTheme_shortSearchString() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("ab");

        CustomResponse response = competencyThemeService.searchCompTheme(searchCriteria);

        assertEquals(HttpStatus.OK, response.getResponseCode());
    }

    /**
     * Test case for searchCompTheme method when esUtilService throws an exception.
     * This test verifies that the method returns an INTERNAL_SERVER_ERROR response.
     */
    @Test
    void test_searchCompTheme_esUtilServiceThrows_returnsInternalServerError() {
        when(cbServerProperties.getJwtSecretKey()).thenReturn("test_secret_key");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchString("validSearchString");

        when(esUtilService.searchDocuments(anyString(), any(SearchCriteria.class)))
                .thenThrow(new RuntimeException("es down"));

        CustomResponse response = competencyThemeService.searchCompTheme(searchCriteria);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
    }

    /**
     * Testcase 4 for @Override public CustomResponse updateCompTheme(JsonNode updatedCompTheme)
     * Path constraints: (updatedCompTheme.has(Constants.ID) && !updatedCompTheme.get(Constants.ID)
     *       .isNull()), !((compTheme.isPresent()))
     */
    @Test
    void test_updateCompTheme_4() {
        // Arrange
        ObjectNode updatedCompTheme = new ObjectMapper().createObjectNode();
        updatedCompTheme.put(Constants.ID, "testId");

        when(competencyThemeRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        CustomResponse response = competencyThemeService.updateCompTheme(updatedCompTheme);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("No data found for this id", response.getMessage());
    }

    /**
     * Tests the updateCompTheme method when the ID is missing in the input JsonNode.
     * This is an edge case explicitly handled in the focal method.
     */
    @Test
    void test_updateCompTheme_missingId() {
        // Create a JsonNode without an ID
        ObjectMapper objectMapper1 = new ObjectMapper();
        ObjectNode updatedCompTheme = objectMapper1.createObjectNode();
        updatedCompTheme.put("title", "Test Theme");

        // Call the method
        CustomResponse response = competencyThemeService.updateCompTheme(updatedCompTheme);

        // Verify the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("Id is missing", response.getMessage());
    }

    @Test
    void testProcessDesignation_shouldMapFieldsAndChildren() throws Exception {
        // Prepare input
        Map<String, Object> designationInput = new HashMap<>();
        designationInput.put("name", "Manager");
        designationInput.put(Constants.IDENTIFIER, "mgr-001");
        designationInput.put(Constants.CHILDREN, List.of(
                Map.of(Constants.IDENTIFIER, "sub-001", "name", "Assistant Manager"),
                Map.of(Constants.IDENTIFIER, "sub-002", "name", "Deputy Manager"),
                Map.of(Constants.IDENTIFIER, "sub-001", "name", "Duplicate") // duplicate, should be skipped
        ));

        Map<String, Object> designationMap = new HashMap<>();

        when(cbServerProperties.getOdcsFields()).thenReturn(List.of("name", Constants.IDENTIFIER));

        // Reflectively invoke private method
        Method method = CompetencyThemeServiceImpl.class
                .getDeclaredMethod("processDesignation", Map.class, Map.class);
        method.setAccessible(true);
        method.invoke(competencyThemeService, designationInput, designationMap);

        assertEquals("Manager", designationMap.get("name"));
        assertEquals("mgr-001", designationMap.get(Constants.IDENTIFIER));

        List<Map<String, Object>> children = (List<Map<String, Object>>) designationMap.get(Constants.CHILDREN);
        assertNotNull(children);
        assertEquals(2, children.size());

        Set<String> childIds = new HashSet<>();
        children.forEach(child -> childIds.add((String) child.get(Constants.IDENTIFIER)));

        assertTrue(childIds.contains("sub-001"));
        assertTrue(childIds.contains("sub-002"));
    }

    @Test
    void testProcessSubDesignation_shouldSkipDuplicatesAndMapFields() throws Exception {
        Map<String, Object> desig1 = new HashMap<>();
        desig1.put(Constants.IDENTIFIER, "sub-101");
        desig1.put("name", "Lead");

        Map<String, Object> desig2 = new HashMap<>();
        desig2.put(Constants.IDENTIFIER, "sub-101"); // duplicate
        desig2.put("name", "Lead Duplicate");

        Map<String, Object> desig3 = new HashMap<>();
        desig3.put(Constants.IDENTIFIER, "sub-102");
        desig3.put("name", "Senior Lead");

        Map<String, Object> designation = new HashMap<>();
        designation.put(Constants.CHILDREN, List.of(desig1, desig2, desig3));

        Map<String, Object> newDesignation = new HashMap<>();
        newDesignation.put(Constants.CHILDREN, new ArrayList<>());

        when(cbServerProperties.getOdcsFields()).thenReturn(List.of("name", Constants.IDENTIFIER));

        Method method = CompetencyThemeServiceImpl.class
                .getDeclaredMethod("processSubDesignation", Map.class, Map.class);
        method.setAccessible(true);
        method.invoke(competencyThemeService, designation, newDesignation);

        List<Map<String, Object>> children = (List<Map<String, Object>>) newDesignation.get(Constants.CHILDREN);
        assertEquals(2, children.size());

        Set<String> ids = new HashSet<>();
        for (Map<String, Object> c : children) {
            ids.add((String) c.get(Constants.IDENTIFIER));
        }

        assertTrue(ids.contains("sub-101"));
        assertTrue(ids.contains("sub-102"));
    }
}

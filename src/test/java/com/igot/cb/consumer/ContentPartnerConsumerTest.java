package com.igot.cb.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.cios.service.CiosContentService;
import com.igot.cb.pores.elasticsearch.dto.SearchResult;
import com.igot.cb.pores.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentPartnerConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CiosContentService ciosContentService;

    private ContentPartnerConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ContentPartnerConsumer(objectMapper, ciosContentService);
        ReflectionTestUtils.setField(consumer, "pageSize", 10);
    }

    @Test
    void consumePartnerDeactivate_missingPartnerId_logsAndReturns() throws Exception {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "{}");
        when(objectMapper.readValue(eq("{}"), eq(Map.class))).thenReturn(new HashMap<>());

        consumer.consumePartnerDeactivate(record);

        verifyNoInteractions(ciosContentService);
    }

    @Test
    void consumePartnerActivate_validPartnerId_updatesAllPartnerContents() throws Exception {
        String payload = "{\"partnerId\":\"p1\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", payload);
        Map<String, Object> event = new HashMap<>();
        event.put(Constants.PARTNER_ID, "p1");
        when(objectMapper.readValue(eq(payload), eq(Map.class))).thenReturn(event);

        // SearchResult with null data -> loop breaks on first page
        when(ciosContentService.searchCotent(any())).thenReturn(new SearchResult());

        consumer.consumePartnerActivate(record);

        verify(ciosContentService).searchCotent(any());
    }

    @Test
    void handlePartnerEvent_exceptionDuringParsing_isSwallowed() throws Exception {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", "not-json");
        when(objectMapper.readValue(eq("not-json"), eq(Map.class)))
                .thenThrow(new RuntimeException("bad json"));

        consumer.consumePartnerDeactivate(record);

        verifyNoInteractions(ciosContentService);
    }
}

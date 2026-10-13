package com.igot.cb.consumer;

import com.igot.cb.cios.service.CiosContentService;
import com.igot.cb.pores.elasticsearch.dto.SearchCriteria;
import com.igot.cb.pores.elasticsearch.dto.SearchResult;
import com.igot.cb.pores.util.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ContentPartnerConsumer {

    private static final Logger log = LoggerFactory.getLogger(ContentPartnerConsumer.class);

    private final ObjectMapper objectMapper;

    private final CiosContentService ciosContentService;

    @Value("${content.partner.search.page-size}")
    private int pageSize;

    // ---------- DEACTIVATE ----------
    @KafkaListener(topics = "${kafka.topic.content.partner.delete}", groupId = "${kafka.topic.content.partner.delete.group}")
    public void consumePartnerDeactivate(ConsumerRecord<String, String> consumerRecord) {
        log.info("Partner DEACTIVATE event received: {}", consumerRecord.value());
        handlePartnerEvent(consumerRecord, false);
    }

    // ---------- ACTIVATE ----------
    @KafkaListener(topics = "${kafka.topic.content.partner.activate}", groupId = "${kafka.topic.content.partner.activate.group}")
    public void consumePartnerActivate(ConsumerRecord<String, String> consumerRecord) {
        log.info("Partner ACTIVATE event received: {}", consumerRecord.value());
        handlePartnerEvent(consumerRecord, true);
    }

    private void handlePartnerEvent(ConsumerRecord<String, String> consumerRecord, boolean isActive) {
        try {
            Map<String, Object> event = objectMapper.readValue(consumerRecord.value(), Map.class);
            String partnerId = (String) event.get(Constants.PARTNER_ID);
            if (partnerId == null || partnerId.isBlank()) {
                log.warn("partnerId missing in event: {}", event);
                return;
            }
            updateAllPartnerContents(partnerId, isActive);
        } catch (Exception e) {
            log.error("Failed to consume partner event", e);
        }
    }

    private void updateAllPartnerContents(String partnerId, boolean isActive) {
        int page = 0;
        int size = pageSize;
        boolean continueProcessing = true;
        long processedCount = 0;
        long totalCount = 0;
        do {
            SearchCriteria criteria = new SearchCriteria();
            HashMap<String, Object> filterMap = new HashMap<>();
            filterMap.put(Constants.FILTER_CONTENT_PARTNER_ID, partnerId);
            filterMap.put(Constants.IS_ACTIVE, null);
            criteria.setFilterCriteriaMap(filterMap);
            criteria.setPageNumber(page);
            criteria.setPageSize(size);
            SearchResult result = ciosContentService.searchCotent(criteria);
            if (result == null || result.getData() == null || !result.getData().isArray()) {
                log.warn("Search failed or invalid response for partnerId={}", partnerId);
                break;
            }
            if (result.getData().size() == 0) {
                break;
            }
            if (page == 0) {
                totalCount = result.getTotalCount();
            }
            ciosContentService.updatePartnerIsActiveInEs(result.getData(), partnerId, isActive);
            processedCount += result.getData().size();
            if (processedCount >= totalCount) {
                break;
            }
            page++;
        } while (continueProcessing);
    }
}
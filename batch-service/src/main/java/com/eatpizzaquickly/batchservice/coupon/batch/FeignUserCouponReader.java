package com.eatpizzaquickly.batchservice.coupon.batch;

import com.eatpizzaquickly.batchservice.common.client.CouponServiceClient;
import com.eatpizzaquickly.batchservice.coupon.dto.UserCouponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class FeignUserCouponReader implements ItemReader<UserCouponDto> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final CouponServiceClient couponServiceClient;
    private final int pageSize;
    private Iterator<UserCouponDto> currentItems;
    private Long lastSeenId;
    private boolean noMoreData = false;

    @Override
    public UserCouponDto read() {
        if (noMoreData) {
            return null;
        }

        try {
            if (currentItems == null || !currentItems.hasNext()) {
                Page<UserCouponDto> page = fetchNextBatch();

                if (!page.hasContent()) {
                    log.info("No more expired user coupons to process");
                    noMoreData = true;
                    return null;
                }

                currentItems = page.getContent().iterator();
                log.info("Initialized iterator with {} items starting from id {}",
                        page.getContent().size(), lastSeenId);
            }

            UserCouponDto nextItem = currentItems.next();
            lastSeenId = nextItem.getId();
            return nextItem;
        } catch (Exception e) {
            log.error("Error reading expired user coupons: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read expired user coupons", e);
        }
    }

    private Page<UserCouponDto> fetchNextBatch() {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        log.info("Requesting expired user coupons - lastSeenId: {}, size: {}, date: {}",
                lastSeenId, pageSize, currentDate);

        try {
            Page<UserCouponDto> page = couponServiceClient.getExpiredUserCouponsAfter(
                    currentDate,
                    lastSeenId,
                    pageSize
            );

            if (page.hasContent()) {
                log.info("Retrieved {} expired user coupons. First item: id={}, userId={}, expiryDate={}",
                        page.getContent().size(),
                        page.getContent().get(0).getId(),
                        page.getContent().get(0).getUserId(),
                        page.getContent().get(0).getExpiryDate());
            } else {
                log.info("No expired user coupons found after id {}", lastSeenId);
            }

            return page;
        } catch (Exception e) {
            log.error("Failed to fetch expired user coupons after id {}: {}",
                    lastSeenId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch expired user coupons", e);
        }
    }
}
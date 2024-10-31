package com.eatpizzaquickly.couponservice.batch;
import com.eatpizzaquickly.couponservice.entity.Coupon;
import com.eatpizzaquickly.couponservice.entity.UserCoupon;
import com.eatpizzaquickly.couponservice.repository.CouponsRepository;
import com.eatpizzaquickly.couponservice.repository.UserCouponRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CouponBatchConfig {

    private static final int CHUNK_SIZE = 1500;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final CouponsRepository couponsRepository;
    private final UserCouponRepository userCouponRepository;

    @Bean
    public Job couponBatchJob() {
        return new JobBuilder("couponBatchJob", jobRepository)
                .start(deleteExpiredUserCouponsStep())
                .next(deactivateExpiredCouponsStep())
                .build();
    }

    @Bean
    public Step deleteExpiredUserCouponsStep() {
        return new StepBuilder("deleteExpiredUserCouponsStep", jobRepository)
                .<UserCoupon, UserCoupon>chunk(CHUNK_SIZE, transactionManager)
                .reader(expiredCouponsReader())
                .processor(expiredCouponsProcessor())
                .writer(expiredCouponsWriter())
                .build();
    }

    @Bean
    public Step deactivateExpiredCouponsStep() {
        return new StepBuilder("deactivateExpiredCouponsStep", jobRepository)
                .<Coupon, Coupon>chunk(CHUNK_SIZE, transactionManager)
                .reader(expiredCouponCleanupReader())
                .processor(expiredCouponCleanupProcessor())
                .writer(expiredCouponCleanupWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<UserCoupon> expiredCouponsReader() {
        return new JpaPagingItemReaderBuilder<UserCoupon>()
                .name("expiredCouponsReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT uc FROM UserCoupon uc " +
                        "WHERE uc.expiryDate < :currentDate")  // idx_user_coupon_status 인덱스 활용
                .parameterValues(Map.of("currentDate", LocalDate.now()))
                .build();
    }

    @Bean
    public ItemProcessor<UserCoupon, UserCoupon> expiredCouponsProcessor() {
        return userCoupon -> {
            log.info("Processing expired user coupon deletion - userId: {}, couponId: {}",
                    userCoupon.getUserId(), userCoupon.getCouponId());
            return userCoupon;
        };
    }

    @Bean
    public ItemWriter<UserCoupon> expiredCouponsWriter() {
        return userCoupons -> {
            userCouponRepository.deleteAll(userCoupons);
            log.info("Deleted {} expired user coupons", userCoupons.size());
        };
    }

    @Bean
    public JpaPagingItemReader<Coupon> expiredCouponCleanupReader() {
        return new JpaPagingItemReaderBuilder<Coupon>()
                .name("expiredCouponCleanupReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT c FROM Coupon c " +
                        "WHERE c.expiryDate < :currentDate " +
                        "AND c.isActive = true")  // idx_coupon_code_type_active_expiry 인덱스 활용
                .parameterValues(Map.of("currentDate", LocalDate.now()))
                .build();
    }

    @Bean
    public ItemProcessor<Coupon, Coupon> expiredCouponCleanupProcessor() {
        return coupon -> {
            log.info("Deactivating expired coupon: {}, code: {}", coupon.getId(), coupon.getCouponCode());
            coupon.isCouponActive(); // 이 메서드 내부에서 isActive를 false로 설정함
            return coupon;
        };
    }

    @Bean
    public ItemWriter<Coupon> expiredCouponCleanupWriter() {
        return coupons -> {
            couponsRepository.saveAll(coupons);
            log.info("Deactivated {} expired master coupons", coupons.size());
        };
    }
}

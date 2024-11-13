package com.eatpizzaquickly.reservationservice.payment.controller;

import com.eatpizzaquickly.reservationservice.common.advice.ApiResponse;
import com.eatpizzaquickly.reservationservice.payment.dto.request.PaymentCancelRequest;
import com.eatpizzaquickly.reservationservice.payment.dto.request.PostPaymentRequest;
import com.eatpizzaquickly.reservationservice.payment.dto.response.GetPaymentResponse;
import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentSimpleResponse;
import com.eatpizzaquickly.reservationservice.payment.exception.PaymentCancelException;
import com.eatpizzaquickly.reservationservice.payment.exception.PaymentSessionExpiredException;
import com.eatpizzaquickly.reservationservice.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<String> requestPayment(
            @RequestBody PostPaymentRequest request,
            @RequestParam(name = "couponId", required = false) Long couponId
    ) {
        String redirectUrl = paymentService.requestTossPayment(request, couponId);
        // 리다이렉트 URL을 반환하도록 수정
        return ResponseEntity.ok(redirectUrl);
    }

    /* 결제 성공 처리 */
    @GetMapping("/toss/success")
    public String handlePaymentSuccess(
            @RequestParam String orderId,
            @RequestParam String paymentKey,
            @RequestParam Long amount,
            RedirectAttributes redirectAttributes
    ) {
        try {
            paymentService.TossPaymentSuccess(paymentKey, orderId, amount);
            return "redirect:/payment/success"; // 성공 페이지로 리다이렉트
        } catch (PaymentSessionExpiredException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "결제 가능 시간이 만료되었습니다. 다시 시도해주세요.");
            return "redirect:/payment/new"; // 새로운 결제 페이지로 리다이렉트
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "결제 처리 중 오류가 발생했습니다.");
            return "redirect:/payment/error"; // 에러 페이지로 리다이렉트
        }
    }

    /* 결제 실패 처리 */
    @GetMapping("/toss/fail")
    public ResponseEntity<GetPaymentResponse> tossPaymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId
    ) {
        return ResponseEntity.ok(paymentService.tossPaymentFail(code, message, orderId));
    }

    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<GetPaymentResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody PaymentCancelRequest request
    ) {
        try {
            GetPaymentResponse response = paymentService.cancelPayment(paymentKey, request.getCancelReason());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new PaymentCancelException("결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentSimpleResponse>>> getPayments(
            @RequestHeader("X-Authenticated-User") Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {

        return ResponseEntity.ok(ApiResponse.success("결제 내역 조회 성공", paymentService.getPayments(userId, page, size)));
    }

    @GetMapping("/payment-page")
    public String getPaymentPage() {
        return "redirect:/payment.html";  // 정적 리소스 리다이렉트
    }
}

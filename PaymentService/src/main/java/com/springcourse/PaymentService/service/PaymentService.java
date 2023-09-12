package com.springcourse.PaymentService.service;

import com.springcourse.PaymentService.model.PaymentRequest;
import com.springcourse.PaymentService.model.PaymentResponse;

public interface PaymentService {
    public long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}

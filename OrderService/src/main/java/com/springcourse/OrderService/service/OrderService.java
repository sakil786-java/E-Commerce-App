package com.springcourse.OrderService.service;

import com.springcourse.OrderService.model.OrderRequest;
import com.springcourse.OrderService.model.OrderResponse;

import java.util.List;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);

   List <OrderResponse> getAllOrderDetails();
}

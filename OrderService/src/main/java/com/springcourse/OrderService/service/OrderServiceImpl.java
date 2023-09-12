package com.springcourse.OrderService.service;

import com.springcourse.OrderService.entity.Order;
import com.springcourse.OrderService.exception.CustomException;
import com.springcourse.OrderService.external.client.PaymentService;
import com.springcourse.OrderService.external.client.ProductService;
import com.springcourse.OrderService.external.request.PaymentRequest;
import com.springcourse.OrderService.external.response.PaymentResponse;
import com.springcourse.OrderService.model.OrderRequest;
import com.springcourse.OrderService.model.OrderResponse;
import com.springcourse.OrderService.repository.OrderRepository;

import com.springcourse.ProductService.model.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order entity-> Save the order into Status Order Created
        //Product Service Block Products(Reduce the quantity)
        //Payment Service to complete the Order else mark as cancel order
        log.info("Placing Order request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating order with status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment Done!!! Changing the order status to placed");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.info("Error Occurred in payment, Changing The order status to PAYMENT_FAILED ");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order Placed successfully with Order Id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for orderId: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id: {} " + orderId, "NOT_FOUND", 404));
        log.info("Invoking Product Service to fetch the product for id {}", order.getProductId());

        ProductResponse productResponse
                = restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);

        log.info("Getting Payment Information from the payment service");
        PaymentResponse paymentResponse
                =restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(),PaymentResponse.class);

        OrderResponse.ProductDetails productDetails=
                                OrderResponse.ProductDetails.builder()
                                        .productName(productResponse.getProductName())
                                        .productId(productResponse.getProductId())
                                        .price(productResponse.getPrice())
                                        .quantity(order.getQuantity())
                                        .build();
        OrderResponse.PaymentDetails paymentDetails=
                OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentStatus(paymentResponse.getStatus())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .build();

        OrderResponse orderResponse =
                OrderResponse.builder()
                        .orderId(order.getId())
                        .orderStatus(order.getOrderStatus())
                        .amount(order.getAmount())
                        .orderDate(order.getOrderDate())
                        .productDetails(productDetails)
                        .paymentDetails(paymentDetails)
                        .build();

        return orderResponse;
    }

    @Override
    public List<OrderResponse> getAllOrderDetails() {
        List<Order> orderList = orderRepository.findAll();
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (Order order : orderList) {
            OrderResponse orderResponse =
                    OrderResponse.builder()
                            .orderId(order.getId())
                            .orderStatus(order.getOrderStatus())
                            .amount(order.getAmount())
                            .orderDate(order.getOrderDate())
                            .build();
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }
}

package com.springcourse.OrderService.service;


import com.springcourse.OrderService.entity.Order;
import com.springcourse.OrderService.exception.CustomException;
import com.springcourse.OrderService.external.client.PaymentService;
import com.springcourse.OrderService.external.client.ProductService;
import com.springcourse.OrderService.external.request.PaymentRequest;
import com.springcourse.OrderService.external.response.PaymentResponse;
import com.springcourse.OrderService.model.OrderRequest;
import com.springcourse.OrderService.model.OrderResponse;
import com.springcourse.OrderService.model.PaymentMode;
import com.springcourse.OrderService.repository.OrderRepository;
import com.springcourse.ProductService.model.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order- Success Scenario")
    @Test
    void test_When_Order_Success() {
        Order order = getMockOrder();
        // Mocking - for internal call
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class)).thenReturn(getMockProductResponse());

        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(), PaymentResponse.class)).thenReturn(getMockPaymentResponse());

        // Actual - Call the Actual Method
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        //Verification
        verify(orderRepository, times(1)).findById(anyLong());
        verify(restTemplate, times(1)).getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);

        verify(restTemplate, times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(), PaymentResponse.class);

        // Assert - For verify the result
        assertNotNull(orderResponse);
        assertEquals(order.getId(), orderResponse.getOrderId());

    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .orderId(1)
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .paymentDate(Instant.now())
                .status("ACCEPTED")
                .build();
    }
    private OrderRequest getMockOrderRequest() {

        return  OrderRequest.builder()
                .paymentMode(PaymentMode.CASH)
                .totalAmount(100)
                .productId(1)
                .quantity(10)
                .build();
    }
    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .quantity(200)
                .amount(100)
                .productId(2)
                .build();

    }

    @DisplayName("Get Orders- Failure Scenario")
    @Test
    void test_When_Get_Order_NOT_FOUND_then_Not_Found() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getOrderDetails(1));

        assertEquals("NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        verify(orderRepository, times(1)).findById(anyLong());

    }



    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        OrderRequest orderRequest=getMockOrderRequest();
        Order order=getMockOrder();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L, HttpStatus.OK));

        long orderId=orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2)).save(any());
        verify(productService, times(1)).reduceQuantity(anyLong(),anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(),orderId);
    }


    @DisplayName(("Place Order- Payment Failed Scenario"))
    @Test
    void test_when_Place_order_Payment_Fails_then_order_Placed(){

        OrderRequest orderRequest=getMockOrderRequest();
        Order order=getMockOrder();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId=orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2)).save(any());
        verify(productService, times(1)).reduceQuantity(anyLong(),anyLong());
        verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));
        assertEquals(order.getId(),orderId);
    }


}
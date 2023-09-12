package com.springcourse.ProductService.service;

import com.springcourse.ProductService.entity.Product;
import com.springcourse.ProductService.model.ProductRequest;
import com.springcourse.ProductService.model.ProductResponse;

import java.util.List;

public interface ProductService {

	long addProduct(ProductRequest productRequest);

	ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);

	List<Product> getAllProduct();
}

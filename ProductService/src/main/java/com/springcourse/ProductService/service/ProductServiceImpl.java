package com.springcourse.ProductService.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springcourse.ProductService.entity.Product;
import com.springcourse.ProductService.exception.ProductServiceCustomException;
import com.springcourse.ProductService.model.ProductRequest;
import com.springcourse.ProductService.model.ProductResponse;
import com.springcourse.ProductService.repository.ProductRepository;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;


@Service
@Log4j2
public class ProductServiceImpl implements ProductService {
	
	@Autowired
	private ProductRepository productRepository;

	@Override
	public long addProduct(ProductRequest productRequest) {
		log.info("Adding Product");
		
		Product product=Product.builder()
								.productName(productRequest.getName())
								.price(productRequest.getPrice())
								.quantity(productRequest.getQuantity())
								.build();
		productRepository.save(product);
		
		log.info("Product Created!!");	
				
				
		return product.getProductId();
	}

	@Override
	public ProductResponse getProductById(long productId) {
		log.info("Get the product for productId: {}",productId);
		Product product=
						productRepository.findById(productId)
						.orElseThrow(()->new ProductServiceCustomException("Product With given Id not found ","PRODUCT_NOT_FOUND"));
		
		ProductResponse productResponse=new ProductResponse();
		//copy all the property of product to product response.
		BeanUtils.copyProperties(product, productResponse);


		return productResponse;
	}

	@Override
	public void reduceQuantity(long productId, long quantity) {
		log.info("Reduce Quantity {} for id {}",quantity,productId);
		Product product=productRepository.findById(productId)
				.orElseThrow(()-> new ProductServiceCustomException("Product With given Id {} is not present","PRODUCT_NOT_FOUND"));
	if(product.getQuantity()<quantity)
	{
		throw  new ProductServiceCustomException("Product does not have sufficient Quantity","INSUFFICIENT_QUANTITY");
	}
	product.setQuantity(product.getQuantity()-quantity);
	productRepository.save(product);
	log.info("Product Quantity Updated Successfully");

	}

	@Override
	public List<Product> getAllProduct() {
		log.info("Get all product ");
		List<Product> productList=productRepository.findAll();


		return productList;
	}

}

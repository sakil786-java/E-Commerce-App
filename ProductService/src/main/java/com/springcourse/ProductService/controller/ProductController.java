package com.springcourse.ProductService.controller;

import com.springcourse.ProductService.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springcourse.ProductService.model.ProductRequest;
import com.springcourse.ProductService.model.ProductResponse;
import com.springcourse.ProductService.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private ProductService productService;
	
	@PostMapping("/add")
	public ResponseEntity<Long> addProduct( @RequestBody ProductRequest productRequest) {
		long productId=productService.addProduct(productRequest);
		
		return new ResponseEntity<>(productId,HttpStatus.CREATED);
	}

	@GetMapping("/all")
	public List<Product>getAllProduct()
	{
		List<Product> productList=productService.getAllProduct();

		return productList;
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse>getProductById(@PathVariable("id") long productId)
	{
		ProductResponse productResponse=productService.getProductById(productId);
		
		return new ResponseEntity<>(productResponse,HttpStatus.OK);
	}

	@PutMapping("/reduceQuantity/{id}")
	public ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity)
	{
		productService.reduceQuantity(productId,quantity);
		return  new ResponseEntity<>(HttpStatus.OK);
	}
	
	
}

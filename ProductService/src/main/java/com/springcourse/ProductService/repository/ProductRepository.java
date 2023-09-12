package com.springcourse.ProductService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springcourse.ProductService.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}

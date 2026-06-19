package com.example.order.controller;

import com.example.order.entity.Product;
import com.example.order.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.example.order.dto.base.BaseResponse;
import com.example.order.enums.ResponseCodeEnum;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public BaseResponse<Page<Product>> getAllProducts(@PageableDefault(size = 10) Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return BaseResponse.success(products, ResponseCodeEnum.SUCCESS_RETRIEVE_DATA);
    }

    @GetMapping("/{id}")
    public BaseResponse<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return BaseResponse.success(product, ResponseCodeEnum.SUCCESS_RETRIEVE_DATA);
    }
}

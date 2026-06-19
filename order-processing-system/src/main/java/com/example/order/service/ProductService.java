package com.example.order.service;

import com.example.order.entity.Product;
import com.example.order.enums.ResponseCodeEnum;
import com.example.order.exception.BusinessException;
import com.example.order.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public void reduceStock(Long id, int quantity) {
        Product product = getProductById(id);
        if (product.getStock() < quantity) {
            throw new BusinessException(ResponseCodeEnum.INSUFFICIENT_STOCK);
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}

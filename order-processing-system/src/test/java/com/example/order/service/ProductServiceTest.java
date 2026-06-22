package com.example.order.service;

import com.example.order.entity.Product;
import com.example.order.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.order.exception.BusinessException;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_Success() {
        Product p = new Product();
        p.setId(1L);
        Page<Product> page = new PageImpl<>(List.of(p));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.getAllProducts(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductById_Success() {
        Product p = new Product();
        p.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Product result = productService.getProductById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> productService.getProductById(1L));
    }

    @Test
    void reduceStock_Success() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        productService.reduceStock(1L, 2);

        assertEquals(8, p.getStock());
        verify(productRepository).save(p);
    }

    @Test
    void reduceStock_InsufficientStock() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessException.class, () -> productService.reduceStock(1L, 2));
    }
}

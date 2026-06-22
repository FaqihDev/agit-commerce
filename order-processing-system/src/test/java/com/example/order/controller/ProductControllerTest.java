package com.example.order.controller;

import com.example.order.entity.Product;
import com.example.order.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProducts_Success() throws Exception {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Product 1");
        p1.setPrice(new BigDecimal("10.00"));
        p1.setStock(100);

        Page<Product> page = new PageImpl<>(List.of(p1));
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Product 1"));
    }

    @Test
    void getProductById_Success() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Product 1");
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(100);

        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Product 1"));
    }
}

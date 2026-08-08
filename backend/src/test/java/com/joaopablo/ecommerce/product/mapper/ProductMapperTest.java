package com.joaopablo.ecommerce.product.mapper;

import com.joaopablo.ecommerce.category.entity.Category;
import com.joaopablo.ecommerce.product.dto.request.CreateProductRequest;
import com.joaopablo.ecommerce.product.dto.request.UpdateProductRequest;
import com.joaopablo.ecommerce.product.dto.response.ProductResponse;
import com.joaopablo.ecommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    void toEntity_shouldMapImageUrl() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Notebook");
        request.setDescription("Gaming laptop");
        request.setPrice(BigDecimal.valueOf(5999.99));
        request.setImageUrl("  https://cdn.example.com/notebook.jpg  ");

        Product product = mapper.toEntity(request);

        assertEquals("https://cdn.example.com/notebook.jpg", product.getImageUrl());
    }

    @Test
    void toEntity_shouldNormalizeBlankImageUrlToNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Notebook");
        request.setPrice(BigDecimal.valueOf(100));
        request.setImageUrl("   ");

        Product product = mapper.toEntity(request);

        assertNull(product.getImageUrl());
    }

    @Test
    void toResponse_shouldIncludeImageUrlAndStockQuantity() {
        Category category = Category.builder().name("Electronics").build();
        category.setId(UUID.randomUUID());

        Product product = Product.builder()
                .name("Notebook")
                .description("Gaming laptop")
                .price(BigDecimal.valueOf(5999.99))
                .active(true)
                .imageUrl("https://cdn.example.com/notebook.jpg")
                .category(category)
                .build();
        product.setId(UUID.randomUUID());

        ProductResponse response = mapper.toResponse(product, 42);

        assertEquals(product.getId(), response.id());
        assertEquals("https://cdn.example.com/notebook.jpg", response.imageUrl());
        assertEquals(42, response.stockQuantity());
        assertEquals(category.getId(), response.categoryId());
        assertEquals("Electronics", response.categoryName());
    }

    @Test
    void updateEntity_shouldClearImageUrlWhenEmptyString() {
        Product product = Product.builder()
                .name("Notebook")
                .price(BigDecimal.valueOf(100))
                .imageUrl("https://cdn.example.com/old.jpg")
                .build();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setImageUrl("");

        mapper.updateEntity(request, product);

        assertNull(product.getImageUrl());
    }

    @Test
    void updateEntity_shouldUpdateImageUrl() {
        Product product = Product.builder()
                .name("Notebook")
                .price(BigDecimal.valueOf(100))
                .imageUrl("https://cdn.example.com/old.jpg")
                .build();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setImageUrl("https://cdn.example.com/new.jpg");

        mapper.updateEntity(request, product);

        assertEquals("https://cdn.example.com/new.jpg", product.getImageUrl());
    }
}

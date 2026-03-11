package com.example.product.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.product.dao.ProductRepository;
import com.example.product.domain.dto.ProductRequestDto;
import com.example.product.domain.dto.ProductResponseDto;
import com.example.product.domain.dto.ProductUpdateDto;
import com.example.product.domain.entity.ProductEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public ProductResponseDto productCreate(ProductRequestDto request, String email) {
        System.out.println("=== product service productCreate ===");

        ProductEntity product = request.toEntity(email);
        return ProductResponseDto.fromEntity(productRepository.save(product));
    }

    public ProductResponseDto productRead(Long productId) {
        System.out.println("=== product service productRead");
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));
        return ProductResponseDto.fromEntity(entity);
    }

    /*
     * Producer(Order) - kafka(topic - update-stock-topic)
     * 
     * @KafkaListener - String message 수신 - ObjectMapper(JSON -> DTO)
     * Consumer(Product)
     * 
     * stockConsumer(String message) 메서드 호출이 발생하여 재고 업데이트
     */
    @KafkaListener(topics = "update-stock-topic")
    public void stockConsumer(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductUpdateDto productUpdateDto = null;
        try {
            productUpdateDto = objectMapper.readValue(message, ProductUpdateDto.class);
            ProductEntity productEntity = productRepository
                    .findById(productUpdateDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않음"));
            productEntity.updateStockQty(productUpdateDto.getQty());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

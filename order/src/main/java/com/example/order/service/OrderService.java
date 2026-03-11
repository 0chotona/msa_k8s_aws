package com.example.order.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.order.dao.OrderRepository;
import com.example.order.domain.dto.OrderRequestDto;
import com.example.order.domain.dto.OrderResponseDto;
import com.example.order.domain.dto.ProductResponseDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductOpenFeignService productOpenFeignService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    /*
     * 1. Client(path: /order/create) -> 주문요청
     * 2. orderFeignKafkaCreate() 실행
     * 3. Feign 상품 조회(동기방식)
     * 4. 재고여부 확인
     * 5. kafka 토픽(KafkaTemplate.send())를 발행
     * 6. kafka broker에 메시지 저장
     * 7. product-service 에서 수신(KafkaListener)하여 stockConsumer()호출
     * 8. 재고감소구현(비동긴)
     */

    // @CircuitBreaker(name="productService", fallbackMethod =
    // "fallbackProductService")
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackProductService")
    public OrderResponseDto orderFeignKafkaCreate(OrderRequestDto request,
            @RequestHeader("X-User-Email") String email) {
        System.out.println("=== order service orderFeginKafkaCreate ===");
        System.out.println("=== 재고 유무 판단을 위해 product-service Feign 통신");
        ProductResponseDto response = productOpenFeignService.getProductId(request.getProductId(), email);
        System.out.println("=== Fieign 통신 결과 수량 : " + response.getStockQty());
        if (response.getStockQty() < request.getQty()) {
            throw new RuntimeException("재고가 없어");
        } else {
            System.out.println("=== order service kafka 토픽 발행");
            kafkaTemplate.send("update-stock-topic", request);
        }
        // 주문저장 --
        return OrderResponseDto.fromEntity(orderRepository.save(request.toEntity(email)));
    }

    public OrderResponseDto fallbackProductService(OrderRequestDto request, String email, Throwable t) {
        throw new RuntimeException("서비스 지연으로 에러 발생... 다시 시도해주세요");
    }
}

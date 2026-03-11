package com.example.product.ctrl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.product.domain.dto.ProductRequestDto;
import com.example.product.domain.dto.ProductResponseDto;
import com.example.product.service.ProductService;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<?> create(ProductRequestDto request,
            @RequestHeader("X-User-Email") String email) {

        System.out.println("=== prodyct ctrl path : /create");
        System.out.println("=== params : " + request);
        System.out.println("=== X-User-Email : " + email);

        ProductResponseDto response = productService.productCreate(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{productId}")
    public ResponseEntity<?> getProductId(@PathVariable("productId") Long productId,
            @RequestHeader("X-User-Email") String email) {

        System.out.println("=== product ctrl path : /productId");
        System.out.println("=== product ctrl - order-service feign connection");

        ProductResponseDto response = productService.productRead(productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

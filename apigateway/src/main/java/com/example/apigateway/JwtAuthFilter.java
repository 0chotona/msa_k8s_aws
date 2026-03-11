package com.example.apigateway;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String secret;
    private Key key;

    private static final List<String> WHITE_LIST_PATHS = List.of(
            "/users/signIn",
            "/product/create",
            "/order/create",
            "/health/alive",
            "/product/list");

    @PostConstruct
    private void init() {
        System.out.println(">>> JwtFilter init jwt secret : " + secret);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=== JwtAuthFilter token validation");
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("=== JwtAuthFilter filter bearerToken : " + bearerToken);

        String endPoint = exchange.getRequest().getURI().getRawPath();
        System.out.println(">>> JwtFilter User Endpoint : " + endPoint);
        String method = exchange.getRequest().getMethod().name();
        System.out.println(">>> JwtFilter User Method : " + method);

        if (WHITE_LIST_PATHS.contains(endPoint)) {
            System.out.println("=== JwtAuthFilter WHITE_LIST_PATHS : " + endPoint);
            return chain.filter(exchange);
        }

        try {
            System.out.println(">>> JwtFilter Authorization : " + bearerToken);
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                System.out.println(">>> JwtFilter Not Authorization : ");
                throw new RuntimeException("JwtAuthFilter token exception");
            }
            String token = bearerToken.substring(7);
            System.out.println("JwtAuthFilter token : " + token);
            // Claims = JWT 데이터
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            System.out.println("=== JwtAuthenticationFilter claims get email : " + email);

            // JWTProvider 의해서 Role 입력된 경우에만 해당
            String role = claims.get("role", String.class);
            System.out.println("=== JwtAuthenticationFilter claims get role : " + role);

            // X-User-Id 변수로 email 값과 Role 추가
            // X custom header 라는 것을 의미하는 관례....

            ServerWebExchange modifyExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Email", email)
                            .header("X-User-Role", "Role_" + role))
                    .build();

            return chain.filter(modifyExchange);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }

}

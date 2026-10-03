package com.sparta.coffee.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Coffee Order System API")
                        .description("대용량 트래픽과 동시성을 고려한 커피 주문 시스템 API 명세서입니다.")
                        .version("1.0.0"));
    }
}

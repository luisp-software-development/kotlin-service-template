package com.luispdev.kotlin_service_template.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {

    @Value($$"${app.cors.allowed-origin}")
    private val allowedOrigin: String? = null

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigin!!)
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
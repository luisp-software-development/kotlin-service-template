package com.luispdev.kotlin_service_template.application.dto

data class PostResponseDTO(
    val id: Long,
    val title: String,
    val content: String?,
)
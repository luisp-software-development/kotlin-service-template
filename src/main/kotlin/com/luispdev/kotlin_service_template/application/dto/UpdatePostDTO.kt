package com.luispdev.kotlin_service_template.application.dto

import com.luispdev.kotlin_service_template.application.error.ErrorMessages
import jakarta.validation.constraints.NotBlank

data class UpdatePostDTO(
    @NotBlank(message = ErrorMessages.POST_TITLE_REQUIRED)
    val title: String?,
    val content: String?,
)
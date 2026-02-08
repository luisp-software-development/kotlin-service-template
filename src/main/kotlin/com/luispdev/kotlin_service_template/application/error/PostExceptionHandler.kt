package com.luispdev.kotlin_service_template.application.error

import com.luispdev.kotlin_service_template.application.dto.ErrorResponseDTO
import com.luispdev.kotlin_service_template.domain.PostNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class PostExceptionHandler(
    private val messageSource: MessageSource
) {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PostNotFoundException::class)
    fun notFound(): ErrorResponseDTO =
        buildResponseDTO(
            code = ErrorMessages.POST_NOT_FOUND
        )

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationExceptions(ex: ConstraintViolationException): ErrorResponseDTO =
        buildResponseDTO(
            code = ex.constraintViolations.first().messageTemplate
        )

    /*@ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun handleUnmappedExceptions(ex: RuntimeException): ErrorResponseDTO =
        buildResponseDTO(
            code = ErrorMessages.INTERNAL_SERVER_ERROR
        )*/

    private fun buildResponseDTO(code: String): ErrorResponseDTO =
        ErrorResponseDTO(
            code = code,
            message = messageSource.getMessage(code, emptyArray(), LocaleContextHolder.getLocale()),
        )
}

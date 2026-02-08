package com.luispdev.kotlin_service_template.domain

import com.luispdev.kotlin_service_template.application.dto.CreatePostDTO
import com.luispdev.kotlin_service_template.application.dto.UpdatePostDTO
import com.luispdev.kotlin_service_template.infrastructure.PostEntity
import com.luispdev.kotlin_service_template.infrastructure.PostRepository
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Service
@Validated
class PostService(
    private val repository: PostRepository,
) {
    fun create(@RequestBody @Valid postDTO: CreatePostDTO): PostEntity =
        repository.save(
            PostEntity(
                title = postDTO.title!!,
                content = postDTO.content,
            )
        )

    fun findAll(): List<PostEntity> =
        repository.findAll()

    fun findById(id: Long): PostEntity =
        repository.findByIdOrNull(id) ?: throw PostNotFoundException()

    fun update(@PathVariable id: Long, @RequestBody @Valid updated: UpdatePostDTO): PostEntity {
        val postEntity = findById(id)
        postEntity.title = updated.title!!
        postEntity.content = updated.content
        return repository.save(postEntity)
    }

    fun delete(@PathVariable id: Long) {
        repository.deleteById(id)
    }
}
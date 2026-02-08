package com.luispdev.kotlin_service_template.application

import com.luispdev.kotlin_service_template.application.dto.CreatePostDTO
import com.luispdev.kotlin_service_template.application.dto.UpdatePostDTO
import com.luispdev.kotlin_service_template.domain.PostService
import com.luispdev.kotlin_service_template.infrastructure.PostEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/posts")
class PostController(
    private val service: PostService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody post: CreatePostDTO): PostEntity =
        service.create(post)

    @GetMapping
    fun getAll(): List<PostEntity> =
        service.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): PostEntity =
        service.findById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody post: UpdatePostDTO): PostEntity =
        service.update(id, post)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}
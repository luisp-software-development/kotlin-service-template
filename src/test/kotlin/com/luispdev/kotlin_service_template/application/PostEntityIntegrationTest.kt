package com.luispdev.kotlin_service_template.application

import com.luispdev.kotlin_service_template.AbstractIntegrationTest
import com.luispdev.kotlin_service_template.application.dto.CreatePostDTO
import com.luispdev.kotlin_service_template.application.dto.UpdatePostDTO
import com.luispdev.kotlin_service_template.application.error.ErrorMessages
import com.luispdev.kotlin_service_template.infrastructure.PostEntity
import com.luispdev.kotlin_service_template.infrastructure.PostRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.instancio.Instancio
import org.instancio.Model
import org.instancio.Select.field
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@AutoConfigureMockMvc
@DisplayName("/posts")
class PostEntityIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var repository: PostRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("Create: should return post")
    @Throws(Exception::class)
    fun givenValidRequestBody_whenPostingPost_shouldReturnCreatedPost() {
        // given
        val validNewPost = Instancio.create(CreatePostDTO::class.java)

        // when
        val response = mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNewPost))
        )

        // then
        response.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.title").value(validNewPost.title))
            .andExpect(jsonPath("$.content").value(validNewPost.content))
    }

    @Test
    @DisplayName("Create: should persist post")
    @Throws(Exception::class)
    fun givenValidNewPostBody_whenPostingPost_shouldPersistIt() {
        // given
        val validNewPost = Instancio.create(CreatePostDTO::class.java)

        // when
        val responseBody = mockMvc.perform(
            MockMvcRequestBuilders.post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNewPost))
        )
            .andReturn()
            .response
            .contentAsString

        val createdPostId = objectMapper.readValue(responseBody, PostEntity::class.java).id!!

        // then
        val persistentPost = repository.findByIdOrNull(createdPostId)
        assertNotNull(persistentPost)
        assertThat(persistentPost.id).isEqualTo(createdPostId)
        assertThat(persistentPost.title).isEqualTo(validNewPost.title)
        assertThat(persistentPost.content).isEqualTo(validNewPost.content)
    }

    @Test
    @DisplayName("Create: should return error when title is blank")
    @Throws(Exception::class)
    fun givenBlankTitle_whenPostingPost_shouldReturnBadRequestWithMatchingErrorMessage() {
        // given
        val blankTitlePost = Instancio.of(CreatePostDTO::class.java)
            .setBlank(field("title"))
            .create()

        // when
        val response = mockMvc.perform(
            post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankTitlePost))
        )

        // then
        response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorMessages.POST_TITLE_REQUIRED))
            .andExpect(jsonPath("$.message").value("Post title is required."))
    }

    @Test
    @DisplayName("Find All: should return all data")
    @Throws(Exception::class)
    fun givenDatabaseWithExistingPosts_whenGettingPosts_shouldReturnAll() {
        // given
        val existingPosts: List<PostEntity> = setupExistingPosts()

        // when
        val response = mockMvc.perform(get("/posts"))

        //then
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(existingPosts.size))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(existingPosts.map { it.id })))
            .andExpect(jsonPath("$[*].title", containsInAnyOrder(existingPosts.map { it.title })))
            .andExpect(jsonPath("$[*].content", containsInAnyOrder(existingPosts.map { it.content })))
    }

    @Test
    @DisplayName("Find All: should return empty array when there's no data")
    @Throws(Exception::class)
    fun givenEmptyDatabase_whenReceivingGetRequest_shouldReturnEmptyArray() {
        // given
        // no setup - empty database

        // when
        val response = mockMvc.perform(get("/posts"))

        //then
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty())
    }


    @Test
    @DisplayName("Find By Id: should return matching post")
    @Throws(Exception::class)
    fun givenExistingPostId_whenGettingPostById_shouldReturnMatchingPost() {
        // given
        val existingPost: PostEntity = setupExistingPost()

        // when
        val response = mockMvc.perform(get("/posts/" + existingPost.id))

        // then
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingPost.id))
            .andExpect(jsonPath("$.title").value(existingPost.title))
            .andExpect(jsonPath("$.content").value(existingPost.content))
    }

    @Test
    @DisplayName("Find By Id: should return not found error when given unknown id")
    @Throws(Exception::class)
    fun givenUnknownPostId_whenGettingPostById_shouldReturnNotFoundError() {
        // given
        val unknownPostId = 999L

        // when
        val response = mockMvc.perform(get("/posts/$unknownPostId"))

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorMessages.POST_NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Post not found."))
    }

    @Test
    @DisplayName("Update By Id: should return updated post")
    @Throws(Exception::class)
    fun givenExistingPostIdAndValidPostChanges_whenUpdatingById_shouldReturnUpdatedPost() {
        // given
        val existingPostId = setupExistingPost().id
        val validPostChanges = Instancio.create(UpdatePostDTO::class.java)

        // when
        val response = mockMvc.perform(
            put("/posts/$existingPostId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPostChanges))
        )

        // then
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(existingPostId))
            .andExpect(jsonPath("$.title").value(validPostChanges.title))
            .andExpect(jsonPath("$.content").value(validPostChanges.content))
    }

    @Test
    @DisplayName("Update By Id: should persist updated post")
    @Throws(Exception::class)
    fun givenExistingPostIdAndValidPostChanges_whenUpdatingById_shouldPersistUpdatedPost() {
        // given
        val existingPostId = setupExistingPost().id!!
        val validPostChanges = Instancio.create(UpdatePostDTO::class.java)

        // when
        mockMvc.perform(
            put("/posts/$existingPostId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPostChanges))
        )

        // then
        val persistentPost = repository.findByIdOrNull(existingPostId)
        assertNotNull(persistentPost)
        assertThat(persistentPost.id).isEqualTo(existingPostId)
        assertThat(persistentPost.title).isEqualTo(validPostChanges.title)
        assertThat(persistentPost.content).isEqualTo(validPostChanges.content)
    }

    @Test
    @DisplayName("Update By Id: should return error when title is blank")
    @Throws(Exception::class)
    fun givenExistingPostIdAndBlankTitleUpdate_whenUpdatingById_shouldReturnBadRequestWithMatchingErrorMessage() {
        // given
        val existingPostId = setupExistingPost().id
        val blankTitleUpdate = Instancio.of(UpdatePostDTO::class.java)
            .setBlank(field("title"))
            .create()

        // when
        val response = mockMvc.perform(
            put("/posts/$existingPostId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankTitleUpdate))
        )

        // then
        response.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorMessages.POST_TITLE_REQUIRED))
            .andExpect(jsonPath("$.message").value("Post title is required."))
    }

    @Test
    @DisplayName("Update By Id: should return not found when given unknown post id")
    @Throws(Exception::class)
    fun givenUnknownPostId_whenUpdatingById_shouldReturnNotFoundError() {
        // given
        val unknownPostId = 999L
        val validPostChanges = Instancio.create(UpdatePostDTO::class.java)

        // when
        val response = mockMvc.perform(
            put("/posts/$unknownPostId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPostChanges))
        )

        // then
        response.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorMessages.POST_NOT_FOUND))
            .andExpect(jsonPath("$.message").value("Post not found."))
    }

    @Test
    @DisplayName("Delete By Id: should return no content")
    @Throws(Exception::class)
    fun givenExistingPostId_whenDeletingPostById_shouldReturnNoContent() {
        // given
        val existingPostId = setupExistingPost().id

        // whenc
        val response = mockMvc.perform(delete("/posts/$existingPostId"))

        // then
        response.andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Delete By Id: should persist removal")
    @Throws(Exception::class)
    fun givenExistingPostId_whenDeletingPostById_shouldPersistRemoval() {
        // given
        val existingPostId = setupExistingPost().id!!

        // when
        mockMvc.perform(delete("/posts/$existingPostId"))

        // then
        assertThat(repository.findById(existingPostId)).isEmpty()
    }

    @Test
    @DisplayName("Delete By Id: should still return no content even if post id is unknown")
    @Throws(Exception::class)
    fun givenUnknownPostId_whenDeletingPostById_shouldReturnNotFoundError() {
        // given
        val unknownPostId = 999L

        // when
        val response = mockMvc.perform(delete("/posts/$unknownPostId"))

        // then
        response.andExpect(status().isNoContent())
    }

    private fun setupExistingPost(): PostEntity =
        repository.save(
            Instancio.of(postModel()).create()
        )


    private fun setupExistingPosts(): List<PostEntity> =
        repository.saveAll(
            Instancio.ofList(postModel()).create()
        )

    private fun postModel(): Model<PostEntity> =
        Instancio.of(PostEntity::class.java)
            .set(field("id"), null)
            .toModel()
}
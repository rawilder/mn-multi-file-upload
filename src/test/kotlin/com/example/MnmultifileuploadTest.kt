package com.example

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.multipart.MultipartBody
import reactor.core.publisher.Flux

@MicronautTest
class MnmultifileuploadTest(
    private val application: EmbeddedApplication<*>,
    @Client("/") private val httpClient: HttpClient
) : StringSpec({

    "test the server is running" {
        assert(application.isRunning)
    }

    // !!
    // this should be failing based on it failing when running the app and making the request manually,
    // but it does not
    // !!
    "test upload endpoint" {
        val body = MultipartBody.builder()
            .addPart("file", "file.json", MediaType.APPLICATION_JSON_TYPE, "{\"title\":\"Foo\"}".toByteArray())
            .build()

        val response = httpClient.toBlocking().exchange(
            HttpRequest.POST("/uploadSuspend", body)
                .contentType(MediaType.MULTIPART_FORM_DATA),
            Int::class.java
        )

        response.status() shouldBe HttpStatus.OK
        response.body.get() shouldBe 15
    }
})

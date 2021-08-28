package com.example

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.StreamingFileUpload
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux

@Controller
class TestController {

    @Post(value = "/upload", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun upload(
        file: StreamingFileUpload
    ): Int {
        return Flux.from(file).map { p -> p.bytes.size }.reduce { f, s -> f + s }.awaitSingle()
    }

    @Post(value = "/uploadMultiple", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun uploadTestMultiple(
        files: Publisher<StreamingFileUpload>
    ): Int? {
        return files.asFlow().map {
            println(it.filename)
            Flux.from(it).map { p -> p.bytes.size }.reduce { f, s -> f + s }.awaitSingle()
        }.reduce { f, s -> (f ?: 0) + (s ?: 0) }
    }
}


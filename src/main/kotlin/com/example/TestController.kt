package com.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.PartData
import io.micronaut.http.multipart.StreamingFileUpload
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitLast
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.concurrent.atomic.LongAdder

@Controller
class TestController {

    fun Publisher<PartData>.asFlow1() = asFlow().buffer(1)

    val logger = LoggerFactory.getLogger(TestController::class.java)

    @Post(value = "/uploadMultiple", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadTestMultiple(
        files: Publisher<StreamingFileUpload>
    ): Publisher<HttpResponse<Long>> {
        return Flux.from(files).subscribeOn(Schedulers.boundedElastic()).flatMap {
            Flux.from(it).onBackpressureBuffer().map { p -> p.bytes }
        }.collect(
            { LongAdder() }
        ) { adder: LongAdder, bytes: ByteArray ->
            adder.add(
                bytes.size.toLong()
            )
        }
            .map { adder: LongAdder ->
                HttpResponse.ok(
                    adder.toLong()
                )
            }
    }

    @Post(value = "/uploadMultipleSuspend", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun uploadTestMultipleSuspend(
        files: Publisher<StreamingFileUpload>
    ): Int {
        return Flux.from(files).subscribeOn(Schedulers.boundedElastic()).flatMap {
            Flux.from(it).map { p -> p.bytes.size }
        }.awaitLast()
    }

    @Post(value = "/uploadMultipleFlow", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun uploadTestMultipleFlow(
        files: Publisher<StreamingFileUpload>
    ): Int {
        return files.asFlow().map {
            it.asFlow1().map {
                it.bytes.size
            }.reduce { f, s -> f + s }
        }.catch { e -> logger.error("error encountered", e) }.reduce { f, s -> f + s }
    }

    @Post(value = "/uploadSuspend", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun uploadSuspend(
        file: StreamingFileUpload
    ): Int {
        return file.asFlow1().buffer(1).withIndex().map { (idx, part) ->
            part.bytes.size
        }.reduce { f, s -> f + s }
    }

    @Post(value = "/uploadSuspendNoBytes", consumes = [MediaType.MULTIPART_FORM_DATA])
    suspend fun uploadSuspendNoBytes(
        file: StreamingFileUpload
    ): Int {
        return file.asFlow1().withIndex().map { (idx, part) ->
            1
        }.reduce { f, s -> f + s }
    }
}


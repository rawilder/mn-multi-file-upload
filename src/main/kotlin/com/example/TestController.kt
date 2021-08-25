package com.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.StreamingFileUpload
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.concurrent.atomic.LongAdder

@Controller
class TestController {

    @Post(value = "/uploadMultiple", consumes = [MediaType.MULTIPART_FORM_DATA])
    fun uploadTestMultiple(
        files: Publisher<StreamingFileUpload>
    ): Publisher<HttpResponse<Long>> {
        return Flux.from(files).subscribeOn(Schedulers.boundedElastic()).flatMap {
            Flux.from(it).map { p -> p.bytes }
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
    ): HttpResponse<Long> {
        return Flux.from(files).subscribeOn(Schedulers.boundedElastic()).flatMap {
            Flux.from(it).map { p -> p.bytes }
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
            }.awaitSingle()
    }
}


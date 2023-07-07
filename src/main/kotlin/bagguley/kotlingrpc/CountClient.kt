package bagguley.kotlingrpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource

class CountClient(private val channel: ManagedChannel) : Closeable {
    private val stub = CounterGrpcKt.CounterCoroutineStub(channel)

    suspend fun count(): CountReply {
        return stub.count(countRequest {  })
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

suspend fun main() {
    val channel = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build()
    val client = CountClient(channel)

    val timeSource = TimeSource.Monotonic
    var start = timeSource.markNow()

    while (true) {
        val response = client.count()

        if (response.count % 5000 == 0L) {
            val duration = timeSource.markNow() - start
            println("Count: ${response.count}, Time: ${duration.inWholeMilliseconds}")
            start = timeSource.markNow()
        }
    }
}

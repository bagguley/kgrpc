package bagguley.kotlingrpc

import io.grpc.Server
import io.grpc.ServerBuilder
import java.util.concurrent.atomic.AtomicLong

class CountServer(private val port: Int) {
    companion object {
        val counter = AtomicLong()
    }

    val server: Server = ServerBuilder
        .forPort(port)
        .addService(CountService())
        .build()

    fun start() {
        server.start()
        println("Server started, on port $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("Shutting down")
                this@CountServer.stop()
                println("Stopped")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    internal class CountService : CounterGrpcKt.CounterCoroutineImplBase() {
        override suspend fun count(request: CountRequest): CountReply {
            return CountReply.newBuilder()
                .setCount(counter.getAndIncrement())
                .build()
        }
    }
}

fun main() {
    val port = 9999
    val server = CountServer(port)
    server.start()
    server.blockUntilShutdown()
}

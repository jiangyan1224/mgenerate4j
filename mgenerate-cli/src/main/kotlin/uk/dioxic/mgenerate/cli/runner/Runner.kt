package uk.dioxic.mgenerate.cli.runner

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import uk.dioxic.mgenerate.cli.extension.*
import uk.dioxic.mgenerate.cli.metric.Summary
import java.util.concurrent.Callable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

@FlowPreview
@ExperimentalTime
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Runner<T>(
        count: Long,
        parallelism: Int,
        val batchSize: Int,
        targetTps: Int = -1,
        monitorLoggingInterval: Duration = 1.seconds,
        hideZeroAndEmpty: Boolean = true,
        producer: () -> T,
        consumer: (List<T>) -> Any) : Callable<Duration> {

//    private val productionDelay = 1000.milliseconds / targetTps

    val flow = flowOf(count, producer)
            .fanOut(parallelism, batchSize, targetTps) {
                lockVariableState {
                    measureTimedResultMetric(it.size) {
                        consumer(it)
                    }
                }
            }
            .monitor(
                    totalExecutions = count,
                    summaryFormat = Summary.SummaryFormat.SPACED,
                    loggingInterval = monitorLoggingInterval,
                    hideZeroAndEmpty = hideZeroAndEmpty
            )

//    val flow = flowOf(count, producer)
//            .buffer(batchSize * 2)
//            .chunked(batchSize)
//            .onEach { delay((productionDelay * it.size).toLongMilliseconds()) }
//            .mapParallel(parallelism) {
//                measureTimedResultMetric(it.size) {
//                    consumer(it)
//                }
//            }
//            .monitor(
//                    totalExecutions = count,
//                    summaryFormat = Summary.SummaryFormat.SPACED,
//                    loggingInterval = monitorLoggingInterval,
//                    hideZeroAndEmpty = hideZeroAndEmpty
//            )

    override fun call() = measureTime {
        runBlocking(Dispatchers.Default) {
            flow.collect { println(it) }
        }
    }

}
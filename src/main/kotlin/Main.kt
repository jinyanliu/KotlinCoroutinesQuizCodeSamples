package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.time.measureTime

suspend fun main() {
    //quiz3()
    //quiz5()
    //quiz7()
    //quiz11()
    //quiz15()
    //quiz17()
    //quiz19()
    //quiz21()
    //quiz23()
    //quiz25()
    //quiz27()
    quiz29()
}

//Quiz1
//Are suspending functions coroutines?
//False
//Suspending a coroutine, not a function
//We suspend a coroutine, not a function. Suspending functions are not coroutines, just functions that can suspend a coroutine.

//Quiz3
//Will "Text" be printed?
//True
//CancellationException does not propagate to its parent
//If an exception is a subclass of CancellationException, it will not be propagated to its parent. It will only cause cancellation of the current coroutine.
suspend fun quiz3() = coroutineScope {
    val job = Job()
    launch(job) {
        delay(1000)
        throw CancellationException("Some error")
    }
    launch(job) {
        delay(2000)
        println("Text")
    }
    delay(3000)
}

//Quiz5
//Does delay(1000L) inside runBlocking block thread?
//True
//runBlocking is a very atypical builder.
//It blocks the thread it has been started on whenever its coroutine is suspended(similar to suspending main). This means that delay(1000L) inside runBlocking will behave like Thread.sleep(1000L.)
fun quiz5() {
    runBlocking {
        delay(1000L)
        println("World!")
    }
    runBlocking {
        delay(1000L)
        println("World!")
    }
    runBlocking {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}

//Quiz7
//Will "Text2" be printed?
//True
//supervisorScope
//supervisorScope overrides the context's Job with SupervisorJob, so it is not cancelled when a child raises an exception.
//class MyException : Throwable()

suspend fun quiz7() = supervisorScope {
    launch {
        delay(1000)
        println("Text1")
        throw MyException()
    }
    launch {
        delay(2000)
        println("Text2")
    }
}

//Quiz9
//Which coroutine context is not inherited by a coroutine from a coroutine?
//Job
//Job
//Job is the only coroutine context that is not inherited by a coroutine from a coroutine. Every coroutine creates its own Job, and the job from an argument or parent coroutine is used as a parent of this new job.

//Quiz11
//Will "Text2" be printed?
//False
//coroutineScope
//coroutineScope is a suspending function that starts a scope. It returns the value produced by the argument function.
class MyException : Throwable()

suspend fun quiz11() = coroutineScope {
    async {
        delay(1000)
        println("Text1")
        throw MyException()
    }

    async {
        delay(2000)
        println("Text2")
    }
}

//Quiz13
//Which dispatcher is designed to run CPU-intensive operations?
//Dispatchers.Default
//Dispatchers.Default
//If you don't set any dispatchers, the one chosen by default is Dispatchers.Default, which is designed to run CPU-intensive operations. It has a pool of threads with a size equal to the number of cores in the machine your code is running on (but no less than two).

//Quiz15
//Will "Caught!" be printed?
//True
//coroutineScope
//If there is an exception in coroutineScope or any of its children, it cancels all other children and rethrow it.
suspend fun quiz15() = coroutineScope {
    try {
        coroutineScope {
            launch {
                delay(1000)
                throw Exception("Exception")
            }
        }
    } catch (e: Exception) {
        println("Caught!")
    }
}

//Quiz17
//Which order will be printed?
//World 1!
//World 2!
//Hello,
//coroutineScope
//Unlike async or launch, the body of coroutineScope is called in-place.
fun quiz17() = runBlocking {
    coroutineScope {
        launch {
            delay(2000L)
            println("World 1!")
        }
    }
    coroutineScope {
        launch {
            delay(2000L)
            println("World 2!")
        }
    }
    println("Hello,")
}

//Quiz19
//How many errors will be caught?
//0
//When a coroutine has its own (independent) job, it has nearly no relation to its parent.
//The parent does not wait for its children because it has no relation with them. This is because the child uses the job from the argument as a parent, so it has no relation to the runBlocking.
fun quiz19() = runBlocking {
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("Caught $throwable")
    }
    val scope = CoroutineScope(SupervisorJob() + handler)
    scope.launch {
        delay(1000)
        throw Error("Some error")
    }
    scope.launch {
        delay(2000)
        throw Error("Some error 2")
    }
}

//Quiz21
//What will be printed?
//12
//A Job can be created without a coroutine using the Job() factory function. It creates a job that isn't associated with any coroutine and can be used as a context. This also means that we can use such a job as a parent of many coroutines.
//A common mistake is to create a job using the Job() factory function, use it as a parent for some coroutines, and then use join on the job. Such a program will never end because Job is still in an active state, even when all its children are finished. This is because this context is still ready to be used by other coroutines.
suspend fun quiz21() = coroutineScope {
    val job = SupervisorJob()
    launch(job) {
        delay(1000)
        print("1")
    }
    launch(job) {
        delay(2000)
        print("2")
    }
    job.join()
    print("3")
}

//Quiz23
//What will be printed?
//XCE
//StateFlow
//Beware that StateFlow is conflated, so slower observers might not receive some intermediate state changes.
suspend fun quiz23(): Unit = coroutineScope {
    val state = MutableStateFlow('X')
    launch {
        for (c in 'A'..'E') {
            delay(300)
            state.value = c
        }
    }

    state.collect {
        delay(1000)
        print(it)
    }
}

//Quiz25
//What will be printed?
//C
//UnconfinedTestDispatcher
//StandardTestDispatcher does not invoke any operations until we use its scheduler. UnconfinedTestDispatcher immediately invokes all the operations before the first delay on started coroutines, which is why the code prints "C".
fun quiz25() {
    CoroutineScope(StandardTestDispatcher()).launch {
        print("A")
        delay(1)
        print("B")
    }
    CoroutineScope(UnconfinedTestDispatcher()).launch {
        print("C")
        delay(1)
        print("D")
    }
}

//Quiz27
//How long does it take?
//Around 1 second
//runBlocking
//runBlocking is a very atypical builder. It blocks the thread it has been started on whenever its coroutine is suspended.
fun quiz27() {
    println(
        measureTime {
            runBlocking {
                async {
                    delay(1000)
                }
                async {
                    delay(1000)
                }
            }
        }
    )
}

//Quiz29
//Will it be cancelled?
//False
//Stopping the unstoppable
//Because cancellation happens at the suspension points, it will not happen if there is no suspension point. The execution needs over 3 minutes, even though it should be cancelled after 1 second.
suspend fun quiz29() = coroutineScope {
    val job = Job()
    launch(job) {
        repeat(1000) { i ->
            Thread.sleep(200)
            println("Printing $i")
        }
    }

    delay(1000)
    job.cancelAndJoin()
    println("Cancelled successfully")
}
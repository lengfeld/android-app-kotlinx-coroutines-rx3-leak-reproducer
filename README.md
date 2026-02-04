# RxScheduler memory leak reproducer

This repository contains an Android Application that demonstrates a memory leak
in the library

    org.jetbrains.kotlinx:kotlinx-coroutines-rx3:1.10.2

in function [CoroutineDispatcher.asScheduler()](https://github.com/Kotlin/kotlinx.coroutines/blob/b11abdf01d4d5db85247ab365abc72efc7b95062/reactive/kotlinx-coroutines-rx3/src/RxScheduler.kt#L31).

See the code in the [MainActivity](app/src/main/java/com/example/leakreproducer/MainActivity.kt).

To see the bug, build and start the application. After some time it will crash
with a `java.lang.OutOfMemoryError` exception.

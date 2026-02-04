package com.example.leakreproducer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx3.asScheduler
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    var disposable : Disposable? = null

    companion object {
        const val TAG = "MainActivity"
        const val BUG_ENALBED = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Centered Content")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val buggyScheduler =  Dispatchers.Default.asScheduler()
        val workingScheduler =  Schedulers.computation()
        
        val s = if (BUG_ENALBED) buggyScheduler else workingScheduler

        disposable = Flowable.interval(1, 1, TimeUnit.SECONDS, Schedulers.computation())
            .doOnNext { Log.d(TAG, "timer tick every 1  second here; create new Flowable") }
            .switchMap {
                val bigObject = ByteArray(10 * 1024 * 1024) // 10 MiB

                Flowable.interval(5, 1, TimeUnit.MINUTES, s)
                    .doOnNext {
                        // The object "bigObject" is captured in this lambda.
                        Log.d(TAG, "Inner Flowable has fired! size=${bigObject.size}")
                    }
            }
            // The following line is never executed. The upstream Flowable is replaced every one second.
            // So it has never a chance to emit it's first element, because it's set to five minutes.
            //
            // What is the bug?
            // The upstream flowable is replaced. Therefore it's disposed. But the implementation in
            // RxScheduler.java does _not_ removed the scheduled Runnable/coroutine from the task queue
            // even so they are disposed.
            // So every second a new runnable/coroutine is added to the task queue that first runs after 5 minutes.
            // These runnables/coroutines queue up instead of being garbage collected. If the runnables
            // contains/reference big objects you have a memory leak that is visible.
            .doOnNext { Log.d(TAG, "Never executed") }
            .subscribe()
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
        disposable = null
    }
}


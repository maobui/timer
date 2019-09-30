package com.me.bui.timer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Transformations.map
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val TIME_UNIT = 1000L
    private var timeCount = 60
    enum class Status(val value: Int) {
        START(0),
        PAUSE(1),
        STOP(2)
    }

    private val tick: BehaviorSubject<Long> = BehaviorSubject.createDefault(0L)
    private val status: BehaviorSubject<Status> = BehaviorSubject.createDefault(Status.STOP)
    private var timePause  = 0
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgReset.setOnClickListener(this)
        imgStartStop.setOnClickListener(this)
        imgReset.visibility = View.INVISIBLE

        compositeDisposable.add(tick.toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d("AAA", "tick--------------$it")
                    progressCircular.progress = it.toInt()
                    tvTime.text = hmsTimeFormatter(it * TIME_UNIT)

                },
                { it.printStackTrace() }
            )
        )
        compositeDisposable.add(status.toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    imgStartStop.setImageDrawable(resources.getDrawable(if (it != Status.STOP) R.drawable.ic_stop else R.drawable.ic_start, this.theme))
                    when(it) {
                        Status.STOP -> {
                            setProgressBarValues()
                        }
                        Status.START -> startTimer(if(timePause > 0) timePause else timeCount)
                        Status.PAUSE -> {
                            disposable?.dispose()
                            edtMinute.isEnabled = false
                            imgReset.visibility = View.INVISIBLE
                            Log.d("AAA", "pause--------------$timeCount  pause $timePause")
                        }
                    }
                }
                , { it.printStackTrace() }
            )
        )
    }



    override fun onDestroy() {
        compositeDisposable.dispose()
        disposable?.takeIf { !it.isDisposed }?.dispose()
        super.onDestroy()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgReset -> onReset()
            R.id.imgStartStop -> onStartStop()
        }
    }

    private fun onReset() {
        status.onNext(Status.START)
    }

    private fun onStartStop() {
        when (status.value) {
            Status.START -> status.onNext(Status.PAUSE)
            Status.PAUSE -> status.onNext(Status.START)
            Status.STOP -> {
                setTimer()
                setProgressBarValues()
                status.onNext(Status.START)
            }
        }
    }

    fun startTimer( timeInSecond: Int) {
        disposable =  Flowable.interval(0, 1, TimeUnit.SECONDS)
            .take((timeInSecond + 1).toLong())
            .doOnNext { Log.d("AAA", "doOnNext--------------$timeInSecond  it $it") }
            .map { it -> timeInSecond - it - 1
            }
            .filter{ aLong -> aLong >= 0 && status.value == Status.START }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                edtMinute.isEnabled = false
                imgReset.visibility = View.INVISIBLE
                imgStartStop.setImageDrawable(resources.getDrawable(R.drawable.ic_stop, this.theme))
            }
            .doFinally {
                edtMinute.isEnabled = true
                imgReset.visibility = View.VISIBLE
                imgStartStop.setImageDrawable(resources.getDrawable(R.drawable.ic_start, this.theme))
                if (timePause == 0) status.onNext(Status.STOP)
            }
            .subscribe(
                {
                    tick.onNext(it)
                    timePause = it.toInt()
                    Log.d("AAA", "--------------$it  pause $timePause value ${status.value}")
                },
                { it.printStackTrace() }

            )
    }

    private fun setTimer() {
        if (edtMinute.text.isNullOrBlank()) {
            Toast.makeText(this, "Timer must not null", Toast.LENGTH_LONG).show()
        } else {
            timeCount = edtMinute.text.toString().toInt()
        }
    }

    private fun setProgressBarValues() {
        progressCircular.max = timeCount
        progressCircular.progress = timeCount
    }


    private fun hmsTimeFormatter(milliSeconds: Long): String {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliSeconds),
            TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
            TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)))
    }

}

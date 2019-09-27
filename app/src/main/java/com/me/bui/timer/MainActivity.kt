package com.me.bui.timer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var countDownTimer: CountDownTimer? = null
    val TIME_INTERVAL = 1000L
    var timeCount = 60000L
    var status: Status = Status.STOP
    enum class Status(val value: Int) {
        START(0),
        STOP(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgReset.setOnClickListener(this)
        imgStartStop.setOnClickListener(this)
        imgReset.visibility = View.INVISIBLE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgReset -> onReset()
            R.id.imgStartStop -> onStartStop()
        }
    }

    private fun onReset() {
        stopTimer()
        startTimer()
    }

    private fun onStartStop() {
        imgStartStop.setImageDrawable(resources.getDrawable(if (status == Status.STOP) R.drawable.ic_stop else R.drawable.ic_start, this.theme))
        if(status == Status.STOP) {
            setTimer()
            setProgressBarValues()
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        imgReset.visibility = View.INVISIBLE
        countDownTimer = object : CountDownTimer(timeCount, TIME_INTERVAL) {
            override fun onFinish() {
                status = Status.STOP
                setProgressBarValues()
                imgReset.visibility = View.VISIBLE
            }

            override fun onTick(tick: Long) {
                tvTime.text = hmsTimeFormatter(tick)
                progressCircular.progress = (tick / 1000).toInt()
            }

        }
        status = Status.START
        countDownTimer?.start()
    }

    private fun stopTimer() {
        status = Status.STOP
        countDownTimer?.cancel()
    }

    private fun  setTimer() {
        if(edtMinute.text.isNullOrBlank()) {
            Toast.makeText(this, "Timer must not null", Toast.LENGTH_LONG).show()
        } else {
            timeCount = edtMinute.text.toString().toLong() * TIME_INTERVAL
        }
    }

    private fun setProgressBarValues() {
        progressCircular.max = (timeCount / 1000).toInt()
        progressCircular.progress = (timeCount / 1000).toInt()
    }



    private fun hmsTimeFormatter(milliSeconds: Long): String {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliSeconds),
            TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
            TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
            )
        )
    }

}

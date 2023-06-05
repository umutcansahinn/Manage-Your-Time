package com.umutcansahin.manageyourtime.ui.count_down_timer_screen

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.umutcansahin.manageyourtime.common.HUNDRED
import com.umutcansahin.manageyourtime.common.ZERO
import com.umutcansahin.manageyourtime.common.convertToMinuteAndSecond
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

class CountDownTimerViewModel : ViewModel() {

    var timerStartValue: Long = 0
    var timerPauseValue: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning: Boolean = false

    private val _state = MutableStateFlow(State())
    val state = _state.asSharedFlow()

    fun startTimer(time: String?) {
        if (!isTimerRunning) {
            if (time.isNullOrBlank().not() && time.equals("0").not()) {
                timer(timerPauseValue)
                isTimerRunning = true
            } else {
                _state.value = State().copy(isTimeNullOrBlank = true)
            }
        }
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    fun resetTimer() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
            timerPauseValue = 0
            isTimerRunning = false
        }
        _state.value = State(
            textViewTime = timerStartValue.convertToMinuteAndSecond(),
            textInputTimeIsEnable = true,
            textInputTimeIsFocusable = true,
            isTimeNullOrBlank = false
        )
    }

    private fun timer(pauseTime: Long) {
        countDownTimer = object : CountDownTimer(
            timerStartValue - pauseTime,
            Long.HUNDRED
        ) {
            override fun onTick(millisUntilFinished: Long) {
                _state.value = State().copy(
                    textViewTime = millisUntilFinished.convertToMinuteAndSecond(),
                    textInputTimeIsEnable = false,
                    textInputTimeIsFocusable = false,
                    isTimeNullOrBlank = false
                )
                timerPauseValue = timerStartValue - millisUntilFinished
            }

            override fun onFinish() {
                resetTimer()
                _state.value = State(textViewTime = "done")
            }
        }.start()
    }
}

data class State(
    val textViewTime: String = Long.ZERO.convertToMinuteAndSecond(),
    val textInputTimeIsEnable: Boolean = true,
    val textInputTimeIsFocusable: Boolean = true,
    val isTimeNullOrBlank: Boolean = false
)
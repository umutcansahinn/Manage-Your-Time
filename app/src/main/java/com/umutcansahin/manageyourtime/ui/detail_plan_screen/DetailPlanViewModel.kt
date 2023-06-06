package com.umutcansahin.manageyourtime.ui.detail_plan_screen

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umutcansahin.manageyourtime.common.*
import com.umutcansahin.manageyourtime.data.local.PlanEntity
import com.umutcansahin.manageyourtime.domain.usecase.AddOrDeleteFromFavoriteUseCase
import com.umutcansahin.manageyourtime.domain.usecase.DeletePlanUseCase
import com.umutcansahin.manageyourtime.domain.usecase.GetPlanEntityByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailPlanViewModel(
    private val getPlanEntityByIdUseCase: GetPlanEntityByIdUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val addOrDeleteFromFavoriteUseCase: AddOrDeleteFromFavoriteUseCase
) : ViewModel() {

    private val _getEntityById = MutableStateFlow<Resource<PlanEntity>>(Resource.Loading)
    val getEntityById = _getEntityById.asStateFlow()

    private val _deleteEntity = MutableStateFlow<RoomResponse>(RoomResponse.Loading)
    val deleteEntity = _deleteEntity.asStateFlow()

    private val _addOrDeleteFromFavorite = MutableStateFlow<RoomResponse>(RoomResponse.Loading)
    val addOrDeleteFromFavorite = _addOrDeleteFromFavorite.asStateFlow()

    private val _state2 = MutableStateFlow(DetailState())
    val state2 = _state2.asSharedFlow()

    var timerStartValue: Long = 0
    var timerPauseValue: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning: Boolean = false
    var isFavorite = false


    fun getPlanEntityById(entityId: Int) {
        viewModelScope.launch {
            getPlanEntityByIdUseCase(entityId).collect {
                _getEntityById.value = it
            }
        }
    }

    fun deletePlan(entity: PlanEntity) {
        viewModelScope.launch {
            deletePlanUseCase(entity).collect {
                _deleteEntity.value = it
            }
        }
    }

    fun addOrDeleteFromFavorite(entity: PlanEntity) {
        viewModelScope.launch {
            addOrDeleteFromFavoriteUseCase(entity).collect {
                _addOrDeleteFromFavorite.value = it
            }
        }
    }

    fun startTimer() {
        if (!isTimerRunning) {
            timer(timerPauseValue)
            isTimerRunning = true
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
        _state2.value = DetailState(
            textViewTime = timerStartValue.convertToMinuteAndSecond(),
            isTimeNullOrBlank = false
        )
    }

    private fun timer(pauseTime: Long) {
        countDownTimer = object : CountDownTimer(
            timerStartValue - pauseTime,
            Long.HUNDRED
        ) {
            override fun onTick(millisUntilFinished: Long) {
                _state2.value = DetailState().copy(
                    textViewTime = millisUntilFinished.convertToMinuteAndSecond(),
                    isTimeNullOrBlank = false
                )
                timerPauseValue = timerStartValue - millisUntilFinished
            }

            override fun onFinish() {
                resetTimer()
                _state2.value = DetailState().copy(isTimeFinish = true)
            }
        }.start()
    }
}
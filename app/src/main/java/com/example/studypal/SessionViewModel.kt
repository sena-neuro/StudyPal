package com.example.studypal

import android.os.CountDownTimer
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SessionViewModel() : ViewModel(), LifecycleObserver {
    companion object {
        private const val ONE_SECOND = 1000 //0 // 1000 milliseconds
        private const val ONE_MINUTE = 1 // 60 seconds
    }
    private lateinit var sessionCountDownTimer: CountDownTimer
    private lateinit var breakCountDownTimer: CountDownTimer
    private var _remainingSession : Int = 5
    private var _sessionMinutes : Long = 25
    private var _breakMinutes : Long = 5

    private var flag = true

    val remainingSessionCount  = MutableLiveData<Int>()
    val milisChangeNotifier = MutableLiveData<Long>()
    val onSession = MutableLiveData<Boolean>()

    // private val remainingMinutes = MutableLiveData<Long>()
    // private val remainingSeconds = MutableLiveData<Long>()

    fun createTimers(remainingSession: Int, sessionMinutes : Long, breakMinutes: Long){
        if(flag) {
            this._remainingSession = remainingSession
            this._sessionMinutes = sessionMinutes
            this._breakMinutes = breakMinutes
            onSession.value = true
            sessionCountDownTimer = object : CountDownTimer(_sessionMinutes*ONE_MINUTE*ONE_SECOND,1000){
                override fun onTick(millisUntilFinished: Long) {
                    milisChangeNotifier.value = millisUntilFinished // was postValue before
                }
                override fun onFinish() {
                    remainingSessionCount.value = --_remainingSession
                    onSession.value = false
                    breakCountDownTimer.start()
                }
            }
            breakCountDownTimer = object : CountDownTimer(_breakMinutes* ONE_MINUTE*ONE_SECOND,1000){
                override fun onTick(millisUntilFinished: Long) {
                    milisChangeNotifier.value = millisUntilFinished
                }
                override fun onFinish() {
                    onSession.value = true
                    sessionCountDownTimer.start()
                }
            }
        }
    }
    fun startTimers(){
        if(flag){
            sessionCountDownTimer.start()
            flag = false
        }
    }

    //@OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun closeCall(){
        sessionCountDownTimer.cancel()
        breakCountDownTimer.cancel()
    }


}
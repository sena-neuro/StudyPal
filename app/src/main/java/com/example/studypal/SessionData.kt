package com.example.studypal

data class SessionData(val sessionDurationMins:Int, val totalSessionDurationMins:Int,val sessionCount:Int,
                       val leftOnSession:Boolean, val breakDurationMins:Int) {
    constructor() : this(0, 0, 0, false, 0)
}
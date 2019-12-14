package com.example.studypal

data class SessionData(val sessionDurationMins:Int, val totalSessionDurationMins:Int,val sessionCount:Int,
                       val leftOnSession:Boolean, val breakDurationMins:Int, val sessionDate:Long,val sessionScore:Float=0f) {
    constructor() : this(0, 0, 0,
        false, 0,0, 0F)
}
package com.example.studypal

import java.util.*

data class User(
    var username: String? = "",
    var email: String?= "",
    var phoneNumber: String?="",
    var birthDate: Date?
    )
{
    constructor() : this("", "","",null
    )
}


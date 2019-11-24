package com.example.studypal

import java.util.*

data class User(
    var username: String? = "",
    var email: String?= "",
    var birthDate: Date?,
    var phoneNumber: String?=""
)
{
    constructor() : this("", "",null,""
    )
}


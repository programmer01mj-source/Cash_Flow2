package com.example.cashflow

object UserSession {
    var token: String = ""
    var email: String = ""
    var name: String = ""
    var role: String = ""
    var phone: String = "+265 99 123 4567"
    var nationalId: String = ""
    var dob: String = ""

    fun clear() {
        token = ""
        email = ""
        name = ""
        role = ""
        phone = "+265 99 123 4567"
        nationalId = ""
        dob = ""
    }
}

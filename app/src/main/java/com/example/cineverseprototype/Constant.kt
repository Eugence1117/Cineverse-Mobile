package com.example.cineverseprototype

class Constant {
    companion object{
        const val WEB_SERVICE_DOMAIN_NAME:String = "domainName"
        const val APPLICATION_PREFERENCES:String = "cineverse_app_pref"
        const val CAMERA_PERMISSION:String = "cineverse_camera_permission"

        const val REMEMBER_OPTION = "rememberCredential"
        const val PASSWORD_KEY = "userPassword"
        const val USERNAME_KEY = "userName"

        const val PASSWORD_MIN_LENGTH = 6
        const val PASSWORD_MAX_LENGTH = 18
        const val TAX_PERCENTAGE = 0.05

        const val INTERNET_BANKING_CODE = 0
        const val CARD_PAYMENT_CODE = 1

        const val PAYMENT_PENDING_STATUS_CODE = 0
        const val PAYMENT_PAID_STATUS_CODE = 1
        const val PAYMENT_COMPLETED_STATUS_CODE = 2
        const val PAYMENT_PENDING_REFUND_STATUS_CODE = 3
        const val PAYMENT_REFUND_STATUS_CODE = 4
        const val PAYMENT_CANCELLED_STATUS_CODE = -1

        const val SET_COOKIE_KEY = "Set-Cookie"
        const val COOKIE_KEY = "Cookie"
        const val SESSION_COOKIE = "JSESSIONID"
    }
}
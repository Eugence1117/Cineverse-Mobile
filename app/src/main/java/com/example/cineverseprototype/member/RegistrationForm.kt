package com.example.cineverseprototype.member

import com.example.cineverseprototype.volley.FileDataPart
import java.io.File
import java.util.*

class RegistrationForm(
    var name:String,
    var username:String,
    var dateOfBirth: Date,
    var password:String,
    var email:String,
    var picture: FileDataPart?
){
    constructor(name:String,username:String,dateOfBirth:Date,password:String,email:String):this(name,username,dateOfBirth,password,email,null)

    public fun toMap():Map<String,String>{
        return mapOf<String,String>(
            "name" to name,
            "username" to username,
            "dateOfBirth" to dateOfBirth.time.toString(),
            "password" to password,
            "email" to email
        )
    }
}
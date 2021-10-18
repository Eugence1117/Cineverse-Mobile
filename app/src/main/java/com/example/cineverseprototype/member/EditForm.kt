package com.example.cineverseprototype.member

class EditForm(val fullName:String,val dateOfBirth:Long) {

    public fun toMap():Map<String,String>{
        return mapOf<String,String>(
            "name" to fullName,
            "dateOfBirth" to dateOfBirth.toString(),
        )
    }
}
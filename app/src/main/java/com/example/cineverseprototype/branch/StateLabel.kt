package com.example.cineverseprototype.branch

class StateLabel(val stateName:String, val totalBranch:Int) {

    fun showSum():String{
        return "Total Branch(s): $totalBranch"
    }
}
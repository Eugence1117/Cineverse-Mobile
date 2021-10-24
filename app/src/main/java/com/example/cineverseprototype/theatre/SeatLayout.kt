package com.example.cineverseprototype.theatre

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class SeatLayout(val layoutId:String,val seatCol:Int,val seatRow:Int,val rows:ArrayList<SeatRow>) {

    fun convertToColumn():ArrayList<Any>{
        val  columns = ArrayList<Any>()
        //Add a Default Label Row first
        for(col in 0 until seatCol+1){ //One extra column for display row
            if(col != 0){
                columns.add(SeatLabel(col.toString(),false))
            }
            else{
                columns.add(SeatLabel("",false))
            }
        }

        for(r in 0 until seatRow){
            val rowLabel = (65+r).toChar().toString()

            var isRowFound = false
            for(row in rows){
                if(rowLabel == row.rowLabel){
                    isRowFound = true
                    val seatColumns = row.columns

                    val rowName = row.rowLabel
                    var skipNext = false
                    for(i in 0 until seatCol+1){

                        if(!skipNext){
                            if(i == 0){
                                columns.add(SeatLabel(rowLabel,false))
                                skipNext = false
                            }
                            else{
                                val seatNum = "${rowName}${i}"

                                var isFound = false
                                for(col in seatColumns){
                                    if(col.seatNum == seatNum){
                                        isFound = true
                                        columns.add(col)
                                        if(!col.reference.isNullOrEmpty()){
                                            skipNext = true
                                        }
                                    }
                                }
                                if(!isFound){
                                    skipNext = false
                                    columns.add(SeatLabel("",true))
                                }
                            }
                        }
                        else{
                            skipNext = false
                        }
                    }
                }
            }
            if(!isRowFound){
                for(i in 1 until seatCol+2){
                    if(i == 1){
                        columns.add(SeatLabel(rowLabel,false))
                    }
                    else{
                        columns.add(SeatLabel("",true))
                    }
                }
            }
        }
        return columns
    }

    companion object{

        private val TAG = javaClass.name

        fun toObject(obj: JSONObject):SeatLayout?{
            return try{
                val layoutId = obj.getString("layoutId")
                val seatRow = obj.getInt("seatRow")
                val seatCol = obj.getInt("seatCol")
                val rowList = obj.getJSONArray("seatLayout")

                val rows = ArrayList<SeatRow>()
                for(i in 0 until rowList.length()){
                    val row = rowList[i] as JSONObject
                    rows.add(SeatRow.toObject(row))
                }

                SeatLayout(layoutId,seatCol,seatRow,rows)

            } catch (ex: JSONException){
                Log.e(TAG,ex.stackTraceToString())
                null
            }
        }
    }

    class SeatRow(val rowLabel:String, val columns:ArrayList<SeatCol>){
        companion object{
            fun toObject(obj : JSONObject):SeatRow{
                val rowLabel = obj.getString("rowLabel")
                val columnList = obj.getJSONArray("column")

                val columns = ArrayList<SeatCol>()
                for(i in 0 until columnList.length()){
                    val column = columnList[i] as JSONObject
                    columns.add(SeatCol.toObject(column))
                }
                return SeatRow(rowLabel,columns)
            }
        }
    }

    class SeatCol(val seatNum:String, val isBind:Boolean, val reference:String, val isSelected:Boolean){

        companion object{
            fun toObject(obj:JSONObject):SeatCol{
                val seatNum = obj.getString("seatNum")
                val isBind = obj.getBoolean("isBind")
                val reference = obj.getString("reference")
                val isSelected = obj.getBoolean("isSelected")

                return SeatCol(seatNum,isBind,reference,isSelected)
            }
        }
    }
}
package com.kang.administrator.andongworkforphone.startTrunk

import com.google.gson.Gson

class StartTruckClass (){

    fun transForJson(result:List<WagonsInfoIn>):String{
        return Gson().toJson(result)
    }

    data class WagonsInfoIn(var line:Int,var wagon:List<WagonInfoIn>)
    data class WagonInfoIn(var idx:String,var number:String,var type:String,var go:String,var state:String)
}
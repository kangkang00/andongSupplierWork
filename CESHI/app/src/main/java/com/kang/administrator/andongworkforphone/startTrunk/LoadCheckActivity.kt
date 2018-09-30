package com.kang.administrator.andongworkforphone.startTrunk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.CaptureActivity
import com.kang.administrator.andongworkforphone.R
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_load_check.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.toast
import java.net.URL

class LoadCheckActivity : AppCompatActivity() {

    private val TAG = "kang"+javaClass.simpleName

    var isScannerVin=true
    var VIN:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_check)
        ok_bt.setOnClickListener {

            if (isScannerVin){
                initializeNullView()
                startActivityForResult(Intent().setClass(applicationContext,CaptureActivity::class.java).putExtra("PROCESS","350"),2)
            }

        }
    }

    fun checkLoadVin(vin:String){
        async {
            var urlstr= CommonStings.GENURL.url+ "loadCheck.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID=${supID}&&usrID=${usrID}&&qrCode=${vin}")
            Log.i(TAG,"urlstr=="+urlstr)
            var url= URL(urlstr)
            var resultText=url.readText()
            Log.i(TAG,"resultText=="+resultText)
            var result= Gson().fromJson<LoadCheck>(resultText, LoadCheck::class.java)
            if (result!=null){
                Log.i(TAG,"result.code=="+result.code)
                if(result.code=="100"){
                    runOnUiThread {
                        toast("用户不存在")
                    }
                }else if(result.code=="300"){
                    runOnUiThread {
                        toast("无此车辆")
                    }
                }else if(result.code=="200"||result.code=="400"){
                    runOnUiThread {
                        toast("XIANSHI")
                        initializeView(result)
                    }

                }
//                toast(result.message)
//                ok_bt.text="扫描"
                isScannerVin=true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 350) {
            if (requestCode == 2) {
                var three = data!!.extras.get("message")
                //设置结果显示框的显示数值
                VIN=three.toString()
                Log.i(TAG,"result.supID=="+VIN)

                checkLoadVin(VIN!!)
            }
        }
    }

    fun initializeView(result:LoadCheck){
        start_tv.text=result.start
        end_tv.text=result.end?:""
        vin_tv.text=result.VIN
        time_tv.text=result.time
        car_load_state_tv.text=result.mess
    }
    fun initializeNullView(){
        start_tv.text=""
        end_tv.text=""
        vin_tv.text=""
        time_tv.text=""
        car_load_state_tv.text=""
    }


    data class LoadCheck(var code:String,var mess:String,var VIN:String,var start:String,var end:String,var time:String,var wagon:String,var local:String,var count:String)
}

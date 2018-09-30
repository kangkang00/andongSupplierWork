package com.kang.administrator.andongworkforphone.startTrunk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.R
import com.kang.administrator.andongworkforphone.WorkControlActivity
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_trunk.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.toast
import java.net.URL

class TrunkActivity : AppCompatActivity() {

    private val TAG = "kang"+javaClass.simpleName

    var VIN:String?=null
    var isRefresh:Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trunk)

        getReceive()

    }


    fun getReceive(){
        async {
            var urlstr= CommonStings.GENURL.url+ "begLoad.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
            var url= URL(urlstr)
            var isRCCRefresh=true
            while (isRefresh){
                var resultText=url.readText()
                var result= Gson().fromJson<BegLoad>(resultText, BegLoad::class.java)
                if (result!=null){
                    Log.i(TAG,"result.state===urlstr=="+result)
                    VIN=result.VIN
                    when(result.code){
                        "100"->{

                            runOnUiThread{
                                initializeVuew(null)
                                toast("暂无车辆")
                                isRCCRefresh=true
                                orcodeWebView.visibility= View.GONE
                                all_rl.visibility=View.VISIBLE
                            }
                        }

                        "300"->{
                            runOnUiThread{
                                initializeVuew(result)
                                ok_bt.text="收车"
                                isRCCRefresh=true
                                orcodeWebView.visibility= View.GONE
                                all_rl.visibility=View.VISIBLE
                                ok_bt.setOnClickListener {
                                    prvload(VIN!!)
                                }
                            }
                        }

                        "320"->{
                            runOnUiThread{
                                initializeVuew(result)
                                ok_bt.text="交车"
                                isRCCRefresh=true
                                orcodeWebView.visibility= View.GONE
                                all_rl.visibility=View.VISIBLE
                                ok_bt.setOnClickListener {
                                    completeload(VIN!!)
                                }
                            }
                        }

                        "310"->{

                            if (isRCCRefresh){
                                isRCCRefresh=false

                                runOnUiThread{
                                    ok_bt.text="准备交车"
                                    ok_bt.setOnClickListener {
                                        startRCCcode(VIN!!)
                                    }

                                }
                            }
                        }
                    }
                }
                delay(5000L)

            }

        }

    }


    fun prvload(vin:String){
        async {
            var urlstr= CommonStings.GENURL.url+ "prvLoad.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID+"&&VIN="+vin)
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<CompleteLoad>(resultText, CompleteLoad::class.java)
            Log.i(TAG,result.toString())
            if (result!=null){
                when(result.code){

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRefresh=false
    }

    fun completeload(vin:String){
        async {
            var urlstr= CommonStings.GENURL.url+ "completeLoad.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID+"&&VIN="+vin)
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<CompleteLoad>(resultText, CompleteLoad::class.java)
            Log.i(TAG,result.toString())
            if (result!=null){
                when(result.code){

                }
            }
        }
    }

    fun initializeVuew(result:BegLoad?){
        if (result==null){
            begStat_tv.text=""
            endStat_tv.text=""
            vin_tv.text=""
            wareNo_tv.text=""
            wagonNo_tv.text=""
            wagonLocaNo_tv.text=""
            waitCount_tv.text=""
        }else{
            begStat_tv.text=result?.begStat
            endStat_tv.text=result?.endStat
            vin_tv.text=result?.VIN
            wareNo_tv.text=result?.wagonNo
            wagonNo_tv.text=result?.wagonNo
            wagonLocaNo_tv.text=result?.wagonLoca
            waitCount_tv.text=result?.waitCount
        }

    }


    fun startRCCcode(VIN: String){

        var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        var supID=database.selectInfo("supID")
        var usrID=database.selectInfo("usrID")
        orcodeWebView.settings.javaScriptEnabled = true
        all_rl.visibility=View.GONE
        orcodeWebView.visibility= View.VISIBLE
        orcodeWebView.setWebViewClient(WebViewClient())

        orcodeWebView.loadUrl("${CommonStings.GENURL.url}startDelivery.php?str=${VIN};${supID};${usrID};;;")
    }


    data class BegLoad(var code:String,var message:String,var VIN:String,var begStat:String,var endStat:String,var wareNo:String,var wagonNo:String,var wagonLoca:String,var waitCount:String,var time:String)
    data class CompleteLoad(var code:String,var message:String,var VIN:String,var start:String,var VDC:String,var time:String)

}

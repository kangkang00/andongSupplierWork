package com.kang.administrator.andongworkforphone

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_start_receive.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.net.URL
import java.util.*

class StartReceiveActivity : AppCompatActivity() ,View.OnClickListener{

    val ACTIVITYCODE=1
    val PROCESS="100"
    var isJieChe=true
    var isCaptureActivity=true
    var isRefresh:Boolean=true


    var isFrash=false
    private val TAG = "kang"+javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_receive)
        Log.i("kang","activity_start_receive===onCreate")
        getReceive()
    }

    fun getReceive(){

        async {
            var urlstr= CommonStings.GENURL.url+ "vdcReceiveState.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
//            var VIN = intent.extras["VIN"].toString()
//            Log.i("kang","vdcReceiveState===urlstr=="+5)
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
//            Log.i("kang","vdcReceiveState===urlstr=="+urlstr)
//            if(VIN!=null){
//                urlstr+=("&&VIN="+VIN)
//            }
            var url= URL(urlstr)
            loop@ while (isRefresh){
                var resultText=url.readText()
                var result= Gson().fromJson<VdcReceiveState>(resultText, VdcReceiveState::class.java)
                if (result!=null){
                    Log.i(TAG,"result.state===urlstr=="+result)
                        when(result.state){
                            "300"->{
                                runOnUiThread{
                                    start_tv.text=result.start
                                    location_tv.text=result.wareNum
                                    vdc_tv.text=result.VDC
                                    vin_text.text=result.VIN
                                    time_tv.text=result.time
                                    ok_bt.text="接车"
                                    isJieChe=true
                                    wait_rl.visibility=View.GONE
                                    orcodeWebView.visibility=View.GONE
                                    ok_bt.setOnClickListener(this@StartReceiveActivity)
                                    zhisun_bt.setOnClickListener(this@StartReceiveActivity)
                                }
                            }
                            "400"->{
                                runOnUiThread{
                                    wait_rl.visibility=View.GONE
                                    orcodeWebView.visibility=View.GONE
                                    if(isCaptureActivity)startActivityForResult(Intent(applicationContext,CaptureActivity::class.java).putExtra("PROCESS",PROCESS),ACTIVITYCODE)

                                }
                            }
                            "500"->{
                                runOnUiThread{
                                    start_tv.text=result.start
                                    location_tv.text=result.wareNum
                                    vdc_tv.text=result.VDC
                                    vin_text.text=result.VIN
                                    time_tv.text=result.time
                                    ok_bt.text="交车"
                                    isJieChe=false
                                    wait_rl.visibility=View.GONE
                                    ok_bt.setOnClickListener(this@StartReceiveActivity)
                                    zhisun_bt.setOnClickListener(this@StartReceiveActivity)
                                }

                            }
                            "600"->{
                                runOnUiThread{
                                    wait_rl.visibility=View.GONE
                                    startRCCcode(result.VIN)
                                }

                            }
                            "700"->{
                                runOnUiThread{
                                    orcodeWebView.visibility=View.GONE
                                    wait_rl.visibility=View.VISIBLE

                                }
                            }
                        }
                }
                delay(5000L)

            }

        }

    }


    fun confirmReceive(){
        async {
            var urlstr= CommonStings.GENURL.url+ "startReceive.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID+"&&"+"VIN="+vin_text.text)
            Log.i(TAG,"result.state===="+urlstr)

            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<StartReceive>(resultText, StartReceive::class.java)
            if (result!=null){
                Log.i(TAG,"result.state===="+result.code)
                runOnUiThread{
                    when(result.code){
                        "100"->{
                            Toast.makeText(applicationContext,"用户不存在", Toast.LENGTH_LONG).show()
                        }
                        "200"->{
                            startRCCcode(result.VIN)
                        }

                        "300"->{
                            Toast.makeText(applicationContext,"无法找到对应的调度", Toast.LENGTH_LONG).show()
                        }

                    }
                }


            }

        }

    }



    override fun onClick(p0: View?) {
        when(p0){
            ok_bt->{

                if(isJieChe){
                    confirmReceive()
                }else{
                    startRCCcode(vin_text.text.toString())
                }
            }
            zhisun_bt->{
                startActivityForResult(Intent(applicationContext,ZhiSunActivity::class.java),ACTIVITYCODE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2) {
                         if (requestCode == ACTIVITYCODE) {
                                 var three = data!!.extras.get("message")
                             isCaptureActivity=false
                             getReceive()
                                //设置结果显示框的显示数值
                             Toast.makeText(applicationContext,"data!!.extras.get(\"message\")=="+three, Toast.LENGTH_LONG).show()

                            }
                     }
    }


    override fun onRestart() {
        super.onRestart()
        isRefresh=true
        isCaptureActivity=false
        getReceive()
    }

    override fun onPause() {
        super.onPause()
        isRefresh=false
    }

    fun startRCCcode(VIN: String){

        var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        var supID=database.selectInfo("supID")
        var usrID=database.selectInfo("usrID")
        orcodeWebView.settings.javaScriptEnabled = true
        orcodeWebView.visibility=View.VISIBLE
        orcodeWebView.setWebViewClient(WebViewClient())

        orcodeWebView.loadUrl("${CommonStings.GENURL.url}startDelivery.php?str=${VIN};${supID};${usrID};;;")
    }

    data class VdcReceiveState(var state:String,var message:String,var VIN:String,var start:String,var VDC:String,var time:String,var wareNum:String)
    data class StartReceive(var code:String,var message:String,var VIN:String,var WareNumber:String,var PlanTime:String)
}

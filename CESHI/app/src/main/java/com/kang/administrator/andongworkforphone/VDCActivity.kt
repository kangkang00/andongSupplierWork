package com.kang.administrator.andongworkforphone

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_vdc.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.toast
import java.net.URL

class VDCActivity : AppCompatActivity() {
    val CURRSTATE1="100"
    val CURRSTATE2="200"
    val CURRSTATE3="300"
    var VIN:String?=null
    private val TAG = "kang"+javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vdc)
        VIN=intent.extras["VIN"].toString()
        start_text.text=intent.extras.get("start").toString()
        end_text.text=intent.extras.get("end").toString()
        vin_text.text=VIN
        time_text.text=intent.extras.get("time").toString()
        ok_bt.setOnClickListener{VDCjieche()}
        var vch3:String=if(intent.extras.get("vch3")==null){"上海汽车"}else{intent.extras.get("vch3").toString()}
        zhisun_bt.setOnClickListener { startActivity(Intent(this,ZhiSunActivity::class.java).putExtra("VIN",VIN).putExtra("vch3",vch3)) }
    }

    fun getState(){
        when(intent.extras["currState"]){
            "100"->{toast("请接车")}
            "200"->{startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("state",CURRSTATE2).putExtra("VIN",VIN))}
            "300"->{startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("state",CURRSTATE3).putExtra("VIN",VIN))}
        }
    }

    fun VDCjieche(){
        checkVDCState()
        async {
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            var url= URL("${CommonStings.GENURL.url}vdcShort.php?VIN=${VIN}&&supID=${supID}&&usrID=${usrID}")
            Log.i(TAG,"VDCjieche()->result==="+url)
            var resultText=url.readText()
            var result= Gson().fromJson<VdcShort>(resultText, VdcShort::class.java)
            Log.i(TAG,"VDCjieche()->result==="+result)
            if(result.state.equals("200")){
                runOnUiThread(){
                    startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("VIN",VIN).putExtra("state",CURRSTATE1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }

    }


    fun checkVDCState() {
        var database = DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        var supID = database.selectInfo("supID")
        var usrID = database.selectInfo("usrID")
        var urlstr = "${CommonStings.GENURL.url}vdcState.php?supID=${supID}&&usrID=${usrID}&&postData=${VIN};"
        Log.i(TAG, "checkVDCState()->urlstr" + urlstr)
        async {
            var url = URL(urlstr)
            var resultText = url.readText()
            var result = Gson().fromJson<MapGaoDeActivity.VDCStateData>(resultText, MapGaoDeActivity.VDCStateData::class.java)
            when(result.state){
                "300"->{
                    runOnUiThread(){
                        startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("VIN",VIN).putExtra("state",CURRSTATE1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
                "400"->{
                    runOnUiThread(){
                        startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("VIN",VIN).putExtra("state",CURRSTATE1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
                "500"->{
                    runOnUiThread(){
                        startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("VIN",VIN).putExtra("state",CURRSTATE1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
                "600"->{
                    runOnUiThread(){
                        startActivity(Intent(applicationContext,MapGaoDeActivity::class.java).putExtra("VIN",VIN).putExtra("state",CURRSTATE1).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }

        }
    }
}

    data class VdcShort(var state:String,var start:String)
}

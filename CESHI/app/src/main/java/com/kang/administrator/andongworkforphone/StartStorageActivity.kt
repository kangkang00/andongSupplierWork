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
import kotlinx.android.synthetic.main.activity_start_storage.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.net.URL

class StartStorageActivity : AppCompatActivity() ,View.OnClickListener{

    val STORAGECODE=2
    val PROCESS="200"

    private val TAG = "kang"+StartStorageActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_storage)
        getReceive()
    }

    fun getReceive() {
        async {
            var urlstr = CommonStings.GENURL.url + "satWareState.php?"
            var database = DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID = database.selectInfo("supID")
            var usrID = database.selectInfo("usrID")
            urlstr += ("supID=" + supID + "&&" + "usrID=" + usrID)

            var url = URL(urlstr)
            Log.i(TAG,"result.state====="+url)
            var resultText = url.readText()
            Log.i(TAG,"result.state====="+resultText)
            var result = Gson().fromJson<SatWareState>(resultText, SatWareState::class.java)
            if (result != null) {
                runOnUiThread {
                    Log.i(TAG,"result.state====="+result)
                    when (result.code) {
                        "200" -> {
                            startActivityForResult(Intent(applicationContext, CaptureActivity::class.java).putExtra("PROCESS", PROCESS), STORAGECODE)
                        }
                        "300" -> {
                            Toast.makeText(applicationContext, "400==" + result.toString(), Toast.LENGTH_LONG).show()
                            start_tv.text=result.start
                            vin_text.text=result.VIN
                            location_tv.text=result.WareNumber
                            time_tv.text=result.time
                            ok_bt.setOnClickListener(this@StartStorageActivity)
                            zhisun_bt.setOnClickListener(this@StartStorageActivity)
                        }

                    }
                }
            }
        }
    }


    fun confirmReceive() {
        async {
            var urlstr = CommonStings.GENURL.url + "satWareReceive.php?"
            var database = DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID = database.selectInfo("supID")
            var usrID = database.selectInfo("usrID")
            urlstr += ("supID=" + supID + "&&" + "usrID=" + usrID +"&&"+ "VIN=" + vin_text.text)
            Log.i(TAG,"urlstr=="+urlstr)
            var url = URL(urlstr)
            var resultText = url.readText()
            var result = Gson().fromJson<StartReceive>(resultText, StartReceive::class.java)
            if (result != null) {
                runOnUiThread {
                    when (result.code) {
                        "100" -> {
                            Toast.makeText(applicationContext, "用户不存在", Toast.LENGTH_LONG).show()
                        }
                        "200" -> {
                            //startActivityForResult(Intent(applicationContext,CaptureActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK),STORAGECODE)
                            startActivity(Intent(applicationContext, WorkControlActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        "300" -> {
                            Toast.makeText(applicationContext, "无法找到对应的调度", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            ok_bt -> {
                confirmReceive()
            }
            zhisun_bt -> {
                startActivityForResult(Intent(applicationContext, ZhiSunActivity::class.java), STORAGECODE)
            }
        }
    }
    override fun onRestart() {
        super.onRestart()
        getReceive()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 2) {
            if (requestCode == STORAGECODE) {
                var three = data!!.extras.get("message")
                //设置结果显示框的显示数值
                Toast.makeText(applicationContext, "data!!.extras.get(\"message\")==" + three, Toast.LENGTH_LONG).show()
                vin_text.text=three.toString()
                getReceive()
            }
        }
    }

    data class SatWareState(var code: String, var message: String, var VIN: String, var start: String, var WareNumber: String, var time: String)
    data class StartReceive(var code: String, var message: String, var VIN: String, var WareLocal: String, var time: String)
}

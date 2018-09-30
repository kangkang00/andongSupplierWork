package com.kang.administrator.andongworkforphone

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.startTrunk.BanCheVDCActivity
import com.kang.administrator.andongworkforphone.startTrunk.LoadCheckActivity
import com.kang.administrator.andongworkforphone.startTrunk.TrunkActivity
import com.kang.administrator.andongworkforphone.startTrunk.WagonNmbActivity
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_work_control.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.net.URL

class WorkControlActivity : AppCompatActivity() {

    private val TAG = "kang"+javaClass.simpleName
    private var first: String by com.kang.administrator.andongworkforphone.model.Preference(this, "first", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_control)

        //startActivity(Intent(this,ZhuangCheActivity::class.java))
//        val countries = listOf("Russia", "USA", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia", "Japan", "Australia")
//        shouAlert("hahahha",countries)
        getTask()
    }


    override fun onRestart() {
        //getTask()
        super.onRestart()
    }

    fun shouAlert(titles:String,item:List<String>){
//        alert {
//            title = titles
//        }

        selector(titles, item) { _, i ->
            toast("So you're living in ${item[i]}, right?")
            testtextview.text=item[i]
        }
    }


    fun getTask(){
        async {

            var urlstr= CommonStings.GENURL.url+ "getTask.php?"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<GetTaskresult>(resultText,GetTaskresult::class.java)

            Log.i(TAG,"result.state====="+result.state)
            first=result.state
            runOnUiThread {
                when(result.state){
                    "0"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "10"->Toast.makeText(applicationContext,"该用户没有权限"+result.state,Toast.LENGTH_LONG).show()
                    "100"->startActivity(Intent(applicationContext,VDCActivity::class.java).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "150"->startActivity(Intent(applicationContext,BanCheVDCActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "200"->startActivity(Intent(applicationContext,StartReceiveActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "230"->startActivity(Intent(applicationContext,StartStorageActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "260"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "300"->startActivity(Intent(applicationContext, TrunkActivity::class.java).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "350"->startActivity(Intent(applicationContext, LoadCheckActivity::class.java).putExtra("state",result.state).putExtra("start",result.start).putExtra("end",result.end).putExtra("VIN",result.VIN).putExtra("SID",result.SID).putExtra("time",result.time).putExtra("currState",result.currState).putExtra("vch3",result.vch3))
                    "400"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "500"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "600"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "700"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "800"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "900"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                    "2000"->startActivity((Intent(applicationContext, WagonNmbActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("state",result.state)))
                    "2010"->startActivity((Intent(applicationContext,WagonNmbActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("state",result.state)))
                    "3000"->Toast.makeText(applicationContext,"没有任务"+result.state,Toast.LENGTH_LONG).show()
                }
            }

        }
    }


    data class GetTaskresult(var state:String,var start:String,var end:String,var VIN:String,var SID:String,var time:String,var currState:String,var vch3:String)

}

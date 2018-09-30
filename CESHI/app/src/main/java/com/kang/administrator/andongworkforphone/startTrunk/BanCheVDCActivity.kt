package com.kang.administrator.andongworkforphone.startTrunk

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.R
import com.kang.administrator.andongworkforphone.ZhiSunActivity
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_ban_che_vdc.*
import kotlinx.android.synthetic.main.item_banche.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.toast
import java.net.URL

class BanCheVDCActivity : AppCompatActivity() {

    var arrayList = ArrayList<CarState>();
    private val TAG = "kang"+javaClass.simpleName
    var state:String?=null
    var myAdapter: MyAdapter?=null
    var isRefresh:Boolean=true
    var database:DBUtils?= null
    var supID:String?=null
    var usrID:String?=null
    var wichItem=-1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ban_che_vdc)
        initView();
    }
    fun initView() {
        database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        supID=database!!.selectInfo("supID")
        usrID=database!!.selectInfo("usrID")
        myAdapter = MyAdapter(arrayList,this);
        myAdapter!!.setListeren { i, s ->
            if (i!=-1){
                wichItem=i
                startRCCcode(s)
                Log.i(TAG,"i, s ->  "+i+" , "+s)
            }

        }
        banche_car_lv.adapter = myAdapter
        getReceive()
        ok_bt.setOnClickListener {
            when(state){
                "10"->{
                    getResiveCars()
                }
                "100"->{
                    affirmResiveCars() }
                "400"->{
                    getResiveCars()
                }
            }
        }
    }

    fun getReceive() {
        async{
            var urlstr= CommonStings.GENURL.url+ "vdcWoodenState.php?"
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
            var url= URL(urlstr)
            loop@ while (isRefresh){
                var resultText=url.readText()
                Log.i(TAG,"resultText=="+resultText)
                var result= Gson().fromJson<VdcWoodenState>(resultText, VdcWoodenState::class.java)
                if (result!=null){
                    state=result.code
                    runOnUiThread {
                        when(result.code){
                            "10"->{
                                path_ll.visibility=View.GONE
                                ok_bt.text="接车"}
                            "100"->{
                                path_ll.visibility=View.VISIBLE
                                start_tv.text=result.name
                                end_tv.text=result.satName
                                ok_bt.text="确认接车"
                            }
                            "200"->{
                                path_ll.visibility=View.VISIBLE
                                start_tv.text=result.name
                                end_tv.text=result.satName
                                ok_bt.visibility=View.GONE}
                            "300"->{
                                path_ll.visibility=View.VISIBLE
                                start_tv.text=result.name
                                end_tv.text=result.satName
                                ok_bt.visibility=View.GONE}
                            "400"->{
                                path_ll.visibility=View.VISIBLE
                                start_tv.text=result.name
                                end_tv.text=result.satName
                                ok_bt.visibility=View.VISIBLE
                                ok_bt.text="重新接车" }
                        }
                    }
                    if(result.list!=null){
                        runOnUiThread {
                            if((result.code=="200"||result.code=="300")&&wichItem!=-1&&result.list[wichItem].state=="300"){
                                if (result.code=="300"){
                                    wichItem=-1
                                }
                                orcodeWebView.visibility=View.GONE
                            }
                            myAdapter!!.list= result.list
                            myAdapter!!.notifyDataSetChanged()
                        }

                    }

                }
                delay(5000L)
            }

        }

    }

    fun getResiveCars(){
        async{
            var urlstr= CommonStings.GENURL.url+ "vdcWoodenCar.php?"
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
            var url= URL(urlstr)
            var resultText=url.readText()
            Log.i(TAG,"getResiveCars() resultText==${resultText}")
            var result= Gson().fromJson<VdcWoodenState>(resultText, VdcWoodenState::class.java)
            Log.i(TAG,"getResiveCars() result==${result.toString()}")
            if (result!=null){
                state=result.code
                when(result.code){
                    "200"->{}
                    "300"->{}
                    "400"->{
                        runOnUiThread {
                           toast(result.message)
                        }
                    }
                    "500"->{}
                }

                if(result.list!=null){
                    runOnUiThread {
                        myAdapter!!.list= result.list
                        myAdapter!!.notifyDataSetChanged()
                    }
                }

            }

        }
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


    fun affirmResiveCars(){
        async{
            var urlstr= CommonStings.GENURL.url+ "vdcChkWoodCar.php?"
            urlstr+=("supID="+supID+"&&"+"usrID="+usrID)
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<VdcWoodenState>(resultText, VdcWoodenState::class.java)
            if (result!=null){
                Log.i(TAG,"result.state===urlstr=="+result)
                state=result.code
                when(result.code){
                    "200"->{}
                    "300"->{}
                }

            }

        }
    }


    override fun onRestart() {
        super.onRestart()
        getReceive()
    }



    class MyAdapter(var list: ArrayList<CarState>?, var ctx:Context) : BaseAdapter() {

        lateinit var mListen: (Int,String) -> Unit // 声明mListen是一个函数（单方法接口）,入参类型自拟，无返回值

        fun setListeren(listener: (Int,String) -> Unit){
            this.mListen = listener
            this.mListen(-1,"") //等于 mListen?.invoke(0)  X()等同于X.invoke()

        }

        override fun getCount(): Int {
            return list!!.size
        }
        override fun getItemId(position: Int): Long {
            return position.toLong();
        }
        override fun getItem(position: Int): Any {
            return list!!.get(position);
        }

//        public fun setLists(newlist: ArrayList<CarState>) {
//            this.list = newlist
//        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder? = null
            var view: View
            if (convertView == null) {
                view = View.inflate(ctx, R.layout.item_banche, null);
                viewHolder = ViewHolder(view)
                view.tag = viewHolder;
            } else {
                view = convertView;
                viewHolder = view.tag as ViewHolder
            }
            val item = getItem(position) as CarState

            when(item.state){
                "100"->{viewHolder.bt_go.text="质损"}
                "200"->{viewHolder.bt_go.text="验车"}
                "300"->{viewHolder.bt_go.text="等待验车"}
                "400"->{viewHolder.bt_go.text="已交车"}

            }

            viewHolder.vintv.text = item.vin
            viewHolder.timetv.text = item.time
            viewHolder.bt_go.setOnClickListener {
                when(item.state){
                    "100"->{
                        ctx.startActivity(Intent(ctx,ZhiSunActivity::class.java).putExtra("VIN",item.vin))
                    }
                    "200"->{
                        mListen.invoke(position,item.vin)
                    }
                    "300"->{}
                    "400"->{}

                }
            }
            return view!!
        }
    }

    class ViewHolder(var viewItem: View) {
        var vintv: TextView = viewItem.vin_tv
        var bt_go: TextView = viewItem.zhisun_bt
        var timetv: TextView = viewItem.time_tv

    }

    data class VdcWoodenState(var code:String,var message:String,var vdc:String,var name:String,var start:String,var satName:String,var schCode:String,var count:String,var list:ArrayList<CarState>)
    data class CarState(var vin:String,var time:String,var state:String)

}
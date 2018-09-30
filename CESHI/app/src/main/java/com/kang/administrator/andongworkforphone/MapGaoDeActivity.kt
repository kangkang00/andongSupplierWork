package com.kang.administrator.andongworkforphone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import com.amap.api.maps.AMap
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.MyLocationStyle
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_map_gao_de.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.net.URL

class MapGaoDeActivity : Activity() {

    var VIN:String?=null

    var database:DBUtils?=null
    var supID:String?=null
    var usrID:String?=null

    var isFrash=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.setApiKey("55b1494f0f2e170df1f83319b150444d")
        setContentView(R.layout.activity_map_gao_de)
        VIN=intent.extras["VIN"].toString()
        Log.i("kang","onCreate->VIN==="+VIN)
        database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        supID=database!!.selectInfo("supID")
        usrID=database!!.selectInfo("usrID")
        checkVDCState()



        button_close.setOnClickListener {
            button_close.visibility = View.GONE
            orcodeWebView.visibility = View.VISIBLE
            startRCCcode()

            
        }



        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        map.onCreate(savedInstanceState)

        var aMap: AMap? = null
        if (aMap == null) {
            aMap = map.map
        }

        val myLocationStyle: MyLocationStyle
        myLocationStyle = MyLocationStyle()//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。

        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）

        //myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap!!.myLocationStyle = myLocationStyle//设置定位蓝点的Style
        aMap.isMyLocationEnabled = true// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。


    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        map.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        map.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        map.onSaveInstanceState(outState)
    }

    fun checkVDCState(){
        var urlstr="${CommonStings.GENURL.url}vdcState.php?supID=${supID}&&usrID=${usrID}&&postData=${VIN};"
        Log.i("kang","checkVDCState()->urlstr"+urlstr)
        async {
            var isRefresh:Boolean=true
            var i=0
            loop@ while (isRefresh){

                delay(5000L)
                var url= URL(urlstr)
                var resultText=url.readText()
                var result= Gson().fromJson<VDCStateData>(resultText,VDCStateData::class.java)

                when(result.state){
                    "300"->{

                    }
                    "400"->{
                        if(isFrash){
                            runOnUiThread{
                                button_close.visibility=View.GONE
                                orcodeWebView.visibility=View.VISIBLE
                                isFrash=!isFrash
                                startRCCcode()
                            }
                        }

                    }
                    "500"->{
                        runOnUiThread{
                            button_close.visibility=View.GONE
                            orcodeWebView.visibility=View.GONE
                            wait_rl.visibility=View.VISIBLE
                        }
                    }
                    "600"->{
                        runOnUiThread{
                            startActivity(Intent(applicationContext,WorkControlActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        break@loop
                    }
                }

            }
        }
    }

    fun startRCCcode(){
        orcodeWebView.settings.javaScriptEnabled = true
        orcodeWebView.setWebViewClient(WebViewClient())

        orcodeWebView.loadUrl("${CommonStings.GENURL.url}startDelivery.php?str=${VIN};${supID};${usrID};;;")
    }



    data class VDCStateData(var state:String,var message:String)
}

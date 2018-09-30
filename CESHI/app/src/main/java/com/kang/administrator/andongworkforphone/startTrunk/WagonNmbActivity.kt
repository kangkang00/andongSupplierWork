package com.kang.administrator.andongworkforphone.startTrunk

import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.R
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_wagon_nmb.*
import kotlinx.android.synthetic.main.item_spinner_chedao.view.*
import kotlinx.android.synthetic.main.train_item_layout.view.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.image
import org.jetbrains.anko.toast
import java.net.URL
import com.kang.administrator.andongworkforphone.utils.CommonStings as CS

class WagonNmbActivity : AppCompatActivity(){
    private val TAG = "kang"+javaClass.simpleName
    var mrc:Drawable?=null
    var chedao=0
    var index=0
    var startWagonInfo:StartWagonInfo?=null
    var wagonArrays: List<StartTruckClass.WagonsInfoIn>?=null
    var start:String?="尧化门"
    var cargoList: List<String>?=null
    var gowhere:String=""



    //stata 2000 车皮录入 2010 确认车皮
    var stata:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wagon_nmb)
        ll_infomation.visibility= View.VISIBLE
        stata=intent.extras["state"].toString()
        Log.i(TAG,"state==="+stata)
        if(stata=="2010"){
            save_tv.text="铁路调度"
        }
        train_vp.offscreenPageLimit=3
        train_vp.pageMargin=0
        spin_zhandian.adapter=null

        var fruitStrings = getResources().getStringArray(R.array.wagontype)
        wagon_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var wagonEditString=wagon_et.text.toString()
                if(wagonEditString.length==7){

                    for (i in 0..fruitStrings.size-1){
                        if(wagonEditString.substring(0,3) in fruitStrings[i]){
                            var type=fruitStrings[i].split(",")[1]
                            wagontype_tv.text=type
                            wagonArrays!![chedao].wagon[index].number=wagonEditString
                            wagonArrays!![chedao].wagon[index].type=type
                            wagonArrays!![chedao].wagon[index].go=gowhere
                        }
                    }

                }else{
                    wagontype_tv.text=""
                }
            }
        })
        var view=layoutInflater.inflate(android.R.layout.simple_list_item_1,null)

        wagonState_sp.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1, listOf("待装","损坏","已发"))
        mrc=getDrawable(R.drawable.wagon)

        var adapter=MyAdapter(layoutInflater,mrc!!)
        adapter.setListeren {
            index=it
            try {
                wagon_et.setText(wagonArrays!![chedao].wagon[index].number)
                wagon_et.setSelection(wagon_et.text.toString().length)

            }catch (e:Exception){

            }

        }

        var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        var supID=database.selectInfo("supID")
        var usrID=database.selectInfo("usrID")

        getStartWagonInfo(supID!!, usrID!!)

        spin_chedao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    chedao=position
                    index=1
                    changeTrain(adapter,chedao,startWagonInfo!!)
                }catch (e:Exception){

                }

            }

        }



        train_vp.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
                Log.i(TAG,"state==="+state)
                if(state==0){
                    adapter.views!!.chexiang_iv.image=getDrawable(R.drawable.unwagon)
                    cargoList!!.forEachIndexed { i, s ->
                        if (s==wagonArrays!![chedao].wagon[index].type){
                            spin_zhandian.setSelection(i)
                        }
                    }
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Log.i(TAG,"position=="+position)
            }
        })

        save_tv.setOnClickListener {
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            when(stata){
                "2000"->{
                    toast("2000")
                    saveWagonInfo(supID!!, usrID!!)
                }
                "2010"->{
                    toast("2010")
                    checkWagonInfo(supID!!,usrID!!)
                }
            }
        }

        ll_infomation.setOnTouchListener { view, motionEvent ->
            wagon_et.setFocusable(false)
            wagon_et.isFocusableInTouchMode=true
            return@setOnTouchListener true
        }
    }

    fun saveWagonInfo(supID:String,usrID:String){
        async {
            var urlstr = CS.GENURL.url + "saveWagonInfo.php?supID=${supID}&&usrID=${usrID}&&start=${start}&&wagonInfo={\"wagonArray\":${StartTruckClass().transForJson(wagonArrays!!)}}"
            Log.i(TAG,urlstr)
            var url = URL(urlstr)
            var resultText = url.readText()
            Log.i(TAG,"startWa-gonInfo.php?supID="+resultText)
            var result = Gson().fromJson<SaveWagonInfo>(resultText, SaveWagonInfo::class.java)
            if(result!=null){
                 runOnUiThread {
                     toast(result.code)
                 }

            }
        }
    }

    fun checkWagonInfo(supID:String,usrID:String){
        async {
            var urlstr = CS.GENURL.url + "scheduleWagon.php?supID=${supID}&&usrID=${usrID}&&start=${start}&&wagonInfo={\"wagonArray\":${StartTruckClass().transForJson(wagonArrays!!)}}"
            Log.i(TAG,"startWagonInfo.php?supID="+urlstr)
            var url = URL(urlstr)
            var resultText = url.readText()
            Log.i(TAG,"startWagonInfo.php?supID="+resultText)
            var result = Gson().fromJson<ScheduleWagon>(resultText, ScheduleWagon::class.java)
            if (result!=null){
                runOnUiThread {
                    toast(result.code)
                }

            }
        }
    }

    fun getStartWagonInfo(supID:String,usrID:String) {
        async {
            var urlstr = CS.GENURL.url + "startWagonInfo.php?supID=${supID}&&usrID=${usrID}"
            var url = URL(urlstr)
            var resultText = url.readText()
            Log.i(TAG,"startWagonInfo.php?supID="+resultText)
            var result = Gson().fromJson<StartWagonInfo>(resultText, StartWagonInfo::class.java)
            if(result.start!=null){
                runOnUiThread {
                    Log.i(TAG,"startWagonInfo.php?supID=result=="+result)
                    startWagonInfo=result
                    wagonArrays=prepareTrainInfo(result)
                    start=result.name
                    spin_chedao.adapter=MyArryAdapter(LayoutInflater.from(this@WagonNmbActivity),result)
                    getStartCarGo(supID,usrID)
                }
            }else{
                toast("没有调度信息")
            }

        }
    }

    fun getStartCarGo(supID:String,usrID:String){
        async {
            var urlstr = CS.GENURL.url + "startCarGo.php?supID=${supID}&&usrID=${usrID}&&start=${start}"
            var url = URL(urlstr)
            var resultText = url.readText()
            Log.i(TAG,"startWagonInfo.php?supID="+resultText)
            var result = Gson().fromJson<StartCarGos>(resultText, StartCarGos::class.java)
            if(result!=null){
                runOnUiThread {
                    cargoList=carGo(result)
                    gowhere=cargoList!![0]
                    setArrydapter()

                }
            }

        }
    }

    fun setArrydapter(){
        spin_zhandian.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,cargoList)

        spin_zhandian.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                gowhere=cargoList!![p2]
            }

        }
    }

    fun carGo(result:StartCarGos):List<String>{
        var cargo= mutableListOf<String>()
        for (go in result.start){
            cargo.add(go.go)
        }
        return cargo
    }


    fun changeTrain(adapter:MyAdapter,position:Int,startWagonInfo:StartWagonInfo){
        adapter.trainLAGE=startWagonInfo.start[position].Line.size
        train_vp.adapter=adapter
    }

    fun prepareTrainInfo(startWagonInfo:StartWagonInfo): List<StartTruckClass.WagonsInfoIn>? {
        var wagonArray=mutableListOf<StartTruckClass.WagonsInfoIn>()
        for (i in 0..startWagonInfo.start.size-1){
           var wagons=mutableListOf<StartTruckClass.WagonInfoIn>()
            for (j in 0..startWagonInfo.start[i].Line.size-1){
                wagons.add(StartTruckClass.WagonInfoIn(""+j,startWagonInfo.start[i].Line[j].number?:"",startWagonInfo.start[i].Line[j].type?:"",startWagonInfo.start[i].Line[j].end?:"",startWagonInfo.start[i].Line[j].state?:""))
            }
            wagonArray.add(StartTruckClass.WagonsInfoIn((i+1),wagons))
        }
        return wagonArray
    }

    class MyArryAdapter(var inflater: LayoutInflater,var result:StartWagonInfo): BaseAdapter(){
        var nums= listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var view =inflater.inflate(R.layout.item_spinner_chedao,null)
            view.textView.text="第${nums[p0]}道"
            return view
        }

        override fun getItem(p0: Int): Any {
            return result.start[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 5
        }

        override fun getCount(): Int {
            return result.start.size
        }


    }



    class MyAdapter(inflater: LayoutInflater, mrc:Drawable): PagerAdapter() {
        private val TAG = "kang"+javaClass.simpleName
        var minflater: LayoutInflater
        var mrcs:Drawable
        var views:View?=null
        var trainLAGE: Int=0
        init {
            minflater=inflater
            mrcs=mrc
        }
        val name:String = "Person"
        lateinit var mListen: (Int) -> Unit // 声明mListen是一个函数（单方法接口）,入参String，无返回值

        fun setListeren(listener: (Int) -> Unit){
            this.mListen = listener
            this.mListen(0) //等于 mListen?.invoke("invoke :" +name)  X()等同于X.invoke()

        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view==`object`
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            //super.destroyItem(container, position, `object`)
            container!!.removeView(`object`as View)
        }

        override fun getCount(): Int {
            return trainLAGE
        }

        override fun setPrimaryItem(container: ViewGroup?, position: Int, `object`: Any?) {
            super.setPrimaryItem(container, position, `object`)
            views = `object` as View
            views!!.chexiang_iv.image=mrcs
            mListen.invoke(position)
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            Log.i(TAG,"position=instantiateItem="+position)
            val view=minflater.inflate(R.layout.train_item_layout,null)
            container!!.addView(view)
            return view
        }

        fun getPrisentViews(): View? {
            return views
        }

    }

    data class StartWagonInfo(var start:List<Line>,var name:String)
    data class Line(var Line:List<WagonInfo>)
    data class WagonInfo(var number:String,var end:String,var type:String,var state:String)
    data class SaveWagonInfo(var code:String,var message:String)
    data class ScheduleWagon(var code:String,var errorList:List<Idx>)
    data class Idx(var indx:String)

    data class StartCarGos(var start:List<StartCarGo>)
    data class StartCarGo(var go:String,var count:Int)


}

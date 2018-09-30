package com.kang.administrator.andongworkforphone

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.Remotye.ProgressRequestBody
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_zhi_sun.*
import kotlinx.android.synthetic.main.zhi_sun_pic.view.*
import kotlinx.coroutines.experimental.async
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*

class ZhiSunActivity : AppCompatActivity() ,View.OnClickListener{
    //var VCH3:String?=null
    var VIN:String?=null
    var partsresult:QDPart? = null
    var partresult:List<Subpart>? = null
    private var first: String by com.kang.administrator.andongworkforphone.model.Preference(this, "first", "")
    var zhiSunResult= mutableListOf<String>("","","","","","","","","","","","","","","","","","  ")
    var paras= mutableListOf<String>("vin","supID","usrID","int0","vch1","vch2","vch3","vch4","vch6","vch7","vch12","vch13","vch14","vch16","vch19","txt0","txt3","dat1")
    var parasCN= mutableListOf<String>("VIN 号","供应商名称","员工名称","公里数","质损部位编号","质损分部位编号","质损类型编号","质损程度编号","发现步骤","质损发现环节","质损原因分析",
            "质损原因大类","处理方式","责任人","二级供方","具体原因描述","照片","确定日期")

    private val TAG = "kang"+javaClass.simpleName


    override fun onClick(p0: View?) {
        when(p0){
            locations_ll->{
                var partslist=getnameslist(partsresult!!.partList) as List<CharSequence>
                selector("请选择：",partslist){_HorizontalScrollView,i->
                    locations_tv.text=partslist[i]
                    partresult=partsresult!!.partList[i].orientList
                    location_ll.visibility=View.VISIBLE
                    reset()
                }
            }
            location_ll->{
                var partlist=getnamelist(partresult!!) as List<String>
                selector("请选择：",partlist){_HorizontalScrollView,i->
                    zhiSunResult[4]=partresult!![i].code
                    location_tv.text=partlist[i]
                }
            }
            type_ll->{
                var typelist=getnamelist(partsresult!!.typeList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[6]=partsresult!!.typeList[i].code
                    type_tv.text=typelist[i]
                }
            }
            level_ll->{
                var typelist=getnamelist(partsresult!!.levelList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[7]=partsresult!!.levelList[i].code
                    level_tv.text=typelist[i]
                }
            }
            sub_ll->{
                var typelist=getnamelist(partsresult!!.subPartList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[5]=partsresult!!.subPartList[i].code
                    sub_tv.text=typelist[i]
                }
            }
            reason_ll->{
                var typelist=getnamelist(partsresult!!.reasonList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[11]=partsresult!!.reasonList[i].code
                    reason_tv.text=typelist[i]
                }
            }
            section_ll->{
                var typelist=getnamelist(partsresult!!.sectionList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[9]=partsresult!!.sectionList[i].code
                    section_tv.text=typelist[i]
                }
            }
            process_ll->{
                var typelist=getnamelist(partsresult!!.processList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[12]=partsresult!!.processList[i].code
                    process_tv.text=typelist[i]
                }
            }
            analy_ll->{
                var typelist=getnamelist(partsresult!!.analysisList) as List<String>
                selector("请选择：",typelist){_HorizontalScrollView,i->
                    zhiSunResult[10]=partsresult!!.analysisList[i].code
                    analy_tv.text=typelist[i]
                }
            }
        }
    }

    var CAMERA_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zhi_sun)
        //VCH3=intent.extras["vch3"].toString()
        VIN=intent.extras["VIN"].toString()
        svn.text=VIN
        zhiSunResult[0]=VIN!!
        getQDPart()
        addpic_al.setOnClickListener {
            getPicture()
            }
        save_button.setOnClickListener {
            var date = "122"
            var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
            var supID=database.selectInfo("supID")
            var usrID=database.selectInfo("usrID")
            zhiSunResult[17]=date
            zhiSunResult[1]=supID!!
            zhiSunResult[2]=usrID!!
            zhiSunResult[8]=first
            if (isSave()){
                //var urlstr="${CommonStings.GENURL.url}qdPart.php?vch3=${VCH3}"
                var urlstr= CommonStings.GENURL.url+"qdSaveInfo.php?"+paramConstructor()
                saveZhisun(urlstr)
            }
        }
    }


    fun getPicture(){
        var getcameraIntenter= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(getcameraIntenter.resolveActivity(packageManager)!=null){
            startActivityForResult(getcameraIntenter, CAMERA_REQUEST_CODE)
        }
    }

    fun getQDPart(){
        var database= DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
        var supID=database.selectInfo("supID")
        var usrID=database.selectInfo("usrID")
        //var urlstr="${CommonStings.GENURL.url}qdPart.php?vch3=${VCH3}"
        var urlstr="${CommonStings.GENURL.url}qdPart.php?vch3=大众"
        //var urlstr="http://192.168.1.119/andong/web3/app/supper/100.000.000/qdPart.php?vch3=上海汽车"
        Log.i(TAG,"result=="+urlstr)
                //"supID=${supID}&&usrID=${usrID}"


        async {
                var url= URL(urlstr)
                var resultText=url.readText()
                var result= Gson().fromJson<QDPart>(resultText, QDPart::class.java)
            runOnUiThread {
                if(result!=null){
                    partsresult=result
                    locations_ll.setOnClickListener(this@ZhiSunActivity)
                    location_ll.setOnClickListener(this@ZhiSunActivity)
                    type_ll.setOnClickListener(this@ZhiSunActivity)
                    level_ll.setOnClickListener(this@ZhiSunActivity)
                    sub_ll.setOnClickListener(this@ZhiSunActivity)
                    reason_ll.setOnClickListener(this@ZhiSunActivity)
                    section_ll.setOnClickListener(this@ZhiSunActivity)
                    process_ll.setOnClickListener(this@ZhiSunActivity)
                    analy_ll.setOnClickListener(this@ZhiSunActivity)

                    all_sv.visibility=View.VISIBLE

                    mile_ev.addTextChangedListener(object :TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                            zhiSunResult[3]=p0.toString()
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            Log.i("kang","bianlesssss")
                        }

                    })

                    esponsible_ev.addTextChangedListener(object :TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                            zhiSunResult[13]=p0.toString()
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            Log.i("kang","bianlesssss")
                        }

                    })

                    secondary_supplier_ev.addTextChangedListener(object :TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                            zhiSunResult[14]=p0.toString()
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            Log.i("kang","bianlesssss")
                        }

                    })

                    particulars_ev.addTextChangedListener(object :TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                            zhiSunResult[15]=p0.toString()
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        }

                    })

                }
            }

        }
    }


    fun setSelectsrs(view: TextView, items:List<String>){
        selector("请选择:",items){_,i ->
            view.text=items[i]
        }
    }

    fun isSave():Boolean{
        for (i in 0..zhiSunResult.size-1){
            if (zhiSunResult[i]==""){
                toast("请完善${parasCN[i]}"+i)
                return false
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if(resultCode==Activity.RESULT_OK&&data!=null){
                    val picBitmap=data.extras.get("data") as Bitmap

                    var urlstr = CommonStings.GENURL.url + "qdUploadImage.php?"
                    var database = DBUtils(applicationContext, DatabaseHelper.InfoTbale.TABLE_NAME)
                    var supID = database.selectInfo("supID")
                    var usrID = database.selectInfo("usrID")
                    urlstr += ("supID=" + supID + "&&" + "usrID=" + usrID)
                    async {
                        var result=clientOkHttp(urlstr,btimapToBtyes(picBitmap))
                        if(result!=null){
                            Log.i(TAG,"result==="+result.toString())
                            runOnUiThread{
                                val view=layoutInflater.inflate(R.layout.zhi_sun_pic,null)
                                pi_ll.addView(view,0)
                                view.img_result.setImageBitmap(picBitmap)
                                zhiSunResult[16]+="${result.file}|:::"
                                view.img_delete.setOnClickListener{
                                    pi_ll.removeView(view)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun getnamelist(list:List<Subpart>):MutableList<String>?{
        var mlist: MutableList<String> = arrayListOf()
        list.forEach {
            mlist.add(it.name)
        }

        return mlist
    }

    fun getnameslist(list:List<PartList>):MutableList<String>?{
        var mlist: MutableList<String> = arrayListOf()
        list.forEach {
            mlist.add(it.orientName)
        }

        return mlist
    }

    fun reset(){
        location_tv.text=""
        zhiSunResult[4]=""
    }

    fun clientOkHttp(POST_IMAGE:String,FILE_IMAGE: ByteArray):QdUpLoadImage?{
        val client = OkHttpClient()
        var result:QdUpLoadImage?=null

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //携带一个表单参数
                .addFormDataPart("username", "Chen-XiaoMo")
                //设置参数名、文件名和文件
                .addFormDataPart("myfile", "Naruto.jpg", ProgressRequestBody(MediaType.parse("image/*"),
                        FILE_IMAGE, null))
                .build()

        //val requestBody=MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("tset","tset").build()


        val request = Request.Builder()
                .url(POST_IMAGE)
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body()
                Log.i(TAG,"body.toString()==="+body.toString())
                result = Gson().fromJson<QdUpLoadImage>(body!!.string(), QdUpLoadImage::class.java)
            }
            response.close()

        }catch (e:Exception){
            Log.i("kang","cuowu==="+e.toString())
        }
        call.cancel()
        return result
    }

    fun paramConstructor():String{
        var parasForResult:String=""
        for (i in 0..zhiSunResult.size-1){
            parasForResult+=(paras[i]+"="+zhiSunResult[i]+"&&")
        }
        if(parasForResult.length>2){
            parasForResult=parasForResult.substringBeforeLast("&&")
        }
        return parasForResult
    }

    /**
     * Btimap转数组
     */
    fun btimapToBtyes(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }


    fun saveZhisun(zhiSunResultss:String){
        async {
            try {
                var url= URL(zhiSunResultss)
                var resultText=url.readText()
                var result= Gson().fromJson<ZhiSunActivity.QdSaveInfo>(resultText, ZhiSunActivity.QdSaveInfo::class.java)
                toast(result.message)
                if (result.code=="200"){
                    runOnUiThread {
                        finish()
                    }
                }else{ }
            }catch (e:Exception){
                Log.i(TAG,e.toString())
            }

        }
    }

    data class QdUpLoadImage(var code:String,var message:String,var server:String,var file:String)
    data class QDPart(var partList:List<PartList>, var subPartList:List<Subpart>, var levelList:List<Subpart>, var reasonList:List<Subpart>, var analysisList:List<Subpart>, var sectionList:List<Subpart>, var processList:List<Subpart>, var typeList:List<Subpart>)
    data class PartList(var orientName:String,var orientList:List<Subpart>)
    data class Subpart(var code:String,var name:String)
    data class QdSaveInfo(var code:String,var message:String)
    //data class PartList(var partList:List<Subpart>,var subPartList:List<Subpart>)
}

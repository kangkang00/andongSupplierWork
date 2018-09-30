package com.kang.administrator.andongworkforphone

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.image
import org.jetbrains.anko.selector
import java.io.Writer
import java.net.URL


import com.kang.administrator.andongworkforphone.utils.CommonStings as CS

class LoginActivity : AppCompatActivity() {
    var isPassWordHide=true
    private val TAG = "kang"+javaClass.simpleName
    private var mySupID:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        shop_et.setOnClickListener {
            getShopNames()
        }

        hidepassword.setOnClickListener {
            if(isPassWordHide){
                hidepassword.image=getDrawable(R.drawable.show)
                passsword_et.inputType= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passsword_et.setSelection(passsword_et.text.toString().length)
                isPassWordHide=!isPassWordHide
            }else{
                hidepassword.image=getDrawable(R.drawable.hide)
                passsword_et.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passsword_et.setSelection(passsword_et.text.toString().length)
                isPassWordHide=!isPassWordHide
            }
        }

        logon_bt.setOnClickListener{login()}

    }


    fun login(){
        async {

            var urlstr=CS.GENURL.url+CS.LOGIN.url
            if(shop_et.text.toString()!=""&&uername_et.text.toString()!=""&&passsword_et.text.toString()!=""){
                urlstr+=("supName="+shop_et.text.toString()+"&&"+"usrName="+uername_et.text.toString()+"&&"+"usrPass="+passsword_et.text.toString())
            }else{
                runOnUiThread { Toast.makeText(applicationContext,"请正确填写信息",Toast.LENGTH_LONG).show() }
                return@async
            }
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<Loginresult>(resultText,Loginresult::class.java)
            runOnUiThread {
                if(result.code.equals("200")){
                    Log.i(TAG,"deng lu cheng gong ")
                    var database = DBUtils(applicationContext,DatabaseHelper.InfoTbale.TABLE_NAME)
                    database.updateTable(Loginresult.keys,listOf<String>(result.supID,result.usrID,result.power))
                    Log.i(TAG,"result.supID=="+result.supID+"   result.usrID==="+result.usrID)
                    startActivity(Intent(applicationContext,WorkControlActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }else{
                    Toast.makeText(applicationContext,"c result.code=="+result.code+" result.message=="+result.message,Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun getShopNames(){
        async {
            var urlstr=CS.GENURL.url+"getAllSupplier.php"
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<Shopnames>(resultText,Shopnames::class.java)
            if(result!=null){
                runOnUiThread {
                    var items=getnShopNamelist(result.list)
                    selector("请选择：",items as List<String>){_ImageSwitcher,i->

                        shop_et.text=items[i]
                        uername_et.text=""
                        mySupID=result.list[i].ID
                        uername_et.setOnClickListener {
                            getUserNames(mySupID!!)
                        }

                    }
                }
            }
        }


    }

    fun getUserNames(userID:String){
        async {
            Log.i(TAG,"getUserNames")
            var urlstr=CS.GENURL.url+"getAllUser.php?supID=${userID}"
            Log.i(TAG,"getUserNames===url=="+urlstr)
            var url= URL(urlstr)
            var resultText=url.readText()
            var result= Gson().fromJson<Usernames>(resultText,Usernames::class.java)
            if(result!=null){
                runOnUiThread {
                    var items=getnUserNamelist(result.list)
                    selector("请选择：",items as List<String>){_ImageSwitcher,i->

                        uername_et.text=items[i]

                    }
                }
            }
        }


    }

    fun getnShopNamelist(list:List<Shopname>):MutableList<String>?{
        var mlist: MutableList<String> = arrayListOf()
        list.forEach {
            mlist.add(it.shortName)
        }

        return mlist
    }

    fun getnUserNamelist(list:List<Username>):MutableList<String>?{
        var mlist: MutableList<String> = arrayListOf()
        list.forEach {
            mlist.add(it.name)
        }

        return mlist
    }


    data class Loginresult(var code:String,var message:String,var supID:String,var usrID:String,var power:String){
        companion object {
            var keys= listOf<String>("supID","usrID","power")
        }
    }

    data class Shopnames(var list:List<Shopname>)
    data class Shopname(var ID:String,var shortName:String)
    data class Usernames(var list:List<Username>)
    data class Username(var ID:String,var name:String)

    //data class SatWarePrvReceive(var code:Int?,var mess:String?,var VIN:String?,var start:String?,var VDC:String?,var WareNumber:String?,var time: String?)
}

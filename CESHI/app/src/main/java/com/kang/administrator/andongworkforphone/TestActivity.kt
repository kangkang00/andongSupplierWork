package com.kang.administrator.andongworkforphone

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.jetbrains.anko.downloadManager
import android.support.v4.content.FileProvider
import android.os.Build
import android.os.Environment
import java.io.File


class TestActivity : AppCompatActivity(){

    private val TAG = "kang"+javaClass.simpleName

    //private var locationManager : LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        getApp()
    }


    fun getApp(){
        var url="http://192.168.1.105/test.apk"

        var request=DownloadManager.Request( Uri.parse(url)).setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"myApk.apk").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE).setTitle("appxiazai")
        var id=downloadManager.enqueue(request)
        var filter=IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        var brocast=object:BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                var uri=downloadManager.getUriForDownloadedFile(id)
                install(uri)
            }

        }

        registerReceiver( brocast,filter)

    }


    private fun install(uri: Uri) {
        Log.i(TAG, "开始执行安装: ${uri.path}")
        val apkFile = File(uri.path)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(
                    this, "com.kang.administrator.andongworkforphone.fileprovider", apkFile)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            Log.w(TAG, "正常进行安装")
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        Log.w(TAG, "正常进行安装")
        try {
            startActivity(intent)
        }catch (e:Exception){
            Log.w(TAG, "cuole =="+e)
        }

    }
}

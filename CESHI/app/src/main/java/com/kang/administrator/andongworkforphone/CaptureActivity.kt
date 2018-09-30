package com.kang.administrator.andongworkforphone

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.kang.administrator.andongworkforphone.camera.CameraManager
import com.kang.administrator.andongworkforphone.result.ResultHandlerFactory
import com.kang.administrator.andongworkforphone.utils.CommonStings
import com.kang.administrator.andongworkforphone.utils.DBUtils
import com.kang.administrator.andongworkforphone.utils.DatabaseHelper
import com.kang.administrator.zhisuntestapplication.*
import kotlinx.android.synthetic.main.activity_capture.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.startActivity
import java.io.IOException
import java.net.URL
import java.sql.Time
import java.util.*

class CaptureActivity : Activity() , SurfaceHolder.Callback{


    private val TAG = "kang"+javaClass.simpleName

    private val DEFAULT_INTENT_RESULT_DURATION_MS = 1500L
    private val BULK_MODE_SCAN_DELAY_MS = 1000L

    var PROCESS:String?=null

    private val ZXING_URLS = arrayOf("http://zxing.appspot.com/scan", "zxing://scan/")

    private val HISTORY_REQUEST_CODE = 0x0000bacc

    val ACTIVITYCODE=1

    private val DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
            ResultMetadataType.SUGGESTED_PRICE,
            ResultMetadataType.ERROR_CORRECTION_LEVEL,
            ResultMetadataType.POSSIBLE_COUNTRY)

    var cameraManager: CameraManager? = CameraManager(this)
    var handler: CaptureActivityHandler? = null

    private var inactivityTimer: InactivityTimer = InactivityTimer(this);
    private var lastResult: Result? = null
    private var source: IntentSource? = null

    private var decodeFormats: AbstractCollection<BarcodeFormat>? = null
    private var decodeHints: Map<DecodeHintType, Any>? = null
    private var characterSet: String? = null
    private var savedResultToShow: Result? = null
    private var hasSurface: Boolean = false

    private lateinit var beepManager: BeepManager
    private var ambientLightManager = AmbientLightManager(this)

    override fun onPause() {
        super.onPause()
        cameraManager!!.closeDriver()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        beepManager= BeepManager(this)
        hasSurface = false
        PROCESS=intent.extras["PROCESS"].toString()
        Log.i(TAG,"PROCESS==="+PROCESS)
    }

    override fun onResume() {
        super.onResume()
        viewfinderView.setCameraManager(cameraManager!!)

        handler = null
        lastResult = null


        beepManager.updatePrefs()
        ambientLightManager.start(cameraManager!!)

        inactivityTimer.onResume()

        val intent = intent


        source = IntentSource.NONE
        decodeFormats = null
        characterSet = null

        if (intent != null) {

            val action = intent.action
            val dataString = intent.dataString

            if (Intents.Scan.ACTION.equals(action)) {
                // Scan the formats the intent requested, and return the result to the calling activity.
                source = IntentSource.NATIVE_APP_INTENT
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent) as AbstractCollection<BarcodeFormat>
                decodeHints = DecodeHintManager.parseDecodeHints(intent)

                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    val width = intent.getIntExtra(Intents.Scan.WIDTH, 0)
                    val height = intent.getIntExtra(Intents.Scan.HEIGHT, 0)
                    if (width > 0 && height > 0) {
                        cameraManager!!.setManualFramingRect(width, height)
                    }
                }

                if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
                    val cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1)
                    if (cameraId >= 0) {
                        cameraManager!!.setManualCameraId(cameraId)
                    }
                }


            } else if (dataString != null &&
                    dataString.contains("http://www.google") &&
                    dataString.contains("/m/products/scan")) {

                // Scan only products and send the result to mobile Product Search.
                source = IntentSource.PRODUCT_SEARCH_LINK
                //sourceUrl = dataString
                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS as AbstractCollection<BarcodeFormat>

            } else if (isZXingURL(dataString)) {

                // Scan formats requested in query string (all formats if none specified).
                // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
                source = IntentSource.ZXING_LINK
                //sourceUrl = dataString
                val inputUri = Uri.parse(dataString)
                //scanFromWebPageManager = ScanFromWebPageManager(inputUri)
                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri) as AbstractCollection<BarcodeFormat>
                // Allow a sub-set of the hints to be specified by the caller.
                decodeHints = DecodeHintManager.parseDecodeHints(inputUri) as Map<DecodeHintType, Any>?

            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET)

        }

        val surfaceHolder = preview_view.holder
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this)
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        if (surfaceHolder == null) {
            throw IllegalStateException("No SurfaceHolder provided")

        }
        if (cameraManager!!.isOpen) {
            return
        }
        try {

            cameraManager!!.openDriver(surfaceHolder)
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager!!)
            }
            decodeOrStoreSavedBitmap(null, null)
        } catch (ioe: IOException) {
            Log.w(TAG, ioe)
        } catch (e: RuntimeException) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e)
        }

    }

    private fun decodeOrStoreSavedBitmap(bitmap: Bitmap?, result: Result?) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result
        } else {
            if (result != null) {
                savedResultToShow = result
            }
            if (savedResultToShow != null) {
                val message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow)
                handler!!.sendMessage(message)
            }
            savedResultToShow = null
        }
    }


    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {

        if (p0 == null) {
        }
        if (!hasSurface) {
            hasSurface = true
            initCamera(p0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
        when(keyCode){
            KeyEvent.KEYCODE_BACK->finish()
        }
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    fun handleDecode(rawResult: Result, barcode: Bitmap?, scaleFactor: Float) {
        inactivityTimer.onActivity()
        lastResult = rawResult
        val resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult)
        val codestr=rawResult.text
        //var urlstr="${CommonStings.GENURL.url}startPrvReceive.php?"
        var urlstr=CommonStings.GENURL.url
        var FORWHAT=0
        when(PROCESS){
            "100"->{urlstr+="startPrvReceive.php?"
                    FORWHAT=1}
            "200"->{urlstr+="satWarePrvReceive.php?"
                    FORWHAT=2}
            "350"->{
                if(codestr.length>17){
                    Log.i(TAG,"result.s111upID=="+codestr.subSequence(0,17).toString())
                    setResult(350,Intent().putExtra("message",codestr.subSequence(0,17).toString()))
                    runOnUiThread {
                        finish()
                    }
                }
            }
        }
        //contents_text_view.text=codestr.subSequence(0,17).toString()+"   "+codestr.length
        async {
            if(codestr.length>17){
                var database=DBUtils(applicationContext,DatabaseHelper.InfoTbale.TABLE_NAME)
                var supID=database.selectInfo("supID")
                var usrID=database.selectInfo("usrID")
                var url= URL(urlstr+"supID=${supID}&&usrID=${usrID}&&"+"qrCode="+codestr.subSequence(0,17).toString())
                var resultText=url.readText()
                var CODE:String="000"
                when(PROCESS){
                    "100"->{
                        var resultsss= Gson().fromJson<StartPrvReceive>(resultText, StartPrvReceive::class.java)
                        CODE=resultsss.code
                    }
                    "200"->{
                        var resultsss= Gson().fromJson<SatWarePrvReceive>(resultText, SatWarePrvReceive::class.java)
                        CODE=resultsss.code}
                }
                Log.i(TAG,"code=="+CODE)

                when(CODE){
                    "200"->{
                        runOnUiThread{
//                            ambientLightManager.stop()
//                            beepManager.close()
//                            cameraManager!!.closeDriver();
                            finish()
                        }
                    }
                }

            }

        }
    }



    fun drawViewfinder() {
        viewfinderView.drawViewfinder()
    }


    private fun isZXingURL(dataString: String?): Boolean {
        if (dataString == null) {
            return false
        }
        for (url in ZXING_URLS) {
            if (dataString.startsWith(url)) {
                return true
            }
        }
        return false
    }

    data class SatWarePrvReceive(var code:String,var message:String?,var VIN:String?,var start:String?,var VDC:String?,var time:String?)
    data class StartPrvReceive(var code:String,var message:String,var VIN:String,var start:String,var end:String,var wagon:String,var local:String,var count:String)

}

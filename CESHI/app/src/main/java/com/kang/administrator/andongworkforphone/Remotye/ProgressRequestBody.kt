package com.kang.administrator.andongworkforphone.Remotye

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(private val contentType: MediaType?,
                          private val file: ByteArray,
                          private val listener: ProgressListener?) : RequestBody() {
    override fun contentType(): MediaType? {
        return contentType
    }
    override fun contentLength(): Long {
        return file.size.toLong()
    }
    override fun writeTo(sink: BufferedSink) {
        try {
            val max = contentLength()
            var current = 0L
            //listener.onStart()
            sink.write(file)
            //listener.onComplete()
        } catch (e: Exception) {
            //listener.onError(e)
        }
    }
}
package com.kang.administrator.andongworkforphone.Remotye

interface ProgressListener {

    /**
     * 上传开始时回调
     */
    fun onStart()
    /**
     * 上传过程中回调
     * @param current 当前上传的大小
     * @param max 文件的总大小
     */
    fun progress(current: Long, max: Long)
    /**
     * 上传完成时回调
     */
    fun onComplete()
    /**
     * 上传过程中出现错误时回调
     * @param e 异常对象
     */
    fun onError(e: Exception)
}
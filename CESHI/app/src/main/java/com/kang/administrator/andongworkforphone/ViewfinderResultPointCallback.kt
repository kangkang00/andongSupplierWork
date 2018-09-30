package com.kang.administrator.zhisuntestapplication

import com.google.zxing.ResultPoint
import com.google.zxing.ResultPointCallback
import com.kang.administrator.andongworkforphone.ViewfinderView

internal class ViewfinderResultPointCallback(private val viewfinderView: ViewfinderView) : ResultPointCallback {

    override fun foundPossibleResultPoint(point: ResultPoint) {
        viewfinderView.addPossibleResultPoint(point)
    }

}
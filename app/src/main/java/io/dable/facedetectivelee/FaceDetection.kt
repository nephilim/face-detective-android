package io.dable.facedetectivelee

import android.R
import android.app.Activity
import android.util.Log
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc.*
import org.opencv.objdetect.CascadeClassifier
import java.io.File


object FaceDetection {

    private const val faceModel = "haarcascade_frontalface_default.xml"

    private lateinit var faceCascade: CascadeClassifier

    fun loadModel(activity: Activity) {
        faceCascade = CascadeClassifier(File(activity.filesDir, "das").apply { writeBytes(activity.assets.open(faceModel).readBytes()) }.path)
    }

    fun detectFaces(image: Mat): MatOfRect {
        val rectangles = MatOfRect()
        val grayScaled = image.prepare()
        faceCascade.detectMultiScale(grayScaled, rectangles, 1.2, 10, 0, Size(40.0, 40.0))
        return rectangles
    }

    private fun Mat.toGrayScale(): Mat =
            if (channels() >= 3) Mat().apply { cvtColor(this@toGrayScale, this, COLOR_BGR2GRAY) }
            else this

    private fun Mat.prepare(): Mat {
        val mat = toGrayScale()
        equalizeHist(mat, mat)
        return mat
    }


}

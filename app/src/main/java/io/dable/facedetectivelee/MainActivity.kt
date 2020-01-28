package io.dable.facedetectivelee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 100
    private val REQUEST_CAMERA_PERMISSION = 200
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        System.loadLibrary("opencv_java4")

        FaceDetection.loadModel(this)

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                startCamera()
            }

        }
    }

    private fun startCamera() {
        uri = FileProvider.getUriForFile(this, "io.dable.facedetectivelee.fileprovider",
            File.createTempFile("picture", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)))
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }, REQUEST_IMAGE_CAPTURE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Snackbar.make(findViewById<View>(android.R.id.content), "Please enable the access to the camera", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            hintText.visibility = INVISIBLE
            detailsGroup.visibility = VISIBLE
            val bitmap = getCapturedImage(uri)
            imageView.setImageBitmap(bitmap)
            detectFace(bitmap, faces_value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCapturedImage(uri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        return when (ExifInterface(contentResolver.openInputStream(uri)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(90F) }, true)
            ExifInterface.ORIENTATION_ROTATE_180 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(180F) }, true)
            ExifInterface.ORIENTATION_ROTATE_270 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(270F) }, true)
            else -> bitmap
        }
    }

    private fun detectFace(image: Bitmap, facesValue: TextView) {
        val mat = Mat()
        val bmp32 = image.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bmp32, mat)
        GlobalScope.launch {
            val rectangles = FaceDetection.detectFaces(mat)
            val faceCount = rectangles.elemSize()
            runOnUiThread {
                facesValue.text = faceCount.toString()
            }
        }

    }
}



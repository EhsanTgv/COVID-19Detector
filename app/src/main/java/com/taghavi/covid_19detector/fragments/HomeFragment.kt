package com.taghavi.covid_19detector.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.utilities.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.homeOpenCameraButton.setOnClickListener {
            val androidVersion = Build.VERSION.SDK_INT
            if (androidVersion >= Build.VERSION_CODES.M) {
                if (checkPermission(Manifest.permission.CAMERA)) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_ID)
                } else {
                    requestForPermission(Manifest.permission.CAMERA)
                }
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_ID)
            }
        }

        binding.homeOpenGalleryButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, GALLERY_ID)
        }

        writeFileOnInternalStorage(context!!, "testFile", "This is a temp Test")

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_ID -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val imageData = data!!.extras!!.get("data") as Bitmap

                        saveBitmapFile(imageData)
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(context, "You didn't get any shot", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            GALLERY_ID -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val contentURI = data!!.data
                        val imageData =
                            MediaStore.Images.Media.getBitmap(context!!.contentResolver, contentURI)

                        saveBitmapFile(imageData)
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(context, "You didn't select any photo", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun storeImage(image: Bitmap) {
        val pictureFile: File?
        val androidVersion = Build.VERSION.SDK_INT
        if (androidVersion >= Build.VERSION_CODES.M) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                pictureFile = getOutputMediaFile()
            } else {
                requestForPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                return
            }

        } else {
            pictureFile = getOutputMediaFile()
        }

        if (pictureFile == null) {
            MyLog.i("Error creating media file, check storage permissions:")
            return
        }

        try {
            val fileOutputStream = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            MyLog.i("File not found: $e")
        } catch (e: IOException) {
            MyLog.i("Error accessing file: $e")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getOutputMediaFile(): File? {
        //add storage permission
        val mediaStoreDir =
            File(Environment.getExternalStorageState() + "/Android/data/data" + context!!.applicationContext.packageName)

        if (!mediaStoreDir.exists()) {
            if (!mediaStoreDir.mkdirs()) {
                return null
            }
        }

        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mediaFile: File
        val myImageName = "MyImage_$timeStamp.jpg"
        mediaFile = File(mediaStoreDir.path + File.separator + myImageName)
        return mediaFile
    }

    private fun checkPermission(permission: String): Boolean {
        val result: Int = ContextCompat.checkSelfPermission(context!!, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission(vararg permissions: String) {
        ActivityCompat.requestPermissions(
            activity!!,
            permissions,
            STORAGE_PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_ID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_ID)
                } else {
                    Toast.makeText(
                        context,
                        "You have to give camera permission to take the shot",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
//            STORAGE_PERMISSION_ID->{
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //granted
//                } else {
//                    //not granted
//                }
//            }
        }
    }

    private fun writeFileOnInternalStorage(context: Context, fileName: String, body: String) {
        val file = File(context.filesDir, "myDir")
        if (!file.exists()) {
            file.mkdir()
        }

        try {
            val myFile = File(file, fileName)
            val writer = FileWriter(myFile)
            writer.append(body)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            MyLog.i("HomeFragment -> writeFileOnInternalStorage -> $e")
        }
    }

    private fun saveBitmapFile(image: Bitmap) {
        val contextWrapper = ContextWrapper(context)
        val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, "image.jpg")
        if (!file.exists()) {
            MyLog.i(file.toString())
            var fileOutStream: FileOutputStream? = null
            try {
                fileOutStream = FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream)
                fileOutStream.flush()
                fileOutStream.close()
            } catch (e: Exception) {
                MyLog.i(e.toString())
            }
        }
    }
}

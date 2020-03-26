package com.taghavi.covid_19detector.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.utilities.CAMERA_ID
import com.taghavi.covid_19detector.utilities.GALLERY_ID
import com.taghavi.covid_19detector.utilities.MyLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
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
                //check camera permission here
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

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_ID -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val imageData = data!!.extras!!.get("data") as Bitmap

                        storeImage(imageData)
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

                        storeImage(imageData)
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
        val pictureFile:File?
        val androidVersion = Build.VERSION.SDK_INT
        if (androidVersion >= Build.VERSION_CODES.M) {
            //check storage permission here
            pictureFile = getOutputMediaFile()
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


}

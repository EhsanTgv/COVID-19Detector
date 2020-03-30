package com.taghavi.covid_19detector.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.apiServices.VolleyApiService
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.utilities.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.*
import java.util.*


class HomeFragment : Fragment() {
    private lateinit var volleyApiService: VolleyApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        initialize()

        events(binding)

        return binding.root
    }

    private fun initialize() {
        volleyApiService = VolleyApiService(context!!)
    }

    private fun events(binding: FragmentHomeBinding) {
        binding.homeOpenCameraButton.setOnClickListener {
            val androidVersion = Build.VERSION.SDK_INT
            if (androidVersion >= Build.VERSION_CODES.M) {
                if (PermissionHandler.checkPermission(context!!, Manifest.permission.CAMERA)) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_ID)
                } else {
                    PermissionHandler.requestForPermission(
                        activity!!,
                        CAMERA_PERMISSION_ID, Manifest.permission.CAMERA
                    )
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_ID -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val imageData = data!!.extras!!.get("data") as Bitmap

                        if (PermissionHandler.checkPermission(
                                context!!,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) &&
                            PermissionHandler.checkPermission(
                                context!!,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            saveBitmapFile(imageData)
                        } else {
                            PermissionHandler.requestForPermission(
                                activity!!,
                                STORAGE_PERMISSION_ID,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            return
                        }
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

                        if (PermissionHandler.checkPermission(
                                context!!,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) &&
                            PermissionHandler.checkPermission(
                                context!!,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            saveBitmapFile(imageData)
                        } else {
                            PermissionHandler.requestForPermission(
                                activity!!,
                                STORAGE_PERMISSION_ID,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            return
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(context, "You didn't select any photo", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
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

    private fun uploadToServer(filePath: String) {
        val retrofit = NetworkClient.getRetrofitClient(context!!)
        val uploadAPIs = retrofit.create(UploadAPIs::class.java)
        //Create a file object using file path
        val file = File(filePath)
        // Create a request body with file and image media type
        val fileReqBody = RequestBody.create(MediaType.parse("image/*"), file)
        // Create MultipartBody.Part using file request-body,file name and part name
        val part = MultipartBody.Part.createFormData("upload", file.getName(), fileReqBody)
        //Create request body with text description and text media type
        val description = RequestBody.create(MediaType.parse("text/plain"), "image-type")
        //
        val call = uploadAPIs.uploadImage(part, description)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

            }

        })
    }

    interface UploadAPIs {
        @Multipart
        @POST("/upload")
        fun uploadImage(
            @Part file: MultipartBody.Part,
            @Part("name") requestBody: RequestBody
        ): Call<ResponseBody>
    }
}
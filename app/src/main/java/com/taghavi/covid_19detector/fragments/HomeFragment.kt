package com.taghavi.covid_19detector.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.apiServices.RetrofitApiService
import com.taghavi.covid_19detector.apiServices.VolleyApiService
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.models.PredictModel
import com.taghavi.covid_19detector.utilities.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

                        uploadBitmap(imageData)

//                        if (PermissionHandler.checkPermission(
//                                context!!,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            ) &&
//                            PermissionHandler.checkPermission(
//                                context!!,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                        ) {
//                            saveBitmapFile(imageData)
//                        } else {
//                            PermissionHandler.requestForPermission(
//                                activity!!,
//                                STORAGE_PERMISSION_ID,
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                            return
//                        }
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

                        uploadBitmap(imageData)
//                        if (PermissionHandler.checkPermission(
//                                context!!,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            ) &&
//                            PermissionHandler.checkPermission(
//                                context!!,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                        ) {
//                            saveBitmapFile(imageData)
//                        } else {
//                            PermissionHandler.requestForPermission(
//                                activity!!,
//                                STORAGE_PERMISSION_ID,
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                            return
//                        }
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
        MyLog.i(file.toString())
        var fileOutStream: FileOutputStream? = null
        try {
            fileOutStream = FileOutputStream(file)
            MyLog.i("HomeFragment -> saveBitmapFile -> ${file.path}")
            image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream)
            fileOutStream.flush()
            fileOutStream.close()
            RetrofitApiService(context!!).uploadToServer(file)
        } catch (e: Exception) {
            MyLog.i(e.toString())
        }

    }

    private fun uploadBitmap(bitmap: Bitmap) {
        MyLog.i("HomeFragment -> uploadBitmap started")
        val progressDialog = ProgressDialog(context!!)
        progressDialog.setTitle("Please wait")
        progressDialog.setIcon(android.R.drawable.ic_popup_sync)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val volleyMultipartRequestJava = object :
            VolleyMultipartRequestJava(
                Method.POST,
                Links.uploadUrl,
                Response.Listener { response ->
                    MyLog.i("HomeFragment -> uploadBitmap $response")
                    progressDialog.dismiss()
                    try {
                        val jsonObject = JSONObject(String(response.data))
                        val predictModel =
                            Gson().fromJson(jsonObject.toString(), PredictModel::class.java)
                        MyLog.i("HomeFragment -> uploadBitmap $predictModel")
                        val alertDialog = AlertDialog.Builder(context!!)
                        alertDialog.setTitle("Response: ")
                        alertDialog.setMessage("Predict: ${predictModel.predict}")
                        if (predictModel.predict == "normal") {
                            alertDialog.setIcon(android.R.drawable.checkbox_on_background)
                        } else {
                            alertDialog.setIcon(android.R.drawable.ic_delete)
                        }
                        alertDialog.show()
                    } catch (e: JSONException) {
                        MyLog.i("HomeFragment -> uploadBitmap $e")
                        Toast.makeText(context!!, "Parsing Error: $e", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    MyLog.i("HomeFragment -> uploadBitmap $error")
                    Toast.makeText(context!!, "something went wrong, $error", Toast.LENGTH_SHORT)
                        .show()
                    progressDialog.dismiss()
                }) {
            override fun getByteData(): MutableMap<String, DataPart> {
                val params: HashMap<String, DataPart> = HashMap()
                val imageName = System.currentTimeMillis()
                params["file"] = DataPart("$imageName.jpg", getFileDataFromDrawable(bitmap))
                return params
            }

            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                var localVolleyError: VolleyError = volleyError!!
                if (localVolleyError.networkResponse != null && localVolleyError.networkResponse.data != null) {
                    val error = VolleyError(String(localVolleyError.networkResponse.data))
                    localVolleyError = error
                }
                return localVolleyError
            }
        }

        volleyMultipartRequestJava.retryPolicy = DefaultRetryPolicy(10000, 1, 1f)

        Volley.newRequestQueue(context).add(volleyMultipartRequestJava);
    }

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
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
package com.taghavi.covid_19detector.apiServices

import android.content.Context
import com.taghavi.covid_19detector.fragments.HomeFragment
import com.taghavi.covid_19detector.utilities.NetworkClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RetrofitApiService(private val context: Context) {

    private fun uploadToServer(filePath: String) {
        val retrofit = NetworkClient.getRetrofitClient(context)
        val uploadAPIs = retrofit!!.create(HomeFragment.UploadAPIs::class.java)
        //Create a file object using file path
        val file = File(filePath)
        // Create a request body with file and image media type
        val fileReqBody = RequestBody.create(MediaType.parse("image/*"), file)
        // Create MultipartBody.Part using file request-body,file name and part name
        val part = MultipartBody.Part.createFormData("upload", file.name, fileReqBody)
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
}
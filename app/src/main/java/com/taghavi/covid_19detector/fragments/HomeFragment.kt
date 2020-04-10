package com.taghavi.covid_19detector.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.taghavi.covid_19detector.R
import com.taghavi.covid_19detector.databinding.FragmentHomeBinding
import com.taghavi.covid_19detector.models.PredictModel
import com.taghavi.covid_19detector.utilities.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class HomeFragment : Fragment() {
    lateinit var progressDialog: ProgressDialog

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aboutFragment -> {
                setupAboutDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initialize() {
        setHasOptionsMenu(true)
        progressDialog = ProgressDialog(context!!)
        setupProgressDialog()
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
        }
    }

    private fun uploadBitmap(bitmap: Bitmap) {
        MyLog.i("HomeFragment -> uploadBitmap started")
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
                        setupResultDialog(predictModel)
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

    private fun setupResultDialog(predictModel: PredictModel) {
        val alertDialog = AlertDialog.Builder(context!!)
        alertDialog.setTitle("Response: ")
        alertDialog.setMessage("Predict: ${predictModel.predict}")
        if (predictModel.predict == "normal") {
            alertDialog.setIcon(R.drawable.predict_normal)
        } else {
            alertDialog.setIcon(R.drawable.predict_covid)
        }
        alertDialog.show()
    }

    private fun setupProgressDialog() {
        progressDialog.setTitle("Please wait")
        progressDialog.setIcon(R.drawable.dialog_loading_icon)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun setupAboutDialog() {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle("About")
        dialog.setMessage("instructions")
        dialog.setIcon(R.drawable.dialog_about_icon)
        dialog.show()
    }

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
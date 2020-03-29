package com.taghavi.covid_19detector.apiServices

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.taghavi.covid_19detector.utilities.MyLog

class VolleyApiService(private val context: Context) {

    fun uploadImageFile() {
        val listener = Response.Listener<String> { response ->
            MyLog.i("VolleyApiService -> uploadImageFile -> $response")
        }
        val errorListener = Response.ErrorListener {
            MyLog.i("VolleyApiService -> uploadImageFile -> $it")
        }
        val request = StringRequest(Request.Method.POST, "", listener, errorListener)
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val queue = Volley.newRequestQueue(context)

        queue.add<String>(request)
    }
}
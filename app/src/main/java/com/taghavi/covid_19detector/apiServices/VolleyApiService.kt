package com.taghavi.covid_19detector.apiServices

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.taghavi.covid_19detector.utilities.Links
import com.taghavi.covid_19detector.utilities.MyLog
import java.io.File

class VolleyApiService(private val context: Context) {

    fun uploadImageFile(image:File) {
        val listener = Response.Listener<String> { response ->
            MyLog.i("VolleyApiService -> uploadImageFile -> $response")
        }
        val errorListener = Response.ErrorListener {
            MyLog.i("VolleyApiService -> uploadImageFile -> $it")
        }
        val request =
            object : StringRequest(Method.POST, Links.uploadUrl, listener, errorListener) {
                override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                    var localVolleyError: VolleyError = volleyError!!
                    if (localVolleyError.networkResponse != null && localVolleyError.networkResponse.data != null) {
                        val error = VolleyError(String(localVolleyError.networkResponse.data))
                        localVolleyError = error
                    }
                    return localVolleyError
                }
            }
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val queue = Volley.newRequestQueue(context)

        queue.add(request)
    }
}
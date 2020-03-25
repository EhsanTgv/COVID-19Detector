package com.taghavi.covid_19detector.utilities

import android.util.Log

class MyLog {
    companion object {
        private const val myTag = "MyTestTag"

        fun i(value: String) {
            Log.i(myTag, value)
        }
    }
}
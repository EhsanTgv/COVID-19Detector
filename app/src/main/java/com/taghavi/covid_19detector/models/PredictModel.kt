package com.taghavi.covid_19detector.models

import com.google.gson.annotations.SerializedName

data class PredictModel(
    @SerializedName("predict") val predict: String,
    @SerializedName("file_id") val fileId: String
)
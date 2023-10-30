package com.example.birdspotterapppoe

import android.net.Uri
import android.util.Log
import java.net.MalformedURLException
import java.net.URL

private val eBirdBASE_URL ="https://api.ebird.org/v2/data/obs/region/recent?r=ZA"
private val PARAM_METRIC = "metric"
private val METRIC_VALUE = "true"
private val PARAM_API_KEY = "key"
private val LOGGING_TAG = "URLWECREATED"

fun buildURLForBirds(): URL? {
    val buildUri: Uri = Uri.parse(eBirdBASE_URL).buildUpon()
        .appendQueryParameter(
            PARAM_API_KEY,
            BuildConfig.EBird_API_KEY
        ) // passing in api key
        .appendQueryParameter(
            PARAM_METRIC,
            METRIC_VALUE
        ) // passing in metric as measurement unit
        .build()
    var url: URL? = null
    try {
        url = URL(buildUri.toString())
    } catch (e: MalformedURLException) {
        e.printStackTrace()
    }
    Log.i(LOGGING_TAG, "buildURLForEBirds: $url")
    return url
}
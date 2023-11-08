package com.example.birdspotterapppoe

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Bird(
    var id: String? = "",
    var name: String? = "",
    val rarity: String = "", // default value
    var notes: String? = "",
    var image: String? = "",
    var latLng: String?  = "",
    var address: String? = "Default Address", // default value
    @ServerTimestamp
    var date: Date? = Date()
)


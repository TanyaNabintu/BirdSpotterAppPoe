package com.example.birdspotterapppoe

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.example.birdspotterapppoe.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URL
import android.content.Intent
import android.net.Uri
import android.view.MenuItem


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var listOfBirds: MutableList<Observation> = mutableListOf()
    private lateinit var searchView: SearchView
    private val customMarkers: MutableList<Marker> = mutableListOf()
    private lateinit var toolBar: Toolbar
    private var selectedHotspotLocation: LatLng? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the SearchView
        searchView = findViewById(R.id.searchView)


        // Add a query listener to the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the search query here
                filterBirds(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search suggestions or live filtering here
                filterBirds(newText)
                return true
            }
        })

        // Setting up Toolbar
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filterBirds(query: String?) {
        val filteredBirds = if (query.isNullOrBlank()) {
            listOfBirds
        } else {
            listOfBirds.filter { birdObservation ->
                birdObservation.comName.contains(query, ignoreCase = true)
            }
        }
        updateMapWithBirdObservations(filteredBirds, customMarkers) // Pass the custom markers list
    }

    private fun updateMapWithBirdObservations(birds: List<Observation>, customMarkers: List<Marker>) {
        if (::mMap.isInitialized) {
            mMap.clear()

            for (birdObservation in birds) {
                val latLng = LatLng(birdObservation.lat, birdObservation.lng)
                mMap.addMarker(MarkerOptions().position(latLng).title(birdObservation.comName))
            }

            if (birds.isNotEmpty()) {
                val firstBirdObservation = birds[0]
                val latLng = LatLng(firstBirdObservation.lat, firstBirdObservation.lng)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f))
            }

            for (marker in customMarkers) {
                mMap.addMarker(MarkerOptions().position(marker.position).title(marker.title))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        val testLocation = LatLng(0.0, 0.0)
        mMap.addMarker(MarkerOptions().position(testLocation).title("Test Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(testLocation))

        mMap.setOnMapLongClickListener { latLng ->
            showMarkerNameDialog(latLng)
        }

        mMap.setOnMarkerClickListener { marker ->
            if (!customMarkers.contains(marker)) {
                selectedHotspotLocation = marker.position
                showDirectionsDialog()
            }
            true
        }

        requestEBirdHotspots(buildURLForBirds())
    }

    private fun getDirectionsToHotspot(hotspotLocation: LatLng) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${hotspotLocation.latitude},${hotspotLocation.longitude}&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps app is not installed", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showDirectionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Get Directions")

        builder.setMessage("Do you want to get directions to this hotspot?")

        builder.setPositiveButton("Yes") { _, _ ->
            selectedHotspotLocation?.let { location ->
                getDirectionsToHotspot(location)
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showMarkerNameDialog(latLng: LatLng) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Marker Name")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val markerName = input.text.toString()
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title(markerName))

            if (marker != null) {
                customMarkers.add(marker)
            } else {
                Toast.makeText(this, "Failed to add the marker", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun requestEBirdHotspots(eBirdApiUrl: URL?) {
        if (eBirdApiUrl != null) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(eBirdApiUrl)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("eBirdAPI", "API request failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody: String = response.body?.string() ?: ""
                        if (responseBody.isNotEmpty()) {
                            consumeEBirdHotspots(responseBody)
                        }
                    }
                }
            })
        } else {
            Log.e("eBirdAPI", "Invalid eBird API URL")
        }
    }

    private fun consumeEBirdHotspots(birdObservationJSON: String?) {
        if (birdObservationJSON != null) {
            try {
                val birdsObservations = JSONArray(birdObservationJSON)
                for (i in 0 until birdsObservations.length()) {
                    val birdsObservation = birdsObservations.getJSONObject(i)

                    val name = birdsObservation.optString("sciName", "Unknown")
                    val number = birdsObservation.optInt("howMany", 0)
                    val comName = birdsObservation.optString("comName", "Unknown")
                    val locName = birdsObservation.optString("locName", "Unknown")
                    val lat = birdsObservation.getDouble("lat")
                    val lng = birdsObservation.getDouble("lng")


                    val birdObservation = Observation(comName, name, locName, number, lat, lng)
                    listOfBirds.add(birdObservation)
                }

                runOnUiThread {
                    updateMapWithBirdObservations(listOfBirds, customMarkers)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}

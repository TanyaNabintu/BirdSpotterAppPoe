package com.example.birdspotterapppoe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.example.birdspotterapppoe.Constants.TAG
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DetailsActivity : AppCompatActivity() {

    private val dbHandler = DatabaseHelper(this, null)
    private lateinit var firestoreClass:FirestoreClass
    private lateinit var auth: FirebaseAuth

    private lateinit var nameEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var toolBar: Toolbar
    private lateinit var uploadImageButton: Button
    private lateinit var uploadImageView: ImageView
    private lateinit var raritySpinner: Spinner
    private lateinit var modifyId: String
    private lateinit var  imageUrl:String
    private var latLng: String = ""
    private var address: String = ""
    private var userId:String=""
     var selectedImage: Uri ?= null

    private var rarityTypes = mapOf(Pair("Common", 0), Pair("Rare", 1), Pair("Extremely rare", 2))

    // Activity Result Launcher for opening the image gallery
    private lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // initialize firestore
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestoreClass = FirestoreClass()
        var currentUser=auth.currentUser?.uid
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Setting up image related vars
        uploadImageButton = findViewById(R.id.uploadImageButton)
        uploadImageView = findViewById(R.id.uploadImageView)

        // Setting up Toolbar
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        nameEditText = findViewById(R.id.nameEditText)
        notesEditText = findViewById(R.id.notesEditText)
        raritySpinner = findViewById(R.id.raritySpinner)

        raritySpinner.adapter =
            ArrayAdapter(
                applicationContext,
                android.R.layout.simple_list_item_1, ArrayList(rarityTypes.keys)
            )

        raritySpinner.setSelection(rarityTypes["Common"]!!)

        /* Check if the activity opened from List Item Click */
        if (intent.hasExtra("id")) {
            modifyId = intent.getStringExtra("id") ?: ""
            nameEditText.setText(intent.getStringExtra("name") ?: "")
            notesEditText.setText(intent.getStringExtra("notes") ?: "")
            val rarity = intent.getStringExtra("rarity") ?: "Common"
            raritySpinner.setSelection(rarityTypes[rarity]!!)
            latLng = intent.getStringExtra("latLng") ?: ""
            address = intent.getStringExtra("address") ?: ""
            userId = intent.getStringExtra("userId")?:""
            /**
             * getting image
             */
            imageUrl = intent.getStringExtra("image") ?: ""
            val imageView = uploadImageView
            val context = imageView.context
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .target(imageView)
                .build()
            imageLoader.enqueue(request)

            findViewById<Button>(R.id.btnAdd).visibility = View.GONE

            val currentUser = auth.currentUser?.uid
            Log.e(TAG,"details activity in mainactvity $imageUrl $address $nameEditText  $notesEditText ")
            if (currentUser == userId) {
                findViewById<Button>(R.id.btnUpdate).visibility = View.VISIBLE
                findViewById<Button>(R.id.btnDelete).visibility = View.VISIBLE
            } else {
                findViewById<Button>(R.id.btnUpdate).visibility = View.GONE
                findViewById<Button>(R.id.btnDelete).visibility = View.GONE
            }
        } else {
            findViewById<Button>(R.id.btnUpdate).visibility = View.GONE
            findViewById<Button>(R.id.btnDelete).visibility = View.GONE
        }

        // Initialize the Activity Result Launcher
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data

                if (data != null) {
                     selectedImage = data.data!!
                    if (selectedImage != null) {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedImage!!)
                            if (inputStream != null) {
                                val bitMap = BitmapFactory.decodeStream(inputStream)
                                uploadImageView.setImageBitmap(bitMap)
                                val id = intent.getStringExtra("id") ?: ""
                                Utils.addBitmapToMemoryCache(id, bitMap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        // Set the click listener for the "UPLOAD IMAGE" button
        uploadImageButton.setOnClickListener {
            // Check if you have the required permission
            Log.e(TAG,"upload image clicked")
            if (checkPermissions()) {
                openImageGallery()
            } else {
                // Request storage permission
                requestStoragePermission()
            }
        }
    }

    private fun openImageGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        try {
            getContent.launch(galleryIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open the image gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
    }


    @Deprecated("This method is deprecated. Please use the Activity Result API instead.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                selectedImage = data.data!!

                if (selectedImage != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(selectedImage!!)
                        if (inputStream != null) {
                            val bitMap = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()
                            uploadImageView.setImageBitmap(bitMap)
                            val id = intent.getStringExtra("id") ?: ""
                            Utils.addBitmapToMemoryCache(id, bitMap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (data != null && data.hasExtra("latLng")) {
                latLng = data.getStringExtra("latLng") ?: ""
                address = data.getStringExtra("address") ?: ""
            }
        }
    }

    fun add(v: View) {
        val name = nameEditText.text.toString()
        val notes = notesEditText.text.toString()
        val rarity = rarityTypes[raritySpinner.selectedItem]
        val userId = auth.currentUser?.uid


        CoroutineScope(Dispatchers.IO).launch {
            val imageUri = firestoreClass.savePictureInStorage(selectedImage!!)
            Log.e(TAG, " firestore Class.savePictureInStorage $imageUri")
            val bird = Bird(
                name = name,
                rarity = rarity.toString(),
                notes = notes,
                image = imageUri,
                latLng = latLng,
                address = address,
                userId = userId.toString()
            )

            firestoreClass.addBird(bird)
            withContext(Dispatchers.Main) {
                Log.e(TAG, "The bird added is $bird")
                Toast.makeText(this@DetailsActivity, "Bird added successfully", Toast.LENGTH_SHORT)
                    .show()
            }

            val intent = Intent(this@DetailsActivity, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun update(v: View) {
        val name = nameEditText.text.toString()
        val notes = notesEditText.text.toString()
        val rarity = rarityTypes[raritySpinner.selectedItem]

        CoroutineScope(Dispatchers.IO).launch {
            var imageUri = imageUrl
            if (selectedImage != null) {
                imageUri = firestoreClass.savePictureInStorage(selectedImage!!)
                Log.e(TAG, "firestore Class.savePictureInStorage $imageUri")
            }

            val birdUpdates = mapOf(
                "name" to name,
                "rarity" to rarity.toString(),
                "notes" to notes,
                "image" to imageUri,
                "latLng" to latLng,
                "address" to address
            )

            firestoreClass.updateBird(modifyId,birdUpdates)
            withContext(Dispatchers.Main) {
                Log.e(TAG, "The bird added is $birdUpdates")
                Toast.makeText(this@DetailsActivity, "Bird updated successfully", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this@DetailsActivity, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun delete(v: View) {
        firestoreClass.deleteBird(modifyId)
        Log.e(TAG, "The bird added is $")
        Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@DetailsActivity, MainActivity2::class.java)
        startActivity(intent)
        finish()
    }
}
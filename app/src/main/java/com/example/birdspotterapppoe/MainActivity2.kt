package com.example.birdspotterapppoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.example.birdspotterapppoe.Constants.TAG
import kotlin.collections.ArrayList


class MainActivity2 : AppCompatActivity() {
    private val dbHandler = DatabaseHelper(this, null)
    private var birdList = ArrayList<Bird>()

    private lateinit var firestoreClass: FirestoreClass

    private lateinit var spinner: Spinner
    private lateinit var textView: TextView
    private lateinit var unitSwitch: SwitchCompat
    private lateinit var listView: ListView
    private var customAdapter: CustomAdapter? = null
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    //    private var lastSelectedSortOption: String = "date"
    private var rarityTypes = mapOf(Pair(0, "Common"), Pair(1, "Rare"), Pair(2, "Extremely rare"))
    private var filteredList: MutableList<Bird> = mutableListOf()
    private var originalBirdList: List<Bird> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // initialize firestore
        firestoreClass = FirestoreClass()

        textView = findViewById(R.id.textView)
        spinner = findViewById(R.id.sortSpinner)
        listView = findViewById(R.id.listView)
        autoCompleteTextView = findViewById(R.id.searchbar)

        val sortNames = resources.getStringArray(R.array.sort_types).toMutableList()
        sortNames.add("HOTSPOTS") // Add the new option

        customAdapter = CustomAdapter(this@MainActivity2, birdList)
        listView.adapter = customAdapter


        val myAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_1,
            sortNames
        )

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = myAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = sortNames[position]
                if (selectedItem == "HOTSPOTS") {
                    // Start the MapsActivity when "HOTSPOTS" is selected
                    val intent = Intent(this@MainActivity2, MapsActivity::class.java)
                    startActivity(intent)
                } else {
                    // Handle other sorting options
                    when (selectedItem) {
                        "SORT BY NAME" -> loadIntoList("name")
                        "SORT BY RARITY" -> loadIntoList("rarity")
                        "SORT BY DATE" -> loadIntoList("date")
                    }
                    Toast.makeText(
                        this@MainActivity2,
                        "Selected: $selectedItem",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        fun updateList() {
            if (autoCompleteTextView.text.toString().isEmpty()) {
                // If search text is cleared, reset the filteredList to the original birdList
                filteredList = birdList.toMutableList()
            } else {
                // If there is search text, filter the birdList
                filteredList = birdList.filter {
                    it.name?.contains(autoCompleteTextView.text.toString(), ignoreCase = true) == true ||
                            it.rarity?.contains(autoCompleteTextView.text.toString(), ignoreCase = true) == true ||
                            it.address?.contains(autoCompleteTextView.text.toString(), ignoreCase = true) == true
                }.toMutableList()
            }

//            when (spinner.selectedItem.toString()) {
//                "SORT BY NAME" -> filteredList.sortBy { it.name }
//                "SORT BY RARITY" -> filteredList.sortBy { it.rarity }
//                "SORT BY DATE" -> filteredList.sortBy { it.date }
//            }
            when (spinner.selectedItem.toString()) {
                "SORT BY NAME" -> loadIntoList("name")
                "SORT BY RARITY" -> loadIntoList("rarity")
                "SORT BY DATE" -> loadIntoList("date")
            }
            customAdapter?.updateList(filteredList)
        }


        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    filteredList = birdList.toMutableList()
                    Log.e(TAG,"filtered list $filteredList")
                } else {
                    filteredList = birdList.filter {
                        it.name?.contains(s.toString(), ignoreCase = true) == true ||
                                it.rarity?.contains(s.toString(), ignoreCase = true) == true ||
                                it.address?.contains(s.toString(), ignoreCase = true) == true
                    }.toMutableList()
                }

                when (spinner.selectedItem.toString()) {
                    "SORT BY NAME" -> filteredList.sortBy { it.name }
                    "SORT BY RARITY" -> filteredList.sortBy { it.rarity }
                    "SORT BY DATE" -> filteredList.sortBy { it.date }
                }
                customAdapter?.updateList(filteredList)
                autoCompleteTextView.clearFocus()
            }
        }
        )

    }


    fun loadIntoList(orderBy: String) {
        firestoreClass.getBirdList(orderBy) { birdList ->
            this.birdList.clear()
            this.birdList.addAll(birdList)

            customAdapter?.notifyDataSetChanged()

            if (birdList.isEmpty()) {
                textView.text = "Add a bird."
            } else {
                textView.visibility = View.GONE

                findViewById<ListView>(R.id.listView).setOnItemClickListener { _, _, i, _ ->
                    val selectedBird = if (filteredList.isNotEmpty()) {
                        filteredList[+i]
                    } else {
                        birdList[+i]
                    }
                    Log.e(TAG, "selectedbird in mainactvity2 $selectedBird")
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("id", selectedBird.id)
                    intent.putExtra("name", selectedBird.name)
                    intent.putExtra("notes", selectedBird.notes)
                    intent.putExtra("image", selectedBird.image)
                    intent.putExtra("latLng", selectedBird.latLng)
                    intent.putExtra("address", selectedBird.address)
                    intent.putExtra("userId", selectedBird.userId)
                    startActivity(intent)
                }
            }
        }
    }

    fun fabClicked(v: View) {
        val intent = Intent(this, DetailsActivity::class.java)
        startActivity(intent)
    }

    public override fun onResume() {
        super.onResume()
    }

}



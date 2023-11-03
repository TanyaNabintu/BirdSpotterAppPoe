package com.example.birdspotterapppoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.database.Cursor
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList


class MainActivity2 : AppCompatActivity() {
    private val dbHandler = DatabaseHelper(this, null)
    private var birdList = ArrayList<Bird>()

    private lateinit var firestoreClass:FirestoreClass

    private lateinit var spinner: Spinner
    private lateinit var textView: TextView
    private lateinit var unitSwitch: SwitchCompat
    private lateinit var listView: ListView
    private var customAdapter: CustomAdapter? = null
    private var rarityTypes = mapOf(Pair(0, "Common"), Pair(1, "Rare"), Pair(2, "Extremely rare"))



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // initialize firestore
        firestoreClass = FirestoreClass()

        textView = findViewById(R.id.textView)
        spinner = findViewById(R.id.sortSpinner)
        listView = findViewById(R.id.listView)

        val sortNames = resources.getStringArray(R.array.sort_types).toMutableList()
        sortNames.add("HOTSPOTS") // Add the new option

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
                        "SORT BY NAME" -> loadIntoList("ORDER BY LENGTH(name) DESC")
                        "SORT BY RARITY" -> loadIntoList("ORDER BY rarity DESC")
                        "SORT BY NOTES" -> loadIntoList("ORDER BY LENGTH(notes) DESC")
                        "SORT BY DATE" -> loadIntoList("ORDER BY date DESC")

                    }
                    Toast.makeText(
                        this@MainActivity2,
                        "Selected: $selectedItem",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun loadIntoList(orderBy: String) {
        firestoreClass.getBirdList { birdList ->

            val sortedList = when (orderBy) {
                "name" -> birdList.sortedBy { it.name }
                "rarity" -> birdList.sortedByDescending { it.rarity }
                "notes" -> birdList.sortedBy { it.notes }
                "date" -> birdList.sortedByDescending { it.date }
                else -> birdList
            }

            this.birdList.clear()
            this.birdList.addAll(birdList)

            customAdapter?.notifyDataSetChanged()

            if (birdList.count() == 0) {
                textView.text = "Add a bird."
            } else {
                textView.visibility = View.GONE

                customAdapter = CustomAdapter(this@MainActivity2, birdList as ArrayList<Bird>)
                listView.adapter = customAdapter

                findViewById<ListView>(R.id.listView).setOnItemClickListener { _, _, i, _ ->
                    val intent = Intent(this, DetailsActivity::class.java)

                    intent.putExtra("id", birdList[+i].id)
                    intent.putExtra("name", birdList[+i].name)
                    intent.putExtra("notes", birdList[+i].notes)
                    intent.putExtra("image", birdList[+i].image)
                    intent.putExtra("latLng", birdList[+i].latLng)
                    intent.putExtra("address", birdList[+i].address)


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
        loadIntoList("ORDER BY date DESC")
    }
}



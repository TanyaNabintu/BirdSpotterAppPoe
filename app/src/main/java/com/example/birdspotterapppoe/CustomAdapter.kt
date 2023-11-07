package com.example.birdspotterapppoe

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.Coil
import coil.load
import coil.request.ImageRequest
import com.example.birdspotterapppoe.Constants.TAG
import java.text.SimpleDateFormat
import java.util.Locale

class CustomAdapter(
    private val context: Context,
    private val birdList: ArrayList<Bird>,
    private val rarityTypes: Map<Int, String> = mapOf(
        Pair(0, "Common"),
        Pair(1, "Rare"),
        Pair(2, "Extremely rare")
    )

) : BaseAdapter() {
    private val inflater: LayoutInflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int): Any {
        return birdList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return birdList.size
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val dataitem = birdList[position]
        val rowView = inflater.inflate(R.layout.list_row, parent, false)

        rowView.findViewById<TextView>(R.id.row_name).text = dataitem.name
        rowView.findViewById<TextView>(R.id.row_notes).text = dataitem.notes

        // Convert FieldValue to String and format the date
        val originalFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
        val targetFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        val date = originalFormat.parse(dataitem.date.toString())
        val formattedDate = targetFormat.format(date)

        rowView.findViewById<TextView>(R.id.row_date).text = formattedDate


//      rowView.findViewById<TextView>(R.id.row_date).text = dataitem.date.toString()
        rowView.findViewById<TextView>(R.id.row_address).text = dataitem.address

        // Convert rarity to Int and use it as a key for rarityTypes
        val rarityInt = dataitem.rarity.toIntOrNull()
        if (rarityInt != null) {
            rowView.findViewById<TextView>(R.id.row_rarity).text = "(${rarityTypes[rarityInt]})"
        } else {
            // Handle the case where rarity is not a valid integer
            rowView.findViewById<TextView>(R.id.row_rarity).text = "(Invalid rarity value)"
        }

        val imageString = dataitem.image // Create a local copy of image
        Log.e(TAG, "the image loaded url is $imageString")

        if (imageString != null) {
            val imageView = rowView.findViewById<ImageView>(R.id.row_image)
            val context = imageView.context
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageString)
                .target(imageView)
                .build()
            imageLoader.enqueue(request)
        }


        rowView.tag = position
        return rowView
    }


}


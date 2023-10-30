package com.example.birdspotterapppoe

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create the 'birds' table
        db.execSQL(
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, " +
                    "$COLUMN_NAME TEXT, $COLUMN_RARITY INT, " +
                    "$COLUMN_NOTES TEXT, $COLUMN_IMAGE BLOB, " +
                    "$COLUMN_LATLONG TEXT, $COLUMN_ADDRESS TEXT, " +
                    "$COLUMN_DATE DATETIME)"
        )

        // Create the 'users' table
        db.execSQL(
            "CREATE TABLE $USERS_TABLE_NAME ($USERS_COLUMN_ID INTEGER PRIMARY KEY, " +
                    "$USERS_COLUMN_USERNAME TEXT, $USERS_COLUMN_PASSWORD TEXT)"
        )
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop existing tables (birds and users) and recreate them
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $USERS_TABLE_NAME")
        onCreate(db)
    }

    fun deleteAllBirds() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }


    fun insertRow(
        name: String,
        rarity: String,
        notes: String,
        image: ByteArray?,
        latLng: String?,
        address: String?
    ) {
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_RARITY, rarity)
        values.put(COLUMN_NOTES, notes)
        values.put(COLUMN_IMAGE, image)
        values.put(COLUMN_LATLONG, latLng)
        values.put(COLUMN_ADDRESS, address)
        values.put(COLUMN_DATE, getDateTime())

        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateRow(
        row_id: String,
        name: String,
        rarity: String,
        notes: String,
        image: ByteArray?,
        latLng: String?,
        address: String?
    ) {
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_RARITY, rarity)
        values.put(COLUMN_NOTES, notes)
        values.put(COLUMN_IMAGE, image)
        values.put(COLUMN_LATLONG, latLng)
        values.put(COLUMN_ADDRESS, address)

        val db = this.writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(row_id))
        db.close()
    }

    fun deleteRow(row_id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(row_id))
        db.close()
    }

    fun getAllRow(orderBy: String): Cursor? {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME $orderBy", null)
    }

    private fun getDateTime(): String? {

        val dateFormat = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }

    fun getBitmapByName(id: String): ByteArray? {
        val db = this.writableDatabase
        val qb = SQLiteQueryBuilder()

        val sqlSelect = arrayOf(COLUMN_IMAGE)
        qb.tables = TABLE_NAME
        val c = qb.query(db, sqlSelect, "id = ?", arrayOf(id), null, null, null)

        var result: ByteArray? = null

        if (c != null) {
            if (c.moveToFirst()) {
                val columnIndex = c.getColumnIndex(COLUMN_IMAGE)
                if (columnIndex != -1) {
                    result = c.getBlob(columnIndex)
                } else {
                    // Handle the case where the COLUMN_IMAGE does not exist
                    Log.e("DatabaseHelper", "COLUMN_IMAGE does not exist in the table")
                    return null
                }
            }
            c.close()
        }
        db.close()

        return result
    }

    // Insert a user into the 'users' table
    fun insertUser(username: String, password: String): Long {
        val values = ContentValues()
        values.put(USERS_COLUMN_USERNAME, username)
        values.put(USERS_COLUMN_PASSWORD, password)

        val db = this.writableDatabase
        val result = db.insert(USERS_TABLE_NAME, null, values)
        db.close()
        return result
    }

    // Check if a username exists in the 'users' table
    fun checkUsernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $USERS_TABLE_NAME WHERE $USERS_COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "myDBfile.db"
        const val TABLE_NAME = "birds"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_RARITY = "rarity"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_DATE = "date"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_LATLONG = "latLng"
        const val COLUMN_ADDRESS = "address"

        const val USERS_TABLE_NAME = "users"
        const val USERS_COLUMN_ID = "id"
        const val USERS_COLUMN_USERNAME = "username"
        const val USERS_COLUMN_PASSWORD = "password"
    }
}
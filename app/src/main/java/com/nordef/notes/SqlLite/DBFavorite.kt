package com.nordef.notes.SqlLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


class DBFavorite : SQLiteOpenHelper {

    internal val tableName = "favorite"
    internal val clmID = "itemID"

    constructor(context: Context, SQLName: String = "favorite.db", SQLVersion: Int = 1) :
            super(context, SQLName, null, SQLVersion)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $tableName (id INTEGER PRIMARY KEY AUTOINCREMENT, $clmID INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists $tableName")
        onCreate(db)
    }

    fun insertData(id: Int): Boolean {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(clmID, id)

        val result = db.insert(tableName, null, contentValues)
        db.close()

        return result.toInt() != -1
    }

    fun getData(): ArrayList<Int> {
        val arrayList = ArrayList<Int>()
        val db: SQLiteDatabase = this.readableDatabase

        val cursor: Cursor = db.rawQuery("select $clmID from $tableName", null)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            arrayList.add(cursor.getInt(0))
            cursor.moveToNext()
        }

        db.close()
        cursor.close()

        return arrayList
    }

    fun deleteData(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(tableName, "$clmID = ?",
                arrayOf(id.toString()))
        return result != 0
    }
}
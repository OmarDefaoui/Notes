package com.nordef.notes.SqlLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nordef.notes.ListItem.ListItemNote
import java.util.*


class DB : SQLiteOpenHelper {

    internal val tableName = "notes"
    internal val clmTitle = "title"
    internal val clmContent = "content"
    internal val clmDate = "date"
    internal val clmID = "id"

    constructor(context: Context, SQLName: String = "note.db", SQLVersion: Int = 1) :
            super(context, SQLName, null, SQLVersion)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $tableName ($clmID INTEGER PRIMARY KEY AUTOINCREMENT, $clmTitle TEXT, $clmContent TEXT, " +
                "$clmDate TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists $tableName")
        onCreate(db)
    }

    fun insertData(title: String, content: String, date: String): Boolean {

        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(clmTitle, title)
        contentValues.put(clmContent, content)
        contentValues.put(clmDate, date)

        val result = db.insert(tableName, null, contentValues)
        db.close()

        return result.toInt() != -1
    }

    fun getData(): ArrayList<ListItemNote> {
        val arrayList = ArrayList<ListItemNote>()
        val db: SQLiteDatabase = this.readableDatabase

        val cursor: Cursor = db.rawQuery("select * from $tableName", null)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            arrayList.add(ListItemNote(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)))
            cursor.moveToNext()
        }

        db.close()
        cursor.close()

        arrayList.reverse()
        return arrayList
    }

    fun getDataFromFavoris(arrayIDs: ArrayList<Int>): ArrayList<ListItemNote> {
        val arrayList = ArrayList<ListItemNote>()
        val db: SQLiteDatabase = this.readableDatabase

        val cursor: Cursor = db.rawQuery("select * from $tableName", null)

        cursor.moveToFirst()
        var i = 0
        while (!cursor.isAfterLast) {
            if (arrayIDs.contains(cursor.getInt(0)))
                arrayList.add(ListItemNote(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)))
            i++
            cursor.moveToNext()
        }

        db.close()
        cursor.close()

        arrayList.reverse()
        return arrayList
    }

    fun updateData(id: Int, title: String, content: String, date: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(clmTitle, title.trim())
        contentValues.put(clmContent, content.trim())
        contentValues.put(clmDate, date.trim())

        val result = db.update(tableName, contentValues, "$clmID = ?", arrayOf(id.toString()))

        db.close()
        return result != 0
    }

    fun deleteData(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(tableName, "$clmID = ?", arrayOf(id.toString()))
        return result != 0
    }
}
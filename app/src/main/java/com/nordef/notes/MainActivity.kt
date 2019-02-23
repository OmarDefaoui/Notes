package com.nordef.notes

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nordef.notes.fragment.DisplayNotes
import com.nordef.notes.fragment.SetNote

class MainActivity : AppCompatActivity() {

    internal var fragment: Fragment? = null
    internal var fragmentManager: FragmentManager? = null
    internal var fragmentTransaction: FragmentTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        moveToDisplayNote()
    }

    //display all notes
    fun moveToDisplayNote() {
        fragment = DisplayNotes()
        fragmentManager = getFragmentManager()
        fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction!!.replace(R.id.fragment, fragment)
        fragmentTransaction!!.commit()
    }

    //display favorite notes
    fun moveToDisplayNote(msg: String) {
        val bundle = Bundle()
        bundle.putString("isNormal", msg)

        fragment = DisplayNotes()
        fragmentManager = getFragmentManager()
        fragmentTransaction = fragmentManager!!.beginTransaction()
        fragment!!.arguments = bundle
        fragmentTransaction!!.replace(R.id.fragment, fragment)
        fragmentTransaction!!.commit()
    }

    //add new note
    fun moveToSetNote() {
        fragment = SetNote()
        fragmentManager = getFragmentManager()
        fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction!!.replace(R.id.fragment, fragment)
        fragmentTransaction!!.addToBackStack(null)
        fragmentTransaction!!.commit()
    }

    //edit existing note
    fun moveToSetNote(id: Int, title: String, content: String, date: String) {
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("title", title)
        bundle.putString("content", content)
        bundle.putString("date", date)

        fragment = SetNote()
        fragmentManager = getFragmentManager()
        fragmentTransaction = fragmentManager!!.beginTransaction()
        fragment!!.arguments = bundle
        fragmentTransaction!!.replace(R.id.fragment, fragment)
        fragmentTransaction!!.addToBackStack(null)
        fragmentTransaction!!.commit()
    }

    //go back to home fragment if we are in another one and back button had been pressed
    override fun onBackPressed() {

        val fm = getFragmentManager()
        if (fm.backStackEntryCount > 0) {
            fm.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }
}

package com.nordef.notes.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.nordef.notes.ListItem.ListItemNote
import com.nordef.notes.MainActivity
import com.nordef.notes.R
import com.nordef.notes.SqlLite.DB
import java.text.SimpleDateFormat
import java.util.*


class SetNote : Fragment() {

    internal lateinit var view: View
    internal lateinit var context: Context
    val TAG = "SetNote.kt"

    //view elements
    internal lateinit var et_title: EditText
    internal lateinit var et_content: EditText
    internal lateinit var btn_add: Button
    internal lateinit var iv_main: ImageView
    internal lateinit var iv_share: ImageView

    var itemID: Int = -1
    internal lateinit var title: String
    internal lateinit var content: String
    internal lateinit var date: String

    var oldTitle: String = ""
    var oldContent: String = ""

    internal lateinit var db: DB
    internal var fullArrayList = ArrayList<ListItemNote>()

    //when its true : we are writing a new note
    internal var isNormal: Boolean = true //is to set a new note

    //is used to see if we need to close the fragment or it will be close automatically by onDetach
    var finish_from_on_detach = false

    //to check if the user clicked on save, and we don't need to save it again when onDetach
    //in case if the user clicked btn back instaed of save
    internal var doneIsClicked = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_set_note, container, false)
        context = inflater.context

        //init data bases
        db = DB(context)
        fullArrayList = db.getData()

        //init views
        initViews()

        //if argument is empty: we are adding new note, else we are editing an existing one
        if (arguments != null) {
            isNormal = false

            itemID = arguments.getInt("id")
            title = arguments.getString("title")
            content = arguments.getString("content")
            date = arguments.getString("date")

            et_title.setText(title)
            et_content.setText(content)

            oldTitle = title
            oldContent = content
        }

        //btn save
        btn_add.setOnClickListener {
            title = et_title.text.toString().trim()
            content = et_content.text.toString().trim()

            callBtnAdd()
        }

        //btn go home (go back) : do same think than btn_add
        iv_main.setOnClickListener {
            btn_add.callOnClick()
        }

        //to share note content
        iv_share.setOnClickListener {
            val shareSubText = et_title.text.toString().trim()
            val shareBodyText = et_content.text.toString().trim()
            if (TextUtils.isEmpty(shareBodyText) && TextUtils.isEmpty(shareSubText))
                return@setOnClickListener

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, (shareSubText + "\n\n" + shareBodyText).trim())
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //display keyboard when open this page to write directly
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(et_content, InputMethodManager.SHOW_IMPLICIT)
        //set cursor in the end of the text
        et_content.setSelection(et_content.text.length)
    }

    private fun initViews() {
        et_title = view.findViewById(R.id.et_title)
        et_content = view.findViewById(R.id.et_content)
        btn_add = view.findViewById(R.id.btn_add)
        iv_main = view.findViewById(R.id.iv_main)
        iv_share = view.findViewById(R.id.iv_share)
    }

    private fun callBtnAdd() {
        if (checkInput(title, content)) {
            if (db.insertData(title, content, getCurrentDate())) {
                doneIsClicked = true
                finishThisFragment()
            } else
                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkInput(title: String, content: String): Boolean {
        //exit if the user clicked ok and didn't modify or add data
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            finishThisFragment()
            return false
        }

        //check if the user updated the note
        if (!title.equals(oldTitle) || !content.equals(oldContent)) {
            //we need to update user data
            if (itemID >= 0) {
                updateNote()
                return false
            }
            //this is a new note that we need to save it
            else
                return true

            //the user didn't change previous saved data
        } else {
            finishThisFragment()
            return false
        }
    }

    private fun updateNote() {
        if (db.updateData(itemID, title, content, getCurrentDate())) {
            doneIsClicked = true
            finishThisFragment()
        } else
            Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentDate(): String {
        val df = SimpleDateFormat.getDateInstance()
        val date = df.format(Calendar.getInstance().time)

        return date
    }

    override fun onDetach() {
        title = et_title.text.toString().trim()
        content = et_content.text.toString().trim()

        //check if user didn't add or modify data, to exist without saving
        if ((TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) ||
                (title.equals(oldTitle) && content.equals(oldContent))) {
            super.onDetach()
            return

        } else
        //if user didn't clicked ok button and we didn't save data yet
            if (!doneIsClicked) {
                Log.d(TAG, "saved before finish in on detach")
                finish_from_on_detach = true
                btn_add.callOnClick()

                //to show updated data in ui, because DisplayNotes fragment load data before saving new data
                //and we need to reload data
                try {
                    val fragment = (context as Activity).getFragmentManager()
                            .findFragmentById(R.id.fragment) as DisplayNotes
                    fragment.displayData()
                    Log.d(TAG, "data updated")
                } catch (e: Exception) {
                }
            }

        super.onDetach()
    }

    //go back to main fragment
    private fun finishThisFragment() {
        //check if we are from inStop
        if (finish_from_on_detach)
            return
        (context as MainActivity).onBackPressed()
    }
}
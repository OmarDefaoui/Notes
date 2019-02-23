package com.nordef.notes.fragment

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.nordef.notes.ListItem.ListItemNote
import com.nordef.notes.MainActivity
import com.nordef.notes.R
import com.nordef.notes.SqlLite.DB
import com.nordef.notes.SqlLite.DBFavorite
import com.nordef.notes.adapter.ListViewAdapter


class DisplayNotes : Fragment() {

    internal lateinit var view: View
    internal lateinit var context: Context
    val TAG = "DisplayNotes"

    internal lateinit var listView: ListView
    internal lateinit var adapter: ListViewAdapter

    internal lateinit var fa_add: FloatingActionButton
    internal lateinit var tv_name_page: TextView

    internal lateinit var iv_favorite: ImageView
    //true: we are displaying normal notes, if false: we are in favorite mode
    internal var isNormal: Boolean = true

    internal lateinit var db: DB
    internal lateinit var dbFavorite: DBFavorite
    internal var fullArrayList = ArrayList<ListItemNote>()

    var snackBarIsShown = false

    companion object {
        lateinit var mySnackbar: Snackbar
        var itemDeletedPosition = -1
        var undoDelete = false
        lateinit var deletedData: ListItemNote
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_display_notes, container, false)
        context = inflater.context

        //initialisation of view
        initViews()

        //init databases
        db = DB(context)
        dbFavorite = DBFavorite(context)

        displayData()

        fa_add.setOnClickListener {
            (context as MainActivity).moveToSetNote()
        }

        iv_favorite.setOnClickListener {
            if (snackBarIsShown) {
                Log.d(TAG, "snack bar still on screen")
                mySnackbar.dismiss()
            }

            if (isNormal)
                (context as MainActivity).moveToDisplayNote("yes")
            else
                (context as MainActivity).moveToDisplayNote()
        }

        return view
    }

    private fun initViews() {
        fa_add = view.findViewById(R.id.fa_add)
        iv_favorite = view.findViewById(R.id.iv_favorite)
        tv_name_page = view.findViewById(R.id.tv_name_page)
    }

    fun displayData() {
        fullArrayList = db.getData()

        //if we are displaying favorites
        if (arguments != null) {
            isNormal = false
            iv_favorite.setImageResource(R.drawable.ic_star_yellow_full)
            tv_name_page.text = getString(R.string.favorites)

            fullArrayList = db.getDataFromFavoris(dbFavorite.getData())
        }

        //load data if exists
        if (fullArrayList.isNotEmpty()) {
            listView = view.findViewById(R.id.list_view)
            adapter = ListViewAdapter(context, fullArrayList, db, dbFavorite, isNormal)
            listView.adapter = adapter
        }
    }

    //this snackbar offers the possibility to undo note delete
    fun displayDeleteSnackBar() {
        mySnackbar = Snackbar.make(view.findViewById(R.id.cl_container),
                context.getString(R.string.note_deleted), Snackbar.LENGTH_SHORT)

        mySnackbar.setAction(context.getString(R.string.undo)) {
            Log.d(TAG, "undo clicked")
            undoDelete = true
        }

        mySnackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                //we have to delete data
                if (!undoDelete) {
                    db.deleteData(deletedData.id)
                    dbFavorite.deleteData(deletedData.id)
                    Log.d(TAG, "item deleted")
                }
                //the user clicked undo delete button
                else {
                    fullArrayList.add(itemDeletedPosition, deletedData)
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "we have to show again deleted data")
                }
                undoDelete = false
                Log.d(TAG, "snackbar dismissed")
                snackBarIsShown = false
            }
        })

        mySnackbar.show()
        snackBarIsShown = true
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //hide keyboard
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
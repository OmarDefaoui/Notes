package com.nordef.notes.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nordef.notes.ListItem.ListItemNote
import com.nordef.notes.MainActivity
import com.nordef.notes.R
import com.nordef.notes.SqlLite.DB
import com.nordef.notes.SqlLite.DBFavorite
import com.nordef.notes.fragment.DisplayNotes

class ListViewAdapter : BaseAdapter {

    var arrayList = ArrayList<ListItemNote>()
    var context: Context
    var db: DB
    var dbFavorite: DBFavorite
    var isNormal: Boolean = true

    constructor(context: Context, arrayList: ArrayList<ListItemNote>, db: DB, dbFavorite: DBFavorite,
                isNormal: Boolean) {
        this.context = context
        this.arrayList = arrayList
        this.db = db
        this.dbFavorite = dbFavorite
        this.isNormal = isNormal
    }

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(p0: Int): Any {
        return arrayList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }


    override fun getView(i: Int, view0: View?, viewGroup: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.row_item_dislay_notes, null)

        //init views
        val tv_title: TextView = view.findViewById(R.id.tv_title)
        val tv_content: TextView = view.findViewById(R.id.tv_content)
        val iv_delete: ImageView = view.findViewById(R.id.iv_delete)
        val iv_modify: ImageView = view.findViewById(R.id.iv_modify)
        val ll_note: LinearLayout = view.findViewById(R.id.ll_note)
        val tv_date: TextView = view.findViewById(R.id.tv_date)
        val iv_favorite: ImageView = view.findViewById(R.id.iv_favorite)
        val iv_share: ImageView = view.findViewById(R.id.iv_share)

        val title = arrayList[i].title
        val content = arrayList[i].content
        val date = arrayList[i].date
        val id = arrayList[i].id

        tv_title.text = if (TextUtils.isEmpty(title)) context.getString(R.string.no_title) else title
        tv_content.text = if (TextUtils.isEmpty(content)) context.getString(R.string.no_content) else content
        tv_date.text = date

        //set yellow star to item that exists in dbFavorite
        if (dbFavorite.getData().contains(id))
            iv_favorite.setImageResource(R.drawable.ic_star_yellow)

        iv_delete.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirmation))
                    .setMessage(context.getString(R.string.confirm_delete_item))
                    .setPositiveButton(context.getString(R.string.yes)) { dialog, which ->

                        //remove row from view
                        arrayList.removeAt(i)
                        notifyDataSetChanged()

                        //save the deleted item data in order to restore it if user want
                        DisplayNotes.itemDeletedPosition = i
                        DisplayNotes.deletedData = ListItemNote(id, title, content, date)

                        //display snackbar
                        val fragment = (context as Activity).getFragmentManager()
                                .findFragmentById(R.id.fragment) as DisplayNotes
                        fragment.displayDeleteSnackBar()
                    }
                    .setNegativeButton(context.getString(R.string.no)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
        }

        iv_modify.setOnClickListener {
            (context as MainActivity).moveToSetNote(id, title, content, date)
        }

        ll_note.setOnClickListener {
            (context as MainActivity).moveToSetNote(id, title, content, date)
        }

        iv_share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, (title + "\n\n" + content).trim())
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_with)))
        }

        iv_favorite.setOnClickListener {
            //use wants to delete an item from dbFavorite
            if (dbFavorite.getData().contains(id)) {
                if (dbFavorite.deleteData(id)) {
                    if (!isNormal) {
                        arrayList.removeAt(i)
                        notifyDataSetChanged()
                    }
                    iv_favorite.setImageResource(R.drawable.ic_star_grey)
                } else
                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //user wants to add new data to favorite
            if (dbFavorite.insertData(id)) {
                iv_favorite.setImageResource(R.drawable.ic_star_yellow)
            } else
                Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
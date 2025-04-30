package com.example.aicte

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat

class CourseAdapter(
    private val context: Context,
    private val items: MutableList<Pair<String, String>>,
    private val onRemoveClicked: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = items[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_course, parent, false)

        val courseName = view.findViewById<TextView>(R.id.course_name)
        val removeButton = view.findViewById<Button>(R.id.remove_button)

        courseName.text = item.second
        removeButton.setOnClickListener {
            onRemoveClicked(item.first)
            items.removeAt(position)
            notifyDataSetChanged()
        }

        return view
    }
}

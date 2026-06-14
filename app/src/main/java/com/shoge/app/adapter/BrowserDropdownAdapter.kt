package com.shoge.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.shoge.app.R
import com.shoge.app.model.BrowserInfo

/**
 * Drives the browser ExposedDropdownMenu.
 * Shows each browser's system icon alongside its label.
 */
class BrowserDropdownAdapter(
    context: Context,
    private val items: List<BrowserInfo>
) : ArrayAdapter<BrowserInfo>(context, R.layout.item_browser, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        bind(position, convertView, parent)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
        bind(position, convertView, parent)

    private fun bind(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_browser, parent, false)

        getItem(position)?.let { browser ->
            view.findViewById<ImageView>(R.id.iv_browser_icon).setImageDrawable(browser.icon)
            view.findViewById<TextView>(R.id.tv_browser_name).text = browser.label
        }
        return view
    }
}
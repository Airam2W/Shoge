package com.shoge.app.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.shoge.app.R

/**
 * Grid adapter for the color picker bottom sheet.
 * Each swatch is a colored oval; the selected one shows a white checkmark.
 */
class ColorSwatchAdapter(
    private val colors: List<Int>,
    initialColor: Int,
    private val onColorPicked: (Int) -> Unit
) : RecyclerView.Adapter<ColorSwatchAdapter.ViewHolder>() {

    private var selectedColor = initialColor

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: View      = view.findViewById(R.id.view_swatch_color)
        val checkIcon: ImageView = view.findViewById(R.id.iv_swatch_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_swatch, parent, false)
    )

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]

        // Paint the oval with this swatch's color
        holder.colorView.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

        holder.checkIcon.visibility = if (color == selectedColor) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val prev = colors.indexOf(selectedColor)
            selectedColor = color
            if (prev >= 0) notifyItemChanged(prev)
            notifyItemChanged(position)
            onColorPicked(color)
        }
    }
}
package com.example.cineverseprototype

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(val context:Context) : RecyclerView.ItemDecoration() {

    private val attrs = intArrayOf(android.R.attr.listDivider)

    private lateinit var divider:Drawable

    init {
        val style = context.obtainStyledAttributes(attrs)
        divider = style.getDrawable(0)!!
        style.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left =parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for(i in 0 until childCount){
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}
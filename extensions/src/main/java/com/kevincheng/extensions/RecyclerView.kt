package com.kevincheng.extensions

import androidx.recyclerview.widget.RecyclerView

/**
 * Forces RecyclerView adapter to call createViewHolder() - i.e. redraw all item's layouts
 */

fun RecyclerView.redraw() {
    val myAdapter = adapter
    adapter = myAdapter
}
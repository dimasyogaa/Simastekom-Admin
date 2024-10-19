package com.yogadimas.simastekom.common.custom

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecoration : RecyclerView.ItemDecoration() {
    private var lastItemPadding = 0
    private var isPaddingAdded = false

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
            if (!isPaddingAdded) {
                outRect.bottom = 300
                lastItemPadding = 300
                isPaddingAdded = true
            }
        } else {
            // Reset padding flag jika bukan item terakhir
            isPaddingAdded = false
        }
    }

    fun getLastItemPadding(): Int {
        return lastItemPadding
    }
}

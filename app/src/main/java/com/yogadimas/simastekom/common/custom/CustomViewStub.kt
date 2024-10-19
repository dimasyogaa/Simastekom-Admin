package com.yogadimas.simastekom.common.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.view.ViewStub
import com.yogadimas.simastekom.R

//class CustomViewStub (
//    private val viewStub: ViewStub
//) {
//    private var inflatedView: View? = null
//
//    fun inflate(): View {
//        if (inflatedView == null) {
//            Log.e("TAG", "inflate 1 : $inflatedView", )
//            inflatedView = viewStub.inflate()
//            Log.e("TAG", "inflate 11 : $inflatedView", )
//        }
//        Log.e("TAG", "inflate 2 : $inflatedView", )
//        return inflatedView!!
//    }
//
//    fun isInflated(): Boolean {
//        return inflatedView != null
//    }
//}

class CustomViewStub(
    private val viewStub: ViewStub,
    private val fallbackParent: ViewGroup
) {
    private var inflatedView: View? = null

    fun inflate(): View {
        if (inflatedView == null) {
            try {
                // Cek apakah parent dari ViewStub masih ada
                val parent = viewStub.parent
                if (parent != null) {
                    inflatedView = viewStub.inflate()
                } else {
                    throw IllegalStateException("ViewStub tidak memiliki parent yang valid.")
                }
            } catch (e: IllegalStateException) {
                // Jika inflate gagal, kita bisa menambahkan View secara manual ke fallbackParent
                inflatedView = LayoutInflater.from(viewStub.context)
                    .inflate(R.layout.layout_student_manipulation_text_inputs_1, fallbackParent, false)
                fallbackParent.addView(inflatedView)
            }
        }
        return inflatedView!!
    }

    fun isInflated(): Boolean {
        return inflatedView != null
    }
}


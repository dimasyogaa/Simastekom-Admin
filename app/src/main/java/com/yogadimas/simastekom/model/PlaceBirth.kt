package com.yogadimas.simastekom.model

import com.yogadimas.simastekom.helper.simpleDateFormatHelper
import java.util.Calendar

data class PlaceBirth(val place: String, val birthDate: Calendar?) {
    override fun toString(): String = "$place, ${birthDate?.time?.let {
        simpleDateFormatHelper().format(it)}}"

    companion object {
        fun parse(input: String): PlaceBirth? {
            val parts = input.split(", ")
            if (parts.size != 2) return null

            val place = parts[0]
            val date = simpleDateFormatHelper().parse(parts[1]) ?: return null

            val calendar = Calendar.getInstance()
            calendar.time = date

            return PlaceBirth(place, calendar)
        }

    }
}

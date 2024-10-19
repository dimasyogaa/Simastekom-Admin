package com.yogadimas.simastekom.model

import android.os.Parcelable
import android.util.Log
import com.yogadimas.simastekom.common.helper.capitalizeWords
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    var province: String = "- ",
    var cityRegency: String = "-, ",
    var district: String = "-, ",
    var village: String = "-, ",
    var rw: String = "-, ",
    var rt: String = "-, ",
    var street: String = "-, ",
    var otherDetailAddress: String = "-, ",
) : Parcelable {
    private fun String.formatData() = if (isNotEmpty()) capitalizeWords().trim() + ", " else "-, "
    private fun String.formatDataView(): String {
        return if (isNotEmpty() && contains("-")) {
            ""
        } else if (isNotEmpty() && !contains("-")) {
            capitalizeWords().trim() + ", "
        } else {
            ""
        }
    }

    fun toDatabase(): String {
        val formatProvince = if (province.isNotEmpty()) province.capitalizeWords().trim() else "-"
        val formatCityRegency = cityRegency.formatData()
        val formatDistrict = district.formatData()
        val formatVillage = village.formatData()
        val formatRW = rw.formatData()
        val formatRT = rt.formatData()
        val formatStreet = street.formatData()
        val formatOtherDetailAddress = otherDetailAddress.formatData()

        return "$formatStreet$formatOtherDetailAddress$formatRW$formatRT$formatVillage$formatDistrict$formatCityRegency$formatProvince"
    }

    fun toView(): String {
        var formatProvince = province.formatDataView()
        val formatCityRegency = cityRegency.formatDataView()
        val formatDistrict = district.formatDataView()
        val formatVillage = village.formatDataView()
        val formatRW = rw.formatDataView()
        val formatRT = rt.formatDataView()
        val formatStreet = street.formatDataView()
        val formatOtherDetailAddress = otherDetailAddress.formatDataView()

        if (formatProvince.contains(",")) {
            val parts = formatProvince.split(", ")
            formatProvince = parts[0]
        }

        var formatRWRT = ""
        if (formatRW.isNotEmpty() &&
            !formatRW.contains("-") &&
            formatRT.isNotEmpty() &&
            !formatRT.contains("-")) {
            val parts = (formatRW + formatRT).split(", ")
            formatRWRT = "RW ${parts[0]}/RT ${parts[1]}, "
        }
        if (formatRW.isNotEmpty() &&
            !formatRW.contains("-") &&
            formatRT.isEmpty()) {
            val parts = (formatRW + formatRT).split(", ")
            formatRWRT = "RW ${parts[0]}, "
        }
        if (formatRT.isNotEmpty() &&
            !formatRT.contains("-") &&
            formatRW.isEmpty()) {
            val parts = (formatRW + formatRT).split(", ")
            formatRWRT = "RT ${parts[1]}, "
        }


        return "$formatStreet$formatOtherDetailAddress$formatRWRT$formatVillage$formatDistrict$formatCityRegency$formatProvince"
    }

    companion object {
        fun parse(input: String?): Address {
            val address = Address()
            input?.let {
                val parts = it.split(", ")
                if (parts.size == 8) {
                    address.province = parts[7].trim()
                    address.cityRegency = parts[6].trim()
                    address.district = parts[5].trim()
                    address.village = parts[4].trim()
                    address.rt = parts[3].trim()
                    address.rw = parts[2].trim()
                    address.otherDetailAddress = parts[1].trim()
                    address.street = parts[0].trim()
                }
            }

            return address
        }
    }
}


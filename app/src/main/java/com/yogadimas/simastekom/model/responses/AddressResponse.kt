package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize



data class AddressObjectResponse(
    @field:SerializedName("data")
    val addressData: AddressData,
)

@Parcelize
data class AddressData(
    @field:SerializedName("id_pengguna")
    var userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    var userType: String? = null,

    @field:SerializedName("nim")
    var studentIdNumber: String? = null,

    @field:SerializedName("nidn")
    var lectureIdNumber: String? = null,

    @field:SerializedName("nama_pengguna")
    var username: String? = null,

    @field:SerializedName("identitas_ortu")
    var identityParentData: StudentIdentityParentData? = null,

    @field:SerializedName("provinsi")
    var province: String? = null,

    @field:SerializedName("kota")
    var cityRegency: String? = null,

    @field:SerializedName("kecamatan")
    var district: String? = null,

    @field:SerializedName("desa_kelurahan")
    var village: String? = null,

    @field:SerializedName("rw")
    var rw: String? = null,

    @field:SerializedName("rt")
    var rt: String? = null,

    @field:SerializedName("nama_jalan_gedung")
    var street: String? = null,

    @field:SerializedName("detail_lainnya")
    var otherDetailAddress: String? = null,

    @field:SerializedName("is_added")
    var isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    var isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    var isDeleted: Boolean = false,

    val isFromAdminStudent: Boolean = false,

    var isStudentIdentityParent: Boolean = false,
): Parcelable {
    companion object {
        private val setRwRtFormat: (String, String) -> String = {rw, rt ->
            if (rw.isNotEmpty()) {
                if (rt.isNotEmpty()) {
                    "RW $rw/RT $rt, "
                } else {
                    "RW $rw"
                }
            } else {
                if (rt.isNotEmpty()) {
                    "RT $rt"
                } else {
                    ""
                }
            }
        }
        fun getAddressData(addressData: AddressData?): String? {
            return addressData?.let { value ->
                // Mengambil bagian-bagian alamat dan memfilter yang kosong
                val parts = listOf(
                    value.street.orEmpty(),
                    value.otherDetailAddress.orEmpty(),
                    setRwRtFormat(value.rw.orEmpty(), value.rt.orEmpty()),
                    value.village.orEmpty(),
                    value.district.orEmpty(),
                    value.cityRegency.orEmpty(),
                    value.province.orEmpty()
                ).filter { it.isNotBlank() } // Hanya menyimpan bagian yang tidak kosong

                // Menggabungkan bagian-bagian yang valid dengan koma
                if (parts.isNotEmpty()) {
                    parts.joinToString(", ")
                } else null

            }
        }

    }
}

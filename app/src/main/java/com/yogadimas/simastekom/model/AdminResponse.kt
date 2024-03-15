package com.yogadimas.simastekom.model

import com.google.gson.annotations.SerializedName

data class AdminResponse(

	@field:SerializedName("data")
	val data: Data,

)

data class Data(

	@field:SerializedName("foto_profil")
	val fotoProfil: String?,

	@field:SerializedName("nama")
	val nama: String,

	@field:SerializedName("nama_pengguna")
	val namaPengguna: String,

	@field:SerializedName("id_pengguna")
	val idPengguna: String,

	@field:SerializedName("token")
	val token: String
)

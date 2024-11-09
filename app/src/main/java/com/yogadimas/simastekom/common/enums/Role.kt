package com.yogadimas.simastekom.common.enums

enum class Role(val value: String) {
    STUDENT("mahasiswa"),
    LECTURE("dosen"),
    ADMIN("admin"),
    PARENT("mahasiswa_identitas_ortu");

    companion object {
        fun fromValue(value: String): Role? {
            return entries.find { it.value == value }
        }
    }
}
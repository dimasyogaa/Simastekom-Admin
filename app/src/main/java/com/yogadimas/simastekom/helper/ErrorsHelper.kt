package com.yogadimas.simastekom.helper

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.yogadimas.simastekom.model.responses.Errors
import org.json.JSONObject

fun getErrors(response: String): Errors {

    val gson = Gson()
    val jsonParser = JsonParser()

    val jsonObjectErrors = try {
        JSONObject(response)
    } catch (e: Exception) {
        JSONObject(getErrorsServer())
    }

    val jsonElementErrors = jsonParser.parse(jsonObjectErrors.toString())

    return gson.fromJson(jsonElementErrors, Errors::class.java)


}

fun getErrorsServer(): String {
    return """
        {
            "errors": {
                "message": [
                    "Maaf terjadi kesalahan pada server kami, segera kami perbaiki."
                ]
            }
        }
    """.trimIndent()
}
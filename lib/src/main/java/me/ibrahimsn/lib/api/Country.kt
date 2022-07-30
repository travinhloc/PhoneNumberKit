package me.ibrahimsn.lib.api

import android.content.Context

data class Country(
    val iso2: String,
    val name: String,
    val code: Int,
){
    fun getFlagResource(context: Context): Int {
        return context.resources.getIdentifier(
            "country_flag_$iso2",
            "drawable",
            context.packageName
        )
    }

}

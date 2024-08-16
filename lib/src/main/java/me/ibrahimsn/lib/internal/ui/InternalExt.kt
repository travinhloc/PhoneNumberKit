package me.ibrahimsn.lib.internal.ui

import android.content.Context

fun Context.getFlagResource(iso2: String?): Int {
    return resources.getIdentifier(
        "country_flag_$iso2",
        "drawable",
        this.packageName
    )
}
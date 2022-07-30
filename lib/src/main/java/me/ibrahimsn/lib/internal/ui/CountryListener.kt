package me.ibrahimsn.lib.internal.ui

import me.ibrahimsn.lib.api.Country

interface CountryListener {
    fun getCountry(country: Country)
}
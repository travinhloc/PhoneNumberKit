package me.ibrahimsn.phonenumberkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.internal.ui.CountryListener
import me.ibrahimsn.lib.internal.ui.CountryPickerBottomSheet
import me.ibrahimsn.phonenumberkit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBottom.setOnClickListener {
            CountryPickerBottomSheet.newInstance().apply {
                setReturnCountryListener(object : CountryListener {
                    override fun getCountry(country: Country) {
                        Toast.makeText(this@MainActivity, country.name,Toast.LENGTH_LONG ).show()
                    }

                })
                show(
                    this@MainActivity.supportFragmentManager,
                    CountryPickerBottomSheet.TAG
                )
            }
        }
    }

    companion object {
        private const val TAG = "###"
    }
}

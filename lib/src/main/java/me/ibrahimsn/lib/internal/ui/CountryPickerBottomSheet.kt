package me.ibrahimsn.lib.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.PhoneNumberKit
import me.ibrahimsn.lib.R
import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.databinding.BottomSheetCountryPickerBinding
import me.ibrahimsn.lib.internal.ext.default
import me.ibrahimsn.lib.internal.ext.toCountryList
import me.ibrahimsn.lib.internal.io.FileReader
import java.util.*

class CountryPickerBottomSheet : BottomSheetDialogFragment() {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.Main)

    private lateinit var binding: BottomSheetCountryPickerBinding

    var onCountrySelectedListener: ((Country) -> Unit)? = null
    lateinit var countryListener: CountryListener

    private val viewState: MutableStateFlow<CountryPickerViewState> = MutableStateFlow(
        CountryPickerViewState(emptyList())
    )

    private val itemAdapter: CountryAdapter by lazy {
        CountryAdapter(R.layout.item_country_picker)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_PhoneNumberKit_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCountryPickerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        collectViewState()
        fetchData()
    }

    private fun initView() = with(binding) {
        recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchCountries(newText)
                return true
            }
        })

        itemAdapter.onItemClickListener = {
            onCountrySelectedListener?.invoke(it)
            countryListener.getCountry(it)
            dismiss()
        }
    }

    private fun collectViewState() = scope.launch {
        viewState.collect {
            itemAdapter.setup(it.countries)
        }
    }

    private fun fetchData() = scope.launch {
        val countries = default {
            FileReader.readAssetFile(requireContext(), PhoneNumberKit.ASSET_FILE_NAME)
                .toCountryList()
        }
        viewState.value = CountryPickerViewState(countries)
    }

    fun setReturnCountryListener(listener: CountryListener){
        this.countryListener = listener
    }

    private fun searchCountries(query: String?) {
        scope.launch {
            query?.let {
                val countries = viewState.value.countries
                val filtered = countries.filter {
                    it.code.toString().startsWith(query) ||
                            it.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
                }
                binding.recyclerView.post {
                    itemAdapter.setup(filtered)
                }
            }
        }
    }


    companion object {
        const val TAG = "countryPickerBottomSheet"
        fun newInstance(listener: CountryListener) = CountryPickerBottomSheet().apply {
            setReturnCountryListener(listener)
        }
    }
}
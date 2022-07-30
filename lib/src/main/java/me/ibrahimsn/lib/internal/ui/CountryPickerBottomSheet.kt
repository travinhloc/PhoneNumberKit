package me.ibrahimsn.lib.internal.ui

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(0x10)
        dialog.setOnShowListener {
            Handler(Looper.getMainLooper()).post {
                val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
                bottomSheet?.let {
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                    bottomSheet.background = ContextCompat.getDrawable(requireContext(),
                        R.drawable.bg_bottom_sheet_white_radius_35)
                }
            }
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
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

        search.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCountries(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
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
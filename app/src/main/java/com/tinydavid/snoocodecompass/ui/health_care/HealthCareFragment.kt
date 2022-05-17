package com.tinydavid.snoocodecompass.ui.health_care

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.tinydavid.snoocodecompass.databinding.FragmentHealthCareBinding
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import com.tinydavid.snoocodecompass.ui.MainViewModel
import com.tinydavid.snoocodecompass.ui.health_care.adapter.HealthCareListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthCareFragment : Fragment() {

    private var _binding: FragmentHealthCareBinding? = null

    private val mBinding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private val mViewModel: HealthCareViewModel by viewModels()

    private lateinit var mLoadingGroup: Group
    private lateinit var mErrorText: TextView

    private lateinit var mHealthCareRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHealthCareBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLoadingGroup = mBinding.groupHealthCareLoading
        mErrorText = mBinding.textHealthCareError

        mHealthCareRecycler = mBinding.recyclerHealthCare

        mHealthCareRecycler.requestFocus()

//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                mViewModel.uiState.collect { uiState ->
//                    // New value received
//                    when (uiState) {
//                        is HealthCareUiState.Loading -> showLoading()
//                        is HealthCareUiState.Success -> showHospitals(uiState.healthCares)
//                        is HealthCareUiState.Error -> showError(getString(R.string.load_health_care_error_message))
//                    }
//                }
//
//            }
//        }

        mainViewModel.healthCares.observe(viewLifecycleOwner) { healthCares ->
            showHospitals(healthCares)
        }

    }

    override fun onResume() {
        super.onResume()
        mHealthCareRecycler.requestFocus()
    }

    private fun showLoading() {
        mErrorText.visibility = View.GONE
        mHealthCareRecycler.visibility = View.GONE
        mLoadingGroup.visibility = View.VISIBLE
    }

    private fun showHospitals(healthCares: List<HealthCare>) {

        val listAdapter =
            HealthCareListAdapter(
                requireContext(),
                ::onHospitalClick,
            )
//        if (applicationContext.resources.configuration.isScreenRound)
//            mHealthCareRecycler.isEdgeItemsCenteringEnabled = true
//
//        mHealthCareRecycler.layoutManager = WearableLinearLayoutManager(this@HospitalActivity)

        mHealthCareRecycler.adapter = listAdapter

        mLoadingGroup.visibility = View.GONE
        mHealthCareRecycler.visibility = View.VISIBLE

        listAdapter.setHealthCares(healthCares)

        setupSearchView(listAdapter)

    }

    private fun onHospitalClick(healthCare: HealthCare) {
        val action =
            HealthCareFragmentDirections.actionFragmentHealthCareToFragmentCompassNavigation(
                healthCare
            )
        findNavController().navigate(action)
    }

    private fun setupSearchView(listAdapter: HealthCareListAdapter) {

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                listAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable) {
                // ignore
            }
        }
        mBinding.searchHealthCare.addTextChangedListener(afterTextChangedListener)

    }

    private fun showError(message: String) {
        mLoadingGroup.visibility = View.GONE

        mErrorText.visibility = View.VISIBLE
        mErrorText.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
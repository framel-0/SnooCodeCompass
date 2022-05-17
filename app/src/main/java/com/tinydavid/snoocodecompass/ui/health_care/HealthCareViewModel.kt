package com.tinydavid.snoocodecompass.ui.health_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinydavid.snoocodecompass.domain.repositories.HealthCareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthCareViewModel @Inject constructor(
    healthCareRepository: HealthCareRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HealthCareUiState>(HealthCareUiState.Loading)

    val uiState: StateFlow<HealthCareUiState> = _uiState

    private val healthCares = healthCareRepository.healthCares

    init {

        viewModelScope.launch {

            _uiState.value = HealthCareUiState.Success(healthCares)
//            hospitals
//                .catch { ex ->
//                    _uiState.value = HospitalUiState.Error(ex)
//                }
//                .collect { hospitals ->
//                    _uiState.value = HospitalUiState.Success(hospitals)
//                }
        }

    }
}
package com.tinydavid.snoocodecompass.ui.home

import com.tinydavid.snoocodecompass.domain.models.HealthCare

sealed class HomeUiState{
    object Loading : HomeUiState()
    class Success(val healthCares: List<HealthCare>) : HomeUiState()
    data class Error(val exception: Throwable) : HomeUiState()
}

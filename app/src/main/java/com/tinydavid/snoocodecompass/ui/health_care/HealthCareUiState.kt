package com.tinydavid.snoocodecompass.ui.health_care

import com.tinydavid.snoocodecompass.domain.models.HealthCare

sealed class HealthCareUiState{
    object Loading : HealthCareUiState()
    class Success(val healthCares: List<HealthCare>) : HealthCareUiState()
    data class Error(val exception: Throwable) : HealthCareUiState()
}

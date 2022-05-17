package com.tinydavid.snoocodecompass.domain.repositories

import com.tinydavid.snoocodecompass.domain.models.HealthCare

interface HealthCareRepository {
    val healthCares: List<HealthCare>
}
package com.tinydavid.snoocodecompass.ui.health_care.adapter

import androidx.recyclerview.widget.RecyclerView
import com.tinydavid.snoocodecompass.databinding.ListItemHealthCareBinding
import com.tinydavid.snoocodecompass.domain.models.HealthCare

class HealthCareViewHolder(
    private val binding: ListItemHealthCareBinding,
    onHealthCareClick: (HealthCare) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var _healthCare: HealthCare

    fun bind(healthCare: HealthCare) {
        _healthCare = healthCare
        binding.textName.text = _healthCare.name
        binding.textAmenity.text = _healthCare.amenity
    }

    init {
        itemView.setOnClickListener {
            onHealthCareClick(_healthCare)
        }
    }

}
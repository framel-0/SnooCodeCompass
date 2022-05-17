package com.tinydavid.snoocodecompass.ui.health_care.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.tinydavid.snoocodecompass.databinding.ListItemHealthCareBinding
import com.tinydavid.snoocodecompass.domain.models.HealthCare
import dagger.hilt.android.qualifiers.ActivityContext
import java.util.*

class HealthCareListAdapter(
    @ActivityContext private val context: Context,
    private val onHealthCareClick: (HealthCare) -> Unit,
) : RecyclerView.Adapter<HealthCareViewHolder>(), Filterable {


    private var _healthCares = listOf<HealthCare>()
    var healthCareFilter = listOf<HealthCare>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthCareViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding =
            ListItemHealthCareBinding.inflate(layoutInflater, parent, false)
        return HealthCareViewHolder(
            binding,
            onHealthCareClick,
        )
    }

    override fun onBindViewHolder(holder: HealthCareViewHolder, position: Int) {
        val hospital = healthCareFilter[position]
        holder.bind(hospital)
    }

    override fun getItemCount(): Int = healthCareFilter.size

    fun setHealthCares(healthCares: List<HealthCare>) {
        _healthCares = healthCares
        healthCareFilter = healthCares
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                healthCareFilter = if (charSearch.isEmpty()) {
                    _healthCares
                } else {
                    val resultList: ArrayList<HealthCare> = arrayListOf()
                    for (healthCare in _healthCares) {

                        val healthCareName = healthCare.name

                        if ((healthCareName.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)))
                        ) {
                            resultList.add(healthCare)
                        }
                    }

                    resultList

                }

                val filterResults = FilterResults()
                filterResults.values = healthCareFilter
                return filterResults

            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                healthCareFilter = results?.values as List<HealthCare>

                notifyDataSetChanged()
            }

        }
    }
}
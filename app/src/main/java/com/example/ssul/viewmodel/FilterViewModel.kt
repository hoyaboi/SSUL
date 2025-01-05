package com.example.ssul.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ssul.model.FilterModel

class FilterViewModel : ViewModel() {

    private val _filters = MutableLiveData<FilterModel>()
    val filters: LiveData<FilterModel> get() = _filters

    init {
        _filters.value = FilterModel()
    }

    // 필터 상태 토글
    fun toggleFilter(filterType: String) {
        val currentFilters = _filters.value ?: FilterModel()

        _filters.value = when (filterType) {
            "group" -> currentFilters.copy(groupFilter = !currentFilters.groupFilter)
            "date" -> currentFilters.copy(dateFilter = !currentFilters.dateFilter)
            "efficiency" -> currentFilters.copy(efficiencyFilter = !currentFilters.efficiencyFilter)
            "partner" -> currentFilters.copy(partnerFilter = !currentFilters.partnerFilter)
            else -> currentFilters
        }
    }
}

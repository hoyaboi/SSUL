package com.example.ssul.model

data class FilterModel(
    val groupFilter: Boolean = false,
    val dateFilter: Boolean = false,
    val efficiencyFilter: Boolean = false,
    val partnerFilter: Boolean = false
) {
    fun hasActiveFilters(): Boolean {
        return groupFilter || dateFilter || efficiencyFilter || partnerFilter
    }
}
package com.example.ssul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ssul.model.FilterModel
import com.example.ssul.model.StoreModel
import com.example.ssul.repository.StoreRepository
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StoreRepository()
    private val _allStores = MutableLiveData<MutableList<StoreModel>>()
    private val _filteredStores = MutableLiveData<MutableList<StoreModel>>()
    val storeItems: LiveData<MutableList<StoreModel>> get() = _filteredStores
    // FavoriteFragment 위해 즐겨찾기 아이템 추가? var favStoreItems

    private var college: String = ""
    private var degree: String = ""

    init {
        loadCollegeInfo()
        loadStores()
    }

    // 학과 정보 불러오기
    private fun loadCollegeInfo() {
        val prefs = getApplication<Application>().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        college = prefs.getString("selectedCollege", "") ?: ""
        degree = prefs.getString("selectedDepartment", "") ?: ""
    }

    // 가게 데이터 불러오기
    private fun loadStores() {
        viewModelScope.launch {
            val stores = repository.getStores(college, degree)
            _allStores.value = stores
            _filteredStores.value = stores
        }
    }

    // 필터 상태에 따른 가게 필터링
    fun applyFilters(filters: FilterModel) {
        val allStores = _allStores.value ?: mutableListOf()

        val filteredList = allStores.filter { item ->
            (filters.groupFilter && item.isFilterGroupChecked || !filters.groupFilter) &&
                    (filters.dateFilter && item.isFilterDateChecked || !filters.dateFilter) &&
                    (filters.efficiencyFilter && item.isFilterEfficiencyChecked || !filters.efficiencyFilter) &&
                    (filters.partnerFilter && item.isAssociated || !filters.partnerFilter)
        }.toMutableList()

        // 필터링이 없으면 전체 리스트 복구
        _filteredStores.value = if (filteredList.isEmpty() && !filters.hasActiveFilters()) {
            allStores
        } else {
            filteredList
        }
    }
}

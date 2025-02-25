package com.example.ssul.viewmodel

import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.ssul.R
import com.example.ssul.StoreItem
import com.example.ssul.model.CoordinateRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class MapViewModel : ViewModel()  {
    private val _repository = CoordinateRepository()

    private val _activeFilters = MutableLiveData<MutableSet<Int>>().apply { value = mutableSetOf() }
    val activeFilters: LiveData<MutableSet<Int>> get() = _activeFilters

    private val _storeList = MutableLiveData<MutableList<StoreItem>>().apply { value = mutableListOf() }
    val storeList: LiveData<MutableList<StoreItem>> get() = _storeList

    private val _markerList = MutableLiveData<MutableList<Marker>>().apply { value = mutableListOf() }
    val markerList: LiveData<MutableList<Marker>> get() = _markerList

    fun addMarker(marker: Marker){
        val currentList = _markerList.value ?: mutableListOf()
        currentList.add(marker)
        _markerList.value = currentList
    }

    fun clearMarkers(){
        _markerList.value?.forEach { it.map = null }
        _markerList.value?.clear()
    }

    fun addStores(stores : List<StoreItem>){
        val currentStores = _storeList.value ?: mutableListOf()
        currentStores.addAll(stores)
        _storeList.value = currentStores
    }

    fun toggleFilter(filterId: Int) {
        val currentFilters = _activeFilters.value ?: mutableSetOf()
        if (currentFilters.contains(filterId)) {
            currentFilters.remove(filterId) // 필터 비활성화
        } else {
            currentFilters.add(filterId) // 필터 활성화
        }
        _activeFilters.value = currentFilters
    }

    fun getFilteredStores(): List<StoreItem> { // 리턴 값이 있는 게 좋지 않음 별도로 필터리스트를 갖는 값을 만들고 그걸 View에서 사용하는게 나을듯
        return storeList.value?.filter { store ->
            _activeFilters.value?.all { filterId ->
                when (filterId) {
                    R.id.filter_group_button -> store.isFilterGroupChecked
                    R.id.filter_date_button -> store.isFilterDateChecked
                    R.id.filter_efficiency_button -> store.isFilterEfficiencyChecked
                    R.id.filter_partner_button -> store.isAssociated
                    else -> true
                }
            } ?: true
        } ?: emptyList()
    }

    fun getCoordinatesFromAddress(context : Context, address : String) : LatLng? { // 마찬가지
        _repository.getCoordinate(address)?.let { return it }

        val geocoder = Geocoder(context, Locale.getDefault())

        return try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                _repository.setCoordinate(address, latLng)
                latLng
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
//현재 min api가 24이고 getFromLocation의 바뀐건 33부터
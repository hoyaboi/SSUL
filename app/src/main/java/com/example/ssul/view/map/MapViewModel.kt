package com.example.ssul.view.map

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssul.R
import com.example.ssul.StoreItem
import com.example.ssul.model.CoordinateRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val _repository = CoordinateRepository()

    private val storeList: MutableList<StoreItem> = mutableListOf()

    private val filteredStoreList = MutableStateFlow<List<StoreItem>>(emptyList())

    private val _filteredMarkers = MutableStateFlow<List<Marker>>(emptyList())
    val filteredMarkers: StateFlow<List<Marker>> = _filteredMarkers.asStateFlow()

    private val _selectedPopup = MutableStateFlow<StorePopUpView?>(null)
    val selectedPopUp = _selectedPopup.asStateFlow()

    private val _activeFilters = MutableStateFlow<MutableSet<Int>>(mutableSetOf())
    val activeFilters = _activeFilters.asStateFlow()

    private var selectedMarker: Marker? = null // 선택된 마커 저장

    fun addStores(stores: MutableList<StoreItem>) {
        storeList.addAll(stores)
        basicSetup()
    }

    fun basicSetup() {
        val updatedFilteredStores = filteredStoreList.value.toMutableList()
        updatedFilteredStores.addAll(storeList)
        filteredStoreList.value = updatedFilteredStores
        markerSetup()
    }

    fun setFilter(filterId: Int) {
        val currentFilters = _activeFilters.value.toMutableSet()
        if (currentFilters.contains(filterId)) {
            currentFilters.remove(filterId)
        } else {
            currentFilters.add(filterId)
        }
        _activeFilters.value = currentFilters
        filteringStore()
    }

    fun searchFilter(query: String, naverMap: NaverMap) {
        val filteredStores = storeList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        val context = getApplication<Application>().applicationContext
        if (filteredStores.isNotEmpty()) {
            filteredStoreList.value = filteredStores
            markerSetup()
            val targetStore = filteredStores.first()
            val targetMarker = _filteredMarkers.value.find { it.captionText == targetStore.name }

            targetMarker?.let { marker ->
                handleMarkerClick(
                    marker,
                    targetStore,
                    context
                )
                naverMap.moveCamera(CameraUpdate.scrollTo(marker.position))
            }
        } else {
            Toast.makeText(context, "$query(은)는 존재하지 않는 술집입니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun filteringStore() {
        val filtered = storeList.filter { store ->
            _activeFilters.value.all { filterId ->
                when (filterId) {
                    R.id.filter_group_button -> store.isFilterGroupChecked
                    R.id.filter_date_button -> store.isFilterDateChecked
                    R.id.filter_efficiency_button -> store.isFilterEfficiencyChecked
                    R.id.filter_partner_button -> store.isAssociated
                    else -> true
                }
            }
        }
        filteredStoreList.value = filtered // StateFlow로 필터링된 리스트 갱신
        markerSetup()
    }

    private fun markerSetup() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val newMarkers = mutableListOf<Marker>()

            _filteredMarkers.value.forEach { marker ->
                withContext(Dispatchers.Main) {
                    marker.map = null
                }
            }

            filteredStoreList.value.forEach { store ->
                try {
                    val coordinates = getCoordinatesFromAddress(context, store.address)
                    if (coordinates != null) {
                        val marker = Marker().apply {
                            position = coordinates
                            captionText = store.name
                            icon = OverlayImage.fromResource(R.drawable.ic_store)
                            captionColor = Color.rgb(0xAF, 0x8E, 0xFF)

                            setOnClickListener {
                                handleMarkerClick(this, store, context)
                                true
                            }
                        }
                        newMarkers.add(marker)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                _filteredMarkers.value = newMarkers
            }
        }
    }

    private fun getCoordinatesFromAddress(context: Context, address: String): LatLng? {
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

    private fun handleMarkerClick(marker: Marker, store: StoreItem, context: Context) {
        selectedMarker?.let {
            it.icon = OverlayImage.fromResource(R.drawable.ic_store) // 기본 아이콘 복구
        }

        selectedMarker = marker
        marker.icon = OverlayImage.fromResource(R.drawable.ic_store_selected) // 선택된 아이콘으로 변경

        // 가게 정보 팝업 표시
        setupStoreInfoPopup(marker, store, context)
    }

    private fun setupStoreInfoPopup(marker: Marker, store: StoreItem, context: Context) {
        val popUpView = StorePopUpView(context, store)
        popUpView.tag = store.id
        popUpView.storeId = store.id
        popUpView.imageUrl = store.imageUrl
        popUpView.marker = marker
        _selectedPopup.value = popUpView
    }


//    fun getLatLngFromAddressAsync(context: Context, address: String, callback: (Pair<Double, Double>?) -> Unit) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            val geocoder = Geocoder(context, Locale.getDefault())
//            geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
//                override fun onGeocode(addresses: MutableList<android.location.Address>) {
//                    val result = addresses.firstOrNull()?.let {
//                        Pair(it.latitude, it.longitude)
//                    }
//                    callback(result)
//                }
//
//                override fun onError(errorMessage: String?) {
//                    callback(null)
//                }
//            })
//        } else {
//            callback(getLatLngFromAddress(context, address))
//        }
//    }
//    //api 33이상부터 사용 가능
}
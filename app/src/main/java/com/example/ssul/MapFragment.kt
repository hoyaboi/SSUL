package com.example.ssul

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MapFragment : Fragment() {
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private var polyline: PolylineOverlay? = null
    private val markerList = mutableListOf<Marker>()

    private var selectedMarker: Marker? = null // 선택된 마커 저장
    private var storeInfoPopup: View? = null // 팝업 뷰 참조
    private var storeItems: MutableList<StoreItem> = mutableListOf() // 전체 가게 리스트
    fun setStoreItems(items: MutableList<StoreItem>) {
        storeItems = items
    }

    private val geocodingCache = mutableMapOf<String, LatLng>() // 주소-좌표 캐시

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTagFilters(view) //태그버튼 초기화
        setupSearchFunctionality(view) // 검색창 초기화

        // 위치 소스 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        // FragmentContainerView 안의 MapFragment 가져오기
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment)
                as? com.naver.maps.map.MapFragment
            ?: com.naver.maps.map.MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
            // 위치 활성화 설정
            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow

            // UI 설정 (현재 위치 버튼 등)
            naverMap.uiSettings.isLocationButtonEnabled = true
            setupMapClickListener() // 지도 클릭 이벤트 초기화
            updateMarkers(storeItems)
        }
    }

    override fun onResume() {
        super.onResume()
        storeInfoPopup?.let { popupView ->
            val favoriteButton = popupView.findViewById<ImageView>(R.id.favorite_button)
            val storeId = popupView.tag as? Int // 팝업에 저장된 가게 ID 가져오기

            storeId?.let { id ->
                val sharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
                val isFavorite = sharedPreferences.getBoolean(id.toString(), false)

                // 버튼 상태 갱신
                updateFavoriteButtonState(favoriteButton, isFavorite)
            }
        }
    }

    private fun setupTagFilters(view: View) {
        // 각 필터 버튼과 관련된 데이터를 매핑
        val tagFilters = mapOf(
            R.id.filter_group_button to { store: StoreItem -> store.isFilterGroupChecked },
            R.id.filter_date_button to { store: StoreItem -> store.isFilterDateChecked },
            R.id.filter_efficiency_button to { store: StoreItem -> store.isFilterEfficiencyChecked },
            R.id.filter_partner_button to { store: StoreItem -> store.isAssociated }
        )

        // 현재 활성화된 필터 상태를 저장하는 Set
        val activeFilters = mutableSetOf<Int>()

        for ((buttonId, filterCondition) in tagFilters) {
            val button = view.findViewById<TextView>(buttonId)
            button.setOnClickListener {
                if (activeFilters.contains(buttonId)) {
                    // 이미 활성화된 필터를 비활성화
                    activeFilters.remove(buttonId)
                    button.setBackgroundResource(R.drawable.filter_non_clicked)
                    button.setTextAppearance(requireContext(), R.style.filter_text_style)
                } else {
                    // 새로운 필터를 활성화
                    activeFilters.add(buttonId)
                    button.setBackgroundResource(R.drawable.filter_clicked)
                    button.setTextAppearance(requireContext(), R.style.filter_selected_text_style)
                }

                // 활성화된 모든 필터 조건을 적용하여 데이터를 필터링
                val filteredStores = storeItems.filter { store ->
                    activeFilters.all { filterId ->
                        tagFilters[filterId]?.invoke(store) ?: true
                    }
                }

                // 마커 업데이트
                updateMarkers(filteredStores)
            }
        }
    }

    private fun updateMarkers(storeList: List<StoreItem>) {
        clearExistingMarkers() // 기존 마커 제거

        storeList.forEach { store ->
            GlobalScope.launch(Dispatchers.IO) { // 비동기로 실행
                try {
                    val coordinates = getCoordinatesFromAddress(store.address) // 좌표 변환
                    if (coordinates != null) {
                        withContext(Dispatchers.Main) { // UI 작업은 메인 스레드에서 처리
                            val marker = Marker().apply {
                                position = coordinates
                                map = naverMap
                                captionText = store.name
                                icon = OverlayImage.fromResource(R.drawable.ic_store)
                                captionColor = Color.rgb(0xAF, 0x8E, 0xFF)

                                setOnClickListener {
                                    handleMarkerClick(this, store)
                                    true
                                }
                            }
                            markerList.add(marker)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 주소를 좌표로 변환하는 메서드 (캐시 적용)
    private fun getCoordinatesFromAddress(address: String): LatLng? {
        // 캐시된 값이 있으면 바로 반환
        geocodingCache[address]?.let { return it }

        // 캐시된 값이 없으면 Geocoder를 사용하여 좌표 변환
        return try {
            val addresses = Geocoder(requireContext()).getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                // 좌표를 캐시에 저장
                geocodingCache[address] = latLng
                latLng
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun handleMarkerClick(marker: Marker, store: StoreItem) {
        // 이전 선택된 마커 복구
        selectedMarker?.let {
            it.icon = OverlayImage.fromResource(R.drawable.ic_store) // 기본 아이콘 복구
        }

        // 현재 마커 선택 상태로 변경
        selectedMarker = marker
        marker.icon = OverlayImage.fromResource(R.drawable.ic_store_selected) // 선택된 아이콘으로 변경

        // 가게 정보 팝업 표시
        showStoreInfoPopup(store)

        // 선택된 가게까지의 경로 표시
        showRouteToStore(marker.position)
    }

    // 팝업 표시
    private fun showStoreInfoPopup(store: StoreItem) {
        // 기존 팝업 제거
        storeInfoPopup?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            storeInfoPopup = null
        }

        // 팝업 레이아웃 인플레이션
        val popupView = layoutInflater.inflate(R.layout.popup_store_info, null).apply {
            val storeName = findViewById<TextView>(R.id.store_name)
            val storeAddress = findViewById<TextView>(R.id.store_address)
            val favoriteButton = findViewById<ImageView>(R.id.favorite_button)
            val storeImage = findViewById<ImageView>(R.id.store_image)
            val partnerTag = findViewById<ImageView>(R.id.partner_status)

            storeName.text = store.name
            storeAddress.text = "주소: ${store.address}"
            Glide.with(this)
                .load(store.imageUrl) // 이미지 URL
                .placeholder(R.drawable.default_image) // 로딩 중 보여줄 이미지
                .error(R.drawable.default_image) // 오류 시 보여줄 이미지
                .into(storeImage) // ImageView에 이미지 적용

            // 제휴 상태에 따라 partnerTag visibility 설정
            partnerTag.visibility = if (store.isAssociated) View.VISIBLE else View.INVISIBLE

            // SharedPreferences에서 즐겨찾기 상태 로드
            val sharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
            val isFavorite = sharedPreferences.getBoolean(store.id.toString(), false)

            // 초기 즐겨찾기 버튼 상태 설정
            updateFavoriteButtonState(favoriteButton, isFavorite)

            // 즐겨찾기 버튼 클릭 리스너
            favoriteButton.setOnClickListener {
                val sharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
                val isFavorite = sharedPreferences.getBoolean(store.id.toString(), false) // 현재 즐겨찾기 상태 확인

                if (isFavorite) {
                    // 이미 즐겨찾기 상태 -> 제거 작업
                    (activity as? MainActivity)?.showMessageBox(
                        message = getString(R.string.remove_favorite),
                        onYesClicked = {
                            sharedPreferences.edit().remove(store.id.toString()).apply() // 즐겨찾기 상태 제거
                            updateFavoriteButtonState(favoriteButton, false) // 버튼 상태 업데이트
                        }
                    )
                } else {
                    // 즐겨찾기 추가
                    sharedPreferences.edit().putBoolean(store.id.toString(), true).apply() // 상태 저장
                    updateFavoriteButtonState(favoriteButton, true) // 버튼 상태 업데이트
                }
            }
        }

        popupView.tag = store.id // 가게 ID를 태그로 설정

        // 팝업의 위치와 크기를 조정하여 추가
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            // 하단 네비게이션 바 위로 팝업을 배치
            setMargins(dpToPx(24), 0, dpToPx(24), dpToPx(35))
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        // 루트 컨테이너에 팝업 추가
        (view as? ViewGroup)?.addView(popupView, layoutParams)
        storeInfoPopup = popupView

        // 팝업 클릭 시 StoreActivity로 이동
        popupView.setOnClickListener {
            val intent = Intent(requireContext(), StoreActivity::class.java).apply {
                putExtra("storeId", store.id)
                putExtra("storeImage", store.imageUrl)
            }
            startActivity(intent)
        }
    }

    // 즐겨찾기 버튼 상태 업데이트 메소드
    private fun updateFavoriteButtonState(favoriteButton: ImageView, isFavorite: Boolean) {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.favorite_clicked
            else R.drawable.favorite_non_clicked
        )
    }

    // 지도 클릭 시 팝업 제거 및 마커 복구
    private fun setupMapClickListener() {
        naverMap.setOnMapClickListener { _, _ ->
            // Restore all markers
            updateMarkers(storeItems)

            // Existing popup and marker reset logic
            storeInfoPopup?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                storeInfoPopup = null
            }

            selectedMarker?.let {
                it.icon = OverlayImage.fromResource(R.drawable.ic_store)
                selectedMarker = null
            }

            // Reset search field
            view?.findViewById<EditText>(R.id.search_store_textfield)?.apply {
                setText("")
                visibility = View.GONE
                view?.findViewById<TextView>(R.id.search_text)?.visibility = View.VISIBLE
            }
        }
    }

    private fun clearExistingMarkers() {
        // 기존 마커 제거
        markerList.forEach { it.map = null }
        markerList.clear()

        // 기존 경로 제거
        polyline?.map = null
        polyline = null
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    //경로 받아오기
    private fun fetchRoute(currentLocation: LatLng, destination: LatLng) {
        val clientId = "5womszkja3" // 네이버 API Client ID
        val clientSecret = "6nCQKXiCGpTayMAo1Ac2QuMS32Cpb7cr6hSLGoAZ" // 네이버 API Client Secret
        val url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
                "?start=${currentLocation.longitude},${currentLocation.latitude}" +
                "&goal=${destination.longitude},${destination.latitude}" +
                "&option=trafast" // 옵션: 'trafast' (최적 경로), 'tracomfort' (안전 경로)

        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
            .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // 실패 시 로그 출력
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        parseRoute(responseBody)
                    }
                }
            }
        })
    }

    private fun showRouteToStore(markerLocation: LatLng) {
        val currentLocation = naverMap.locationOverlay.position // 현재 위치 가져오기
        fetchRoute(currentLocation, markerLocation)
    }

    private fun parseRoute(responseBody: String) {
        val jsonObject = JSONObject(responseBody)
        val route = jsonObject.getJSONObject("route").getJSONArray("trafast").getJSONObject(0)
        val path = route.getJSONArray("path")

        val polylineCoords = mutableListOf<LatLng>()
        for (i in 0 until path.length()) {
            val point = path.getJSONArray(i)
            val lat = point.getDouble(1)
            val lng = point.getDouble(0)
            polylineCoords.add(LatLng(lat, lng))
        }

        activity?.runOnUiThread {
            polyline?.map = null
            polyline = PolylineOverlay().apply {
                coords = polylineCoords
                color = 0xFF0000FF.toInt() // 파란색
                width = 10 // 선의 두께
            }
            polyline?.map = naverMap
        }
    }

    private fun setupSearchFunctionality(view: View) {
        val searchTextField = view.findViewById<EditText>(R.id.search_store_textfield)
        val searchButton = view.findViewById<ImageView>(R.id.search_button)
        val searchText = view.findViewById<TextView>(R.id.search_text)

        // Toggle search input visibility
        searchText.setOnClickListener {
            searchText.visibility = View.GONE
            searchTextField.visibility = View.VISIBLE
            searchTextField.requestFocus()

            //키보드 표시
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchTextField, InputMethodManager.SHOW_IMPLICIT)
        }

        searchButton.setOnClickListener {
            performSearch(searchTextField.text.toString())
        }

        searchTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchTextField.text.toString())

                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                searchTextField.clearFocus() // 검색창 포커스 해제
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        //필터링 로직
        val filteredStores = storeItems.filter {
            it.name.contains(query, ignoreCase = true)
        }

        if (filteredStores.isNotEmpty()) {
            updateMarkers(filteredStores)

            val firstStore = filteredStores.first()
            val firstMarker = markerList.find { it.captionText == firstStore.name }

            firstMarker?.let { marker ->
                handleMarkerClick(marker, firstStore)
                naverMap.moveCamera(CameraUpdate.scrollTo(marker.position))
            }
        } else {
            Toast.makeText(context,"$query(은)는 존재하지 않는 술집입니다.", Toast.LENGTH_LONG).show()
        }
    }

    // dp 값을 px 값으로 변환
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}


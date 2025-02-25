package com.example.ssul

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ssul.viewmodel.MapViewModel
import com.example.ssul.viewmodel.RouteParser
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class MapFragment : Fragment() {
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private var selectedMarker: Marker? = null // 선택된 마커 저장
    private var storeInfoPopup: View? = null // 팝업 뷰 참조

    private var routeParser: RouteParser? = null // 경로생성기
    private lateinit var mapViewModel: MapViewModel // ViewModel

    fun setStoreItems(items: MutableList<StoreItem>) {
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        mapViewModel.addStores(items)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routeParser = RouteParser()

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
            mapViewModel.storeList.value?.let { updateMarkers(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        storeInfoPopup?.let { popupView ->
            val favoriteButton = popupView.findViewById<ImageView>(R.id.favorite_button)
            val storeId = popupView.tag as? Int // 팝업에 저장된 가게 ID 가져오기

            storeId?.let { id ->
                val sharedPreferences =
                    requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
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

        // 버튼 클릭 시 ViewModel에 필터 상태 변경 요청
        tagFilters.forEach { (buttonId, _) ->
            val button = view.findViewById<TextView>(buttonId)
            button.setOnClickListener {
                mapViewModel.toggleFilter(buttonId) // 필터 상태 변경
            }
        }

        // 필터 상태를 관찰하여 마커 업데이트
        mapViewModel.activeFilters.observe(viewLifecycleOwner, Observer { activeFilters ->
            for ((buttonId, _) in tagFilters) {
                val button = view.findViewById<TextView>(buttonId)
                if (activeFilters.contains(buttonId)) {
                    button.setBackgroundResource(R.drawable.filter_clicked)
                    button.setTextAppearance(requireContext(), R.style.filter_selected_text_style)
                } else {
                    button.setBackgroundResource(R.drawable.filter_non_clicked)
                    button.setTextAppearance(requireContext(), R.style.filter_text_style)
                }
            }

            // 필터 적용 후 마커 업데이트
            val filteredStores = mapViewModel.getFilteredStores()
            updateMarkers(filteredStores)
        })
    }

    private fun updateMarkers(storeList: List<StoreItem>) {
        clearExistingMarkers() // 기존 마커 제거
        storeList.forEach { store ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { // 비동기로 실행
                try {
                    val coordinates = mapViewModel.getCoordinatesFromAddress(
                        requireContext(),
                        store.address
                    ) // 좌표 변환
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
                            mapViewModel.addMarker(marker)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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
            storeAddress.text = getString(R.string.address_text, store.address)
            Glide.with(this)
                .load(store.imageUrl) // 이미지 URL
                .placeholder(R.drawable.default_image) // 로딩 중 보여줄 이미지
                .error(R.drawable.default_image) // 오류 시 보여줄 이미지
                .into(storeImage) // ImageView에 이미지 적용

            // 제휴 상태에 따라 partnerTag visibility 설정
            partnerTag.visibility = if (store.isAssociated) View.VISIBLE else View.INVISIBLE

            // SharedPreferences에서 즐겨찾기 상태 로드
            val sharedPreferences =
                requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
            val isFavorite = sharedPreferences.getBoolean(store.id.toString(), false)

            // 초기 즐겨찾기 버튼 상태 설정
            updateFavoriteButtonState(favoriteButton, isFavorite)

            // 즐겨찾기 버튼 클릭 리스너
            favoriteButton.setOnClickListener {
                val btnSharedPreferences =
                    requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
                val btnIsFavorite =
                    btnSharedPreferences.getBoolean(store.id.toString(), false) // 현재 즐겨찾기 상태 확인

                if (btnIsFavorite) {
                    // 이미 즐겨찾기 상태 -> 제거 작업
                    (activity as? MainActivity)?.showMessageBox(
                        message = getString(R.string.remove_favorite),
                        onYesClicked = {
                            btnSharedPreferences.edit().remove(store.id.toString())
                                .apply() // 즐겨찾기 상태 제거
                            updateFavoriteButtonState(favoriteButton, false) // 버튼 상태 업데이트
                        }
                    )
                } else {
                    // 즐겨찾기 추가
                    btnSharedPreferences.edit().putBoolean(store.id.toString(), true).apply() // 상태 저장
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
            mapViewModel.storeList.value?.let { updateMarkers(it) }

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
        mapViewModel.clearMarkers()
        // 기존 경로 제거
        routeParser?.clearPolyLine()
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
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchTextField, InputMethodManager.SHOW_IMPLICIT)
        }

        searchButton.setOnClickListener {
            performSearch(searchTextField.text.toString())
        }

        searchTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchTextField.text.toString())

                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                searchTextField.clearFocus() // 검색창 포커스 해제
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        //필터링 로직
        val filteredStores = mapViewModel.storeList.value?.filter {
            it.name.contains(query, ignoreCase = true)
        }

        filteredStores?.let {
            if (filteredStores.isNotEmpty()) {
                updateMarkers(filteredStores)

                val firstStore = filteredStores.first()
                val firstMarker =
                    mapViewModel.markerList.value?.find { it.captionText == firstStore.name }

                firstMarker?.let { marker ->
                    handleMarkerClick(marker, firstStore)
                    naverMap.moveCamera(CameraUpdate.scrollTo(marker.position))
                }
            } else {
                Toast.makeText(context, "$query(은)는 존재하지 않는 술집입니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRouteToStore(markerLocation: LatLng) {
        val currentLocation = naverMap.locationOverlay.position // 현재 위치 가져오기
        activity?.let {
            routeParser?.showRouteToStore(
                it,
                naverMap,
                currentLocation,
                markerLocation
            )
        }
    }

    // dp 값을 px 값으로 변환
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
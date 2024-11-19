package com.example.ssul

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ssul.model.TestModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTagFilters(view) //태그버튼 초기화

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
        }
    }

    private fun setupTagFilters(view: View) {
        val tagButtons = listOf(
            R.id.filter_group_button to listOf(
                TestModel("씨밤", 37.49465, 126.9564),
                TestModel("젠", 37.49465, 126.9566230)
            ),
            R.id.filter_date_button to listOf(
                TestModel("블루힐", 37.49467, 126.9564230)
            ),
            R.id.filter_efficiency_button to listOf(
                TestModel("젠", 37.49465, 126.9566230),
                TestModel("블루힐", 37.49467, 126.9564230)
            ),
            R.id.filter_partner_button to listOf(
                TestModel("씨밤", 37.49465, 126.9564),
                TestModel("블루힐", 37.49467, 126.9564230)
            )
        )

        var activeButtonId: Int? = null

        for ((buttonId, storeList) in tagButtons) {
            val button = view.findViewById<TextView>(buttonId)
            button.setOnClickListener {
                if (activeButtonId == buttonId) {
                    // 동일 버튼을 다시 눌렀을 경우 상태 초기화
                    button.setBackgroundResource(R.drawable.filter_non_clicked)
                    button.setTextAppearance(requireContext(), R.style.filter_text_style)
                    activeButtonId = null
                    clearExistingMarkers()
                } else {
                    // 이전 활성 버튼 초기화
                    activeButtonId?.let { prevButtonId ->
                        view.findViewById<TextView>(prevButtonId).apply {
                            setBackgroundResource(R.drawable.filter_non_clicked) // 배경 복구
                            setTextAppearance(
                                requireContext(),
                                R.style.filter_text_style
                            ) // 텍스트 스타일 복구
                        }
                    }

                    // 새로 활성화된 버튼 상태 설정
                    button.setBackgroundResource(R.drawable.filter_clicked) // 새로운 배경
                    button.setTextAppearance(
                        requireContext(),
                        R.style.filter_selected_text_style
                    ) // 새로운 텍스트 스타일
                    activeButtonId = buttonId

                    // 마커 업데이트
                    updateMarkers(storeList)
                }
            }
        }
    }

    private fun updateMarkers(storeList: List<TestModel>) {
        // 기존 마커 제거
        clearExistingMarkers()

        // 새로운 마커 추가
        storeList.forEach { store ->
            val marker = Marker().apply {
                position = LatLng(store.latitude, store.longitude)
                map = naverMap
                captionText = store.name
                icon = OverlayImage.fromResource(R.drawable.ic_store)
                captionColor = Color.rgb(0xAF, 0x8E, 0xFF)
                setOnClickListener {
                    showRouteToStore(position)
                    true
                }
            }
            markerList.add(marker)
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


    //주소 좌표로 바꾸는 지오코드 메소드(아직 사용안함)
    private fun convertAddressToCoordinates(address: String) {
        val geocoder = context?.let { Geocoder(it) }
        try {
            val addresses: MutableList<Address>? = geocoder?.getFromLocationName(address, 1)

            if (addresses!!.isNotEmpty()) {
                val location: Address = addresses[0]
                val latitude = location.latitude
                val longitude = location.longitude
            } else {
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
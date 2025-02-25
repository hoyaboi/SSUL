package com.example.ssul.viewmodel

import androidx.activity.ComponentActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.PolylineOverlay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RouteParser{
    private var polyline: PolylineOverlay? = null

    fun showRouteToStore(activity : ComponentActivity, naverMap: NaverMap,currentLocation: LatLng, destination: LatLng) {
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
                        parseRoute(activity, naverMap, responseBody)
                    }
                }
            }
        })
    }

    fun clearPolyLine(){
        polyline?.map = null
        polyline = null
    }

    private fun parseRoute(activity : ComponentActivity, naverMap: NaverMap, responseBody: String) {
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

        activity.runOnUiThread {
            polyline?.map = null
            polyline = PolylineOverlay().apply {
                coords = polylineCoords
                color = 0xFF0000FF.toInt() // 파란색
                width = 10 // 선의 두께
            }
            polyline?.map = naverMap
        }
    }
}
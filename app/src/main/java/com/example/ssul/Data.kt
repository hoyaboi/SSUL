package com.example.ssul

import com.example.ssul.api.RetrofitClient
import com.example.ssul.api.collegeCodeMap
import com.example.ssul.api.degreeCodeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StoreItem(
    val id: Int,                                // Store ID
    val name: String,                           // Store 이름
    var address: String = "정보 없음",           // 위치 텍스트
    var isFavorite: Boolean = false,            // 즐겨찾기 상태
    val isFilterGroupChecked: Boolean,          // group 필터 선택 여부
    val isFilterDateChecked: Boolean,           // date 필터 선택 여부
    val isFilterEfficiencyChecked: Boolean,     // efficiency 필터 선택 여부
    var imageUrl: String = "",                  // Store 이미지 리소스 ID
    val isAssociated: Boolean                   // partner 필터 선택 여부
)

data class StoreInfo(
    val id: Int,                                // Store ID
    var imageUrl: Int = 0,                      // Store 이미지 리소스 ID
    val name: String,                           // Store 이름
    val isAssociated: Boolean,                  // 제휴 정보
    var isFavorite: Boolean = false,            // 즐겨찾기 상태
    var address: String = "정보 없음",           // 위치 텍스트
    var contact: String = "정보 없음",           // 번호 텍스트
    val associationInfo: Pair<String, String> = "" to "", // 제휴 정보 리스트
    val menus: List<MenuItem> = emptyList()     // 메뉴 리스트
) {
    data class MenuItem(
        val name: String,                    // 메뉴 이름
        val price: String,                   // 메뉴 가격
        var imageUrl: String = ""            // 메뉴 이미지 리소스 ID
    )
}

// 가게 전체 리스트 API 요청 + MutableList<StoreItem> 형식으로 반환
suspend fun getStores(college: String, degree: String): MutableList<StoreItem> {
    return withContext(Dispatchers.IO) {
        try {
            // college와 degree를 코드로 변환
            val collegeCode = collegeCodeMap[college] ?: throw IllegalArgumentException("Unknown college name: $college")
            val degreeCode = degreeCodeMap[degree] ?: throw IllegalArgumentException("Unknown degree name: $degree")

            // API 요청
            val response = RetrofitClient.apiService.getStores(collegeCode, degreeCode).execute()
            if (response.isSuccessful) {
                val storeResponses = response.body() ?: emptyList()

                // StoreItem으로 변환
                storeResponses.map { storeResponse ->
                    StoreItem(
                        id = storeResponse.id,
                        name = storeResponse.name,
                        address = storeResponse.address,
                        isFavorite = false, // 초기값 설정
                        isFilterGroupChecked = storeResponse.themes.contains("THE-001"),
                        isFilterDateChecked = storeResponse.themes.contains("THE-002"),
                        isFilterEfficiencyChecked = storeResponse.themes.contains("THE-003"),
                        imageUrl = storeResponse.imageUrl,
                        isAssociated = storeResponse.isAssociated
                    )
                }.toMutableList()
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }
}

// 가게 세부 사항 API 요청 + StoreInfo 형식으로 반환
suspend fun getStoreInfo(storeId: Int, college: String, degree: String): StoreInfo {
    return withContext(Dispatchers.IO) {
        try {
            // college와 degree를 코드로 변환
            val collegeCode = collegeCodeMap[college] ?: throw IllegalArgumentException("Unknown college name: $college")
            val degreeCode = degreeCodeMap[degree] ?: throw IllegalArgumentException("Unknown degree name: $degree")

            // API 요청
            val response = RetrofitClient.apiService.getStoreInfo(storeId, collegeCode, degreeCode).execute()
            if (response.isSuccessful) {
                val storeResponse = response.body() ?: throw IllegalArgumentException("Invalid Store Response")

                // AssociationInfo의 target을 학과/단과대학 이름으로 변환
                val associationTarget = collegeCodeMap.entries.find { it.value == storeResponse.associationInfo?.target }?.key
                    ?: degreeCodeMap.entries.find { it.value == storeResponse.associationInfo?.target }?.key
                    ?: "정보 없음"

                // StoreInfo로 변환
                StoreInfo(
                    id = storeResponse.id,
                    name = storeResponse.name,
                    isAssociated = storeResponse.isAssociated,
                    address = storeResponse.address,
                    contact = storeResponse.contact ?: "정보 없음",
                    associationInfo = associationTarget to (storeResponse.associationInfo?.description ?: "정보 없음"),
                    menus = storeResponse.menus.map { menu ->
                        StoreInfo.MenuItem(
                            name = menu.name,
                            price = menu.price,
                            imageUrl = menu.imageUrl ?: ""
                        )
                    }
                )
            } else {
                throw IllegalArgumentException("Failed to fetch store info: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            StoreInfo(
                id = 0,
                name = "알 수 없음",
                isAssociated = false,
                address = "정보 없음",
                contact = "정보 없음",
                associationInfo = "정보 없음" to "정보 없음",
                menus = emptyList()
            )
        }
    }
}
package com.example.ssul.model

data class StoreModel(
    val id: Int,                                // Store ID
    val name: String,                           // Store 이름
    val address: String,                        // 위치 텍스트
    val isFilterGroupChecked: Boolean,          // group 필터 선택 여부
    val isFilterDateChecked: Boolean,           // date 필터 선택 여부
    val isFilterEfficiencyChecked: Boolean,     // efficiency 필터 선택 여부
    val imageUrl: String,                       // Store 이미지 리소스 ID
    val isAssociated: Boolean                   // partner 필터 선택 여부
)

data class StoreInfoModel(
    val id: Int,
    val name: String,
    val isAssociated: Boolean,
    val address: String,
    val contact: String,
    val associationInfo: Pair<String, String>,  // 제휴 정보
    val menus: List<MenuItem>
) {
    data class MenuItem(
        val name: String,
        val price: String,
        val imageUrl: String
    )
}
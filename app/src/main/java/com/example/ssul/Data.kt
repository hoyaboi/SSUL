package com.example.ssul

data class StoreItem(
    val storeId: Int,                           // Store ID
    var storeImage: Int = 0,                    // Store 이미지 리소스 ID
    val storeText: String,                      // Store 이름
    var isFavorite: Boolean,                    // 즐겨찾기 상태
    var locationText: String = "정보 없음",       // 위치 텍스트
    val isFilterGroupChecked: Boolean,          // group 필터 선택 여부
    val isFilterDateChecked: Boolean,           // date 필터 선택 여부
    val isFilterEfficiencyChecked: Boolean,     // efficiency 필터 선택 여부
    val isFilterPartnerChecked: Boolean         // partner 필터 선택 여부
)

data class StoreInfo(
    val storeId: Int,                           // Store ID
    var storeImage: Int = 0,                    // Store 이미지 리소스 ID
    val storeText: String,                      // Store 이름
    val isFilterPartnerChecked: Boolean,        // 제휴 정보
    var isFavorite: Boolean,                    // 즐겨찾기 상태
    var locationText: String = "정보 없음",       // 위치 텍스트
    var phoneText: String = "정보 없음",          // 번호 텍스트
    val partnerships: List<Pair<String, String>> = emptyList(), // 제휴 정보 리스트
    val menuItems: List<MenuItem> = emptyList() // 메뉴 리스트
) {
    data class MenuItem(
        val menuImage: Int,                     // 메뉴 이미지 리소스 ID
        val menuName: String,                   // 메뉴 이름
        val menuPrice: String                   // 메뉴 가격
    )
}
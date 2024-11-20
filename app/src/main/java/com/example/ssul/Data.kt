package com.example.ssul

data class StoreItem(
    val id: Int,                                // Store ID
    val name: String,                           // Store 이름
    var address: String = "정보 없음",           // 위치 텍스트
    var isFavorite: Boolean = false,            // 즐겨찾기 상태
    val isFilterGroupChecked: Boolean,          // group 필터 선택 여부
    val isFilterDateChecked: Boolean,           // date 필터 선택 여부
    val isFilterEfficiencyChecked: Boolean,     // efficiency 필터 선택 여부
    var imageUrl: Int = 0,                      // Store 이미지 리소스 ID
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
        val imageUrl: Int                    // 메뉴 이미지 리소스 ID
    )
}

// 가게 전체 리스트 API 요청 + MutableList<StoreItem> 형식으로 반환
fun getStores(college: String, degree: String): MutableList<StoreItem> {
    return mutableListOf(
        StoreItem(
            id = 1,
            name = "블루힐",
            address = "서울 동작구 사당로 14",
            isFavorite = false,
            isFilterGroupChecked = true,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = false,
            imageUrl = R.drawable.sample_store1,
            isAssociated = false
        ),
        StoreItem(
            id = 2,
            name = "스팅 (BAR)",
            address = "서울 동작구 사당로 8 2층",
            isFavorite = false,
            isFilterGroupChecked = false,
            isFilterDateChecked = true,
            isFilterEfficiencyChecked = false,
            imageUrl = R.drawable.sample_store2,
            isAssociated = false
        ),
        StoreItem(
            id = 3,
            name = "파동추야",
            address = "서울 동작구 상도로58번길",
            isFavorite = false,
            isFilterGroupChecked = true,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = true,
            imageUrl = R.drawable.sample_store3,
            isAssociated = true
        ),
        StoreItem(
            id = 4,
            name = "역전할머니맥주",
            address = "서울 동작구 상도로61길 40",
            isFavorite = false,
            isFilterGroupChecked = false,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = true,
            imageUrl = R.drawable.sample_store4,
            isAssociated = true
        )
    )
}

// 가게 상세 정보 API 요청 + StoreInfo 형식으로 반환
fun getStoreInfo(storeId: Int, college: String, degree: String): StoreInfo {
    return StoreInfo(
        id = 1,
        imageUrl = R.drawable.sample_store3,
        name = "파동추야",
        isAssociated = true,
        isFavorite = false,
        address = "서울 동작구 상도로58번길",
        contact = "02-123-4567",
        associationInfo = "IT대학" to "모든 안주 무료",
        menus = listOf(
            StoreInfo.MenuItem(
                name = "메뉴 1",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            ),
            StoreInfo.MenuItem(
                name = "메뉴 2",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            ),
            StoreInfo.MenuItem(
                name = "메뉴 3",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            ),
            StoreInfo.MenuItem(
                name = "메뉴 4",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            ),
            StoreInfo.MenuItem(
                name = "메뉴 5",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            ),
            StoreInfo.MenuItem(
                name = "메뉴 6",
                price = "500원",
                imageUrl = R.mipmap.ic_launcher
            )
        )
    )
}
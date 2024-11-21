package com.example.ssul.api

// API Response 담는 데이터 클래스
data class StoreResponse(
    // 가게 정보(홈, 즐겨찾기)
    val id: Int,
    val name: String,
    val address: String,
    val themes: List<String>,
    val imageUrl: String,
    val isAssociated: Boolean
)

data class StoreInfoResponse(
    // 가게 세부 정보(가게 세부 화면)
    val id: Int,
    val name: String,
    val isAssociated: Boolean,
    val address: String,
    val contact: String?,
    val associationInfo: AssociationInfo?,
    val menus: List<MenuResponse>
) {
    data class AssociationInfo(
        val target: String,
        val description: String
    )

    data class MenuResponse(
        val name: String,
        val price: String,
        val imageUrl: String?
    )
}
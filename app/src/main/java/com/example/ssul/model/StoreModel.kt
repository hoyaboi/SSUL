package com.example.ssul.model

data class StoreModel(
    val id: Int,                                // Store ID
    val name: String,                           // Store 이름
    var address: String = "정보 없음",           // 위치 텍스트
    val isFilterGroupChecked: Boolean,          // group 필터 선택 여부
    val isFilterDateChecked: Boolean,           // date 필터 선택 여부
    val isFilterEfficiencyChecked: Boolean,     // efficiency 필터 선택 여부
    var imageUrl: String = "",                  // Store 이미지 리소스 ID
    val isAssociated: Boolean                   // partner 필터 선택 여부
)
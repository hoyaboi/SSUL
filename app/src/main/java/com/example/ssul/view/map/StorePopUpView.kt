package com.example.ssul.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.ssul.R
import com.example.ssul.StoreItem
import com.naver.maps.map.overlay.Marker

@SuppressLint("ViewConstructor")
class StorePopUpView(context: Context, private val store: StoreItem) : LinearLayout(context) {

    private lateinit var storeName: TextView
    private lateinit var storeAddress: TextView
    private lateinit var favoriteButton: ImageView
    private lateinit var storeImage: ImageView
    private lateinit var partnerTag: ImageView
    var imageUrl = ""
    var storeId = 0
    var marker : Marker? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.popup_store_info, this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // 뷰 컴포넌트 찾기
        storeName = findViewById(R.id.store_name)
        storeAddress = findViewById(R.id.store_address)
        favoriteButton = findViewById(R.id.favorite_button)
        storeImage = findViewById(R.id.store_image)
        partnerTag = findViewById(R.id.partner_status)

        // 스토어 정보 설정
        storeName.text = store.name
        storeAddress.text = context.getString(R.string.address_text, store.address)

        // Glide를 이용한 이미지 로딩
        Glide.with(this)
            .load(store.imageUrl) // 이미지 URL
            .placeholder(R.drawable.default_image) // 로딩 중 보여줄 이미지
            .error(R.drawable.default_image) // 오류 시 보여줄 이미지
            .into(storeImage)

        // 제휴 상태에 따라 partnerTag visibility 설정
        partnerTag.visibility = if (store.isAssociated) View.VISIBLE else View.INVISIBLE

        // SharedPreferences에서 즐겨찾기 상태 로드
        val sharedPreferences = context.getSharedPreferences("favorite", Context.MODE_PRIVATE)
        val isFavorite = sharedPreferences.getBoolean(store.id.toString(), false)

        // 초기 즐겨찾기 버튼 상태 설정
        updateFavoriteButtonState(favoriteButton, isFavorite)

        // 즐겨찾기 버튼 클릭 리스너
        favoriteButton.setOnClickListener {
            val btnSharedPreferences = context.getSharedPreferences("favorite", Context.MODE_PRIVATE)
            val btnIsFavorite = btnSharedPreferences.getBoolean(store.id.toString(), false) // 현재 즐겨찾기 상태 확인

            if (btnIsFavorite) {
                // 이미 즐겨찾기 상태 -> 제거 작업
                btnSharedPreferences.edit().remove(store.id.toString()).apply() // 즐겨찾기 상태 제거
                updateFavoriteButtonState(favoriteButton, false) // 버튼 상태 업데이트
            } else {
                // 즐겨찾기 추가
                btnSharedPreferences.edit().putBoolean(store.id.toString(), true).apply() // 상태 저장
                updateFavoriteButtonState(favoriteButton, true) // 버튼 상태 업데이트
            }
        }
    }

    private fun updateFavoriteButtonState(favoriteButton: ImageView, isFavorite: Boolean) {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.favorite_clicked
            else R.drawable.favorite_non_clicked
        )
    }
}
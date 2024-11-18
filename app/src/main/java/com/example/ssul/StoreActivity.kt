package com.example.ssul

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class StoreActivity : AppCompatActivity() {

    private lateinit var storeImage: ImageView
    private lateinit var cancelButton: ImageView
    private lateinit var storeNameTextView: TextView
    private lateinit var filterPartnerImage: ImageView
    private lateinit var favoriteButton: ImageView
    private lateinit var locationTextView: TextView
    private lateinit var businessHoursTextView: TextView
    private lateinit var websiteTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences


    private val sampleStoreInfo = mutableListOf(
        StoreInfo(
            storeId = 1,
            storeImage = R.drawable.sample_store1,
            storeText = "블루힐",
            isFilterPartnerChecked = false,
            isFavorite = false,
            locationText = "서울 동작구 사당로 14",
            businessHours = "매일 17:00 ~ 02:00",
            menuItems = listOf(
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 1",
                    menuPrice = "500원"
                ),
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 2",
                    menuPrice = "500원"
                )
            )
        ),
        StoreInfo(
            storeId = 2,
            storeImage = R.drawable.sample_store2,
            storeText = "스팅 (BAR)",
            isFilterPartnerChecked = false,
            isFavorite = false,
            locationText = "서울 동작구 사당로 8 2층",
            businessHours = "일 정보 없음\n" +
                    "월 17:00 ~ 02:00\n" +
                    "화 17:00 ~ 02:00\n" +
                    "수 17:00 ~ 02:00\n" +
                    "목 17:00 ~ 02:00\n" +
                    "금 17:00 ~ 02:00\n" +
                    "토 17:00 ~ 02:00",
            menuItems = listOf(
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 1",
                    menuPrice = "500원"
                ),
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 2",
                    menuPrice = "500원"
                )
            )
        ),
        StoreInfo(
            storeId = 3,
            storeImage = R.drawable.sample_store3,
            storeText = "파동추야",
            isFilterPartnerChecked = true,
            isFavorite = false,
            locationText = "서울 동작구 상도로58번길",
            businessHours = "일 정기휴무 (매주 일요일)\n" +
                    "월 16:00 ~ 02:00\n" +
                    "화 16:00 ~ 02:00\n" +
                    "수 16:00 ~ 02:00\n" +
                    "목 16:00 ~ 02:00\n" +
                    "금 16:00 ~ 02:00\n" +
                    "토 16:00 ~ 02:00",
            partnerships = listOf("IT대학" to "모든 안주 무료"),
            menuItems = listOf(
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 1",
                    menuPrice = "500원"
                ),
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 2",
                    menuPrice = "500원"
                )
            )
        ),
        StoreInfo(
            storeId = 4,
            storeImage = R.drawable.sample_store4,
            storeText = "역전할머니맥주",
            isFilterPartnerChecked = true,
            isFavorite = false,
            locationText = "서울 동작구 상도로61길 40",
            businessHours = "매일 16:00 ~ 02:00",
            officialWebsite = "http://역전할머니맥주.com/",
            partnerships = listOf("IT대학" to "모든 안주 무료"),
            menuItems = listOf(
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 1",
                    menuPrice = "500원"
                ),
                StoreInfo.MenuItem(
                    menuImage = R.mipmap.ic_launcher,
                    menuName = "메뉴 2",
                    menuPrice = "500원"
                )
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        // 상태표시줄 색상 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // 선택된 가게 저장
        val storeId = intent.getIntExtra("storeId", 0)
        val currentStore = sampleStoreInfo.find { it.storeId == storeId }

        setupViews()

        // 구현 사항
        // 1. 즐겨찾기 정보 불러오기
        // 2. 가게 이미지 정보 입력 + 뒤로 가기 버튼 기능
        // 3. 가게 정보 컨테이너 입력 + 즐겨찾기 클릭 처리
        // 4. 제휴 정보 입력
        // 5. 메뉴 정보 입력


        // 1. 즐겨찾기 정보 불러오기
        sharedPreferences = this.getSharedPreferences("favorite", Context.MODE_PRIVATE)
        sampleStoreInfo.forEach { item ->
            item.isFavorite = sharedPreferences.getBoolean(item.storeId.toString(), false)
        }


        // 2. 가게 이미지 정보 입력 + 뒤로 가기 버튼 기능 구현
        setStoreInfoImage(currentStore)

        // 3. 가게 정보 컨테이너(가게 이름, 제휴 마크, 즐겨찾기 상태, 주소, 영업시간, 웹사이트 주소) 입력
        setStoreInfoContainer(currentStore)

        // 즐겨찾기 설정 동작
        favoriteButton.setOnClickListener {
            toggleFavorite(storeId, currentStore)
        }

        // 4. 제휴 정보 입력

        // 5. 메뉴 정보 입력

    }

    private fun setupViews() {
        storeImage = findViewById(R.id.store_image)
        cancelButton = findViewById(R.id.cacnel_button)
        storeNameTextView = findViewById(R.id.store_text)
        filterPartnerImage = findViewById(R.id.filter_partner_image)
        favoriteButton = findViewById(R.id.favorite_button)
        locationTextView = findViewById(R.id.location_text)
        businessHoursTextView = findViewById(R.id.business_hours_text)
        websiteTextView = findViewById(R.id.website_text)
    }

    // 가게 이미지 부분 동작 함수 : 이미지 표시 + 뒤로가기 버튼
    private fun setStoreInfoImage(currentStore: StoreInfo?) {
        // 이미지 표시
        currentStore?.let { store ->
            if (store.storeImage != 0) {
                storeImage.setImageResource(store.storeImage)
            } else {
                storeImage.setImageResource(R.drawable.default_image) // 기본 이미지 리소스 사용
            }
        } ?: run {
            storeImage.setImageResource(R.drawable.default_image) // currentStore가 null인 경우 기본 이미지 설정
        }

        // 뒤로가기 버튼 동작
        cancelButton.setOnClickListener {
            finish()
        }
    }

    // 가게 정보 컨테이너 세팅 함수
    private fun setStoreInfoContainer(currentStore: StoreInfo?) {
        // 정보 표시
        currentStore?.let { store ->
            storeNameTextView.text = store.storeText
            locationTextView.text = store.locationText
            businessHoursTextView.text = store.businessHours
            websiteTextView.text = store.officialWebsite

            // 제휴 마크 표시
            if (store.isFilterPartnerChecked) {
                filterPartnerImage.visibility = View.VISIBLE
            } else {
                filterPartnerImage.visibility = View.GONE
            }

            // 즐겨찾기 표시
            if (store.isFavorite) {
                favoriteButton.setImageResource(R.drawable.favorite_clicked)
            } else {
                favoriteButton.setImageResource(R.drawable.favorite_non_clicked)
            }
        } ?: run {
            storeNameTextView.text = "정보 없음"
        }
    }

    // 즐겨 찾기 상태 변경 함수
    private fun toggleFavorite(storeId: Int, currentStore: StoreInfo?) {
        currentStore?.let {
            // 즐겨찾기 상태 변경
            it.isFavorite = !it.isFavorite

            // 내부 저장소 업데이트
            with(sharedPreferences.edit()) {
                putBoolean(storeId.toString(), it.isFavorite)
                apply()
            }
        }

        setStoreInfoContainer(currentStore)
    }
}
package com.example.ssul

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ssul.adapter.MenuAdapter
import kotlinx.coroutines.launch

class StoreActivity : AppCompatActivity() {

    private lateinit var storeImage: ImageView
    private lateinit var cancelButton: ImageView
    private lateinit var storeNameTextView: TextView
    private lateinit var filterPartnerImage: ImageView
    private lateinit var favoriteButton: ImageView
    private lateinit var locationTextView: TextView
    private lateinit var phoneNumberTextView: TextView
    private lateinit var partnershipContainer: LinearLayout
    private lateinit var degreeTextView: TextView
    private lateinit var partnerInfoTextView: TextView
    private lateinit var menuList: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var messageBox: ConstraintLayout
    private lateinit var messageBoxYesButton: TextView
    private lateinit var messageBoxNoButton: TextView

    private lateinit var favoriteSharedPreferences: SharedPreferences
    private lateinit var degreeSharedPreferences: SharedPreferences

    private lateinit var storeInfo: StoreInfo // 선택된 가게 세부 정보

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        // 상태표시줄 색상 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setupViews()

        // 구현 사항
        // 1. 즐겨찾기, 학과 정보 불러오기
        // 2. 선택된 가게 불러오기
        // 3. API 요청 및 UI 업데이트
        // 4. 기타 버튼 동작 설정(뒤로가기, 즐겨찾기)


        // 1. 즐겨찾기, 학과 정보 불러오기
        favoriteSharedPreferences = this.getSharedPreferences("favorite", Context.MODE_PRIVATE)
        degreeSharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // 2. 선택된 가게 불러오기
        val storeId = intent.getIntExtra("storeId", 0)
        val storeImageUrl = intent.getStringExtra("storeImage")

        // 3. API 요청 및 UI 업데이트
        lifecycleScope.launch {
            try {
                val college = degreeSharedPreferences.getString("selectedCollege", "")
                val degree = degreeSharedPreferences.getString("selectedDepartment", "")

                // API 요청
                storeInfo = getStoreInfo(storeId, college!!, degree!!)
                storeInfo.isFavorite = favoriteSharedPreferences.getBoolean(storeInfo.id.toString(), false)

                // UI 업데이트
                Glide.with(this@StoreActivity).clear(storeImage)
                Glide.with(this@StoreActivity)
                    .load(storeImageUrl)
                    .into(storeImage)

                setStoreInfoContainer(storeInfo)
                setPartnership(storeInfo)

                menuAdapter = MenuAdapter(storeInfo.menus)
                menuList.adapter = menuAdapter
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StoreActivity, "가게 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 4. 기타 버튼 동작 설정(뒤로가기, 즐겨찾기)
        cancelButton.setOnClickListener {
            finish()
        }

        favoriteButton.setOnClickListener {
            toggleFavorite(storeId, storeInfo)
        }
    }

    private fun setupViews() {
        storeImage = findViewById(R.id.store_image)
        cancelButton = findViewById(R.id.cacnel_button)
        storeNameTextView = findViewById(R.id.store_text)
        filterPartnerImage = findViewById(R.id.filter_partner_image)
        favoriteButton = findViewById(R.id.favorite_button)
        locationTextView = findViewById(R.id.location_text)
        phoneNumberTextView = findViewById(R.id.phone_number_text)
        partnershipContainer = findViewById(R.id.partnership_container)
        degreeTextView = findViewById(R.id.degree_text)
        partnerInfoTextView = findViewById(R.id.partner_info_text)
        menuList = findViewById(R.id.menu_list)
        menuList.layoutManager = LinearLayoutManager(this)

        messageBox = findViewById(R.id.message_box)
        messageBoxYesButton = findViewById(R.id.message_box_yes_button)
        messageBoxNoButton = findViewById(R.id.message_box_no_button)
    }

    // 가게 정보 컨테이너 세팅 함수
    private fun setStoreInfoContainer(currentStore: StoreInfo) {
        storeNameTextView.text = currentStore.name
        locationTextView.text = currentStore.address
        phoneNumberTextView.text = currentStore.contact

        // 제휴 마크 표시
        if (currentStore.isAssociated) {
            filterPartnerImage.visibility = View.VISIBLE
        } else {
            filterPartnerImage.visibility = View.GONE
        }

        // 즐겨찾기 표시
        if (currentStore.isFavorite) {
            favoriteButton.setImageResource(R.drawable.favorite_clicked)
        } else {
            favoriteButton.setImageResource(R.drawable.favorite_non_clicked)
        }
    }

    // 즐겨 찾기 토글 함수
    private fun toggleFavorite(storeId: Int, currentStore: StoreInfo) {
        if (currentStore.isFavorite) {
            showMessageBox(storeId, currentStore)
        } else {
            updateFavoriteState(storeId, currentStore)
        }
    }

    // 즐겨 찾기 제거 시 띄울 메시지 박스 함수
    private fun showMessageBox(storeId: Int, currentStore: StoreInfo) {
        messageBox.visibility = View.VISIBLE

        // Yes 버튼 클릭 시 상태 변경
        messageBoxYesButton.setOnClickListener {
            updateFavoriteState(storeId, currentStore)
            messageBox.visibility = View.GONE
        }

        messageBoxNoButton.setOnClickListener {
            messageBox.visibility = View.GONE
        }
    }

    // 즐겨 찾기 상태 변경 함수
    private fun updateFavoriteState(storeId: Int, currentStore: StoreInfo) {
        // 즐겨찾기 상태 변경
        currentStore.isFavorite = !currentStore.isFavorite

        // 내부 저장소 업데이트
        with(favoriteSharedPreferences.edit()) {
            putBoolean(storeId.toString(), currentStore.isFavorite)
            apply()
        }

        // UI 갱신
        setStoreInfoContainer(currentStore)
    }

    // 제휴 정보 설정 함수
    private fun setPartnership(store: StoreInfo) {
        if (store.isAssociated) {
            partnershipContainer.visibility = View.VISIBLE
            degreeTextView.text = store.associationInfo.first
            partnerInfoTextView.text = store.associationInfo.second
        } else {
            partnershipContainer.visibility = View.GONE
        }
    }

}
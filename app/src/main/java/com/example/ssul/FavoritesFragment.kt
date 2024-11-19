package com.example.ssul

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ssul.adapter.StoreAdapter

class FavoritesFragment : Fragment() {

    private lateinit var degreeTextView: TextView
    private lateinit var setDegreeButton: ImageView
    private lateinit var groupFilterButton: TextView
    private lateinit var dateFilterButton: TextView
    private lateinit var efficiencyFilterButton: TextView
    private lateinit var partnerFilterButton: TextView
    private lateinit var storeList: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private lateinit var favoriteSharedPreferences: SharedPreferences
    private lateinit var degreeSharedPreferences: SharedPreferences

    private val sampleStoreItems = mutableListOf(
        StoreItem(
            storeId = 1,
            storeImage = R.drawable.sample_store1,
            storeText = "블루힐",
            isFavorite = false,
            locationText = "서울 동작구 사당로 14",
            isFilterGroupChecked = true,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = false,
            isFilterPartnerChecked = false
        ),
        StoreItem(
            storeId = 2,
            storeImage = R.drawable.sample_store2,
            storeText = "스팅 (BAR)",
            isFavorite = false,
            locationText = "서울 동작구 사당로 8 2층",
            isFilterGroupChecked = false,
            isFilterDateChecked = true,
            isFilterEfficiencyChecked = false,
            isFilterPartnerChecked = false
        ),
        StoreItem(
            storeId = 3,
            storeImage = R.drawable.sample_store3,
            storeText = "파동추야",
            isFavorite = false,
            locationText = "서울 동작구 상도로58번길",
            isFilterGroupChecked = true,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = true,
            isFilterPartnerChecked = true
        ),
        StoreItem(
            storeId = 4,
            storeImage = R.drawable.sample_store4,
            storeText = "역전할머니맥주",
            isFavorite = false,
            locationText = "서울 동작구 상도로61길 40",
            isFilterGroupChecked = false,
            isFilterDateChecked = false,
            isFilterEfficiencyChecked = true,
            isFilterPartnerChecked = true
        )
    )
    private val activeFilters = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)

        // 구현 사항
        // 1. 즐겨찾기, 학과 정보 상태 불러오기
        // 2. 즐겨 찾기 제거 처리
        // 3. 가게 클릭 시 상세 화면(StoreActivity)로 이동
        // 4. 학과 표시 + 학과 설정 버튼 클릭 시 확과 설정 액티비티(StartActivity)로 이동
        // 5. 필터 클릭 시 현재 불러온 데이터에서 일치하는 필터만 표시 -> 선택된 필터 색상 변경


        // 1. 내부 저장소에서 즐겨찾기 상태, 학과 정보 로드
        favoriteSharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        sampleStoreItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.storeId.toString(), false)
        }
        val favoriteItems = sampleStoreItems.filter { it.isFavorite }.toMutableList() // isFavorite이 true인 항목만 필터링

        degreeSharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val degree = degreeSharedPreferences.getString("selectedDepartment", "학과 정보가 없습니다")

        // 2. 어댑터 초기화 + 즐겨찾기 클릭 제거 로직
        // 3. 가게 클릭 처리 -> 상세 화면으로 이동
        storeAdapter = StoreAdapter(favoriteItems, { storeId ->
            changeFavoriteStatus(storeId)
        }, { storeId ->
            val intent = Intent(requireContext(), StoreActivity::class.java)
            intent.putExtra("storeId", storeId)
            startActivity(intent)
        })
        storeList.adapter = storeAdapter

        // 4. 학과 표시 + 학과 설정 클릭 시 학과 설정 액티비티로 이동
        degreeTextView.text = degree
        setDegreeButton.setOnClickListener {
            (activity as? MainActivity)?.showMessageBox(
                message = getString(R.string.reset_degree),
                onYesClicked = {
                    val intent = Intent(requireContext(), StartActivity::class.java)
                    startActivity(intent)
                }
            )
        }

        // 5. toggleFilter 함수로 필터 이름과 클릭된 뷰(it) 전달
        groupFilterButton.setOnClickListener { toggleFilter("group", it as TextView) }
        dateFilterButton.setOnClickListener { toggleFilter("date", it as TextView) }
        efficiencyFilterButton.setOnClickListener { toggleFilter("efficiency", it as TextView) }
        partnerFilterButton.setOnClickListener { toggleFilter("partner", it as TextView) }
    }

    override fun onResume() {
        super.onResume()

        // SharedPreferences를 다시 읽어들여서 데이터를 갱신
        favoriteSharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        sampleStoreItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.storeId.toString(), false)
        }
        val favoriteItems = sampleStoreItems.filter { it.isFavorite }.toMutableList() // isFavorite이 true인 항목만 필터링

        // 어댑터 갱신
        storeAdapter.updateItems(favoriteItems)

        // 필터 초기화
        resetFilter()
    }

    private fun setupViews(view: View) {
        degreeTextView = view.findViewById(R.id.degree_text)
        setDegreeButton = view.findViewById(R.id.set_degree_button)
        groupFilterButton = view.findViewById(R.id.filter_group_button)
        dateFilterButton = view.findViewById(R.id.filter_date_button)
        efficiencyFilterButton = view.findViewById(R.id.filter_efficiency_button)
        partnerFilterButton = view.findViewById(R.id.filter_partner_button)
        storeList = view.findViewById(R.id.store_list)
        storeList.layoutManager = LinearLayoutManager(requireContext())
    }

    // 즐겨찾기 클릭 시 내부 저장소에 상태 저장/업데이트
    private fun toggleFavorite(storeId: Int) {
        val favoriteItems = sampleStoreItems.filter { item ->
            favoriteSharedPreferences.getBoolean(item.storeId.toString(), false)
        }

        val storeItem = favoriteItems.find { it.storeId == storeId }
        storeItem?.let {
            it.isFavorite = !it.isFavorite

            // SharedPreferences 업데이트
            with(favoriteSharedPreferences.edit()) {
                putBoolean(storeId.toString(), it.isFavorite)
                apply()
            }

            // 어댑터에 데이터 업데이트
            val updatedFavoriteItems = sampleStoreItems.filter { item ->
                favoriteSharedPreferences.getBoolean(item.storeId.toString(), false)
            }
            storeAdapter.updateItems(updatedFavoriteItems)
        }
    }

    // 즐겨 찾기 제거
    private fun changeFavoriteStatus(storeId: Int) {
        (activity as? MainActivity)?.showMessageBox(
            message = getString(R.string.remove_favorite),
            onYesClicked = {
                toggleFavorite(storeId)
            }
        )
    }

    // 필터 선택 시 필터 UI 변경 + 선택된 필터에 해당하는 가게 정보 표시
    private fun toggleFilter(filter: String, button: TextView) {
        // 필터 UI 변경
        if (activeFilters.contains(filter)) {
            activeFilters.remove(filter)
            button.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_non_clicked)
            button.setTextAppearance(requireContext(), R.style.filter_text_style)
        } else {
            activeFilters.add(filter)
            button.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_clicked)
            button.setTextAppearance(requireContext(), R.style.filter_selected_text_style)
        }

        // 필터링된 가게가 없을 때
        if (activeFilters.isEmpty()) {
            val favoriteItems = sampleStoreItems.filter { item ->
                favoriteSharedPreferences.getBoolean(item.storeId.toString(), false)
            }
            storeAdapter.updateItems(favoriteItems)
            return
        }

        // 필터링 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하지 않으면 출력 X
        val filteredItems = sampleStoreItems.filter { item ->
            favoriteSharedPreferences.getBoolean(item.storeId.toString(), false) &&
                    activeFilters.all { filter ->
                        when (filter) {
                            "group" -> item.isFilterGroupChecked
                            "date" -> item.isFilterDateChecked
                            "efficiency" -> item.isFilterEfficiencyChecked
                            "partner" -> item.isFilterPartnerChecked
                            else -> false
                        }
                    }
        }
        storeAdapter.updateItems(filteredItems)
    }

    private fun resetFilter() {
        groupFilterButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_non_clicked)
        groupFilterButton.setTextAppearance(requireContext(), R.style.filter_text_style)
        dateFilterButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_non_clicked)
        dateFilterButton.setTextAppearance(requireContext(), R.style.filter_text_style)
        efficiencyFilterButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_non_clicked)
        efficiencyFilterButton.setTextAppearance(requireContext(), R.style.filter_text_style)
        partnerFilterButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_non_clicked)
        partnerFilterButton.setTextAppearance(requireContext(), R.style.filter_text_style)
        activeFilters.clear()
    }

}
package com.example.ssul

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

    private lateinit var storeItems: MutableList<StoreItem> // 전체 가게 리스트
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
        // 2. 학과 전체 리스트 API 요청 + 즐겨찾기 상태 업데이트
        // 3. 즐겨 찾기 제거 처리 + 가게 클릭 시 상세 화면(StoreActivity)로 이동
        // 4. 학과 표시 + 학과 설정 버튼 클릭 시 확과 설정 액티비티(StartActivity)로 이동
        // 5. 필터 클릭 시 현재 불러온 데이터에서 일치하는 필터만 표시 -> 선택된 필터 색상 변경


        // 1. 내부 저장소에서 즐겨찾기 상태, 학과 정보 로드
        favoriteSharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        degreeSharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // 2. 가게 전체 리스트 API 요청 + 즐겨찾기 상태 업데이트
        val college = degreeSharedPreferences.getString("selectedCollege", "")
        val degree = degreeSharedPreferences.getString("selectedDepartment", "")
        storeItems = getStores(college!!, degree!!)
        storeItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }

        // 3. 어댑터 초기화 + 즐겨찾기 클릭 제거 로직 + 가게 클릭 시 상세 화면으로 이동
        val favoriteItems = storeItems.filter { it.isFavorite }.toMutableList() // isFavorite이 true인 항목만 필터링
        storeAdapter = StoreAdapter(favoriteItems, { storeId ->
            changeFavoriteStatus(storeId)
        }, { storeId ->
            moveToStoreActivity(storeId)
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
        storeItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }
        val favoriteItems = storeItems.filter { it.isFavorite }.toMutableList() // isFavorite이 true인 항목만 필터링

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
        val favoriteItems = storeItems.filter { item ->
            favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }

        val storeItem = favoriteItems.find { it.id == storeId }
        storeItem?.let {
            it.isFavorite = !it.isFavorite

            // SharedPreferences 업데이트
            with(favoriteSharedPreferences.edit()) {
                putBoolean(storeId.toString(), it.isFavorite)
                apply()
            }

            // 어댑터에 데이터 업데이트
            val updatedFavoriteItems = storeItems.filter { item ->
                favoriteSharedPreferences.getBoolean(item.id.toString(), false)
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
            val favoriteItems = storeItems.filter { item ->
                favoriteSharedPreferences.getBoolean(item.id.toString(), false)
            }
            storeAdapter.updateItems(favoriteItems)
            return
        }

        // 필터링 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하지 않으면 출력 X
        val filteredItems = storeItems.filter { item ->
            favoriteSharedPreferences.getBoolean(item.id.toString(), false) &&
                    activeFilters.all { filter ->
                        when (filter) {
                            "group" -> item.isFilterGroupChecked
                            "date" -> item.isFilterDateChecked
                            "efficiency" -> item.isFilterEfficiencyChecked
                            "partner" -> item.isAssociated
                            else -> false
                        }
                    }
        }
        storeAdapter.updateItems(filteredItems)
    }

    // 필터 초기화
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

    // 가게 세부 화면 이동
    private fun moveToStoreActivity(storeId: Int) {
        val selectedStore = storeItems.find { it.id == storeId }
        val intent = Intent(requireContext(), StoreActivity::class.java).apply {
            putExtra("storeId", storeId)
            selectedStore?.let {
                putExtra("storeImage", it.imageUrl)
            }
        }
        startActivity(intent)
    }

}
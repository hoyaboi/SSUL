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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ssul.adapter.StoreAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var searchBackButton: ImageView
    private lateinit var ssulIcon: ImageView
    private lateinit var searchTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var filterContainer: HorizontalScrollView
    private lateinit var groupFilterButton: TextView
    private lateinit var dateFilterButton: TextView
    private lateinit var efficiencyFilterButton: TextView
    private lateinit var partnerFilterButton: TextView
    private lateinit var storeList: RecyclerView
    private lateinit var searchList: RecyclerView

    private lateinit var storeAdapter: StoreAdapter
    private lateinit var searchAdapter: StoreAdapter
    private lateinit var favoriteSharedPreferences: SharedPreferences

    private var isSearchScreenOpen = false
    private var searchedStoreItems = mutableListOf<StoreItem>()
    private var storeItems: MutableList<StoreItem> = mutableListOf() // 전체 가게 리스트
    private val activeFilters = mutableSetOf<String>()

    fun setStoreItems(items: MutableList<StoreItem>) {
        storeItems = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)

        // 구현 사항
        // 1. 즐겨찾기, 학과 정보 상태 불러오기
        // 2. 가게 전체 리스트 표시
        // 3. 메인 화면에서 텍스트 필드 클릭시 검색 화면으로 전환 -> 검색 결과 처리
        // 4. 필터 클릭 시 현재 불러온 데이터에서 일치하는 필터만 표시 -> 선택된 필터 색상 변경
        // 5. 어댑터 초기화 + 즐겨 찾기 처리 + 가게 클릭시 세부 화면으로 이동


        // 1. 내부 저장소에서 즐겨찾기 상태 로드
        favoriteSharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)

        // 2. 가게 전체 리스트 표시
        storeItems.forEach { item -> // 즐겨찾기 로드
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }

        // 3-1. 검색 텍스트 필드 클릭 시 검색 화면으로 전환
        searchTextView.setOnClickListener {
            openSearchScreen()
        }
        // 3-2. 검색 결과 처리
        searchButton.setOnClickListener {
            if(isSearchScreenOpen) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotBlank()) {
                    searchedStoreItems = searchStoreList(query).toMutableList() // 저장
                    Log.d("SearchButton", "Search query: $query")
                }
            }
        }
        // 3-3. 키보드에서 엔터 클릭 시 검색 버튼 클릭 처리
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick()
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        // 4. toggleFilter 함수로 필터 이름과 클릭된 뷰(it) 전달
        groupFilterButton.setOnClickListener { toggleFilter("group", it as TextView) }
        dateFilterButton.setOnClickListener { toggleFilter("date", it as TextView) }
        efficiencyFilterButton.setOnClickListener { toggleFilter("efficiency", it as TextView) }
        partnerFilterButton.setOnClickListener { toggleFilter("partner", it as TextView) }

        // 5. 어댑터 초기화 + 즐겨찾기 상태 처리 + 가게 클릭 시 세부 화면으로 이동
        setupAdapters()

    }

    override fun onResume() {
        super.onResume()

        // 즐겨찾기 상태 갱신
        favoriteSharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        storeItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }
        searchedStoreItems.forEach { item ->
            item.isFavorite = favoriteSharedPreferences.getBoolean(item.id.toString(), false)
        }

        // 어댑터 갱신 -> 필터 적용한 상태였으면 필터 상태 복구
        applyFilters()
    }

    private fun setupViews(view: View) {
        searchBackButton = view.findViewById(R.id.search_back_button)
        ssulIcon = view.findViewById(R.id.ssul_icon)
        searchTextView = view.findViewById(R.id.search_text)
        searchEditText = view.findViewById(R.id.search_store_textfield)
        searchButton = view.findViewById(R.id.search_button)
        filterContainer = view.findViewById(R.id.filter_container)
        groupFilterButton = view.findViewById(R.id.filter_group_button)
        dateFilterButton = view.findViewById(R.id.filter_date_button)
        efficiencyFilterButton = view.findViewById(R.id.filter_efficiency_button)
        partnerFilterButton = view.findViewById(R.id.filter_partner_button)
        storeList = view.findViewById(R.id.store_list)
        storeList.layoutManager = LinearLayoutManager(requireContext())
        searchList = view.findViewById(R.id.search_list)
        searchList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapters() {
        // 가게 리스트 어댑터 초기화
        storeAdapter = StoreAdapter(storeItems, { storeId ->
            // 즐겨찾기 상태 변경
            changeFavoriteStatus(storeId)
        }, { storeId ->
            // 가게 클릭 시 세부 화면으로 이동
            moveToStoreActivity(storeId)
        })
        storeList.adapter = storeAdapter

        // 검색 리스트 어댑터 초기화
        searchAdapter = StoreAdapter(searchedStoreItems, { storeId ->
            changeFavoriteStatus(storeId)
        }, { storeId ->
            moveToStoreActivity(storeId)
        })
        searchList.adapter = searchAdapter
    }

    // 검색 화면 전환 함수(open)
    private fun openSearchScreen() {
        searchBackButton.visibility = View.VISIBLE
        searchEditText.visibility = View.VISIBLE
        ssulIcon.visibility = View.GONE
        searchTextView.visibility = View.GONE
        filterContainer.visibility = View.GONE
        storeList.visibility = View.GONE

        searchEditText.requestFocus()

        // 키보드 표시
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)

        isSearchScreenOpen = true

        searchBackButton.setOnClickListener {
            closeSearchScreen()
        }
    }

    // 검색 화면 전환 함수(close)
    private fun closeSearchScreen() {
        searchBackButton.visibility = View.GONE
        searchEditText.visibility = View.GONE
        ssulIcon.visibility = View.VISIBLE
        searchTextView.visibility = View.VISIBLE
        filterContainer.visibility = View.VISIBLE
        storeList.visibility = View.VISIBLE
        searchList.visibility = View.GONE

        searchEditText.clearFocus()
        searchEditText.setText("")

        // 키보드 숨기기
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)

        isSearchScreenOpen = false

    }

    // 검색 화면 표시 시에 뒤로 가기 버튼 클릭 로직
    fun handleBackPressed(): Boolean {
        return if (isSearchScreenOpen) {
            closeSearchScreen()
            true
        } else {
            false
        }
    }

    // 즐겨찾기 클릭 시 내부 저장소에 상태 저장/업데이트
    private fun toggleFavorite(storeId: Int) {
        if (isSearchScreenOpen) {
            val storeItem = searchedStoreItems.find { it.id == storeId }
            storeItem?.let {
                it.isFavorite = !it.isFavorite
                searchAdapter.updateItems(searchedStoreItems)

                // SharedPreferences 업데이트
                with(favoriteSharedPreferences.edit()) {
                    putBoolean(storeId.toString(), it.isFavorite)
                    apply()
                }
            }
        } else {
            var storeItem = storeItems.find { it.id == storeId }
            storeItem?.let {
                it.isFavorite = !it.isFavorite
                storeAdapter.updateItems(storeItems)

                // SharedPreferences 업데이트
                with(favoriteSharedPreferences.edit()) {
                    putBoolean(storeId.toString(), it.isFavorite)
                    apply()
                }
            }
        }
        applyFilters()
    }

    // 즐겨 찾기 상태 변경
    private fun changeFavoriteStatus(storeId: Int) {
        val storeItem = storeItems.find { it.id == storeId }
        if (storeItem?.isFavorite == true) {
            // 즐겨 찾기 체크가 되어 있는 경우
            (activity as? MainActivity)?.showMessageBox(
                message = getString(R.string.remove_favorite),
                onYesClicked = {
                    toggleFavorite(storeId)
                }
            )
        } else {
            // 즐겨 찾기 체크가 안 되어 있는 경우
            toggleFavorite(storeId)
        }
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
            storeAdapter.updateItems(storeItems)
            return
        }

        // 필터링 1 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하면 출력
//        val filteredItems = favoriteSharedPreferences.filter { item ->
//            (activeFilters.contains("group") && item.isFilterGroupChecked) ||
//                    (activeFilters.contains("date") && item.isFilterDateChecked) ||
//                    (activeFilters.contains("efficiency") && item.isFilterEfficiencyChecked) ||
//                    (activeFilters.contains("partner") && item.isFilterPartnerChecked)
//        }

        // 필터링 2 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하지 않으면 출력 X
        val filteredItems = storeItems.filter { item ->
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

    // 검색 필터링 함수
    private fun searchStoreList(query: String): List<StoreItem> {
        // 가게 이름과 한 글자라도 일치하면 필터링
        val filteredItems = storeItems.filter { storeItem ->
            query.all { char ->
                storeItem.name.contains(char, ignoreCase = true) ||
                        storeItem.address.contains(char, ignoreCase = true)
            }
        }

        if (filteredItems.isEmpty()) {
            Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 필터링된 리스트 업데이트
            searchAdapter.updateItems(filteredItems)
            searchList.visibility = View.VISIBLE
        }

        return filteredItems
    }

    // 필터 복구
    private fun applyFilters() {
        if (activeFilters.isEmpty()) {
            // 필터가 없을 경우 전체 데이터를 표시
            storeAdapter.updateItems(storeItems)
        } else {
            // 필터가 있을 경우 데이터를 필터링
            val filteredItems = storeItems.filter { item ->
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
    }

    // 가게 세부 화면으로 이동
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
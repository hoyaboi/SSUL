package com.example.ssul

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ssul.adapter.StoreAdapter

class HomeFragment : Fragment() {

    private lateinit var searchBackButton: ImageView
    private lateinit var ssulIcon: ImageView
    private lateinit var searchTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var groupFilterButton: TextView
    private lateinit var dateFilterButton: TextView
    private lateinit var efficiencyFilterButton: TextView
    private lateinit var partnerFilterButton: TextView
    private lateinit var storeList: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var isSearchScreenOpen = false

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)

        // 구현 사항
        // 1. 데이터 불러오기 -> isFavorite은 내부 저장소, 나머지는 서버
        //    ** 즐겨찾기 상태는 storeId와 isFavorite 매핑하여 저장 **
        // 2. 필터 클릭 시 현재 불러온 데이터에서 일치하는 필터만 표시 -> 선택된 필터 색상 변경
        // 3. 메인 화면에서 텍스트 필드 클릭시 검색 화면으로 전환
        // 4. 가게 클릭 시 세부 정보 화면(StoreActivity)로 이동 -> storeId만 intent에 extra로 전달

        // 내부 저장소에서 즐겨찾기 상태 로드
        sharedPreferences = requireContext().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        sampleStoreItems.forEach { item ->
            item.isFavorite = sharedPreferences.getBoolean(item.storeId.toString(), false)
        }

        // 검색 텍스트 필드 클릭 시 검색 화면으로 전환
        searchTextView.setOnClickListener {
            openSearchScreen()
        }

        // 검색 결과 처리
        searchButton.setOnClickListener {
            if(isSearchScreenOpen) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotBlank()) {
                    filterStoreList(query)
                    Log.d("SearchButton", "Search query: $query")
                }
            }
        }

        // 키보드에서 엔터 클릭 시 검색 버튼 클릭 처리
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchButton.performClick()
                true
            } else {
                false
            }
        }

        // toggleFilter 함수로 필터 이름과 클릭된 뷰(it) 전달
        groupFilterButton.setOnClickListener { toggleFilter("group", it as TextView) }
        dateFilterButton.setOnClickListener { toggleFilter("date", it as TextView) }
        efficiencyFilterButton.setOnClickListener { toggleFilter("efficiency", it as TextView) }
        partnerFilterButton.setOnClickListener { toggleFilter("partner", it as TextView) }

        // 어댑터 초기화 + 즐겨찾기 추가 로직
        storeAdapter = StoreAdapter(sampleStoreItems) { storeId ->
            val storeItem = sampleStoreItems.find { it.storeId == storeId }
            if (storeItem?.isFavorite == true) {
                (activity as? MainActivity)?.showMessageBox(
                    message = "즐겨찾기에서 삭제하시겠습니까?",
                    onYesClicked = {
                        toggleFavorite(storeId)
                    }
                )
            } else {
                toggleFavorite(storeId)
            }

            // 내부 저장소 로깅
            val allFavorites = sharedPreferences.all
            allFavorites.forEach { (key, value) ->
                if (value is Boolean) {
                    Log.d("FavoritesLog", "Store ID: $key, isFavorite: $value")
                }
            }
        }
        storeList.adapter = storeAdapter
    }

    private fun setupViews(view: View) {
        searchBackButton = view.findViewById(R.id.search_back_button)
        ssulIcon = view.findViewById(R.id.ssul_icon)
        searchTextView = view.findViewById(R.id.search_text)
        searchEditText = view.findViewById(R.id.search_store_textfield)
        searchButton = view.findViewById(R.id.search_button)
        groupFilterButton = view.findViewById(R.id.filter_group_button)
        dateFilterButton = view.findViewById(R.id.filter_date_button)
        efficiencyFilterButton = view.findViewById(R.id.filter_efficiency_button)
        partnerFilterButton = view.findViewById(R.id.filter_partner_button)
        storeList = view.findViewById(R.id.store_list)
        storeList.layoutManager = LinearLayoutManager(requireContext())
    }

    // 검색 화면 전환 함수(open)
    private fun openSearchScreen() {
        searchBackButton.visibility = View.VISIBLE
        searchEditText.visibility = View.VISIBLE
        ssulIcon.visibility = View.GONE
        searchTextView.visibility = View.GONE
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
        storeList.visibility = View.VISIBLE

        searchEditText.clearFocus()
        searchEditText.setText("")

        // 키보드 숨기기
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)

        isSearchScreenOpen = false

        // 전체 리스트 복원
        storeAdapter.updateItems(sampleStoreItems)
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
        val storeItem = sampleStoreItems.find { it.storeId == storeId }
        storeItem?.let {
            it.isFavorite = !it.isFavorite
            storeAdapter.notifyDataSetChanged()

            // SharedPreferences 업데이트
            with(sharedPreferences.edit()) {
                putBoolean(storeId.toString(), it.isFavorite)
                apply()
            }

            // FavoritesFragment 갱신
            (activity as MainActivity).refreshFragment(0)
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

        // 필터링된 가게 표시
        if (activeFilters.isEmpty()) {
            storeAdapter.updateItems(sampleStoreItems)
            return
        }

        // 필터링 1 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하면 출력
//        val filteredItems = sampleStoreItems.filter { item ->
//            (activeFilters.contains("group") && item.isFilterGroupChecked) ||
//                    (activeFilters.contains("date") && item.isFilterDateChecked) ||
//                    (activeFilters.contains("efficiency") && item.isFilterEfficiencyChecked) ||
//                    (activeFilters.contains("partner") && item.isFilterPartnerChecked)
//        }

        // 필터링 2 : 가게 필터에 내가 선택한 필터 중 하나라도 일치하지 않으면 출력 X
        val filteredItems = sampleStoreItems.filter { item ->
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

    // 검색 필터링 함수
    private fun filterStoreList(query: String) {
        val filteredItems = sampleStoreItems.filter { storeItem ->
            query.all { char ->
                storeItem.storeText.contains(char, ignoreCase = true) ||
                        storeItem.locationText.contains(char, ignoreCase = true)
            }
        }

        if (filteredItems.isEmpty()) {
            Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 필터링된 리스트 업데이트
            storeAdapter.updateItems(filteredItems)
            storeList.visibility = View.VISIBLE
        }
    }

}
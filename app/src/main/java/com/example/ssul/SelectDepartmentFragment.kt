package com.example.ssul

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ssul.viewmodel.DegreeViewModel

class SelectDepartmentFragment : Fragment() {

    private val viewModel: DegreeViewModel by activityViewModels()

    private val departmentMap = mapOf(
        "IT대학" to listOf(
            "컴퓨터학부",
            "소프트웨어학부",
            "글로벌미디어학부",
            "전자정보공학부",
            "AI융합학부",
            "미디어경영학과",
            "정보보호학과"
        ),
        "인문대학" to listOf(
            "국어국문학과",
            "영어영문학과",
            "사학과",
            "기독교학과",
            "독어독문학과",
            "불어불문학과",
            "중어중문학과",
            "일어일문학과",
            "철학과",
            "예술창작학부",
            "스포츠학부"
        ),
        "법과대학" to listOf("법학과", "국제법학과"),
        "공과대학" to listOf("기계공학부", "전기공학부", "건축학부", "화학공학과", "산업정보시스템공학과", "신소재공학과"),
        "자연과학대학" to listOf("수학과", "물리학과", "화학과", "정보통계보험수리학과", "의생명시스템학부"),
        "사회과학대학" to listOf("정치외교학과", "사회복지학부", "행정학부", "정보사회학과", "언론홍보학과", "평생교육학과"),
        "경제통상대학" to listOf("경제학과", "국제무역학과", "금융경제학과", "통상산업학과", "글로벌통상학과"),
        "경영대학" to listOf(
            "경영학부",
            "회계학과",
            "벤처경영학과",
            "복지경영학과",
            "회계세무학과",
            "혁신경영학과",
            "금융학부",
            "벤처중소기업학과"
        ),
        "융합특성화자유전공학부" to listOf("융합특성화자유전공학부"),
        "차세대반도체학과" to listOf("차세대반도체학과"),
        "베어드교양학과" to listOf("기독교교육", "의사소통교육", "영어교육", "IT정보교육", "소양교육", "베어드교양대학 산하 기관")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val departmentSpinner: Spinner? = view.findViewById(R.id.departmentSpinner)
        departmentSpinner?.let {
            // 이전 프래그먼트에서 전달된 단과대 가져오기
            val selectedCollege = viewModel.selectedCollege.value ?: "학과를 선택하세요"

            val departments =
                listOf(selectedCollege) + (departmentMap[selectedCollege] ?: listOf("학과 없음"))

            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, departments)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            it.adapter = adapter

            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    // 기본 텍스트가 아닌 항목이 선택된 경우에만 처리
                    if (position != 0) {
                        val selectedDepartment = departments[position]
                        viewModel.setDepartment(selectedDepartment) // 선택한 학과 저장
                        findNavController().navigate(R.id.action_selectDepartmentFragment_to_welcomeFragment)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // 아무것도 선택되지 않은 상태일 때의 처리가 필요한 경우 여기에 추가
                }
            }
        } ?: run {
            // Spinner가 null일 경우 처리
            throw RuntimeException("Department Spinner is missing in fragment_select_department.xml")
        }
    }
}

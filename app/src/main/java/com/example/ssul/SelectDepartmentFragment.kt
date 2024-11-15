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
import com.example.ssul.viewmodel.DepartmentRegisterViewModel

class SelectDepartmentFragment : Fragment() {

    private val viewModel: DepartmentRegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_department, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val departmentSpinner: Spinner = view.findViewById(R.id.departmentSpinner)

        // 이전 프래그먼트에서 전달된 단과대 가져오기
        val selectedCollege = viewModel.selectedCollege.value ?: "학과를 선택하세요"
        val departments = listOf(selectedCollege, "컴퓨터공학부", "소프트웨어학부", "글로벌미디어학부") // 첫 항목을 기본 텍스트로 설정

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departments)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = adapter

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
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
    }
}

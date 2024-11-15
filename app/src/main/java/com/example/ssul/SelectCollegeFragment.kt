package com.example.ssul

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ssul.viewmodel.DepartmentRegisterViewModel

class SelectCollegeFragment : Fragment() {

    private val viewModel: DepartmentRegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_college, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collegeSpinner: Spinner = view.findViewById(R.id.collegeSpinner)
        val colleges = listOf("단과 대학", "IT대학", "인문대학", "법과대학", "공과대학") // 첫 번째 항목을 기본 텍스트로 설정
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colleges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        collegeSpinner.adapter = adapter

        // 스피너 선택 이벤트 리스너 설정
        collegeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // 기본 텍스트가 아닌 항목이 선택된 경우에만 처리
                if (position != 0) {
                    val selectedCollege = colleges[position]
                    viewModel.setCollege(selectedCollege) // 선택한 단과대 저장
                    findNavController().navigate(R.id.action_selectCollegeFragment_to_selectDepartmentFragment)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않은 상태일 때의 처리가 필요한 경우 여기에 추가
            }
        }
    }
}

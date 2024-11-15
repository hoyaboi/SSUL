package com.example.ssul

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ssul.viewmodel.DepartmentRegisterViewModel

class WelcomeFragment : Fragment() {

    private val viewModel: DepartmentRegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeTextView: TextView = view.findViewById(R.id.welcomeTextView)
        viewModel.selectedDepartment.observe(viewLifecycleOwner) { department ->
            welcomeTextView.text = "환영합니다!\n$department 학생이군요!"
        }

        view.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

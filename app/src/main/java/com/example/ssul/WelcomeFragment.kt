package com.example.ssul

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ssul.viewmodel.DegreeViewModel

class WelcomeFragment : Fragment() {

    private val viewModel: DegreeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val welcomeTextView: TextView = view.findViewById(R.id.welcomeTextView)
        viewModel.selectedCollege.observe(viewLifecycleOwner) { college ->
            viewModel.selectedDepartment.observe(viewLifecycleOwner) { department ->
                welcomeTextView.text = "$department\n 학생이군요!"

                // SharedPreferences에 저장
                editor.putString("selectedCollege", college)
                editor.putString("selectedDepartment", department)
                editor.apply()
            }
        }

        view.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}

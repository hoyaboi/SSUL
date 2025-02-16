package com.example.ssul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DegreeViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedCollege = MutableLiveData<String>()
    val selectedCollege: LiveData<String> get() = _selectedCollege

    private val _selectedDepartment = MutableLiveData<String>()
    val selectedDepartment: LiveData<String> get() = _selectedDepartment

    init {
        loadCollegeInfo()
    }

    private fun loadCollegeInfo() {
        val prefs = getApplication<Application>().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        _selectedCollege.value = prefs.getString("selectedCollege", "") ?: ""
        _selectedDepartment.value = prefs.getString("selectedDepartment", "") ?: ""
    }

    fun setCollege(college: String) {
        _selectedCollege.value = college
    }

    fun setDepartment(department: String) {
        _selectedDepartment.value = department
    }
}
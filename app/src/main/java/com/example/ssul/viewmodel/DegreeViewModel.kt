package com.example.ssul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DegreeViewModel : ViewModel() {
    private val _selectedCollege = MutableLiveData<String>()
    val selectedCollege: LiveData<String> get() = _selectedCollege

    private val _selectedDepartment = MutableLiveData<String>()
    val selectedDepartment: LiveData<String> get() = _selectedDepartment

    init {
        loadCollegeInfo()
    }

    private fun loadCollegeInfo() {

    }

    fun setCollege(college: String) {
        _selectedCollege.value = college
    }

    fun setDepartment(department: String) {
        _selectedDepartment.value = department
    }
}
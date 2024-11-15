package com.example.ssul.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DepartmentRegisterViewModel : ViewModel() {
    private val _selectedCollege = MutableLiveData<String>()
    val selectedCollege: LiveData<String> get() = _selectedCollege

    private val _selectedDepartment = MutableLiveData<String>()
    val selectedDepartment: LiveData<String> get() = _selectedDepartment

    fun setCollege(college: String) {
        _selectedCollege.value = college
    }

    fun setDepartment(department: String) {
        _selectedDepartment.value = department
    }
}
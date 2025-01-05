package com.example.ssul.repository

import com.example.ssul.api.RetrofitClient
import com.example.ssul.api.collegeCodeMap
import com.example.ssul.api.degreeCodeMap
import com.example.ssul.model.StoreModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository {
    suspend fun getStores(college: String, degree: String): MutableList<StoreModel> {
        return withContext(Dispatchers.IO) {
            try {
                val collegeCode = collegeCodeMap[college] ?: throw IllegalArgumentException("Unknown college name: $college")
                val degreeCode = degreeCodeMap[degree] ?: throw IllegalArgumentException("Unknown degree name: $degree")

                val response = RetrofitClient.apiService.getStores(collegeCode, degreeCode).execute()
                if (response.isSuccessful) {
                    val storeResponses = response.body() ?: emptyList()
                    storeResponses.map { storeResponse ->
                        StoreModel(
                            id = storeResponse.id,
                            name = storeResponse.name,
                            address = storeResponse.address,
                            isFilterGroupChecked = storeResponse.themes.contains("THE-001"),
                            isFilterDateChecked = storeResponse.themes.contains("THE-002"),
                            isFilterEfficiencyChecked = storeResponse.themes.contains("THE-003"),
                            imageUrl = storeResponse.imageUrl,
                            isAssociated = storeResponse.isAssociated
                        )
                    }.toMutableList()
                } else {
                    mutableListOf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }
}

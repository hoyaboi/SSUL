package com.example.ssul.model

import com.naver.maps.geometry.LatLng

class CoordinateRepository {
    private val coordinateCache = mutableMapOf<String, LatLng>()

    fun getCoordinate(address: String): LatLng? {
        return coordinateCache[address]
    }

    fun setCoordinate(address: String, coordinate: LatLng) {
        coordinateCache[address] = coordinate
    }
}
package com.example.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.adapters.weatherModel

class MainViewModel: ViewModel() {
    val liveDataCurrent = MutableLiveData<weatherModel>()
    val liveDataList = MutableLiveData<List<weatherModel>>()
}
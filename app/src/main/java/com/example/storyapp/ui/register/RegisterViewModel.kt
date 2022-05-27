package com.example.storyapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.remote.ApiConfig
import com.example.storyapp.data.remote.GenericResponse
import com.example.storyapp.utils.Event
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Event<Boolean>>()
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        ApiConfig.getApiService().register(name, email, password)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    _isLoading.value = false

                    if (response.isSuccessful) {
                        _isSuccess.value = Event(true)
                    } else {
                        // ref: https://stackoverflow.com/a/62513959/18277301
                        val errorResponse = Gson().fromJson(
                            response.errorBody()!!.charStream(),
                            GenericResponse::class.java
                        )
                        _error.value = Event(errorResponse.message)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = Event(t.message.toString())
                }
            })
    }
}
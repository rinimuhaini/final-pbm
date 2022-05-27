package com.example.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.remote.ApiConfig
import com.example.storyapp.data.remote.GenericResponse
import com.example.storyapp.data.remote.LoginResponse
import com.example.storyapp.utils.Event
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _token = MutableLiveData<Event<String>>()
    val token: LiveData<Event<String>> = _token

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun login(email: String, password: String) {
        _isLoading.value = true
        ApiConfig.getApiService().login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    val token = response.body()?.loginResult?.token ?: ""
                    _token.value = Event(token)
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()!!.charStream(),
                        GenericResponse::class.java
                    )
                    _error.value = Event(errorResponse.message)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = Event(t.message.toString())
            }
        })
    }
}
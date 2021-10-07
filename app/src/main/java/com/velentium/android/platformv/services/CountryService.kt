package com.example.mysecondapp.services

import retrofit2.Call
import com.example.mysecondapp.models.MyCountry
import retrofit2.http.GET

interface CountryService {
  @GET("countries")
  fun getAffectedCountryList() : Call<List<MyCountry>>
}
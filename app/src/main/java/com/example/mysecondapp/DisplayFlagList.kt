package com.example.mysecondapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.example.mysecondapp.R
import com.example.mysecondapp.helpers.CountriesAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysecondapp.models.MyCountry
import com.example.mysecondapp.services.CountryService
import com.example.mysecondapp.services.ServiceBuilder
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DisplayFlagList : AppCompatActivity() {
  private lateinit var myRecicleView: RecyclerView
  private var countryList : List<MyCountry> = listOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_display_flag_list)

    myRecicleView = findViewById(R.id.country_recycler)
    myRecicleView.layoutManager = GridLayoutManager(this, 2)
    myRecicleView.adapter = CountriesAdapter(countryList)
    loadCountries()

    Realm.init(this)

  }

  private fun loadCountries() {
    //initiate the service
    val destinationService  = ServiceBuilder.buildService(CountryService::class.java)
    val requestCall = destinationService.getAffectedCountryList()
    //make network call asynchronously
    requestCall.enqueue(object : Callback<List<MyCountry>> {
      override fun onResponse(call: Call<List<MyCountry>>, response: Response<List<MyCountry>>) {
        Log.d("Response", "onResponse: ${response.body()}")
        if (response.isSuccessful){
          countryList = response.body()!!
          Log.d("Response", "countrylist size : ${countryList.size}")
          myRecicleView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@DisplayFlagList,2)
          }
          myRecicleView.adapter?.notifyDataSetChanged()
        }else{
          Toast.makeText(this@DisplayFlagList, "Something went wrong ${response.message()}", Toast.LENGTH_SHORT).show()
        }
      }
      override fun onFailure(call: Call<List<MyCountry>>, t: Throwable) {
        Toast.makeText(this@DisplayFlagList, "Something went wrong $t", Toast.LENGTH_SHORT).show()
      }
    })
  }
}
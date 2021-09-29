package com.example.mysecondapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mysecondapp.helpers.CountriesAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysecondapp.models.MyCountry
import com.example.mysecondapp.services.CountryService
import com.example.mysecondapp.services.RealmService
import com.example.mysecondapp.services.ServiceBuilder
import io.realm.Realm
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DisplayFlagList : AppCompatActivity() {
  private lateinit var myRecicleView: RecyclerView
  private lateinit var countryList: List<MyCountry>
  private lateinit var realmService: RealmService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_display_flag_list)
    myRecicleView = findViewById(R.id.country_recycler)

    Realm.init(this)
    realmService = RealmService()

    if (checkForInternet(this)){
      Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
      loadCountries()
    }else{
      var retrievedCountries: List<MyCountry>
      runBlocking{
        launch {
          retrievedCountries = realmService.retrieveCountries()
          if (retrievedCountries.size > 0) {
            showCounties(retrievedCountries)
            Toast.makeText(this@DisplayFlagList, "${retrievedCountries.size} countries found", Toast.LENGTH_SHORT).show()
          }else{
            Toast.makeText(this@DisplayFlagList, "No countries found", Toast.LENGTH_SHORT).show()
          }
        }
      }
      Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
    }

  }

  private fun checkForInternet(context: Context): Boolean {

    // register activity with the connectivity manager service
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // if the android version is equal to M
    // or greater we need to use the
    // NetworkCapabilities to check what type of
    // network has the internet connection
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      // Returns a Network object corresponding to
      // the currently active default data network.
      val network = connectivityManager.activeNetwork ?: return false

      // Representation of the capabilities of an active network.
      val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

      return when {
        // Indicates this network uses a Wi-Fi transport,
        // or WiFi has network connectivity
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

        // Indicates this network uses a Cellular transport. or
        // Cellular has network connectivity
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

        // else return false
        else -> false
      }
    } else {
      // if the android version is below M
      @Suppress("DEPRECATION") val networkInfo =
        connectivityManager.activeNetworkInfo ?: return false
      @Suppress("DEPRECATION")
      return networkInfo.isConnected
    }
  }

  private fun loadCountries() {
    //initiate the service
    val destinationService = ServiceBuilder.buildService(CountryService::class.java)
    val requestCall = destinationService.getAffectedCountryList()
    //make network call asynchronously
    requestCall.enqueue(object : Callback<List<MyCountry>> {
      override fun onResponse(call: Call<List<MyCountry>>, response: Response<List<MyCountry>>) {
        Log.d("Response", "onResponse: ${response.body()}")
        if (response.isSuccessful){
          runBlocking {
            launch {
              realmService.removeAllCountries()
              countryList = response.body()!!
              Log.d("Response", "countrylist size : ${countryList.size}")
              showCounties(countryList)
              realmService.insertCountries(countryList)
            }
          }
        }else{
          Toast.makeText(this@DisplayFlagList, "Something went wrong ${response.message()}", Toast.LENGTH_SHORT).show()
        }
      }
      override fun onFailure(call: Call<List<MyCountry>>, t: Throwable) {
        Toast.makeText(this@DisplayFlagList, "Something went wrong $t", Toast.LENGTH_SHORT).show()
      }
    })
  }

  private fun showCounties(countryList: List<MyCountry>){
    myRecicleView.apply {
      setHasFixedSize(true)
      layoutManager = GridLayoutManager(this@DisplayFlagList,2)
      adapter = CountriesAdapter(countryList)
    }
  }
}
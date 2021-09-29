package com.example.mysecondapp.services

import android.util.Log
import com.example.mysecondapp.models.MyCountry
import com.example.mysecondapp.realm.CountryInfoRealm
import com.example.mysecondapp.realm.MyCountryRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Dispatcher

class RealmService {
  private val realmVersion = 1L

//  @Singleton
//  @Provides
  fun providesRealmConfig(): RealmConfiguration =
    // 2.
    RealmConfiguration.Builder()
      .schemaVersion(realmVersion)
      .build()

  private val config: RealmConfiguration = providesRealmConfig()
  val realm = Realm.getInstance(config)

  fun insertCountries(myCountryList: List<MyCountry>){
    for(country in myCountryList){
      runBlocking {
        launch {
          insertCountry(country)
        }
      }
    }
  }

  suspend fun insertCountry(myCountry: MyCountry){
    var countryInfoRealm = CountryInfoRealm(flag = myCountry.countryInfo.flag, originalId = myCountry.countryInfo.id)
    realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
      val countryRealm = MyCountryRealm(
        active = myCountry.active,
        activePerOneMillion = myCountry.activePerOneMillion,
        cases = myCountry.cases,
        casesPerOneMillion = myCountry.casesPerOneMillion,
        continent = myCountry.continent,
        country = myCountry.country,
        countryInfo = countryInfoRealm,
        critical = myCountry.critical,
        criticalPerOneMillion = myCountry.criticalPerOneMillion,
        deaths = myCountry.deaths,
        deathsPerOneMillion = myCountry.deathsPerOneMillion,
        oneCasePerPeople = myCountry.oneCasePerPeople,
        oneDeathPerPeople = myCountry.oneDeathPerPeople,
        oneTestPerPeople = myCountry.oneTestPerPeople,
        population = myCountry.population,
        recovered = myCountry.recovered,
        recoveredPerOneMillion = myCountry.recoveredPerOneMillion,
        tests = myCountry.tests,
        testsPerOneMillion = myCountry.testsPerOneMillion,
        todayCases = myCountry.todayCases,
        todayDeaths = myCountry.todayDeaths,
        todayRecovered = myCountry.todayRecovered,
        updated = myCountry.updated
      )
      realmTransaction.insert(countryRealm)
    }

  }

  //HELPER FUNCTION TO MAP THE REALM RESULT INTO OBJECT
  private fun mapCountry(countryRealm: MyCountryRealm): MyCountry{
    val countryInfo = MyCountry.CountryInfo(countryRealm.countryInfo!!.flag,countryRealm.countryInfo!!.originalId)
    return MyCountry(
      active = countryRealm.active,
      activePerOneMillion = countryRealm.activePerOneMillion,
      cases = countryRealm.cases,
      casesPerOneMillion = countryRealm.casesPerOneMillion,
      continent = countryRealm.continent,
      country = countryRealm.country,
      countryInfo = countryInfo,
      critical = countryRealm.critical,
      criticalPerOneMillion = countryRealm.criticalPerOneMillion,
      deaths = countryRealm.deaths,
      deathsPerOneMillion = countryRealm.deathsPerOneMillion,
      oneCasePerPeople = countryRealm.oneCasePerPeople,
      oneDeathPerPeople = countryRealm.oneDeathPerPeople,
      oneTestPerPeople = countryRealm.oneTestPerPeople,
      population = countryRealm.population,
      recovered = countryRealm.recovered,
      recoveredPerOneMillion = countryRealm.recoveredPerOneMillion,
      tests = countryRealm.tests,
      testsPerOneMillion = countryRealm.testsPerOneMillion,
      todayCases = countryRealm.todayCases,
      todayDeaths = countryRealm.todayDeaths,
      todayRecovered = countryRealm.todayRecovered,
      updated = countryRealm.updated
    )
  }

  suspend fun retrieveCountries(): List<MyCountry>{
    //val realm = Realm.getInstance(config)
    val countryList = mutableListOf<MyCountry>()

    realm.executeTransactionAwait(Dispatchers.IO) { realTransaction ->
      countryList.addAll(realTransaction
        .where(MyCountryRealm::class.java)
        .findAll()
        .map {
          mapCountry(it)
        }
      )
    }
    return countryList
  }

  suspend fun retrieveCountry(byName: String): MyCountry?{
    val countryList = mutableListOf<MyCountry>()

    realm.executeTransactionAwait(Dispatchers.IO) { realTransaction ->
      countryList.addAll(realTransaction
        .where(MyCountryRealm::class.java)
        .findAll()
        .map {
          mapCountry(it)
        }
      )
    }
    return countryList.first()
  }

  suspend fun removeAllCountries(){
    realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
      // 1.
      val countryToRemove = realmTransaction
        .where(MyCountryRealm::class.java)
        .findAll()
      // 2.
      countryToRemove?.deleteAllFromRealm()
    }
  }

  suspend fun removeCountry(byName: String) {
    val realm = Realm.getInstance(config)
    realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
      // 1.
      val countryToRemove = realmTransaction
        .where(MyCountryRealm::class.java)
        .equalTo("country", byName)
        .findFirst()
      // 2.
      countryToRemove?.deleteFromRealm()
    }
  }
}
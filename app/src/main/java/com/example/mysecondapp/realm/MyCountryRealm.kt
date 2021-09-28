package com.example.mysecondapp.realm

import com.example.mysecondapp.models.MyCountry
import com.google.gson.annotations.SerializedName
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import javax.annotation.Nullable

open class MyCountryRealm(
  @PrimaryKey
  var id: String = ObjectId().toHexString(),
  var active: Int = 0,
  var activePerOneMillion: Double = 0.0,
  var cases: Int = 0,
  var casesPerOneMillion: Double = 0.0,
  var continent: String = "",
  var country: String = "",
  var countryInfo: CountryInfoRealm? = CountryInfoRealm(),
  var critical: Int = 0,
  var criticalPerOneMillion: Double = 0.0,
  var deaths: Int = 0,
  var deathsPerOneMillion: Double = 0.0,
  var oneCasePerPeople: Int = 0,
  var oneDeathPerPeople: Int = 0,
  var oneTestPerPeople: Int = 0,
  var population: Int = 0,
  var recovered: Int = 0,
  var recoveredPerOneMillion: Double = 0.0,
  var tests: Int = 0,
  var testsPerOneMillion: Double = 0.0,
  var todayCases: Int = 0,
  var todayDeaths: Int = 0,
  var todayRecovered: Int = 0,
  var updated: Long = 1000
): RealmObject()

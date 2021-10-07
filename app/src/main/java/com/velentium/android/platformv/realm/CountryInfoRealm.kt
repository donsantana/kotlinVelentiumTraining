package com.example.mysecondapp.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

open class CountryInfoRealm(
  @PrimaryKey
  var id: String = ObjectId().toHexString(),
  var flag: String = "",
  var originalId: Int = 0
): RealmObject()
package com.velentium.android.platformv.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject

//region JSON Object

fun JSONObject.getBooleanOptional(name: String): Boolean? {
    return if (this.has(name)) this.getBoolean(name) else null
}

fun JSONObject.getStringOptional(name: String): String? {
    return if (this.has(name)) this.getString(name) else null
}

fun JSONObject.getIntOptional(name: String): Int? {
    return if (this.has(name)) this.getInt(name) else null
}

fun JSONObject.getLongOptional(name: String): Long? {
    return if (this.has(name)) this.getLong(name) else null
}

fun JSONObject.getDoubleOptional(name: String): Double? {
    return if (this.has(name)) this.getDouble(name) else null
}

fun JSONObject.getJsonObjectOptional(name: String): JSONObject? {
    return if (this.has(name)) this.getJSONObject(name) else null
}

fun JSONObject.getJsonArrayOptional(name: String): JSONArray? {
    return if (this.has(name)) this.getJSONArray(name) else null
}

//endregion

//region JSON Array

fun JSONArray.getIntOptional(index: Int): Int? {
    return try {
        this.getInt(index)
    } catch (ex: Exception) {
        null
    }
}

fun JSONArray.getDoubleOptional(index: Int): Double? {
    return try {
        this.getDouble(index)
    } catch (ex: Exception) {
        null
    }
}

fun JSONArray.toIntArray(): IntArray {
    val intArray = IntArray(this.length())
    for (i in 0 until this.length()) {
        intArray[i] = this.getInt(i)
    }
    return intArray
}

fun JSONArray.toDoubleArray(): DoubleArray {
    val doubleArray = DoubleArray(this.length())
    for (i in 0 until this.length()) {
        doubleArray[i] = this.getDouble(i)
    }
    return doubleArray
}

fun JSONArray.toBooleanArray(): BooleanArray {
    val booleanArray = BooleanArray(this.length())
    for (i in 0 until this.length()) {
        booleanArray[i] = this.getBoolean(i)
    }
    return booleanArray
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "forEachAny")
inline fun JSONArray.forEach(action: (Any) -> Unit) {
    for (i in 0 until this.length()) {
        action.invoke(this[i])
    }
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "forEachString")
inline fun JSONArray.forEach(action: (String) -> Unit) {
    for (i in 0 until this.length()) {
        this.optString(i)?.run { action.invoke(this) }
    }
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "forEachJsonObject")
inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    for (i in 0 until this.length()) {
        this.optJSONObject(i)?.run { action.invoke(this) }
    }
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "mapJsonObject")
inline fun <R> JSONArray.map(action: (JSONObject) -> R): List<R> {
    val results = mutableListOf<R>()
    for (i in 0 until this.length()) {
        this.optJSONObject(i)?.let { action.invoke(it) }?.run { results.add(this) }
    }

    return results
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "mapIntObject")
inline fun <R> JSONArray.map(action: (Int) -> R): List<R> {
    val results = mutableListOf<R>()
    for (i in 0 until this.length()) {
        this.getIntOptional(i)?.let { action.invoke(it) }?.run { results.add(this) }
    }
    return results
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "mapIndexedIntObject")
inline fun <R> JSONArray.mapIndexed(action: (Int, Int) -> R): List<R> {
    val results = mutableListOf<R>()
    for (i in 0 until this.length()) {
        this.getIntOptional(i)?.let { action.invoke(i, it) }?.run { results.add(this) }
    }
    return results
}

/**
 * Performs the given [action] on each element in the [JSONArray].
 */
@JvmName(name = "mapIndexedDoubleObject")
inline fun <R> JSONArray.mapIndexed(action: (Int, Double) -> R): List<R> {
    val results = mutableListOf<R>()
    for (i in 0 until this.length()) {
        this.getDoubleOptional(i)?.let { action.invoke(i, it) }?.run { results.add(this) }
    }
    return results
}

/**
 * Filters out elements in the [JSONArray] that do not meet the [predicate].
 */
@JvmName(name = "filterIntObject")
inline fun JSONArray.filter(predicate: (Int) -> Boolean): List<Int> {
    val results = mutableListOf<Int>()
    for (i in 0 until this.length()) {
        this.getIntOptional(i)?.takeIf { predicate(it) }?.run { results.add(this) }
    }
    return results
}

/**
 * Filters out elements in the [JSONArray] that do not meet the [predicate].
 */
@JvmName(name = "filterDoubleObject")
inline fun JSONArray.filter(predicate: (Double) -> Boolean): List<Double> {
    val results = mutableListOf<Double>()
    for (i in 0 until this.length()) {
        this.getDoubleOptional(i)?.takeIf { predicate(it) }?.run { results.add(this) }
    }
    return results
}

//endregion

//region Other

fun String.toJSONObject(): JSONObject {
    return JSONObject(this)
}

fun Any.toJsonStringWithGson(
    gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
): String {
    return gson.toJson(this)
}

fun Any.toJSONObjectWithGson(
    gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
): JSONObject {
    return JSONObject(this.toJsonStringWithGson(gson = gson))
}

//endregion
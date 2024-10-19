package com.position

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments


class PositionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val locationManager: LocationManager = reactContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

  override fun getName(): String {
    return NAME
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  fun getGNSS(promise: Promise) {

    val locationListener = object : LocationListener {
      override fun onLocationChanged(location: Location) {
        val result: WritableMap = Arguments.createMap().apply {
          putDouble("latitude", location.latitude)
          putDouble("longitude", location.longitude)
          putDouble("accuracy", location.accuracy.toDouble())
          putDouble("altitude", location.altitude)
        }
        promise.resolve(result)
        locationManager.removeUpdates(this)
      }

      override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
      override fun onProviderEnabled(provider: String) {}
      override fun onProviderDisabled(provider: String) {}
    }

    try {
      locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        0L,
        0f,
        locationListener
      )
    } catch (e: Exception) {
      promise.reject("LOCATION_ERROR", "获取位置信息失败: ${e.message}")
    }
  }

  companion object {
    const val NAME = "Position"
  }
}

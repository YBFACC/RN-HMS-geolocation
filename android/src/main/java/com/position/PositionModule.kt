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
import android.os.Handler
import android.os.Looper


class PositionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val locationManager: LocationManager = reactContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val handler = Handler(Looper.getMainLooper())

  override fun getName(): String {
    return NAME
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  fun getGNSS(timeout: Double, promise: Promise) {
    val locationListener = object : LocationListener {
      override fun onLocationChanged(location: Location) {
        // 移除位置更新和延迟的超时回调
        locationManager.removeUpdates(this)
        handler.removeCallbacksAndMessages(null)

        val result: WritableMap = Arguments.createMap().apply {
          putDouble("latitude", location.latitude)
          putDouble("longitude", location.longitude)
        }
        promise.resolve(result)
      }

      override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
      override fun onProviderEnabled(provider: String) {}
      override fun onProviderDisabled(provider: String) {}
    }

    val timeoutRunnable = Runnable {
      locationManager.removeUpdates(locationListener)
      promise.reject("TIMEOUT", "获取GNSS位置超时")
    }

    try {
      locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        0L,
        0f,
        locationListener
      )

      // GNSS超时处理
      handler.postDelayed(timeoutRunnable, timeout.toLong())
    } catch (e: Exception) {
      handler.removeCallbacks(timeoutRunnable)
      promise.reject("LOCATION_ERROR", "获取位置信息失败: ${e.message}")
    }
  }

  companion object {
    const val NAME = "Position"
  }
}

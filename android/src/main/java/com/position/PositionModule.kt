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
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation


class PositionModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val locationManager: LocationManager =
    reactContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val handler = Handler(Looper.getMainLooper())

  private val wifiManager: WifiManager =
    reactContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

  private val telephonyManager: TelephonyManager =
    reactContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

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

  @ReactMethod
  fun getWifi(promise: Promise) {
    if (!wifiManager.isWifiEnabled) {
      val map = Arguments.createMap().apply {
        putString("ssid", "")
        putString("bssid", "")
        putInt("rssi", 0)
        putBoolean("isWifiEnabled", false)
      }
      promise.resolve(map)
      return
    }

    val wifiInfo = wifiManager.connectionInfo

    if (wifiInfo != null) {
      val map = Arguments.createMap().apply {
        putString("ssid", wifiInfo.ssid)
        putString("bssid", wifiInfo.bssid)
        putInt("rssi", wifiInfo.rssi)
        putBoolean("isWifiEnabled", true)
      }
      promise.resolve(map)
    } else {
      promise.reject("WIFI_INFO_ERROR", "无法获取 WiFi 信息")
    }
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  fun getCell(promise: Promise) {
    try {
      val cel = telephonyManager.getCellLocation()
      var cellid = 0
      var lac = 0
      var type = ""

      if (cel is GsmCellLocation) {
        cellid = cel.cid
        lac = cel.lac
        type = "GSM"
      } else if (cel is CdmaCellLocation) {
        cellid = cel.baseStationId
        lac = cel.networkId
        type = "CDMA"
      }

      var rssi = 50
      val networkType = telephonyManager.networkType
      if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
        val signalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          telephonyManager.signalStrength
        } else {
          null
        }
        if (signalStrength != null) {
          rssi = signalStrength.getGsmSignalStrength()
        }
      }

      val rat = when (telephonyManager.networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "1"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "2"
        TelephonyManager.NETWORK_TYPE_LTE -> "3"
        TelephonyManager.NETWORK_TYPE_NR -> "4"
        else -> "3"
      }

      val map = Arguments.createMap()
      map.putString("lac", lac.toString())
      map.putString("cellId", cellid.toString())
      map.putString("simOperator", telephonyManager.getSimOperator())
      map.putString("rat", rat)
      map.putString("rssi", rssi.toString())
      map.putString("type", type)
      promise.resolve(map)
    } catch (e: Exception) {
      promise.reject("CELL_ERROR", "获取蜂窝网络信息失败: ${e.message}")
    }
  }

  companion object {
    const val NAME = "Position"
  }
}

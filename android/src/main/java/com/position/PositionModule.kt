package com.position

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoTdscdma
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap


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
  fun getWifis(promise: Promise) {
    try {
      // 检查WiFi是否启用
      if (!wifiManager.isWifiEnabled) {
        promise.reject("WIFI_DISABLED", "WiFi is not enabled")
        return
      }

      // 开始扫描
      if (!wifiManager.startScan()) {
        promise.reject("SCAN_FAILED", "Failed to start WiFi scan")
        return
      }

      // 获取扫描结果
      val scanResults = wifiManager.scanResults
        .distinctBy { it.SSID }
        .filter { it.SSID.isNotEmpty() }
        .take(10)

      val resultArray = Arguments.createArray()

      scanResults.forEach { result ->
        Arguments.createMap().apply {
          putString("ssid", result.SSID)
          putString("bssid", result.BSSID)
          putInt("rssi", result.level)
          resultArray.pushMap(this)
        }
      }

      promise.resolve(resultArray)
    } catch (e: Exception) {
      promise.reject("WIFI_ERROR", "Failed to get WiFi list: ${e.message}")
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

      val rat = when (telephonyManager.networkType) {
        // 3G 网络类型
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NETWORK_3G
        // 4G 网络类型
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN -> NETWORK_4G
        // 5G 网络类型
        TelephonyManager.NETWORK_TYPE_NR -> NETWORK_5G
        else -> NETWORK_UNKNOWN
      }

      var rssi = 0
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        telephonyManager.signalStrength?.let { signalStrength ->
          rssi = when {
            signalStrength.getCellSignalStrengths(CellSignalStrengthLte::class.java)
              .isNotEmpty() -> {
              val lteRssi =
                signalStrength.getCellSignalStrengths(CellSignalStrengthLte::class.java)[0].rssi
              if (lteRssi == Integer.MAX_VALUE) 0 else lteRssi
            }

            signalStrength.getCellSignalStrengths(CellSignalStrengthGsm::class.java)
              .isNotEmpty() -> {
              val gsmDbm =
                signalStrength.getCellSignalStrengths(CellSignalStrengthGsm::class.java)[0].dbm
              if (gsmDbm == Integer.MAX_VALUE) 0 else gsmDbm
            }

            signalStrength.getCellSignalStrengths(CellSignalStrengthWcdma::class.java)
              .isNotEmpty() -> {
              val wcdmaDbm =
                signalStrength.getCellSignalStrengths(CellSignalStrengthWcdma::class.java)[0].dbm
              if (wcdmaDbm == Integer.MAX_VALUE) 0 else wcdmaDbm
            }

            signalStrength.getCellSignalStrengths(CellSignalStrengthNr::class.java)
              .isNotEmpty() -> {
              val nrDbm =
                signalStrength.getCellSignalStrengths(CellSignalStrengthNr::class.java)[0].dbm
              if (nrDbm == Integer.MAX_VALUE) 0 else nrDbm
            }

            else -> 0
          }
        }
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

  @SuppressLint("MissingPermission")
  @ReactMethod
  fun getNeighborCell(promise: Promise) {
    try {
      val neighboringCells = Arguments.createArray()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        telephonyManager.allCellInfo?.forEach { cellInfo ->
          val cellMap = Arguments.createMap()

          when (cellInfo) {
            is CellInfoGsm -> {
              // 无 pid 跳过
            }

            is CellInfoCdma -> {
              val rssi = cellInfo.cellSignalStrength.dbm
              val cNum = cellInfo.cellIdentity.basestationId
              val pId = cellInfo.cellIdentity.systemId
              
              if (isValidCellData(rssi, cNum, pId)) {
                cellMap.apply {
                  putInt("rssi", rssi)
                  putInt("cNum", cNum)
                  putInt("pId", pId)
                }
                neighboringCells.pushMap(cellMap)
              }
            }

            is CellInfoTdscdma -> {
              val rssi = cellInfo.cellSignalStrength.dbm
              val cNum = cellInfo.cellIdentity.cid
              val pId = cellInfo.cellIdentity.cpid
              
              if (isValidCellData(rssi, cNum, pId)) {
                cellMap.apply {
                  putInt("rssi", rssi)
                  putInt("cNum", cNum)
                  putInt("pId", pId)
                }
                neighboringCells.pushMap(cellMap)
              }
            }

            is CellInfoLte -> {
              val rssi = cellInfo.cellSignalStrength.rssi
              val cNum = cellInfo.cellIdentity.ci
              val pId = cellInfo.cellIdentity.pci
              
              if (isValidCellData(rssi, cNum, pId)) {
                cellMap.apply {
                  putInt("rssi", rssi)
                  putInt("cNum", cNum)
                  putInt("pId", pId)
                }
                neighboringCells.pushMap(cellMap)
              }
            }

            is CellInfoWcdma -> {
              val rssi = cellInfo.cellSignalStrength.dbm
              val cNum = cellInfo.cellIdentity.cid
              val pId = cellInfo.cellIdentity.psc
              
              if (isValidCellData(rssi, cNum, pId)) {
                cellMap.apply {
                  putInt("rssi", rssi)
                  putInt("cNum", cNum)
                  putInt("pId", pId)
                }
                neighboringCells.pushMap(cellMap)
              }
            }
          }
        }
      }

      promise.resolve(neighboringCells)
    } catch (e: Exception) {
      val emptyArray = Arguments.createArray()
      promise.resolve(emptyArray)
    }
  }

  // 添加辅助函数来验证数据
  private fun isValidCellData(rssi: Int, cNum: Int, pId: Int): Boolean {
    return rssi != Integer.MAX_VALUE && 
           rssi != 0 && 
           cNum != Integer.MAX_VALUE && 
           cNum > 0 && 
           pId != Integer.MAX_VALUE && 
           pId >= 0
  }

  companion object {
    const val NAME = "Position"

    const val NETWORK_3G = "2"
    const val NETWORK_4G = "3"
    const val NETWORK_5G = "4"
    const val NETWORK_UNKNOWN = "3"
  }
}

import { NativeModules, Platform } from 'react-native'
import { HMSLocation } from './HMSLocation'
import { macTransform } from './utils'

const LINKING_ERROR =
    `The package 'react-native-position' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n'

const Position: {
    getGNSS: (timeout: number) => Promise<{
        latitude: number
        longitude: number
    }>
    getCell: () => Promise<{
        cellId: string
        rat: number
        lac: string
        rssi: number
        simOperator: string
        type: 'GSM' | 'CDMA'
    }>
    getWifi: () => Promise<{
        isWifiEnabled: boolean
        bssid: string
        rssi: number
    }>
} = NativeModules.Position
    ? NativeModules.Position
    : new Proxy(
          {},
          {
              get() {
                  throw new Error(LINKING_ERROR)
              },
          },
      )

type ConfigType = {
    hmsKey: string
    GNSStimeout: number
}

export async function getCurrentPosition(config: ConfigType): Promise<{
    latitude: number
    longitude: number
    from: 'GNSS' | 'Cell' | 'Wifi'
    HMSResult?: any
    HMSRequest?: any
}> {
    const _config = {
        hmsKey: config.hmsKey ?? '',
        GNSStimeout: config.GNSStimeout ?? 5000,
    }

    try {
        const wifi = await Position.getWifi()

        if (wifi.isWifiEnabled && wifi.bssid) {
            // WiFi 已连接，使用 HMS Location 服务
            const HMSRequest = {
                mac: macTransform(wifi.bssid),
                rssi: wifi.rssi - 100,
                time: Date.now() * 1000,
            }

            const hmsLocation = await HMSLocation(_config.hmsKey, [], [HMSRequest])
            return {
                latitude: hmsLocation.latitude,
                longitude: hmsLocation.longitude,
                from: 'Wifi',
                HMSResult: hmsLocation.HMSResult,
                HMSRequest,
            }
        } else {
            // WiFi 未连接，尝试使用 GNSS
            try {
                const gnss = await Position.getGNSS(_config.GNSStimeout)
                return {
                    latitude: gnss.latitude,
                    longitude: gnss.longitude,
                    from: 'GNSS',
                }
            } catch (gnssError) {
                // GNSS 失败，使用 Cell 信息
                const cell = await Position.getCell()

                const mcc = +cell.simOperator.substring(0, 3)
                let mnc = +cell.simOperator.substring(3)
                if (mnc < 10) {
                    mnc = 0
                }
                const HMSRequest = {
                    currentCell: {
                        cellId: +cell.cellId,
                        lac: +cell.lac,
                        mcc,
                        mnc,
                        rat: cell.rat,
                        rssi: cell.rssi - 100,
                    },
                }

                const hmsLocation = await HMSLocation(_config.hmsKey, [HMSRequest], [])
                return {
                    latitude: hmsLocation.latitude,
                    longitude: hmsLocation.longitude,
                    from: 'Cell',
                    HMSResult: hmsLocation.HMSResult,
                    HMSRequest,
                }
            }
        }
    } catch (error) {
        console.error('定位失败:', error)
        throw error
    }
}

export async function getCellInfo(): Promise<{
    cellId: string
    rat: number
    lac: string
    rssi: number
    simOperator: string
    type: 'GSM' | 'CDMA'
}> {
    return await Position.getCell()
}

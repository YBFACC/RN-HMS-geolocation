type CellInfoType = {
    currentCell: {
        cellId: number
        lac: number
        mcc: number
        mnc: number
        rat: number
        rssi: number
    }
}

type WifiInfoType = {
    mac: number
    rssi: number
    time: number
}

export function HMSLocation(
    hmsKey: string,
    cellInfos: CellInfoType[],
    wifiInfos: WifiInfoType[],
): Promise<{
    longitude: number
    latitude: number
    HMSResult: any
}> {
    console.log('cellInfos', cellInfos)
    console.log('wifiInfos', wifiInfos)

    return fetch('https://locationapi.cloud.huawei.com/networklocation/v1/geoLocation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${hmsKey}`,
        },
        body: JSON.stringify({
            boottime: Date.now() * 1000,
            indoorMode: 0,
            cellInfos,
            wifiInfos,
        }),
    })
        .then(res => res.json())
        .then(res => {
            if (res.errorCode !== '0') {
                throw Error(
                    'HMSResult:' +
                        JSON.stringify({
                            errorCode: res.errorCode,
                            errorMsg: res.errorMsg,
                            cellInfos,
                            wifiInfos,
                        }),
                )
            }

            return {
                longitude: res.position.lon,
                latitude: res.position.lat,
                HMSResult: res,
            }
        })
}

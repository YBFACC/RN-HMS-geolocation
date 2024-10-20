import { useState, useEffect } from 'react'
import { StyleSheet, View, Text, PermissionsAndroid } from 'react-native'
import { getCurrentPosition } from 'react-native-position'

export default function App() {
    const [result, setResult] = useState<any>()

    useEffect(() => {
        const requestLocationPermission = async () => {
            try {
                const granteds = await PermissionsAndroid.requestMultiple([
                    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION!,
                    PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION!,
                    PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE!,
                ])
                if (
                    granteds['android.permission.ACCESS_FINE_LOCATION'] !== 'granted' ||
                    granteds['android.permission.ACCESS_COARSE_LOCATION'] !== 'granted' ||
                    granteds['android.permission.READ_PHONE_STATE'] !== 'granted'
                ) {
                    console.log('权限拒绝')
                    return
                }

                const position = await getCurrentPosition({
                    hmsKey: 'your hms key',
                    GNSStimeout: 5000,
                })
                setResult(JSON.stringify(position))
            } catch (err) {
                console.warn(err)
            }
        }

        requestLocationPermission()
    }, [])

    return (
        <View style={styles.container}>
            <Text>Result: {result}</Text>
        </View>
    )
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    box: {
        width: 60,
        height: 60,
        marginVertical: 20,
    },
})

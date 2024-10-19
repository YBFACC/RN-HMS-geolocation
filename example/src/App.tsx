import { useState, useEffect } from 'react'
import { StyleSheet, View, Text, PermissionsAndroid, Alert, Linking } from 'react-native'
import { getCurrentPosition } from 'react-native-position'

export default function App() {
    const [result, setResult] = useState<any>()

    useEffect(() => {
        const requestLocationPermission = async () => {
            try {
                const granted = await PermissionsAndroid.request(
                    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
                    {
                        title: '位置权限',
                        message: '此应用需要访问您的位置信息',
                        buttonNeutral: '稍后询问',
                        buttonNegative: '取消',
                        buttonPositive: '确定',
                    },
                )
                if (granted === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
                    console.log('用户选择了不再询问')
                    // 在这里处理 "不再询问" 的情况
                    Alert.alert(
                        '需要位置权限',
                        '此应用需要位置权限才能正常工作。请在设置中手动开启位置权限。',
                        [
                            { text: '取消', style: 'cancel' },
                            { text: '去设置', onPress: () => Linking.openSettings() },
                        ],
                    )
                }

                if (granted === PermissionsAndroid.RESULTS.GRANTED) {
                    console.log('位置权限已授予')
                    const position = await getCurrentPosition()
                    setResult(JSON.stringify(position))
                } else {
                    console.log('位置权限被拒绝')
                }
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

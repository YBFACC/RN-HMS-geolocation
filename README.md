# react-native-position

通过 `Android’s Location API` 和 `HMS Location REST API` 获取位置信息。

解决 `Google’s Location Services API` 无法使用时,也能获取位置信息。

## 优势

-   不需要集成第三方 SDK, 只需要调用 Https 请求
-   由于 HMS 不需要商业授权, 只需要按量付费([HMS 位置服务](https://developer.huawei.com/consumer/cn/doc/HMSCore-References/web-network-location-0000001051602603))([安卓定位方案的价格对比](http://www.likehide.com/blogs/android/location_server/))

## 实现原理

1. 在有 `wifi`连接时, 通过 `HMS Location REST API` 请求位置
2. 在无 `wifi`连接时, 尝试 `Android’s Location API` 请求
3. 保底情况, 通过 `cell`信息 `HMS Location REST API` 请求位置

## 必要准备

1. [注册华为开发者账号](https://developer.huawei.com/consumer/cn/doc/start/registration-and-verification-0000001053628148)
2. [创建项目](https://developer.huawei.com/consumer/cn/doc/app/agc-help-createproject-0000001100334664)和[创建应用](https://developer.huawei.com/consumer/cn/doc/app/agc-help-createapp-0000001146718717)
3. 获取到 `apiKey`![screenshot-20241020-051107](https://github.com/user-attachments/assets/4dd30de6-a541-46e0-9ed8-38be44b3754a)

## 使用

```sh
npm install react-native-position
```

```ts
import { PermissionsAndroid } from 'react-native'
import { getCurrentPosition } from 'react-native-position'

const getLocation = async () => {
    const granteds = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        PermissionsAndroid.PERMISSIONS.READ_PHONE_STATE,
    ])
    if (
        granteds['android.permission.ACCESS_FINE_LOCATION'] !== 'granted' ||
        granteds['android.permission.ACCESS_COARSE_LOCATION'] !== 'granted' ||
        granteds['android.permission.READ_PHONE_STATE'] !== 'granted'
    ) {
        console.log('权限拒绝')
        return
    }

    return await getCurrentPosition({
        hmsKey: '',
        GNSStimeout: 5000,
    })
}
```

## 注意事项

1. GNSS 的坐标系是 WGS84,HMS 返回的坐标系是 GCJ02
2. 如果要在本地运行项目 `yarn install`
3. GNSS 需要`<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`和`<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`权限
4. 需要使用 `wifi` 信息, 需要 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`权限
5. 需要使用 `cell` 信息, 需要 `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`权限
6. 使用 `HMS` 服务中需要遵守[关于华为开发者联盟与隐私的声明](https://developer.huawei.com/consumer/cn/devservice/term)

## TODO

- [ ] rssi 修正
- [ ] ios 实现
- [ ] 经纬度修正

## 开发中遇到的问题

1. 移动的 sim 卡 telephonyManager.allCellInfo 中得不到信息
2. mnc < 10 时, 需要设置为 0(HMS 奇怪的配置)

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

# react-native-position

通过 `Android’s Location API` 和 `HMS Location REST API` 获取位置信息。

解决 `Google’s Location Services API` 无法使用时,也能获取位置信息。

## 优势

-   不需要集成第三方 SDK, 只需要调用 Https 请求
-   由于 HMS 不需要商业授权, 只需要按量付费([HMS 位置服务](https://developer.huawei.com/consumer/cn/doc/HMSCore-References/web-network-location-0000001051602603))([安卓定位方案的价格对比](http://www.likehide.com/blogs/android/location_server/))

## 实现原理

1. 可获取附近的 `wifi` 信息时, 通过 `HMS Location REST API` 请求位置
2. 无 `wifi` 信息时, 尝试 `Android’s Location API` 请求
3. 保底情况, 通过 `cell` 信息 `HMS Location REST API` 请求位置

## 必要准备

1. [注册华为开发者账号](https://developer.huawei.com/consumer/cn/doc/start/registration-and-verification-0000001053628148)
2. [创建项目](https://developer.huawei.com/consumer/cn/doc/app/agc-help-createproject-0000001100334664)和[创建应用](https://developer.huawei.com/consumer/cn/doc/app/agc-help-createapp-0000001146718717)
3. 获取到 `apiKey`![screenshot-20241020-051107](https://github.com/user-attachments/assets/4dd30de6-a541-46e0-9ed8-38be44b3754a)
4. `GNSS` 需要`<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`和`<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />`权限
5. `wifi` 需要 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />` 和 `<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />`权限
6. `cell` 需要 `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`权限

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
        hmsKey: 'your hms key',
        GNSStimeout: 5000,
    })
}
```

## 注意事项

1. 使用 `HMS` 服务中需要遵守[关于华为开发者联盟与隐私的声明](https://developer.huawei.com/consumer/cn/devservice/term)
2. 尽量保证 `Android 10 (API 29)` 以上
3. `mnc` < 10 时, 需要设置为 0(HMS 奇怪的配置)

## TODO

-   [ ] ios 实现
-   [ x ] 附近 wifi 或基站信息获取, 加强网络定位

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

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

`开发中`

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

# react-native-position

通过 `Android’s Location API` 和 `HMS Location REST API` 获取位置信息。
解决 `Google’s Location Services API` 无法使用时,也能获取位置信息。

## 优势

-   不需要集成第三方 SDK, 只需要调用 Http 请求
-   由于 HMS 不需要商业授权, 只需要按量付费([HMS 位置服务](https://developer.huawei.com/consumer/cn/doc/HMSCore-References/web-network-location-0000001051602603))([安卓定位方案的价格对比](http://www.likehide.com/blogs/android/location_server/))

## 实现原理

1. 在有 `wifi`连接时, 通过 `HMS Location REST API` 请求位置
2. 在无 `wifi`连接时, 尝试 `Android’s Location API` 请求
3. 保底情况, 通过 `cell`信息 `HMS Location REST API` 请求位置

## 必要准备

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

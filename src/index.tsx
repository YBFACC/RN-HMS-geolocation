import { NativeModules, Platform } from 'react-native'

const LINKING_ERROR =
    `The package 'react-native-position' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n'

const Position = NativeModules.Position
    ? NativeModules.Position
    : new Proxy(
          {},
          {
              get() {
                  throw new Error(LINKING_ERROR)
              },
          },
      )

let config = {
    hmsKey: '',
    GNSStimeout: 500,
}
type ConfigType = typeof config

export function setConfig(options: Partial<ConfigType>) {
    config = { ...config, ...options }
}

export async function getCurrentPosition(): Promise<any> {
    return await Position.getGNSS(config.GNSStimeout)
}

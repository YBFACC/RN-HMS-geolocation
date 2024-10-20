//16 进制 转 10 进制(9c:2b:a6:96:d3:e7 )
export function macTransform(mac: string): number {
    mac = mac.replace(/:/g, '')
    return parseInt(mac, 16)
}

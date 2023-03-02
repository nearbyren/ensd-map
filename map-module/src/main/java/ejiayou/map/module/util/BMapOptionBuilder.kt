package ejiayou.map.module.util

import com.baidu.location.LocationClientOption

/**
 * @author:
 * @created on: 2022/12/15 14:45
 * @description:
 */
class BMapOptionBuilder {
    lateinit var option: LocationClientOption

    fun builder(): BMapOptionBuilder {
        val builder = BMapOptionBuilder()
        builder.option = builder.initOption()
        return builder
    }

    /**
     * 设置坐标系
     *
     * @return
     * @see CoorType
     */
    fun setCoorType(): BMapOptionBuilder {
        return setCoorType(CoorType.bd09ll)
    }

    fun setCoorType(coorType: CoorType): BMapOptionBuilder {
        option.coorType = coorType.name
        return this
    }

    /**
     * 连续定位
     * 可选，设置发起定位请求的间隔，int类型，单位ms
     * 如果设置为0，则代表单次定位，即仅定位一次，默认为0
     * 如果设置非0，需设置1000ms以上才有效
     *
     * @param time
     * @return
     */
    fun setScanSpan(time: Int): BMapOptionBuilder {
        option.scanSpan = time
        return this
    }

    fun bulid(): LocationClientOption {
        return option
    }

    private fun initOption(): LocationClientOption {
        val option = LocationClientOption()
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.isOpenGps = true

        //可选，设置定位模式，默认高精度
        //定位模式
        //LocationClientOption.LocationMode.Battery_Saving; 省电模式
        //LocationClientOption.LocationMode.Device_Sensors; 仅设备(GPS)
        //LocationClientOption.LocationMode.Hight_Accuracy; 高精度模式
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setCoorType("bd09ll")

        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
//        option.setScanSpan(0)
        //是否需要地址
        option.setIsNeedAddress(true)

        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.isLocationNotify = true

        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
//        option.setIgnoreKillProcess(true);

        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(true);

        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
//        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
//        option.setEnableSimulateGps(false);
        //是否需要位置描述
        option.setIsNeedLocationDescribe(true)
        return option
    }

    /**
     * 坐标系
     */
    enum class CoorType {
        gcj02, bd09, bd09ll
    }
}
package ejiayou.map.module.util

import android.content.Context
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.orhanobut.logger.Logger
import java.io.Serializable

/**
 * @author:
 * @created on: 2022/12/15 15:06
 * @description:
 */

class BMapResultListener private constructor() : Serializable, BDAbstractLocationListener() {

    private val LOCATION_SUCCESS = 1
    private val LOCATION_FAIL = -1
    private val lock: Any = Any()
    private var callBack: BMapLocationCallBack? = null

    /**
     * 开始定位
     */
    fun locStart() {
        synchronized(lock) {
            if (!mLocationClient.isStarted) {
                mLocationClient.start()
                mLocationClient.requestLocation()
                Logger.d("定位返回结果 : locStart -------------------------------------------------------------- 开始定位 ")
            }
        }
    }

    /**
     * 停止定位
     */
    fun locStop() {
        synchronized(lock) {
            if (mLocationClient.isStarted) {
                mLocationClient.stop()
                mLocationClient.unRegisterLocationListener(this)
                Logger.d("定位返回结果 : locStop -------------------------------------------------------------- 开始结束 ")
            }
        }
    }

    fun locReStart() {
        synchronized(lock) {
            mLocationClient.restart()
            Logger.d("定位返回结果 : locReStart -------------------------------------------------------------- 重新启动 ")
        }
    }

    fun getLocationClient(): LocationClient {
        return mLocationClient
    }


    override fun onReceiveLocation(location: BDLocation) {
        Logger.d("定位返回结果 : location = $location - adCode = ${location.adCode} - city = ${location.city}  ${this.javaClass} ")
        val locType = location.locType
        var status: Int = LOCATION_SUCCESS
        if (locType != 61 && locType != 161 && locType != 66) status = LOCATION_FAIL
        val errMsg: String = getLocationResultMsg(locType)
        callBack?.onReceiveLocation(status, location, errMsg)
        locStop()
    }

    override fun onLocDiagnosticMessage(locType: Int, diagnosticType: Int, diagnosticMessage: String) {
        Logger.d("定位返回结果 : locType = $locType - diagnosticType = $diagnosticType diagnosticMessage = $diagnosticMessage")
        callBack?.onLocDiagnosticMessage(locType, diagnosticType, getLocDiagnosticMessage(locType, diagnosticType))
        super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage)
        locStop()
    }


    fun registerListener(callBack: BMapLocationCallBack) {
        this.callBack = callBack
        mLocationClient.registerLocationListener(this)
    }

    fun setLocationOption(option: LocationClientOption) {
        if (option != null) {
            if (mLocationClient.isStarted)
                mLocationClient.stop()
            mLocationClient.locOption = option
        }
    }

    fun getLocationOption(): LocationClientOption {
        return BMapOptionBuilder().builder().setCoorType().bulid()
    }

    fun unRegisterListener() {
        this.callBack = null
        mLocationClient.unRegisterLocationListener(this)
    }

    /**
     * 错误的状态码
     * <a>http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/addition-func/error-code</a>
     *
     * @param locType 当前定位类型
     * @return String 定位成功或失败的信息
     */
    private fun getLocationResultMsg(locType: Int): String {
        return when (locType) {
            61 -> "GPS定位结果，GPS定位成功"
            62 -> "无法获取有效定位依据，定位失败，请检查运营商网络或者WiFi网络是否正常开启，尝试重新请求定位"
            63 -> "网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位"
            66 -> "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果"
            67 -> "离线定位失败"
            161 -> "网络定位结果，网络定位成功"
            162 -> "请求串密文解析失败，一般是由于客户端SO文件加载失败造成，请严格参照开发指南或demo开发，放入对应SO文件"
            167 -> "服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位"
            505 -> "AK不存在或者非法，请按照说明文档重新申请AK"
            else -> ""
        }
    }

    /**
     * @param locType        当前定位类型
     * @param diagnosticType 诊断类型（1~9）
     * @return String
     */
    private fun getLocDiagnosticMessage(locType: Int, diagnosticType: Int): String? {
        return when (locType) {
            62 -> {
                when (diagnosticType) {
                    4 -> return "定位失败，无法获取任何有效定位依据"
                    5 -> return "定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位"
                    6 -> return "定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试"
                    7 -> return "定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试"
                    9 -> return "定位失败，无法获取任何有效定位依据"
                }
                if (diagnosticType == 3) return "定位失败，请您检查您的网络状态"
                when (diagnosticType) {
                    1 -> return "定位失败，建议您打开GPS"
                    2 -> return "定位失败，建议您打开Wi-Fi"
                }
                if (diagnosticType == 8) "定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限" else "未知错误"
            }
            67 -> {
                if (diagnosticType == 3) return "定位失败，请您检查您的网络状态"
                when (diagnosticType) {
                    1 -> return "定位失败，建议您打开GPS"
                    2 -> return "定位失败，建议您打开Wi-Fi"
                }
                if (diagnosticType == 8) "定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限" else "未知错误"
            }
            161 -> {
                when (diagnosticType) {
                    1 -> return "定位失败，建议您打开GPS"
                    2 -> return "定位失败，建议您打开Wi-Fi"
                }
                if (diagnosticType == 8) "定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限" else "未知错误"
            }
            167 -> {
                if (diagnosticType == 8) "定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限" else "未知错误"
            }
            else -> "未知错误"
        }
    }

    companion object {
        lateinit var mLocationClient: LocationClient
        private var mInstance: BMapResultListener? = null
            get() {
                return field ?: BMapResultListener()
            }

        @JvmStatic
        @Synchronized//添加synchronized同步锁
        fun getInstance(context: Context?): BMapResultListener {
            mLocationClient = LocationClient(context)
            return requireNotNull(mInstance)
        }
    }

    //防止单例对象在反序列化时重新生成对象
    private fun readResolve(context: Context?): Any {
        return BMapResultListener.getInstance(context)
    }
}

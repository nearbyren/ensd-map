package ejiayou.map.module.util

import com.baidu.location.BDLocation

/**
 * @author:
 * @created on: 2022/12/15 14:50
 * @description:
 */
abstract class BMapLocationCallBack {
    /**
     * 定位的结果
     *
     * @param statusCode 状态码,1:定位成功，-1定位失败
     * @param bdLocation 定位成功时返回的定位结果对象
     * @param errMsg     定位失败时的错误信息，成功时则为null
     */
    abstract fun onReceiveLocation(statusCode: Int, location: BDLocation?, errMsg: String?)

    /**
     * 错误的状态码
     * <a>http://lbsyun.baidu.com/index.php?title=android-locsdk/guide/addition-func/error-code</a>
     *
     *
     * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
     *
     * @param locType           当前定位类型
     * @param diagnosticType    诊断类型（1~9）
     * @param diagnosticMessage 具体的诊断信息释义
     */
    abstract fun onLocDiagnosticMessage(locType: Int, diagnosticType: Int, diagnosticMessage: String?)
}

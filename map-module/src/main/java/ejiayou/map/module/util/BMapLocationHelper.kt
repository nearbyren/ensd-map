package ejiayou.map.module.util

import android.content.Context

/**
 * @author:
 * @created on: 2022/12/15 14:38
 * @description:
 */
class BMapLocationHelper private constructor() {
    companion object {
        val instance: BMapLocationHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BMapLocationHelper()
        }
    }

    fun create(context: Context?, callBack: BMapLocationCallBack) {
        val listener = BMapResultListener.getInstance(context)
        listener.setLocationOption(listener.getLocationOption())
        listener.registerListener(callBack)
        listener.locStart()
    }
}

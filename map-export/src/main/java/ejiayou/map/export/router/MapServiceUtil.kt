package ejiayou.map.export.router

import com.alibaba.android.arouter.launcher.ARouter
import ejiayou.map.export.router.service.IMapService

/**
 * @author: lr
 * @created on: 2022/7/16 4:26 下午
 * @description: 提供启动activity  service 等动作
 */
open class MapServiceUtil {


    companion object {

        fun initService(): IMapService? {
            var service =
                ARouter.getInstance().build(MapRouterTable.PATH_SERVICE_MAP).navigation()
            if (service is IMapService) return service
            return null
        }
        /**
         * 拖动位置定位
         */
        fun navigateMapDragPage() {
            ARouter.getInstance().build(MapRouterTable.PATH_MAP_UI_DRAG)
                    .navigation()
        }
    }


}
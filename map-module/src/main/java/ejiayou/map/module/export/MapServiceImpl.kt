package ejiayou.map.module.export

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import ejiayou.map.export.router.MapRouterTable
import ejiayou.map.export.router.service.IMapService

@Route(path = MapRouterTable.PATH_SERVICE_MAP)
class MapServiceImpl: IMapService {

    override fun init(context: Context?) {}

}
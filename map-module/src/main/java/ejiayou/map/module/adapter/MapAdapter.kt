package ejiayou.map.module.adapter

import com.baidu.mapapi.search.core.PoiInfo
import ejiayou.map.module.R
import ejiayou.map.module.databinding.MapLocationMapItemBinding
import ejiayou.uikit.module.recyclerview.BaseBindRecyclerAdapter


/**
 * @author:
 * @created on: 2022/7/13 19:00
 * @description:
 */
class MapAdapter : BaseBindRecyclerAdapter<MapLocationMapItemBinding, PoiInfo>() {

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.map_location_map_item
    }

    override fun onBindingItem(binding: MapLocationMapItemBinding, item: PoiInfo, position: Int) {
        binding.homeMapText.text = item.address
    }


}
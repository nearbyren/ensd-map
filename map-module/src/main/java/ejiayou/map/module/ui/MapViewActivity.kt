package ejiayou.map.module.ui

/**
 * @author:
 * @created on: 2023/3/1 10:54
 * @description:
 */
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.location.BDLocation
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.geocode.*
import com.orhanobut.logger.Logger
import ejiayou.common.module.base.BaseAppBindActivity
import ejiayou.common.module.bus.BusConstants
import ejiayou.common.module.map.BMapLocationCallBack
import ejiayou.common.module.map.BMapLocationHelper
import ejiayou.common.module.signal.livebus.LiveBus
import ejiayou.common.module.ui.BarHelperConfig
import ejiayou.common.module.ui.BarOnNextListener
import ejiayou.common.module.utils.MMKVUtil
import ejiayou.common.module.utils.string
import ejiayou.map.export.router.MapRouterTable
import ejiayou.map.module.R
import ejiayou.map.module.adapter.MapAdapter
import ejiayou.map.module.databinding.MapViewActivityBinding
import ejiayou.uikit.module.recyclerview.BaseRecyclerAdapter
import ejiayou.uikit.module.recyclerview.SpaceItemDecoration


/**
 * @author:
 * @created on: 2022/10/13 15:05
 * @description:
 */
@Route(path = MapRouterTable.PATH_MAP_UI_DRAG)
class MapViewActivity : BaseAppBindActivity<MapViewActivityBinding>(),
    BaiduMap.OnMapStatusChangeListener,
    OnGetGeoCoderResultListener,
    BaiduMap.OnMapClickListener,
    BaiduMap.OnMapRenderCallback {
    private var latitude by MMKVUtil.mks.string(key = "latitude", defaultValue = "22.487379")
    private var longitude by MMKVUtil.mks.string(key = "longitude", defaultValue = "113.922152")
    private var mBaiduMap: BaiduMap? = null
    private var baiduMapView: MapView? = null
    private var mCenter: LatLng? = null
    private val mapAdapter by lazy { MapAdapter() }
    private var poiInfos = mutableListOf<PoiInfo>()
    private var mMarker: Marker? = null
    private var mGeoCoder: GeoCoder? = null

    private var mLatitude: String? = null
    private var mLongitude: String? = null
    private var mCityName: String? = null
    private var mAdCode: String? = null

    private var mapRenderFinished = false

    // 初始化全局 bitmap 信息，不用时及时 recycle
    private val mBitmap = BitmapDescriptorFactory.fromResource(R.drawable.map_icon_drag)


    override fun layoutRes(): Int {
        return R.layout.map_view_activity
    }

    private fun savePosition() {
        MMKVUtil.encode("is_dummy", "1")
        mCityName?.let {
            MMKVUtil.encode("dummy_cityName", it)
        }
        mAdCode?.let {
            MMKVUtil.encode("dummy_adCode", it)
        }
        mLongitude?.let {
            MMKVUtil.encode("dummy_longitude", it)
        }
        mLatitude?.let {
            MMKVUtil.encode("dummy_latitude", it)
        }
        LiveBus.get(BusConstants.INDEX_LOGIN_SUCCESS).post(BusConstants.INDEX_LOGIN_SUCCESS)
        finish()
    }

    override fun initBarHelperConfig(): BarHelperConfig {
        return BarHelperConfig.builder().setBack(true)
                .setTitle("拖动选择坐标", titleColor = R.color.ensd_main_text, titleBold = true)
                .setRightText("确定定位")
                .setOnNextListener(object : BarOnNextListener {
                    override fun onNextClick() {
                        Logger.d("Map -> 确定定位")
                        savePosition()
                    }
                })
                .setBgColor(R.color.app_content_bg).build()
    }

    private fun startMap() {
        BMapLocationHelper.instance.create(this, object : BMapLocationCallBack() {
            override fun onLocDiagnosticMessage(locType: Int, diagnosticType: Int, diagnosticMessage: String?) {
                Logger.d("Map -> onLocDiagnosticMessage locType = $locType diagnosticType = $diagnosticType diagnosticMessage = $diagnosticMessage")
            }

            override fun onReceiveLocation(statusCode: Int, location: BDLocation?, errMsg: String?) {
                Logger.d("Map -> onReceiveLocation statusCode = $statusCode location = $location errMsg = $errMsg")
                location?.let {
                    location(it)
                }
            }

        })
    }

    private fun initMapView() {
        //初始化配置禁止缩放控制是他
        val options = BaiduMapOptions()
        options.zoomControlsEnabled(false)
        options.scaleControlEnabled(false)
        baiduMapView = MapView(this, options)
        //已启用滚动手势
        mBaiduMap?.uiSettings?.isScrollGesturesEnabled = true
        //已启用缩放手势
        mBaiduMap?.uiSettings?.isZoomGesturesEnabled = true
        //设置当前位置跟随模式
        mBaiduMap?.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null))
        //隐藏logo
        val child = baiduMapView?.getChildAt(1)
        if (child != null && (child is ImageView)) {
            child.isVisible = false
        }
        //动态添加mapView地图
        binding.mapLl.addView(baiduMapView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        mBaiduMap = baiduMapView?.map
        if (latitude!!.isNotEmpty() && longitude!!.isNotEmpty()) {
            mCenter = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        }
    }


    private fun initPositionAdapter() {
        mapAdapter.setItems(poiInfos)
        binding.homeRecycle.adapter = mapAdapter
        binding.homeRecycle.layoutManager = LinearLayoutManager(this)
        val spaceItemDecoration = SpaceItemDecoration(0, 10, 15)
        binding.homeRecycle.addItemDecoration(spaceItemDecoration)
        binding.homeRecycle.setHasFixedSize(true)
        mapAdapter.setOnItemClickListener(listener = object :
            BaseRecyclerAdapter.OnItemClickListener<PoiInfo> {
            override fun onItemClick(holder: Any, item: PoiInfo, position: Int) {
                item.let {
                    val latitude = it.location.latitude
                    val longitude = it.location.longitude
                    val city = it.city ?: "深圳市"
                    val adCode = it.adCode
                    MMKVUtil.encode("cityName", city)
                    MMKVUtil.encode("longitude", longitude)
                    MMKVUtil.encode("latitude", latitude)
                    MMKVUtil.encode("adCode", adCode)
                }
                mStatusChangeByItemClick = true
                val mapStatusUpdate = MapStatusUpdateFactory.newLatLng(item.getLocation())
                mBaiduMap?.setMapStatus(mapStatusUpdate)
            }
        })
    }

    override fun initialize(savedInstanceState: Bundle?) {
        initMapView()
        initPositionAdapter()
        startMap()
        binding.homeIvCurrent.setOnClickListener {
            startMap()
        }
    }

    override fun onPause() {
        super.onPause()
        baiduMapView?.onPause()


    }

    override fun onResume() {
        super.onResume()
        baiduMapView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMarker = null
        mBitmap.recycle()
        baiduMapView?.onDestroy()
        binding.mapLl.removeAllViews()
    }

    /**
     * 创建地图中心点marker
     */
    private fun createMaker(point: LatLng?) {
        mCenter = point
        if (mMarker == null) {
            val markerOptions = MarkerOptions().position(point).icon(mBitmap).zIndex(9)
            mMarker = mBaiduMap?.addOverlay(markerOptions) as Marker
        }
        mMarker?.let {
            it.position = point
            it.icon = mBitmap
        }
    }

    private fun location(location: BDLocation) {
        mBaiduMap?.let {
            Logger.d("Map -> 定位成功")
            mCenter = LatLng(location.latitude, location.longitude)
            val locData = MyLocationData.Builder()
                    .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.direction).latitude(location.latitude)
                    .longitude(location.longitude).build()
            it.setMyLocationData(locData)
            val mapStatusUpdate =
                MapStatusUpdateFactory.newLatLngZoom(mCenter, 16f)
            it.setMapStatus(mapStatusUpdate)
            //设置地图状态监听
            it.setOnMapStatusChangeListener(this)
            //设置地图单击事件监听
            it.setOnMapClickListener(this)
            //地图加载完成回调接口：
            it.setOnMapRenderCallbadk(this)
        }
    }

    /**
     * 逆地理编码请求
     *
     * @param latLng
     */
    private fun reverseRequest(latLng: LatLng) {
        val reverseGeoCodeOption = ReverseGeoCodeOption().location(latLng)
                .newVersion(1) // 建议请求新版数据
                .radius(500)
        if (null == mGeoCoder) {
            mGeoCoder = GeoCoder.newInstance()
            mGeoCoder?.setOnGetGeoCodeResultListener(this)
        }
        mGeoCoder?.reverseGeoCode(reverseGeoCodeOption)
    }


    /********************************************地图状态改变接口*******************************************************/
    private var mStatusChangeByItemClick = false

    /**
     * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
     *
     * @param mapStatus 地图状态改变开始时的地图状态
     */
    override fun onMapStatusChangeStart(mapStatus: MapStatus?) {
    }

    /**
     * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
     *
     * @param mapStatus 地图状态改变开始时的地图状态
     *
     * @param reason 地图状态改变的原因
     */

    //用户手势触发导致的地图状态改变,比如双击、拖拽、滑动底图
    //int REASON_GESTURE = 1;
    //SDK导致的地图状态改变, 比如点击缩放控件、指南针图标
    //int REASON_API_ANIMATION = 2;
    //开发者调用,导致的地图状态改变
    //int REASON_DEVELOPER_ANIMATION = 3;
    override fun onMapStatusChangeStart(mapStatus: MapStatus?, reason: Int) {
    }

    /**
     * 地图状态变化中
     *
     * @param mapStatus 当前地图状态
     */
    override fun onMapStatusChange(mapStatus: MapStatus?) {
    }

    /**
     * 地图状态改变结束
     *
     * @param mapStatus 地图状态改变结束后的地图状态
     */
    override fun onMapStatusChangeFinish(mapStatus: MapStatus?) {

    }


    /********************************************地图状态改变接口*******************************************************/


    /********************************************地理编码/反地理编码结果*******************************************************/
    override fun onGetGeoCodeResult(geoCodeResult: GeoCodeResult?) {
    }

    override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
        if (null == reverseGeoCodeResult) {
            return
        }
        reverseGeoCodeResult.location?.let {
            mLongitude = it.longitude.toString()
            mLatitude = it.latitude.toString()
            mAdCode = reverseGeoCodeResult.adcode.toString()
            mCityName = reverseGeoCodeResult.addressDetail.city
            mCenter = LatLng(mLatitude!!.toDouble(), mLongitude!!.toDouble())
            Logger.d("Map -> onGetReverseGeoCodeResult $mCenter mAdCode = $mAdCode - mCityName = $mCityName")
        }
    }


    override fun onMapClick(point: LatLng?) {
        Logger.d("Map -> onMapClick $point")
        point?.let {
            createMaker(it)
            reverseRequest(it)
        }
    }

    override fun onMapPoiClick(mapPoi: MapPoi?) {
        mapPoi?.let {
            Logger.d("Map -> onMapPoiClick ${it.position}")
            createMaker(it.position)
            reverseRequest(it.position)
        }
    }

    override fun onMapRenderFinished() {
        Logger.d("Map -> onMapRenderFinished $mCenter mapRenderFinished = $mapRenderFinished")
        mCenter?.let {
            if (!mapRenderFinished) {
                mapRenderFinished = true
                createMaker(it)
                reverseRequest(it)
            }
        }
    }
    /********************************************地理编码/反地理编码结果*******************************************************/

}
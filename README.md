# SwiftPullToRefresh
支持nestedScroll的下拉刷新库

使用方式通SwipeRefreshLayout一致，

通过enableFloat控制是否为普通效果或者是谷歌官方的那种悬浮效果

* 相较官方的SwipeRefreshLayout的改进点：
  * 支持普通模式和悬浮模式
  * 支持自定义刷新头部
  * 支持多点触控（在非实现nestedScrollChild接口的子view时官方的SwipeRefreshLayout多点触控有问题）
  
* 不支持的东西：
  * 不支持上拉加载更多，此功能实现较为简单。
  * 不支持官方的autoRefresh，后面考虑加入;
  
* 使用时需要注意的地方：
  * 如果使用nestedScrollChild接口的子view时,类如recycleView内部有viewPage，则需要改写recycleView拦截时x>y的问题，不需要更改SwiftPullToRefresh,
  因为此时不走SwiftPullToRefresh的onTouch事件
  * 如果使用没有实现nestedScrollChild接口的子view时,类如recycleView内部有viewPage，则需要改写SwiftPullToRefresh拦截时x>y的问题，或者是子控件去实现nestedScrollChild接口。

import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util
import java.util.{Locale, Properties, Date}

import com.alibaba.fastjson.{JSONArray, JSONObject, JSON}
import com.xuehai.utils.{DateUtil, Constants, ESUtils, Utils}
import org.elasticsearch.action.search.{SearchResponse, SearchRequest}
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
/**
  * Created by root on 2019/10/30.
  */
object test1 extends Constants {

	def main(args: Array[String]) {
		val strError = "{\"BOARD\":\"MSM8916\",\"IS_SYSTEM_SECURE\":\"false\",\"sdcardTotalSpaceSize\":\"11257\",\"CPU_ABI2\":\"armeabi\",\"HOST\":\"SWDG5220\",\"versionName\":\"v1.23.6.20200316\",\"SUPPORTED_64_BIT_ABIS\":\"[Ljava.lang.String;@f3d5330\",\"deviceId\":\"R22K90092WH\",\"CPU_ABI\":\"armeabi-v7a\",\"FINGERPRINT\":\"samsung/gt5note8ltezc/gt5note8ltechn:6.0.1/MMB29M/P355CZCU3BRK1:user/release-keys\",\"PRODUCT\":\"gt5note8ltezc\",\"X-B3-TraceId\":\"000000000001e4250000017273da2fde\",\"ID\":\"MMB29M\",\"dnsDuration\":\"-1\",\"TYPE\":\"user\",\"sdcardAvailableSpaceSize\":\"4846\",\"DEVICE\":\"gt5note8ltechn\",\"isLastFail\":\"true\",\"IS_SECURE\":\"false\",\"BRAND\":\"samsung\",\"responseHeaders\":\"response is null\",\"requestHeaders\":[\"UserId: 123941\",\"SchoolId: 4847\",\"User-Agent: com.xh.arespunc/v1.23.6.20200316 (SM-P355C; android; 6.0.1; R22K90092WH)\",\"Content-Type: application/json; charset:UTF-8\",\"X-B3-SpanId: e37daee64203f6cb\",\"X-B3-TraceId: 000000000001e4250000017273da2fde\"],\"SUPPORTED_32_BIT_ABIS\":\"[Ljava.lang.String;@8b68873\",\"BOOTLOADER\":\"P355CZCU3BRK1\",\"tinkerPatchVersion\":\"3\",\"recentActivityStack\":\"[com.xh.arespunc.launcher.view.LauncherActivity@adf92b4e->com.xh.arespunc.chat.view.ChatDetailActivity@73507b69->com.xh.arespunc.imagepreview.view.ImagePreviewActivity@fbd9fd10->]\",\"xhcoreVersionName\":\"3.7.1.1\",\"TAGS\":\"release-keys\",\"httpStatusCode\":\"-1\",\"deviceMacAddress\":\"A8:51:5B:F4:A6:2A\",\"logType\":\"ERROR\",\"IS_TRANSLATION_ASSISTANT_ENABLED\":\"false\",\"Description\":\"域名解析失败！\",\"responseBody\":\"\",\"requestMethod\":\"GET\",\"crashHappenTime\":\"1591081586179\",\"connectionDuration\":\"-1\",\"requestHost\":\"response.yunzuoye.net\",\"FOTA_INFO\":\"1542709839\",\"SUPPORTED_ABIS\":\"[Ljava.lang.String;@3953ba9\",\"DISPLAY\":\"MMB29M.P355CZCU3BRK1\",\"requestBody\":\"null\",\"schoolId\":\"4847\",\"isOSUpgradeKK2LL\":\"false\",\"packageName\":\"com.xh.arespunc\",\"SSID\":\"\\\"YKSZ-XH\\\"\",\"SERIAL\":\"ace04804\",\"remoteIp\":\"223.94.85.17\",\"TIME\":\"1542709280000\",\"MODEL\":\"SM-P355C\",\"httpEventMessage\":[\"1591081578378 71:0.000-callStart;4.304-dnsStart;7.472-callFailed;\"],\"USER\":\"dpi\",\"MANUFACTURER\":\"samsung\",\"userId\":123941,\"url\":\"https://response.yunzuoye.net/api/v1/users/123941/messages/unread?version=1589965512113&sign=a4ae6ac6a07a52da62471994ec13b072&t=1591081578378\",\"versionCode\":\"216\",\"RSSI\":\"-48\",\"totalMemory\":\"1891\",\"BSSID\":\"70:3a:73:85:ed:f6\",\"availableMemory\":\"968\",\"IS_DEBUGGABLE\":\"false\",\"HARDWARE\":\"qcom\",\"Frequency\":\"5.8\",\"RADIO\":\"unknown\",\"ErrorCode\":\"107000006\",\"UNKNOWN\":\"unknown\",\"TAG\":\"Build\",\"ErrorMessage\":[\"append error message: java.net.UnknownHostException: com.xh.xhcore.common.http.dns.BootstrapDNS@da31ad returned no addresses for response.yunzuoye.net\",\"\\tat okhttp3.internal.connection.RouteSelector.resetNextInetSocketAddress(RouteSelector.java:187)\",\"\\tat okhttp3.internal.connection.RouteSelector.nextProxy(RouteSelector.java:149)\",\"\\tat okhttp3.internal.connection.RouteSelector.next(RouteSelector.java:84)\",\"\\tat okhttp3.internal.connection.StreamAllocation.findConnection(StreamAllocation.java:214)\",\"\\tat okhttp3.internal.connection.StreamAllocation.findHealthyConnection(StreamAllocation.java:135)\",\"\\tat okhttp3.internal.connection.StreamAllocation.newStream(StreamAllocation.java:114)\",\"\\tat okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.java:42)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.java:93)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.java:93)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.java:126)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat okhttp3.logging.HttpLoggingInterceptor.intercept(HttpLoggingInterceptor.java:213)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat com.xh.xhcore.common.http.dns.DNSStateRetryInterceptor.intercept(DNSStateRetryInterceptor.kt:27)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat com.xh.xhcore.common.http.strategy.okhttp.interceptors.RedirectInterceptor.intercept(RedirectInterceptor.kt:13)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:147)\",\"\\tat okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:121)\",\"\\tat okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.java:200)\",\"\\tat okhttp3.RealCall$AsyncCall.execute(RealCall.java:147)\",\"\\tat okhttp3.internal.NamedRunnable.run(NamedRunnable.java:32)\",\"\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113)\",\"\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)\",\"\\tat java.lang.Thread.run(Thread.java:818)\",\"append error message: java.lang.Throwable\",\"\\tat com.xh.xhcore.common.http.strategy.xh.request.XHRequestOkHttpProxy$XHRequestCallbackInC.failedCallBack(XHRequestOkHttpProxy.java:293)\",\"\\tat com.xh.xhcore.common.http.strategy.xh.request.XHRequestOkHttpProxy$XHRequestCallbackInC.executeRestfulCallBack(XHRequestOkHttpProxy.java:277)\",\"\\tat com.xh.xhcore.common.http.strategy.xh.request.XHRequestOkHttpProxy$XHRequestCallbackInC$1.run(XHRequestOkHttpProxy.java:261)\",\"\\tat android.os.Handler.handleCallback(Handler.java:739)\",\"\\tat android.os.Handler.dispatchMessage(Handler.java:95)\",\"\\tat com.xh.logutils.FireLooper.run(FireLooper.java:76)\",\"\\tat android.os.Handler.handleCallback(Handler.java:739)\",\"\\tat android.os.Handler.dispatchMessage(Handler.java:95)\",\"\\tat android.os.Looper.loop(Looper.java:148)\",\"\\tat android.app.ActivityThread.main(ActivityThread.java:7325)\",\"\\tat java.lang.reflect.Method.invoke(Native Method)\",\"\\tat com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1321)\",\"\\tat com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1211)\"],\"recentActivityListString\":\"[com.xh.arespunc.launcher.view.LauncherActivity@adf92b4e->com.xh.arespunc.main.view.MainActivity@5fad1e7e->com.xh.arespunc.chat.view.ChatDetailActivity@68e6b73e->com.xh.arespunc.imagepreview.view.ImagePreviewActivity@f4164cbe->com.xh.arespunc.chat.view.ChatDetailActivity@68e6b73e->com.xh.arespunc.main.view.MainActivity@5fad1e7e->com.xh.arespunc.chat.view.ChatDetailActivity@6bbca99c->com.xh.arespunc.main.view.MainActivity@5fad1e7e->com.xh.arespunc.chat.view.ChatDetailActivity@70a5aa01->com.xh.arespunc.imagepreview.view.ImagePreviewActivity@f7948cb6->com.xh.arespunc.chat.view.ChatDetailActivity@70a5aa01->com.xh.arespunc.imagepreview.view.ImagePreviewActivity@f9e68911->com.xh.arespunc.chat.view.ChatDetailActivity@70a5aa01->com.xh.arespunc.imagepreview.view.ImagePreviewActivity@f616e2d0->com.xh.arespunc.chat.view.ChatDetailActivity@70a5aa01->]\"}"
		val strCrash = "{\"@version\":\"1\",\"host\":\"crash-jvm-01\",\"path\":\"/opt/xuehaiserver/log/crash.log\",\"@timestamp\":\"2020-06-05T07:27:05.855Z\",\"message\":\"{\\\"BOARD\\\":\\\"MSM8916\\\",\\\"IS_SYSTEM_SECURE\\\":\\\"false\\\",\\\"sdcardTotalSpaceSize\\\":\\\"11257MB\\\",\\\"CPU_ABI2\\\":\\\"armeabi\\\",\\\"HOST\\\":\\\"SWDG5220\\\",\\\"versionName\\\":\\\"v3.14.06.20190926\\\",\\\"SUPPORTED_64_BIT_ABIS\\\":\\\"[Ljava.lang.String;@2d9d08a\\\",\\\"deviceId\\\":\\\"R22J8008WHZ\\\",\\\"CPU_ABI\\\":\\\"armeabi-v7a\\\",\\\"FINGERPRINT\\\":\\\"samsung/gt5note8ltezc/gt5note8ltechn:6.0.1/MMB29M/P355CZCU3BRK1:user/release-keys\\\",\\\"PRODUCT\\\":\\\"gt5note8ltezc\\\",\\\"X-B3-TraceId\\\":\\\"\\\",\\\"ID\\\":\\\"MMB29M\\\",\\\"TYPE\\\":\\\"user\\\",\\\"connectedWifiInfo\\\":\\\"SSID: NYZX-XH, BSSID: d4:68:ba:87:78:93, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, RSSI: -51, Link speed: 72Mbps, Frequency: 5745MHz, Net ID: 0, Metered hint: false, score: 60\\\",\\\"sdcardAvailableSpaceSize\\\":\\\"5615MB\\\",\\\"DEVICE\\\":\\\"gt5note8ltechn\\\",\\\"isLastFail\\\":\\\"true\\\",\\\"IS_SECURE\\\":\\\"false\\\",\\\"BRAND\\\":\\\"samsung\\\",\\\"requestHeaders\\\":\\\"request is null\\\",\\\"SUPPORTED_32_BIT_ABIS\\\":\\\"[Ljava.lang.String;@e8f9af5\\\",\\\"BOOTLOADER\\\":\\\"P355CZCU3BRK1\\\",\\\"xhcoreVersionName\\\":\\\"3.4.13\\\",\\\"TAGS\\\":\\\"release-keys\\\",\\\"logType\\\":\\\"ERROR\\\",\\\"IS_TRANSLATION_ASSISTANT_ENABLED\\\":\\\"false\\\",\\\"Description\\\":\\\"微服务未获取到ip\\\",\\\"requestMethod\\\":\\\"GET\\\",\\\"crashHappenTime\\\":\\\"1591336872084\\\",\\\"FOTA_INFO\\\":\\\"1542709839\\\",\\\"SUPPORTED_ABIS\\\":\\\"[Ljava.lang.String;@90406fb\\\",\\\"DISPLAY\\\":\\\"MMB29M.P355CZCU3BRK1\\\",\\\"requestBody\\\":\\\"requestBodyString: null requestBodyMediaTypeString: null\\\",\\\"schoolId\\\":\\\"4181\\\",\\\"isOSUpgradeKK2LL\\\":\\\"false\\\",\\\"packageName\\\":\\\"com.xh.aklestu\\\",\\\"SERIAL\\\":\\\"de9a4ead\\\",\\\"remoteIp\\\":\\\"60.190.128.47\\\",\\\"TIME\\\":\\\"1542709280000\\\",\\\"MODEL\\\":\\\"SM-P355C\\\",\\\"USER\\\":\\\"dpi\\\",\\\"MANUFACTURER\\\":\\\"samsung\\\",\\\"userId\\\":64434,\\\"url\\\":\\\"\\\",\\\"versionCode\\\":\\\"315\\\",\\\"totalMemory\\\":\\\"1891MB\\\",\\\"availableMemory\\\":\\\"1042MB\\\",\\\"IS_DEBUGGABLE\\\":\\\"false\\\",\\\"HARDWARE\\\":\\\"qcom\\\",\\\"RADIO\\\":\\\"unknown\\\",\\\"ErrorCode\\\":\\\"107001206\\\",\\\"UNKNOWN\\\":\\\"unknown\\\",\\\"TAG\\\":\\\"Build\\\",\\\"ErrorMessage\\\":[\\\"append error message: java.lang.Throwable\\\",\\\"\\\\tat com.xh.xhcore.common.http.strategy.xh.request.XHRequestOkHttpProxy$1.run(XHRequestOkHttpProxy.java:128)\\\",\\\"\\\\tat android.os.Handler.handleCallback(Handler.java:739)\\\",\\\"\\\\tat android.os.Handler.dispatchMessage(Handler.java:95)\\\",\\\"\\\\tat com.xh.logutils.FireLooper.run(FireLooper.java:76)\\\",\\\"\\\\tat android.os.Handler.handleCallback(Handler.java:739)\\\",\\\"\\\\tat android.os.Handler.dispatchMessage(Handler.java:95)\\\",\\\"\\\\tat android.os.Looper.loop(Looper.java:148)\\\",\\\"\\\\tat android.app.ActivityThread.main(ActivityThread.java:7325)\\\",\\\"\\\\tat java.lang.reflect.Method.invoke(Native Method)\\\",\\\"\\\\tat com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1321)\\\",\\\"\\\\tat com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1211)\\\"]}\",\"type\":\"assist-service\"}"

		val json = assistStrFunc(strCrash)

	}


	private def assistStrFunc(assistStr: String): JSONObject = {
		var jsonOrange: JSONObject = null
		if(null == JSON.parseObject(assistStr).getString("logType")){
 			jsonOrange = JSON.parseObject(JSON.parseObject(assistStr).getString("message"))
		}else{
			jsonOrange = JSON.parseObject(assistStr)
		}
		val json = JSON.parseObject("{}")

		if (jsonOrange.getString("logType") == "ERROR" || jsonOrange.getString("logType") == "CRASH") {
			json.put("logType", Utils.null2Str(jsonOrange.getString("logType")))
			json.put("traceId", Utils.null2Str(jsonOrange.getString("X-B3-TraceId")))
			try{
				json.put("spandId", Utils.null2Str(jsonOrange.getString("requestHeaders").split("X-B3-SpanId:")(1).split("\"")(0).replaceAll(" ", "")))
			}catch {case e: Exception => {
				// println("解析spandId异常", assistStr)
			}}

			json.put("schoolId", Utils.null2Str(jsonOrange.getString("schoolId")).replaceAll("null", ""))
			json.put("userId", Utils.null2Str(jsonOrange.getString("userId")))
			json.put("deviceId", Utils.null2Str(jsonOrange.getString("deviceId")))
			json.put("packageName", Utils.null2Str(jsonOrange.getString("packageName")))
			json.put("appVersionName", Utils.null2Str(jsonOrange.getString("versionName")))
			json.put("appVersionCode", Utils.null2Str(jsonOrange.getString("versionCode")))
			json.put("remoteIp", Utils.null2Str(jsonOrange.getString("remoteIp")))
			json.put("errorCode", Utils.null2Str(jsonOrange.getString("ErrorCode")))
			json.put("model", Utils.null2Str(jsonOrange.getString("MODEL")))
			json.put("availableMemory", Utils.null2Double(jsonOrange.getString("availableMemory")))
			json.put("totalMemory", Utils.null2Double(jsonOrange.getString("totalMemory")))
			json.put("sdcardAvailableSpaceSize", Utils.null2Double(jsonOrange.getString("sdcardAvailableSpaceSize")))
			json.put("sdcardTotalSpaceSize", Utils.null2Double(jsonOrange.getString("sdcardTotalSpaceSize")))
			json.put("connectionDuration", Utils.null2Double(jsonOrange.getString("connectionDuration")))
			json.put("dnsDuration", Utils.null2Double(jsonOrange.getString("dnsDuration")))
			json.put("requestMethod", Utils.null2Str(jsonOrange.getString("requestMethod")))
			json.put("assistMessage", Utils.null2Str(jsonOrange.getString("ErrorMessage")))
			json.put("SSID", Utils.null2Str(jsonOrange.getString("SSID")))
			json.put("BSSID", Utils.null2Str(jsonOrange.getString("BSSID")))
			json.put("requestLogScope", "1")
			json.put("processStatus", "0")


			//解决没有crashHappenTime
			if(null != jsonOrange.getLong("crashHappenTime")){
				json.put("assitCreateTime", jsonOrange.getLong("crashHappenTime"))
			}else{
				json.put("assitCreateTime", 0L)
			}
			json.put("@timestamp", json.getDate("assitCreateTime")) //Date类型

			if(jsonOrange.getString("logType") == "CRASH"){
				json.put("errorMessage", Utils.null2Str(jsonOrange.getString("USER")))
				json.put("errorMessageHashCode", Utils.hashMD5(Utils.null2Str(jsonOrange.getString("USER"))))
			}

			try{
				val url = jsonOrange.getString("url").split("\\?").head
				json.put("requestUrl", url)
				val uri = (url + "/").replaceAll("http.*://[^/]*/", "").replaceAll("/[0-9]+/", "/-/").replaceAll("/[0-9]+/", "/-/").replaceAll(".*/api/", "api/").replaceAll("/$", "")
				json.put("requestUri", uri)
			}catch {
				case e:Exception => {
					json.put("requestUrl", "")
					json.put("requestUri", "")
				}
			}
		}

		json
	}



}

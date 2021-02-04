//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.{TimeZone, Date, Locale, Arrays}
//import com.alibaba.fastjson.{JSONArray, JSON, JSONObject}
//import com.xuehai.utils._
//import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
//import org.elasticsearch.action.index.IndexRequest
//import org.elasticsearch.action.search.{ClearScrollRequest, SearchScrollRequest, SearchResponse, SearchRequest}
//import org.elasticsearch.action.update.UpdateRequest
//import org.elasticsearch.common.unit.TimeValue
//import org.elasticsearch.index.query.QueryBuilders
//import org.elasticsearch.search.{Scroll, SearchHit}
//import org.elasticsearch.search.builder.SearchSourceBuilder
//import scala.collection.JavaConverters._
//import scala.collection.mutable.ListBuffer
//
///**
//  * Created by root on 2019/10/16.
//  */
//object AppMain extends Constants{
//  def main(args: Array[String]) {
//
//    //		val list = List("xhjvm-2019.12.06", "xhjvm-2019.12.07", "xhjvm-2019.12.08")
//    //补数据
//    //		list.foreach(index => {
//    //			try{
//    //				AddDataUtil.addDataByDay(index, jvmType, "logType", "ERROR")
//    //			}catch {
//    //				case e: Exception => e.printStackTrace()
//    //			}
//    //		})
//
//    // 表关联
//    //		val list = List("xh2_assist_union_2019.12.06", "xh2_assist_union_2019.12.07", "xh2_assist_union_2019.12.08")
//    //		list.foreach(index => {
//    //			try{
//    //				AddDataUtil.joinDataByDay(index, assistNginxType, "logType", "ERROR")
//    //			}catch {
//    //				case e: Exception => e.printStackTrace()
//    //			}
//    //		})
//
//
//
//    val index = args(0)
//    val kafkaConsumer = new KafkaConsumer[String, String](kafkaParams.asJava)
//
//    if(index == "assist"){
//      kafkaConsumer.subscribe(Arrays.asList(assistTopic))
//    }else if(index == "nginx"){
//      kafkaConsumer.subscribe(Arrays.asList(nginxTopic))
//    }else if(index == "jvm"){
//      kafkaConsumer.subscribe(Arrays.asList(xhjvmTopic))
//    }else{
//      println("参数出错：%s".format(index))
//      System.exit(0)
//    }
//
//    println("starting.......")
//    while(true){
//      try{
//        if(index == "assist"){
//          assistStart(kafkaConsumer.poll(3000))
//        }else if(index == "nginx"){
//          nginxStart(kafkaConsumer.poll(3000))
//        }else if(index == "jvm"){
//          xhjvmStart(kafkaConsumer.poll(3000))
//        }
//      }catch {
//        case e: IOException => {e.printStackTrace()}
//        case e: Exception => {
//          // java.lang.OutOfMemoryError
//          e.printStackTrace()
//          Utils.dingDingRobot("all", "full link: %s, %s".format(index, e.getMessage))
//        }
//      }
//    }
//
//  }
//
//  def assistStart(records: ConsumerRecords[String, String]): Unit ={
//    try{
//      //println("assist:" + records.count().toString)
//
//      val assistRequestList: ListBuffer[IndexRequest] = ListBuffer[IndexRequest]()
//
//      import scala.collection.JavaConversions._
//      for (record <- records) {
//        try{
//          val assistStr: String = record.value
//
//          val assistJson: JSONObject = assistStrFunc(assistStr) //只存储错误日志、崩溃日志
//
//          if(assistJson.size()>0){
//            val timeStr = DateUtil.long2DateStr(assistJson.getLong("assitCreateTime"))
//
//            val insertRequest = ESUtils.insertRequestFunc(assistNginxIndex+timeStr, assistNginxType, assistJson)
//            assistRequestList.append(insertRequest)
//          }
//        }catch {
//          case e: NullPointerException => {println(record.value)}
//          case e: Exception => {
//            println(record.value)
//            e.printStackTrace()
//          }
//        }
//      }
//
//      // 1;初始化宽表assist-nginx
//      if (assistRequestList.nonEmpty) ESUtils.bulkInsertRequestFunc(assistRequestList)
//
//
//      // 2;更新两张宽表数据
//      val time = (System.currentTimeMillis() - 1000*60*30).toString
//      val timeStr = DateUtil.long2DateStr(time.toLong)
//      assistNginxSearchScrollRangeFunc(assistNginxIndex+timeStr, assistNginxType, "assitCreateTime", time, "requestLogScope", "1")
//      assistJvmSearchScrollRangeFunc(assistNginxIndex+timeStr, assistNginxType, "assitCreateTime", time, "requestLogScope", "2")
//    }catch {
//      case e: Exception => {
//        e.printStackTrace()
//      }
//    }
//  }
//
//  def nginxStart(records: ConsumerRecords[String, String]): Unit ={
//    try{
//      val nginxRequestList: ListBuffer[IndexRequest] = ListBuffer[IndexRequest]()
//
//      import scala.collection.JavaConversions._
//      for (record <- records) {
//        try{
//          val nginxStr: String = record.value
//
//          println(nginxStr)
//
//          val nginxJson: JSONObject = nginxParse(nginxStr)
//
//          if(nginxJson.size()>0){
//            val timeStr = DateUtil.long2DateStr(nginxJson.getDate("nginxCreateTime").getTime)
//            val insertRequest = ESUtils.insertRequestFunc(nginxDetailIndex+timeStr, nginxDetailType, nginxJson)
//
//            nginxRequestList.append(insertRequest)
//          }
//        }catch {
//          case e: NullPointerException => {}
//          case e: Exception => {
//            println(record.value)
//            e.printStackTrace()
//          }
//        }
//      }
//
//      // 1;初始化宽表nginx
//      if (nginxRequestList.nonEmpty) ESUtils.bulkInsertRequestFunc(nginxRequestList)
//    }catch {
//      case e: Exception => {
//        e.printStackTrace()
//      }
//    }
//  }
//
//  def nginxParse(nginxStr: String): JSONObject ={
//    // nginx
//    val nginxJson: JSONObject = JSON.parseObject("{}")
//
//    if(null != nginxStr && nginxStr.size>3){
//
//      val nginxStr1 = nginxStr.replace("\"","").replaceAll("com.*\\)", "-")
//      //val nginxStr1 = nginxStr.replaceAll("\".*\"", "-")
//      val nginxArray = nginxStr1.split(" ")
//
//      var responseCode = nginxArray(10)
//      if(responseCode.contains(".")) responseCode = nginxArray(11)
//
//      if(responseCode.startsWith("4") || responseCode.startsWith("5")){ //只存储4xx或者5xx的数据
//        val nginxCreateTime = nginxArray(2) + " " + nginxArray(3)
//        val requestTime = nginxArray(8).toFloat
//        val traceId = nginxArray.last
//        val userId = nginxArray(16)
//        val schoolId = nginxArray(17)
//
//        try{
//          val requestUri = (nginxArray(6).split("\\?").head + "/").replaceAll("/[0-9]+/", "/-/").replaceAll("/[0-9]+/", "/-/").replaceAll(".*/api/", "api/").replaceAll("/$", "")
//          nginxJson.put("requestUri", requestUri)
//        }catch {
//          case e:Exception => nginxJson.put("requestUri", "")
//        }
//
//        nginxJson.put("traceId", traceId)
//        nginxJson.put("schoolId", schoolId)
//        nginxJson.put("userId", userId)
//
//        try{
//          val nginxStr2 = nginxStr.split("\"")(1)
//
//          if(nginxStr2.startsWith("com")){
//            val deviceId = nginxStr2.split(" ").last.replace(")", "")
//            val packageName = nginxStr2.split("/").head
//            nginxJson.put("deviceId", deviceId)
//            nginxJson.put("packageName", packageName)
//          }else{
//            nginxJson.put("deviceId", "")
//            nginxJson.put("packageName", "")
//          }
//        }catch {
//          case e: Exception => {
//            nginxJson.put("deviceId", "")
//            nginxJson.put("packageName", "")
//          }
//        }
//
//        try{
//          val date = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss",Locale.ENGLISH).parse(nginxCreateTime)
//
//          nginxJson.put("nginxCreateTime", date)
//          nginxJson.put("@timestamp", date)
//        }catch {
//          case e: Exception => {
//            nginxJson.put("nginxCreateTime",  null)
//            nginxJson.put("@timestamp", null)
//          }
//        }
//
//        nginxJson.put("requestTime", requestTime*1000)
//        nginxJson.put("responseCode", responseCode)
//        nginxJson.put("assitCreateTime", "")
//        nginxJson.put("errorCode", "")
//        nginxJson.put("assistMessage", "")
//        nginxJson.put("nginxMessage", nginxStr)
//      }
//    }
//
//    nginxJson
//  }
//
//  def xhjvmStart(records: ConsumerRecords[String, String]): Unit ={
//    try{
//      val xhjvmRequestList: ListBuffer[IndexRequest] = ListBuffer[IndexRequest]()
//
//      import scala.collection.JavaConversions._
//      for (record <- records) {
//        try{
//          val xhjvmStr: String = record.value
//
//          println(xhjvmStr)
//
//          val xhjvmJson: JSONObject = xhjvmParse(xhjvmStr)
//
//          if(xhjvmJson.size()>0){
//            val timeStr = DateUtil.long2DateStr(xhjvmJson.getDate("jvmCreateTime").getTime)
//            val insertRequest = ESUtils.insertRequestFunc(assistJvmIndex+timeStr, assistJvmType, xhjvmJson)
//            xhjvmRequestList.append(insertRequest)
//          }
//        }catch {
//          case e: NullPointerException => {}
//          case e: Exception => {
//            println(record.value)
//            e.printStackTrace()
//          }
//        }
//      }
//
//      // 1;初始化宽表nginx
//      if (xhjvmRequestList.nonEmpty) ESUtils.bulkInsertRequestFunc(xhjvmRequestList)
//
//    }catch {
//      case e: Exception => {
//        e.printStackTrace()
//      }
//    }
//  }
//
//  def xhjvmParse(xhjvmStr: String): JSONObject ={
//    // nginx
//    val xhjvmJson: JSONObject = JSON.parseObject("{}")
//
//    if(null != xhjvmStr && xhjvmStr.size>3){
//      val message = JSON.parseObject(xhjvmStr).getString("message")
//      if(message.split(" ")(2)=="ERROR"){ //存储ERROR数据
//        val messageArray = message.split(" ")
//        val jvmCreateTime = "%s %s".format(messageArray(0), messageArray(1))
//
//        val index1 = message.indexOf("[") + 1
//        val index2 = message.indexOf("]") + 1
//        val data = message.substring(index1, index2).split(",")
//
//        val appName = data(0)
//        val traceId = data(1)
//        val spandId = data(2)
//        val parentSpandId = ""
//
//        xhjvmJson.put("spandId", spandId)
//        xhjvmJson.put("parentSpandId", parentSpandId)
//        xhjvmJson.put("appName", appName)
//        xhjvmJson.put("traceId", traceId)
//        xhjvmJson.put("message", message)
//
//        try{
//          val date: Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jvmCreateTime)
//          xhjvmJson.put("jvmCreateTime", date)  // 2019-10-25 16:40:11.518
//          xhjvmJson.put("jvmCreateTimeLong", date.getTime)  // 1578029747000
//        }catch {
//          case e: Exception => xhjvmJson.put("jvmCreateTime", null)
//        }
//
//        xhjvmJson.put("assitCreateTime", "")
//        xhjvmJson.put("errorCode", "")
//        xhjvmJson.put("requestUri", "")
//        xhjvmJson.put("assistMessage", "")
//        xhjvmJson.put("schoolId", "")
//        xhjvmJson.put("userId", "")
//        xhjvmJson.put("deviceId", "")
//        xhjvmJson.put("packageName", "")
//        xhjvmJson.put("nginxCreateTime", "")
//        xhjvmJson.put("requestTime", "")
//        xhjvmJson.put("responseCode", "")
//
//        //添加jvmMessage数组
//        val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()
//        val json = JSON.parseObject("{}")
//        json.put("traceId", traceId)
//        json.put("packageName", "")
//        json.put("requestUri", "")
//
//        json.put("message", message)
//        json.put("spandId", spandId)
//        json.put("parentSpandId", parentSpandId)
//        json.put("appName", appName)
//        json.put("jvmCreateTime", xhjvmJson.getDate("jvmCreateTime"))
//
//        assistJvms.append(json)
//        xhjvmJson.put("jvmMessage", assistJvms.toArray)
//      }
//    }
//
//    xhjvmJson
//  }
//
//  /**
//    * 客户端日志解析
//    *
//    * @param assistStr 客户端日志字符串
//    * @return
//    */
//  private def assistStrFunc(assistStr: String): JSONObject = {
//    var jsonOrange: JSONObject = null
//    if(null == JSON.parseObject(assistStr).getString("logType")){
//      jsonOrange = JSON.parseObject(JSON.parseObject(assistStr).getString("message"))
//    }else{
//      jsonOrange = JSON.parseObject(assistStr)
//    }
//    val json = JSON.parseObject("{}")
//
//    if (jsonOrange.getString("logType") == "ERROR" || jsonOrange.getString("logType") == "CRASH") {
//      json.put("logType", Utils.null2Str(jsonOrange.getString("logType")))
//      json.put("traceId", Utils.null2Str(jsonOrange.getString("X-B3-TraceId")))
//      try{
//        json.put("spandId", Utils.null2Str(jsonOrange.getString("requestHeaders").split("X-B3-SpanId:")(1).split("\"")(0).replaceAll(" ", "")))
//      }catch {case e: Exception => {
//        // println("解析spandId异常", assistStr)
//      }}
//
//      json.put("schoolId", Utils.null2Str(jsonOrange.getString("schoolId")).replaceAll("null", ""))
//      json.put("userId", Utils.null2Str(jsonOrange.getString("userId")))
//      json.put("deviceId", Utils.null2Str(jsonOrange.getString("deviceId")))
//      json.put("packageName", Utils.null2Str(jsonOrange.getString("packageName")))
//      json.put("appVersionName", Utils.null2Str(jsonOrange.getString("versionName")))
//      json.put("appVersionCode", Utils.null2Str(jsonOrange.getString("versionCode")))
//      json.put("remoteIp", Utils.null2Str(jsonOrange.getString("remoteIp")))
//      json.put("errorCode", Utils.null2Str(jsonOrange.getString("ErrorCode")))
//      json.put("model", Utils.null2Str(jsonOrange.getString("MODEL")))
//      json.put("availableMemory", Utils.null2Double(jsonOrange.getString("availableMemory")))
//      json.put("totalMemory", Utils.null2Double(jsonOrange.getString("totalMemory")))
//      json.put("sdcardAvailableSpaceSize", Utils.null2Double(jsonOrange.getString("sdcardAvailableSpaceSize")))
//      json.put("sdcardTotalSpaceSize", Utils.null2Double(jsonOrange.getString("sdcardTotalSpaceSize")))
//      json.put("connectionDuration", Utils.null2Double(jsonOrange.getString("connectionDuration")))
//      json.put("dnsDuration", Utils.null2Double(jsonOrange.getString("dnsDuration")))
//      json.put("requestMethod", Utils.null2Str(jsonOrange.getString("requestMethod")))
//      json.put("assistMessage", Utils.null2Str(jsonOrange.getString("ErrorMessage")))
//      json.put("SSID", Utils.null2Str(jsonOrange.getString("SSID")))
//      json.put("BSSID", Utils.null2Str(jsonOrange.getString("BSSID")))
//      json.put("requestLogScope", "1")
//      json.put("processStatus", "0")
//
//
//      //解决没有crashHappenTime
//      if(null != jsonOrange.getLong("crashHappenTime")){
//        json.put("assitCreateTime", jsonOrange.getLong("crashHappenTime"))
//      }else{
//        json.put("assitCreateTime", 0L)
//      }
//      json.put("@timestamp", json.getDate("assitCreateTime")) //Date类型
//
//      if(jsonOrange.getString("logType") == "CRASH"){
//        json.put("errorMessage", Utils.null2Str(jsonOrange.getString("USER")))
//        json.put("errorMessageHashCode", Utils.hashMD5(Utils.null2Str(jsonOrange.getString("USER"))))
//      }
//
//      try{
//        val url = jsonOrange.getString("url").split("\\?").head
//        json.put("requestUrl", url)
//        val uri = (url + "/").replaceAll("http.*://[^/]*/", "").replaceAll("/[0-9]+/", "/-/").replaceAll("/[0-9]+/", "/-/").replaceAll(".*/api/", "api/").replaceAll("/$", "")
//        json.put("requestUri", uri)
//      }catch {
//        case e:Exception => {
//          json.put("requestUrl", "")
//          json.put("requestUri", "")
//        }
//      }
//    }
//
//    json
//  }
//
//  /**
//    * 关联宽表：assist-nginx
//    *
//    * @param assistJson
//    * @param nginxStr
//    * @return
//    */
//  def updateAssistNginxJson(assistJson: JSONObject, nginxStr: String): JSONObject ={
//    if(null == nginxStr || nginxStr.size<3){
//      assistJson.put("nginxCreateTime",  null)
//      assistJson.put("requestTime", null)
//      assistJson.put("responseCode", null)
//      assistJson.put("rsip", null)
//    }else{
//      val nginxArray = nginxStr.split(" ")
//
//      val nginxCreateTime = nginxArray(2) + " " + nginxArray(3)
//      val requestTime = nginxArray(8).toFloat
//      val responseCode = nginxArray(10)
//      val rsip = nginxArray(nginxArray.size-2).replace("[", "").replace("]", "")
//
//      try{
//        val date = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss",Locale.ENGLISH).parse(nginxCreateTime)
//        assistJson.put("nginxCreateTime", date)
//      }catch {
//        case e: Exception => assistJson.put("nginxCreateTime",  null)
//      }
//
//      assistJson.put("requestTime", requestTime*1000)
//      assistJson.put("responseCode", responseCode)
//      assistJson.put("rsip", rsip)
//    }
//
//    assistJson
//  }
//
//  /**
//    * 关联宽表：assist-jvm
//    *
//    * @param assistJson
//    * @param jvmStr
//    * @return
//    */
//  def initAssistJvmJson(assistJson: JSONObject, jvmStr: String): JSONObject ={
//    val assistJvmJson = JSON.parseObject("{}")
//
//    assistJvmJson.put("traceId", assistJson.getString("traceId"))
//    assistJvmJson.put("packageName", assistJson.getString("packageName"))
//    assistJvmJson.put("requestUri", assistJson.getString("requestUri"))
//    assistJvmJson.put("processStatus", assistJson.getString("processStatus"))
//
//    val message = JSON.parseObject(jvmStr).getString("message")
//    assistJvmJson.put("message", message)
//
//
//    if(null == message || message.size<3){
//      assistJvmJson.put("spandId", null)
//      assistJvmJson.put("parentSpandId", null)
//      assistJvmJson.put("appName", null)
//      assistJvmJson.put("jvmCreateTime", null)
//
//    }else{
//      val messageArray = message.split(" ")
//      val jvmCreateTime = "%s %s".format(messageArray(0), messageArray(1))
//
//      val index1 = message.indexOf("[") + 1
//      val index2 = message.indexOf("]") + 1
//      val data = message.substring(index1, index2).split(",")
//
//      val appName = data(0)
//      val spandId = data(2)
//      val parentSpandId = ""
//
//      assistJvmJson.put("spandId", spandId)
//      assistJvmJson.put("parentSpandId", parentSpandId)
//      assistJvmJson.put("appName", appName)
//
//      try{
//        val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jvmCreateTime)
//        assistJvmJson.put("jvmCreateTime", date)  // 2019-10-25 16:40:11.518
//      }catch {
//        case e: Exception => assistJvmJson.put("jvmCreateTime", null)
//      }
//    }
//
//    assistJvmJson
//  }
//
//
//  /**
//    * 关联
//    */
//  def assistNginxSearchScrollRangeFunc(index: String, typeStr:String, key1: String, value1: String, key2: String, value2: String) ={
//    val highClient = ESUtils.getHighClient()
//
//    val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
//    //多条件查询的写法
//    val q1 = QueryBuilders.rangeQuery(key1).from(value1)
//    val q2 = QueryBuilders.matchQuery(key2, value2)
//    val qs = QueryBuilders.boolQuery().must(q1).must(q2)
//    searchSourceBuilder.query(qs)
//    searchSourceBuilder.size(5000)
//
//    val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引
//    searchRequest.types(typeStr)
//    searchRequest.source(searchSourceBuilder)
//    searchRequest.scroll(TimeValue.timeValueMinutes(1L))//设置scroll间隔 ----
//
//    var scrollId: String = null
//
//    try{
//      var searchResponse: SearchResponse = highClient.search(searchRequest)
//      scrollId = searchResponse.getScrollId
//
//      var hits: Array[SearchHit] = searchResponse.getHits.getHits
//      assistNginxTable(hits, index)
//
//      val scroll = new Scroll(TimeValue.timeValueMinutes(1L))
//      while(hits != null && hits.size>0){
//        val scrollRequest = new SearchScrollRequest(scrollId)
//        scrollRequest.scroll(scroll)
//        searchResponse = highClient.searchScroll(scrollRequest)
//
//        scrollId = searchResponse.getScrollId()
//        hits = searchResponse.getHits.getHits
//
//        assistNginxTable(hits, index)
//      }
//    }catch {
//      case e: Exception => {
//        //TODO
//        e.printStackTrace()
//      }
//    }finally {
//      val clearScrollRequest = new ClearScrollRequest()
//      clearScrollRequest.addScrollId(scrollId)
//      highClient.clearScroll(clearScrollRequest)
//    }
//  }
//
//  def assistNginxTable(a: Array[SearchHit], index: String) ={
//    val assistNginxjsonList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
//
//    //更新nginx和jvm的索引
//    val updateNginxList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
//    val updateJvmList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
//
//    a.foreach(searchHit => {
//      val id = searchHit.getId
//      val traceId = searchHit.getSourceAsMap.asScala.get("traceId").toString
//      val timeStr = DateUtil.long2DateStr(searchHit.getSourceAsMap.asScala.get("assitCreateTime").getOrElse().toString.toLong)
//
//      if(null != traceId && traceId.size>3){
//        val nginxSearchHits = ESUtils.searchFunc(nginxIndex+timeStr, nginxType, "TraceId", traceId)
//        val jvmSearchHits = ESUtils.searchFunc(jvmIndex+timeStr, jvmType, "X_B3_TraceId", traceId)
//
//        if(nginxSearchHits.size>0){
//          val assistJson: JSONObject = Utils.map2json(searchHit.getSourceAsMap.asScala)
//          val nginxMessageStr = nginxSearchHits(0).getSourceAsMap.asScala.get("message").toString
//
//          val assistNginxJson = updateAssistNginxJson(assistJson, nginxMessageStr)
//          assistNginxJson.put("requestLogScope", "2")
//
//
//          if(jvmSearchHits.size>0){
//            val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()
//
//            val assistJson: JSONObject = Utils.map2json(searchHit.getSourceAsMap.asScala)
//            jvmSearchHits.foreach(x => {
//              val jvmStr: String = x.getSourceAsMap.get("message").toString
//
//              if(jvmStr.contains("ERROR")){
//                val assistJvmJson = initAssistJvmJson(assistJson, jvmStr)
//
//                assistJvms.append(assistJvmJson)
//              }
//            })
//
//            assistNginxJson.put("requestLogScope", "3")
//            assistNginxJson.put("jvmMessage", assistJvms.toArray)
//
//
//
//
//            //更新jvmDetail
//            val jvmDetailSearchHits = ESUtils.searchFunc(assistJvmIndex+timeStr, assistJvmType, "traceId", traceId)
//            if(jvmDetailSearchHits.size>0){
//              jvmDetailSearchHits.foreach(x => {
//                val id = x.getId
//                val jvmDetailJson: JSONObject = Utils.map2json(x.getSourceAsMap.asScala)
//                jvmDetailJson.put("assitCreateTime", assistNginxJson.get("assitCreateTime"))
//                jvmDetailJson.put("errorCode", assistNginxJson.get("errorCode"))
//                jvmDetailJson.put("requestUri", assistNginxJson.get("requestUri"))
//                jvmDetailJson.put("assistMessage", assistNginxJson.get("assistMessage"))
//                jvmDetailJson.put("schoolId", assistNginxJson.get("schoolId"))
//                jvmDetailJson.put("userId", assistNginxJson.get("userId"))
//                jvmDetailJson.put("deviceId", assistNginxJson.get("deviceId"))
//                jvmDetailJson.put("packageName", assistNginxJson.get("packageName"))
//                jvmDetailJson.put("jvmMessage", assistNginxJson.get("jvmMessage"))
//                jvmDetailJson.put("nginxCreateTime", assistNginxJson.get("nginxCreateTime"))
//                jvmDetailJson.put("requestTime", assistNginxJson.get("requestTime"))
//                jvmDetailJson.put("responseCode", assistNginxJson.get("responseCode"))
//                jvmDetailJson.put("processStatus", assistNginxJson.get("processStatus"))
//
//
//                val jvmDetailUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(assistJvmIndex+timeStr, assistJvmType, id, jvmDetailJson)
//                updateJvmList.append(jvmDetailUpdateRequest)
//              })
//            }
//          }
//
//          //更新nginxDetail
//          val nginxDetailSearchHits = ESUtils.searchFunc(nginxDetailIndex+timeStr, nginxDetailType, "traceId", traceId)
//          if(nginxDetailSearchHits.size>0){
//            nginxDetailSearchHits.foreach(x => {
//              val id = x.getId
//              val nginxDetailJson: JSONObject = Utils.map2json(x.getSourceAsMap.asScala)
//              nginxDetailJson.put("assitCreateTime", assistNginxJson.get("assitCreateTime"))
//              nginxDetailJson.put("errorCode", assistNginxJson.get("errorCode"))
//              nginxDetailJson.put("jvmMessage", assistNginxJson.get("jvmMessage"))
//              nginxDetailJson.put("assistMessage", assistNginxJson.get("assistMessage"))
//
//              val nginxDetailUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(nginxDetailIndex+timeStr, nginxDetailType, id, nginxDetailJson)
//              updateNginxList.append(nginxDetailUpdateRequest)
//            })
//          }
//
//
//          val assistNginxUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(index, assistNginxType, id, assistNginxJson)
//          assistNginxjsonList.append(assistNginxUpdateRequest)
//
//        }else if(nginxSearchHits.size==0 && jvmSearchHits.size>0){
//          val assistJson: JSONObject = Utils.map2json(searchHit.getSourceAsMap.asScala)
//          val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()
//
//          jvmSearchHits.foreach(x => {
//            val jvmStr: String = x.getSourceAsMap.get("message").toString
//
//            if(jvmStr.contains("ERROR")){
//              val assistJvmJson = initAssistJvmJson(assistJson, jvmStr)
//
//              assistJvms.append(assistJvmJson)
//            }
//          })
//
//          // 因为nginx没有数据，所以直接使用assist的json存储
//          assistJson.put("requestLogScope", "3")
//          assistJson.put("jvmMessage", assistJvms.toArray)
//
//
//
//          //更新jvmDetail
//          val jvmDetailSearchHits = ESUtils.searchFunc(assistJvmIndex+timeStr, assistJvmType, "traceId", traceId)
//          if(jvmDetailSearchHits.size>0){
//            jvmDetailSearchHits.foreach(x => {
//              val id = x.getId
//              val jvmDetailJson: JSONObject = Utils.map2json(x.getSourceAsMap.asScala)
//              jvmDetailJson.put("assitCreateTime", assistJson.get("assitCreateTime"))
//              jvmDetailJson.put("errorCode", assistJson.get("errorCode"))
//              jvmDetailJson.put("requestUri", assistJson.get("requestUri"))
//              jvmDetailJson.put("assistMessage", assistJson.get("assistMessage"))
//              jvmDetailJson.put("schoolId", assistJson.get("schoolId"))
//              jvmDetailJson.put("userId", assistJson.get("userId"))
//              jvmDetailJson.put("deviceId", assistJson.get("deviceId"))
//              jvmDetailJson.put("packageName", assistJson.get("packageName"))
//              jvmDetailJson.put("jvmMessage", assistJson.get("jvmMessage"))
//              jvmDetailJson.put("nginxCreateTime", assistJson.get("nginxCreateTime"))
//              jvmDetailJson.put("requestTime", assistJson.get("requestTime"))
//              jvmDetailJson.put("responseCode", assistJson.get("responseCode"))
//              jvmDetailJson.put("processStatus", assistJson.get("processStatus"))
//
//
//
//              val jvmDetailUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(assistJvmIndex+timeStr, assistJvmType, id, jvmDetailJson)
//              updateJvmList.append(jvmDetailUpdateRequest)
//            })
//          }
//
//          val assistNginxUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(index, assistNginxType, id, assistJson)
//          assistNginxjsonList.append(assistNginxUpdateRequest)
//        }
//      }
//    })
//
//    if (assistNginxjsonList.nonEmpty) ESUtils.bulkUpdateRequestFunc(assistNginxjsonList)
//    if (updateNginxList.nonEmpty) ESUtils.bulkUpdateRequestFunc(updateNginxList)
//    if (updateJvmList.nonEmpty) ESUtils.bulkUpdateRequestFunc(updateJvmList)
//
//  }
//
//  def assistJvmSearchScrollRangeFunc(index: String, typeStr:String, key1: String, value1: String, key2: String, value2: String) ={
//    val highClient = ESUtils.getHighClient()
//
//    val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
//    val q1 = QueryBuilders.rangeQuery(key1).from(value1)
//    val q2 = QueryBuilders.matchQuery(key2, value2)
//    val qs = QueryBuilders.boolQuery().must(q1).must(q2)
//    searchSourceBuilder.query(qs)
//
//
//    searchSourceBuilder.size(5000)
//
//    val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引
//    searchRequest.types(typeStr)
//    searchRequest.source(searchSourceBuilder)
//    searchRequest.scroll(TimeValue.timeValueMinutes(1L))//设置scroll间隔 ----
//    var scrollId: String = null
//
//    try{
//      var searchResponse: SearchResponse = highClient.search(searchRequest)
//      scrollId = searchResponse.getScrollId
//
//      var hits: Array[SearchHit] = searchResponse.getHits.getHits
//      assistJvmTable(hits, index)
//
//      val scroll = new Scroll(TimeValue.timeValueMinutes(1L))
//      while(hits != null && hits.size>0){
//        val scrollRequest = new SearchScrollRequest(scrollId)
//        scrollRequest.scroll(scroll)
//        searchResponse = highClient.searchScroll(scrollRequest)
//
//        scrollId = searchResponse.getScrollId()
//        hits = searchResponse.getHits.getHits
//
//        assistJvmTable(hits, index)
//      }
//    }catch {
//      case e: Exception => {
//        //TODO
//        e.printStackTrace()
//      }
//    }finally {
//      val clearScrollRequest = new ClearScrollRequest()
//      clearScrollRequest.addScrollId(scrollId)
//      highClient.clearScroll(clearScrollRequest)
//    }
//  }
//
//  def assistJvmTable(b: Array[SearchHit], index: String): Unit ={
//    val assistNginxjsonList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
//    val updateJvmList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
//
//    b.foreach(searchHit => {
//      val id = searchHit.getId
//      val traceId = searchHit.getSourceAsMap.asScala.get("traceId").toString
//      val timeStr = DateUtil.long2DateStr(searchHit.getSourceAsMap.asScala.get("assitCreateTime").getOrElse().toString.toLong)
//
//      if(null != traceId && traceId.size>3) {
//        val jvmSearchHits = ESUtils.searchFunc(jvmIndex+timeStr, jvmType, "X_B3_TraceId", traceId)
//
//        if(jvmSearchHits.size>0){
//          val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()
//
//          val assistNginxJson: JSONObject = Utils.map2json(searchHit.getSourceAsMap.asScala)
//          jvmSearchHits.foreach(x => {
//            val jvmStr: String = x.getSourceAsMap.get("message").toString
//            if(jvmStr.contains("ERROR")){
//              val assistJvmJson = initAssistJvmJson(assistNginxJson, jvmStr)
//              assistJvms.append(assistJvmJson)
//            }
//          })
//
//          assistNginxJson.put("requestLogScope", "3")
//          assistNginxJson.put("jvmMessage", assistJvms.toArray)
//
//
//          //更新jvmDetail
//          val jvmDetailSearchHits = ESUtils.searchFunc(assistJvmIndex+timeStr, assistJvmType, "traceId", traceId)
//          if(jvmDetailSearchHits.size>0){
//            jvmDetailSearchHits.foreach(x => {
//              val id = x.getId
//              val jvmDetailJson: JSONObject = Utils.map2json(x.getSourceAsMap.asScala)
//              jvmDetailJson.put("assitCreateTime", assistNginxJson.get("assitCreateTime"))
//              jvmDetailJson.put("errorCode", assistNginxJson.get("errorCode"))
//              jvmDetailJson.put("requestUri", assistNginxJson.get("requestUri"))
//              jvmDetailJson.put("assistMessage", assistNginxJson.get("assistMessage"))
//              jvmDetailJson.put("schoolId", assistNginxJson.get("schoolId"))
//              jvmDetailJson.put("userId", assistNginxJson.get("userId"))
//              jvmDetailJson.put("deviceId", assistNginxJson.get("deviceId"))
//              jvmDetailJson.put("packageName", assistNginxJson.get("packageName"))
//              jvmDetailJson.put("jvmMessage", assistNginxJson.get("jvmMessage"))
//              jvmDetailJson.put("nginxCreateTime", assistNginxJson.get("nginxCreateTime"))
//              jvmDetailJson.put("requestTime", assistNginxJson.get("requestTime"))
//              jvmDetailJson.put("responseCode", assistNginxJson.get("responseCode"))
//              jvmDetailJson.put("processStatus", assistNginxJson.get("processStatus"))
//
//              val jvmDetailUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(assistJvmIndex+timeStr, assistJvmType, id, jvmDetailJson)
//              updateJvmList.append(jvmDetailUpdateRequest)
//            })
//          }
//
//
//
//          val assistNginxUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(index, assistNginxType, id, assistNginxJson)
//          assistNginxjsonList.append(assistNginxUpdateRequest)
//        }
//      }
//    })
//
//
//
//    if (assistNginxjsonList.nonEmpty) ESUtils.bulkUpdateRequestFunc(assistNginxjsonList)
//    if (updateJvmList.nonEmpty) ESUtils.bulkUpdateRequestFunc(updateJvmList)
//  }
//
//
//
//}

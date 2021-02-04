package com.xuehai.utils

import java.text.SimpleDateFormat
import com.alibaba.fastjson.{JSON, JSONObject}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{ClearScrollRequest, SearchScrollRequest, SearchResponse, SearchRequest}
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.{Scroll, SearchHit}
import org.elasticsearch.search.builder.SearchSourceBuilder
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * Created by root on 2019/12/9.
  */
object AddDataUtil extends Constants{
	def joinDataByDay(index: String, typeStr:String, key2: String, value2: String) ={
		val highClient = ESUtils.getHighClient()

		val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
		val q2 = QueryBuilders.matchQuery(key2, value2)
		val qs = QueryBuilders.boolQuery().must(q2)
		searchSourceBuilder.query(qs)


		searchSourceBuilder.size(5000)

		val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引
		searchRequest.types(typeStr)
		searchRequest.source(searchSourceBuilder)
		searchRequest.scroll(TimeValue.timeValueMinutes(1L))//设置scroll间隔 ----
		var scrollId: String = null

		try{
			var searchResponse: SearchResponse = highClient.search(searchRequest)
			scrollId = searchResponse.getScrollId

			var hits: Array[SearchHit] = searchResponse.getHits.getHits
			assistJvmTable(hits, index)

			val scroll = new Scroll(TimeValue.timeValueMinutes(1L))
			while(hits != null && hits.size>0){
				val scrollRequest = new SearchScrollRequest(scrollId)
				scrollRequest.scroll(scroll)
				searchResponse = highClient.searchScroll(scrollRequest)

				scrollId = searchResponse.getScrollId()
				hits = searchResponse.getHits.getHits

				assistJvmTable(hits, index)
			}
		}catch {
			case e: Exception => {
				//TODO
				e.printStackTrace()
			}
		}finally {
			val clearScrollRequest = new ClearScrollRequest()
			clearScrollRequest.addScrollId(scrollId)
			highClient.clearScroll(clearScrollRequest)
		}
	}

	def assistJvmTable(b: Array[SearchHit], index: String): Unit ={
		val assistNginxjsonList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()
		val updateJvmList: ListBuffer[UpdateRequest] = ListBuffer[UpdateRequest]()

		b.foreach(searchHit => {
			val id = searchHit.getId
			val traceId = searchHit.getSourceAsMap.asScala.get("traceId").toString
			val timeStr = DateUtil.long2DateStr(searchHit.getSourceAsMap.asScala.get("assitCreateTime").getOrElse().toString.toLong)

			if(null != traceId && traceId.size>3) {
				val jvmSearchHits = ESUtils.searchFunc(jvmIndex+timeStr, jvmType, "X_B3_TraceId", traceId)

				if(jvmSearchHits.size>0){
					val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()

					val assistNginxJson: JSONObject = Utils.map2json(searchHit.getSourceAsMap.asScala)
					jvmSearchHits.foreach(x => {
						val jvmStr: String = x.getSourceAsMap.get("message").toString
						if(jvmStr.contains("ERROR")){
							val assistJvmJson = initAssistJvmJson(assistNginxJson, jvmStr)
							assistJvms.append(assistJvmJson)
						}
					})

					assistNginxJson.put("requestLogScope", "3")
					assistNginxJson.put("jvmMessage", assistJvms.toArray)


					//更新jvmDetail
					val jvmDetailSearchHits = ESUtils.searchFunc(assistJvmIndex+timeStr, assistJvmType, "traceId", traceId)
					if(jvmDetailSearchHits.size>0){
						jvmDetailSearchHits.foreach(x => {
							val id = x.getId
							val jvmDetailJson: JSONObject = Utils.map2json(x.getSourceAsMap.asScala)
							jvmDetailJson.put("assitCreateTime", assistNginxJson.get("assitCreateTime"))
							jvmDetailJson.put("errorCode", assistNginxJson.get("errorCode"))
							jvmDetailJson.put("requestUri", assistNginxJson.get("requestUri"))
							jvmDetailJson.put("assistMessage", assistNginxJson.get("assistMessage"))
							jvmDetailJson.put("schoolId", assistNginxJson.get("schoolId"))
							jvmDetailJson.put("userId", assistNginxJson.get("userId"))
							jvmDetailJson.put("deviceId", assistNginxJson.get("deviceId"))
							jvmDetailJson.put("packageName", assistNginxJson.get("packageName"))
							jvmDetailJson.put("jvmMessage", assistNginxJson.get("jvmMessage"))
							jvmDetailJson.put("nginxCreateTime", assistNginxJson.get("nginxCreateTime"))
							jvmDetailJson.put("requestTime", assistNginxJson.get("requestTime"))
							jvmDetailJson.put("responseCode", assistNginxJson.get("responseCode"))
							jvmDetailJson.put("processStatus", assistNginxJson.get("processStatus"))

							val jvmDetailUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(assistJvmIndex+timeStr, assistJvmType, id, jvmDetailJson)
							updateJvmList.append(jvmDetailUpdateRequest)
						})
					}

					val assistNginxUpdateRequest: UpdateRequest = ESUtils.updateRequestFunc(index, assistNginxType, id, assistNginxJson)
					assistNginxjsonList.append(assistNginxUpdateRequest)
				}
			}
		})



		if (assistNginxjsonList.nonEmpty) ESUtils.bulkUpdateRequestFunc(assistNginxjsonList)
		if (updateJvmList.nonEmpty) ESUtils.bulkUpdateRequestFunc(updateJvmList)
	}

	def initAssistJvmJson(assistJson: JSONObject, jvmStr: String): JSONObject ={
		val assistJvmJson = JSON.parseObject("{}")

		assistJvmJson.put("traceId", assistJson.getString("traceId"))
		assistJvmJson.put("packageName", assistJson.getString("packageName"))
		assistJvmJson.put("requestUri", assistJson.getString("requestUri"))
		assistJvmJson.put("processStatus", assistJson.getString("processStatus"))

		val message = JSON.parseObject(jvmStr).getString("message")
		assistJvmJson.put("message", message)


		if(null == message || message.size<3){
			assistJvmJson.put("spandId", null)
			assistJvmJson.put("parentSpandId", null)
			assistJvmJson.put("appName", null)
			assistJvmJson.put("jvmCreateTime", null)

		}else{
			val messageArray = message.split(" ")
			val jvmCreateTime = "%s %s".format(messageArray(0), messageArray(1))

			val index1 = message.indexOf("[") + 1
			val index2 = message.indexOf("]") + 1
			val data = message.substring(index1, index2).split(",")

			val appName = data(0)
			val spandId = data(2)
			val parentSpandId = ""

			assistJvmJson.put("spandId", spandId)
			assistJvmJson.put("parentSpandId", parentSpandId)
			assistJvmJson.put("appName", appName)

			try{
				val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jvmCreateTime)
				assistJvmJson.put("jvmCreateTime", date)  // 2019-10-25 16:40:11.518
			}catch {
				case e: Exception => assistJvmJson.put("jvmCreateTime", null)
			}
		}

		assistJvmJson
	}


	def addDataByDay(index: String, typeStr:String, key2: String, value2: String): Unit ={
		val highClient = ESUtils.getHighClient()

		val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
		//多条件查询的写法
		//val q1 = QueryBuilders.rangeQuery(key1).from(value1).to(value3).includeLower(true).includeUpper(true)
		val q2 = QueryBuilders.matchQuery(key2, value2)
		val qs = QueryBuilders.boolQuery().must(q2)
		searchSourceBuilder.query(qs)
		searchSourceBuilder.size(5000)

		val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引
		searchRequest.types(typeStr)
		searchRequest.source(searchSourceBuilder)
		searchRequest.scroll(TimeValue.timeValueMinutes(1L))//设置scroll间隔 ----

		var scrollId: String = null

		try{
			var searchResponse: SearchResponse = highClient.search(searchRequest)
			scrollId = searchResponse.getScrollId

			var hits: Array[SearchHit] = searchResponse.getHits.getHits
			addJvmStart(hits)

			val scroll = new Scroll(TimeValue.timeValueMinutes(1L))
			while(hits != null && hits.size>0){
				val scrollRequest = new SearchScrollRequest(scrollId)
				scrollRequest.scroll(scroll)
				searchResponse = highClient.searchScroll(scrollRequest)

				scrollId = searchResponse.getScrollId()
				hits = searchResponse.getHits.getHits

				addJvmStart(hits)
			}
		}catch {
			case e: Exception => {
				//TODO
				e.printStackTrace()
			}
		}finally {
			val clearScrollRequest = new ClearScrollRequest()
			clearScrollRequest.addScrollId(scrollId)
			highClient.clearScroll(clearScrollRequest)
		}
	}

	def addJvmStart(hits: Array[SearchHit]): Unit ={
		try{
			val xhjvmRequestList: ListBuffer[IndexRequest] = ListBuffer[IndexRequest]()
			for (hit <- hits) {
				try{
					val xhjvmStr: String = hit.getSourceAsMap.get("message").toString
					val xhjvmJson: JSONObject = xhjvmParse(xhjvmStr)

					if(xhjvmJson.size()>0){
						val timeStr = DateUtil.long2DateStr(xhjvmJson.getDate("jvmCreateTime").getTime)
						val insertRequest = ESUtils.insertRequestFunc(assistJvmIndex+timeStr, assistJvmType, xhjvmJson)
						xhjvmRequestList.append(insertRequest)
					}
				}catch {
					case e: NullPointerException => {}
					case e: Exception => {
						println(hit.getSourceAsMap.get("message").toString)
						e.printStackTrace()
					}
				}
			}

			// 1;初始化宽表nginx
			if (xhjvmRequestList.nonEmpty) ESUtils.bulkInsertRequestFunc(xhjvmRequestList)

		}catch {
			case e: Exception => {
				e.printStackTrace()
			}
		}
	}

	def xhjvmParse(xhjvmStr: String): JSONObject ={
		// nginx
		val xhjvmJson: JSONObject = JSON.parseObject("{}")

		if(null != xhjvmStr && xhjvmStr.size>3){
			val message = JSON.parseObject(xhjvmStr).getString("message")
			if(message.split(" ")(2)=="ERROR"){
				val messageArray = message.split(" ")
				val jvmCreateTime = "%s %s".format(messageArray(0), messageArray(1))

				val index1 = message.indexOf("[") + 1
				val index2 = message.indexOf("]") + 1
				val data = message.substring(index1, index2).split(",")

				val appName = data(0)
				val traceId = data(1)
				val spandId = data(2)
				val parentSpandId = ""

				xhjvmJson.put("spandId", spandId)
				xhjvmJson.put("parentSpandId", parentSpandId)
				xhjvmJson.put("appName", appName)
				xhjvmJson.put("traceId", traceId)
				xhjvmJson.put("message", message)

				try{
					val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jvmCreateTime)
					xhjvmJson.put("jvmCreateTime", date)  // 2019-10-25 16:40:11.518
				}catch {
					case e: Exception => xhjvmJson.put("jvmCreateTime", null)
				}

				xhjvmJson.put("assitCreateTime", "")
				xhjvmJson.put("errorCode", "")
				xhjvmJson.put("requestUri", "")
				xhjvmJson.put("assistMessage", "")
				xhjvmJson.put("schoolId", "")
				xhjvmJson.put("userId", "")
				xhjvmJson.put("deviceId", "")
				xhjvmJson.put("packageName", "")
				xhjvmJson.put("nginxCreateTime", "")
				xhjvmJson.put("requestTime", "")
				xhjvmJson.put("responseCode", "")

				//添加jvmMessage数组
				val assistJvms: ListBuffer[JSONObject] = ListBuffer[JSONObject]()
				val json = JSON.parseObject("{}")
				json.put("traceId", traceId)
				json.put("packageName", "")
				json.put("requestUri", "")

				json.put("message", message)
				json.put("spandId", spandId)
				json.put("parentSpandId", parentSpandId)
				json.put("appName", appName)
				json.put("jvmCreateTime", xhjvmJson.getDate("jvmCreateTime"))

				assistJvms.append(json)
				xhjvmJson.put("jvmMessage", assistJvms.toArray)
			}
		}

		xhjvmJson
	}
}

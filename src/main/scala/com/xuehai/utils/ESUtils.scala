package com.xuehai.utils

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.http.HttpHost
import org.elasticsearch.action.bulk.{BulkRequest, BulkResponse}
import org.elasticsearch.action.delete.{DeleteRequest, DeleteResponse}
import org.elasticsearch.action.get.{GetRequest, GetResponse}
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.elasticsearch.action.search._
import org.elasticsearch.action.update.{UpdateRequest, UpdateResponse}
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}
import org.elasticsearch.search.{Scroll, SearchHit, SearchHits}
import org.elasticsearch.search.builder.SearchSourceBuilder
import scala.collection.mutable.ListBuffer


/**
  * Created by root on 2019/10/17.
  */
object ESUtils extends Constants{
	var client: RestClient = null

	def init(): RestClient ={
		val hostAndPortArray = hostAndPort.split(",").map(x => (x.split(":")(0), x.split(":")(1).toInt))
		if(hostAndPortArray.size==3){
			val restClient: RestClientBuilder = RestClient.builder(new HttpHost(hostAndPortArray(0)._1, hostAndPortArray(0)._2, "http"), new HttpHost(hostAndPortArray(1)._1, hostAndPortArray(1)._2, "http"), new HttpHost(hostAndPortArray(2)._1, hostAndPortArray(2)._2, "http"))
			client = restClient.build
		}else{
			val restClient: RestClientBuilder = RestClient.builder(new HttpHost(hostAndPortArray(0)._1, hostAndPortArray(0)._2, "http"))
			client = restClient.build
		}

		println("ES init susscse.....")

		client
	}

	def getHighClient(): RestHighLevelClient ={
		if(null == client) init()
		val highClient: RestHighLevelClient = new RestHighLevelClient(client)

		highClient
	}

	def selectFunc(index: String, typeStr: String, id: String): GetResponse ={
		val highClient = getHighClient()
		val getRequest: GetRequest = new GetRequest(index, typeStr, id)

		highClient.get(getRequest)
	}

	def insertFunc(index: String, typeStr: String, json: JSONObject): IndexResponse ={
		val highClient = getHighClient()
		val request = new IndexRequest(	index, typeStr).source(json, XContentType.JSON)

		highClient.index(request)
	}

	def insertRequestFunc1(index: String, typeStr: String, json: JSONObject,id:String): IndexRequest ={
		val request: IndexRequest = new IndexRequest(index, typeStr,id).source(json, XContentType.JSON)

		request
	}

	def insertRequestFunc(index: String, typeStr: String, json: JSONObject): IndexRequest ={
		val request: IndexRequest = new IndexRequest(index, typeStr).source(json, XContentType.JSON)

		request
	}

	def deleteFunc(index: String, typeStr: String, id: String): DeleteResponse ={
		val highClient = getHighClient()
		val request = new DeleteRequest(index, typeStr, id)

		highClient.delete(request)
	}

	def updateFunc(index: String, typeStr: String, id: String, json: JSONObject): UpdateResponse ={
		val highClient = getHighClient()
		val request = new UpdateRequest(index, typeStr, id).doc(json, XContentType.JSON)

		highClient.update(request)
	}

	def updateRequestFunc(index: String, typeStr: String, id: String, json: JSONObject): UpdateRequest ={
		val request = new UpdateRequest(index, typeStr, id).doc(json, XContentType.JSON)

		request
	}

	def bulkFunc(index: String, typeStr: String, jsonList: ListBuffer[JSONObject]): BulkResponse ={
		val highClient = getHighClient()
		val request = new BulkRequest()

		jsonList.foreach(json => request.add(new IndexRequest(index, typeStr).source(json, XContentType.JSON)))

		highClient.bulk(request)
	}

	def bulkUpdateRequestFunc(jsonList: ListBuffer[UpdateRequest]) ={
		val highClient = getHighClient()
		val request = new BulkRequest()

		jsonList.foreach(updateRequest => request.add(updateRequest))

		highClient.bulk(request)
	}

	def bulkInsertRequestFunc(jsonList: ListBuffer[IndexRequest]) ={
		jsonList.sliding(800, 800).foreach(list => {
			val highClient = getHighClient()
			val request = new BulkRequest()
			list.foreach(insertRequest => request.add(insertRequest))
			highClient.bulk(request)

		})


	}

	def searchFunc(index: String, typeStr:String, key: String, value: String) ={
		val highClient = getHighClient()

		val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
		searchSourceBuilder.query(QueryBuilders.matchQuery(key, value))
		searchSourceBuilder.from(0)
		searchSourceBuilder.size(100)


		val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引"assist-md5-2019.06.18"
		searchRequest.types(typeStr)
		searchRequest.source(searchSourceBuilder)

		val response: SearchResponse = highClient.search(searchRequest)

		response.getHits.getHits
	}

	def searchScrollRangeFunc(index: String, typeStr:String, key1: String, value1: String, key2: String, value2: String): Array[SearchHit] ={
		val highClient = getHighClient()

		val searchSourceBuilder = new SearchSourceBuilder();//大多数查询参数要写在searchSourceBuilder里
		searchSourceBuilder.query(QueryBuilders.rangeQuery(key1).from(value1)).query(QueryBuilders.matchQuery(key2, value2))
		searchSourceBuilder.size(10000)

		val searchRequest = new SearchRequest(index); //构造search request .在这里无参，查询全部索引
		searchRequest.types(typeStr)
		searchRequest.source(searchSourceBuilder)
		searchRequest.scroll(TimeValue.timeValueMinutes(1L))//设置scroll间隔 ----

		var searchResponse: SearchResponse = highClient.search(searchRequest)

		var scrollId = searchResponse.getScrollId

		var hits: Array[SearchHit] = searchResponse.getHits.getHits

		var hitsArray: Array[SearchHit] = Array[SearchHit]()
		hitsArray = hitsArray.++(hits)

		val scroll = new Scroll(TimeValue.timeValueMinutes(1L))
		while(hits != null && hits.size>0){
			val scrollRequest = new SearchScrollRequest(scrollId)
			scrollRequest.scroll(scroll)
			searchResponse = highClient.searchScroll(scrollRequest)

			scrollId = searchResponse.getScrollId()
			hits = searchResponse.getHits.getHits

			hitsArray = hitsArray.++(hits)
		}

		val clearScrollRequest = new ClearScrollRequest()
		clearScrollRequest.addScrollId(scrollId)
		highClient.clearScroll(clearScrollRequest)

		hitsArray
	}

	def main(args: Array[String]) {
		while(true){
			println(assistNginxIndex)


			Thread.sleep(100)
		}

	}


}


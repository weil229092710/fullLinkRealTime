import java.io.IOException
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.{Arrays, Date, Locale, TimeZone}

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.xuehai.utils._
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{ClearScrollRequest, SearchRequest, SearchResponse, SearchScrollRequest}
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.{Scroll, SearchHit}
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object UserServerMain extends Constants{


//将用户信息加载到内存中
	val quUserInfoSql = "select student_id, school_name,class_name,student_name from fact_student_info_distinct_daily "
	var userInfoMap = new mutable.HashMap[Int, JSON]()

	val results: ResultSet = MysqlUtils.select1(quUserInfoSql)

	while (results.next()) {
		val json = JSON.parseObject("{}")
		val user_id = results.getInt(1)
		json.put("user_id", results.getInt(1))
		json.put("school_name", results.getString(2))
		json.put("class_name", results.getString(3))
		json.put("student_name", results.getString(4))
		userInfoMap += (user_id -> json)
	}
	val quModelSql = "select iUserId,  GROUP_CONCAT(distinct sModel SEPARATOR ',') as bb from XHSys_AccountDeviceLocked \ngroup by  iUserId"
	var emMap = new mutable.HashMap[Int,String]()
	val results1: ResultSet = MysqlUtils.select(quModelSql)
	while (results1.next()) {
		val user_id = results1.getInt(1)
		val sModel = results1.getString(2)
		emMap += (user_id -> sModel)
	}





	def main(args: Array[String]) {

		val kafkaConsumer = new KafkaConsumer[String, String](kafkaParams.asJava)

		kafkaConsumer.subscribe(Arrays.asList(userServerTopic))

		println("starting.......")
		while(true){
			try{
				nginxStart(kafkaConsumer.poll(3000))
			}catch {
				case e: IOException => {e.printStackTrace()}
				case e: Exception => {
					// java.lang.OutOfMemoryError
					e.printStackTrace()
				//	Utils.dingDingRobot("all", "full link: %s, %s".format(index, e.getMessage))
				}
			}
		}

	}

	def nginxStart(records: ConsumerRecords[String, String]): Unit ={
		try{
			val nginxRequestList: ListBuffer[IndexRequest] = ListBuffer[IndexRequest]()



			import scala.collection.JavaConversions._
			for (record <- records) {
				try{
					var nginxJson: JSONObject=JSON.parseObject("{}")
					val nginxStr: String = record.value
					if((nginxStr.contains("com.xh.zhitongyunstu")||nginxStr.contains("com.xh.zhitongyuntch") ||
							nginxStr.contains("com.xuehai.response_launcher_teacher")||nginxStr.contains("com.xuehai.launcher"))
						  &&(nginxStr.contains("INSTALLED")||nginxStr.contains("UPGRADED"))) {
						nginxJson = nginxParse(nginxStr)
					}

					if(nginxJson.size()>0){
						val insertRequest = ESUtils.insertRequestFunc("app_service_"+LocalDate.now(), DetailType, nginxJson)
						nginxRequestList.append(insertRequest)
					}

				}catch {
					case e: NullPointerException => {}
					case e: Exception => {
						println(record.value)
						e.printStackTrace()
					}
				}
			}

			// 1;初始化宽表nginx
			if (nginxRequestList.nonEmpty) ESUtils.bulkInsertRequestFunc(nginxRequestList)
		}catch {
			case e: Exception => {
				e.printStackTrace()
			}
		}
	}

	def nginxParse(nginxStr: String): JSONObject ={
		// nginx
		val obj: JSONObject = JSON.parseObject("{}")
		val json=	JSON.parseObject(nginxStr)
		val sql_type=json.getString("op")
		val table=json.getString("ns")
		val array: JSONArray = JSON.parseArray(json.getString("o"))
		for( a <- 0 to array.size()-1) {

			val data = array.get(a).asInstanceOf[JSONObject]
			if (table=="AppService.user_app_changes_logs" && sql_type == "i") {

       val key_data=data.getString("Name")
				key_data match {
					case "userId" => {
						val userId = data.getInteger("Value")
						obj.put("userId", userId.toString)
						val userInfo=getUserInfo(userId)
						obj.put("student_name",userInfo.getString("student_name"))
						obj.put("school_name",userInfo.getString("school_name"))
						obj.put("class_name",userInfo.getString("class_name"))
						obj.put("sModel",userInfo.getString("sModel"))
					}
					case "deviceId" => {
						val deviceId = data.getString("Value")
						obj.put("deviceId", deviceId)
					}
					case "packageName" => {
						val packageName = data.getString("Value")
						obj.put("packageName", packageName)
					}
					case "type" => {
						val type1 = data.getString("Value")
						obj.put("type", type1)
					}
					case "platformVersion" => {
						val platformVersion = Utils.null2Str(data.getString("Value"))
						obj.put("platformVersion", platformVersion)
					}
					case "versionCode" => {
						val versionCode = data.getString("Value")
						obj.put("versionCode", versionCode)
					}
					case "oldVersionCode" => {
						val oldVersionCode = data.getString("Value")
						obj.put("oldVersionCode", oldVersionCode)
					}
					case "changeAt" => {
						val changeAt = data.getString("Value")
						obj.put("changeAt", changeAt)
					}
					case "createdAt" => {
						val changeAt = data.getString("Value")
						obj.put("changeAt", changeAt)

					}
					case "osDisplay" => {
						val osDisplay = data.getString("Value")
						obj.put("osDisplay", osDisplay)
					}
					case _ => {}
				}
			}
		}

		obj
	}

//获取用户信息
	def getUserInfo(userId: Int): JSONObject = {
		val obj: JSONObject = JSON.parseObject("{}")
		val userinfo = userInfoMap.getOrElse(userId, "信息错误")
		var student_name=""
		var school_name=""
		var class_name=""
		var sModel=""
		if (userinfo != "信息错误") {
			student_name=	userinfo.asInstanceOf[JSONObject].getString("student_name")
			school_name=	userinfo.asInstanceOf[JSONObject].getString("school_name")
			class_name=	userinfo.asInstanceOf[JSONObject].getString("class_name")
		}
		if (userinfo == "信息错误") {
			val quUserInfoSql = "select student_id, school_name,class_name,student_name from fact_student_info_distinct_daily where  student_id ="+userId
			val results2: ResultSet = MysqlUtils.select1(quUserInfoSql)
			while (results2.next()) {
				school_name= results2.getString(2)
				class_name=results2.getString(3)
				student_name=results2.getString(4)
			}
		}
		 sModel = emMap.getOrElse(userId, "")
		if (sModel == "") {
			val quModelSql = "select iUserId,  GROUP_CONCAT(distinct sModel SEPARATOR ',') as bb from XHSys_AccountDeviceLocked where iUserId="+userId+" group by  iUserId"
			val results1: ResultSet = MysqlUtils.select(quModelSql)
			while (results1.next()) {
				sModel = results1.getString(2)
			}
		}
		obj.put("userId",userId)
		obj.put("student_name",student_name)
		obj.put("school_name",school_name)
		obj.put("class_name",class_name)
		obj.put("sModel",sModel)
		obj
	}
}

package com.xuehai.utils

import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Created by Administrator on 2019/5/10 0010.
  */
trait Constants {
	val LOG = LoggerFactory.getLogger(this.getClass)

	/**
	  * kafka
	  */
	val assistTopic = PropertiesUtil.getKey("assist_topic")
	val apmlogTopic = PropertiesUtil.getKey("apm-log")
	val nginxTopic = PropertiesUtil.getKey("nginx_topic")
	val xhjvmTopic = PropertiesUtil.getKey("xhjvm_topic")
	val brokerList = PropertiesUtil.getKey("brokerList")
	val userServerTopic = PropertiesUtil.getKey("userServerTopic")
  val OperateTopic = PropertiesUtil.getKey("OperateTopic")
  val mysql_binlog = PropertiesUtil.getKey("topicName")
	val platLoginTopic = PropertiesUtil.getKey("platLoginTopic")

  val kafkaParams: Map[String, Object] = Map(
		"bootstrap.servers" -> brokerList,
		"auto.offset.reset" -> "latest", // latest, earliest, none
		"enable.auto.commit" -> "false",
		"group.id" -> PropertiesUtil.getKey("kafkaGroupId"),
    "max.poll.records" -> "20000",  //2w条
		"max.partition.fetch.bytes" -> "104857600", //100MB
    "heartbeat.interval.ms" -> "120000",    //3min
		"session.timeout.ms" -> "300000",   //5min
		"request.timeout.ms" -> "360000",   //12min
    "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
		"value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
	)

	/**
	  * redis
	  */
	val host = PropertiesUtil.getKey("host")
	val port = PropertiesUtil.getKey("port").toInt
	val db = PropertiesUtil.getKey("db").toInt
	val MAX_ACTIVE = 1024
	val MAX_IDLE = 200
	val MAX_WAIT = 10000000
	val TIMEOUT = 10000000
	val TEST_ON_BORROW = true
	val  start_time=1633708800000L


	/**
	  * ES数据的index和type
	  **/
	val hostAndPort = PropertiesUtil.getKey("host_port")

	val assistIndex = PropertiesUtil.getKey("assist_index")
	val assistType = PropertiesUtil.getKey("assist_type")
	val jvmIndex = PropertiesUtil.getKey("jvm_index")
	val jvmType = PropertiesUtil.getKey("jvm_type")
	val nginxIndex = PropertiesUtil.getKey("nginx_index")
	val nginxType = PropertiesUtil.getKey("nginx_type")

	val assistJvmIndex = PropertiesUtil.getKey("assist_jvm_index")
	val assistJvmType = PropertiesUtil.getKey("assist_jvm_type")
	val assistNginxIndex = PropertiesUtil.getKey("assist_nginx_index")
	val assistNginxType = PropertiesUtil.getKey("assist_nginx_type")
	val nginxDetailIndex = PropertiesUtil.getKey("nginx_detail_index")
	val nginxDetailType = PropertiesUtil.getKey("nginx_detail_type")
	val DetailType = PropertiesUtil.getKey("detail_type")



	/**
		* mysql
		*/
	val mysqlHost = PropertiesUtil.getKey("mysql_host")
	val mysqlPort = PropertiesUtil.getKey("mysql_port")
	val mysqlUser = PropertiesUtil.getKey("mysql_user")
	val mysqlPassword = PropertiesUtil.getKey("mysql_password")
	val mysqlDB = PropertiesUtil.getKey("mysql_db")
	val mysqlUtilsUrl = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(mysqlHost, mysqlPort, mysqlDB)



	/**
		* 云mysql
		*/
	val Host = PropertiesUtil.getKey("Host")
	val Port = PropertiesUtil.getKey("Port")
	val User = PropertiesUtil.getKey("User")
	val Password = PropertiesUtil.getKey("Password")
	val DB = PropertiesUtil.getKey("DB")
	val Url = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host, Port, DB)






	/**
		* 云mysql
		*/
	val Host1 = PropertiesUtil.getKey("Host1")
	val Port1 = PropertiesUtil.getKey("Port1")
	val User1 = PropertiesUtil.getKey("User1")
	val Password1 = PropertiesUtil.getKey("Password1")
	val DB1 = PropertiesUtil.getKey("DB1")
	val Url1 = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host1, Port1, DB1)


  /**
    * 云mysql
    */
  val Host2 = PropertiesUtil.getKey("Host2")
  val Port2 = PropertiesUtil.getKey("Port2")
  val User2 = PropertiesUtil.getKey("User2")
  val Password2 = PropertiesUtil.getKey("Password2")
  val DB2 = PropertiesUtil.getKey("DB2")
  val Url2 = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host2, Port2, DB2)



	val Host3 = PropertiesUtil.getKey("Host3")
	val Port3 = PropertiesUtil.getKey("Port3")
	val User3 = PropertiesUtil.getKey("User3")
	val Password3 = PropertiesUtil.getKey("Password3")
	val DB3 = PropertiesUtil.getKey("DB3")
	val Url3 = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host3, Port3, DB3)



	val Host4 = PropertiesUtil.getKey("Host4")
	val Port4 = PropertiesUtil.getKey("Port4")
	val User4 = PropertiesUtil.getKey("User4")
	val Password4 = PropertiesUtil.getKey("Password4")
	val DB4 = PropertiesUtil.getKey("DB4")
	val Url4 = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host4, Port4, DB4)



	val Host5 = PropertiesUtil.getKey("Host5")
	val Port5 = PropertiesUtil.getKey("Port5")
	val User5 = PropertiesUtil.getKey("User5")
	val Password5 = PropertiesUtil.getKey("Password5")
	val DB5 = PropertiesUtil.getKey("DB5")
	val Url5 = "jdbc:mysql://%s:%s/%s?autoReconnect=true&characterEncoding=utf8".format(Host5, Port5, DB5)




	/**
	  * 钉钉机器人
	  */
	val DingDingUrl = PropertiesUtil.getKey("dingding_url")



	val graidMap = new mutable.HashMap[Int, Int]()
	graidMap+=(1 ->11)
	graidMap+=(2 ->12)
	graidMap+=(3 ->13)
	graidMap+=(4 ->14)
	graidMap+=(5 ->15)
	graidMap+=(6 ->16)
	graidMap+=(7->21)
	graidMap+=(8 ->22)
	graidMap+=(9 ->23)
	graidMap+=(10 ->31)
	graidMap+=(11 ->32)
	graidMap+=(12 ->33)


	val gMap = new mutable.HashMap[Int, String]()
	gMap+=(1 ->"一年级")
	gMap+=(2 ->"二年级")
	gMap+=(3 ->"三年级")
	gMap+=(4->"四年级")
	gMap+=(5 ->"五年级")
	gMap+=(6 ->"六年级")
	gMap+=(7->"七年级")
	gMap+=(8->"八年级")
	gMap+=(9->"九年级")
	gMap+=(10->"高中一年级")
	gMap+=(11->"高中二年级")
	gMap+=(12->"高中三年级")



	val grsMap = new mutable.HashMap[Int, Int]()
	grsMap+=(11 ->1)
	grsMap+=(12 ->2)
	grsMap+=(13 ->3)
	grsMap+=(14->4)
	grsMap+=(15 ->5)
	grsMap+=(16 ->6)
	grsMap+=(21->7)
	grsMap+=(22->8)
	grsMap+=(23->9)
	grsMap+=(31->10)
	grsMap+=(32->11)
	grsMap+=(33->12)

	val mongoHost = PropertiesUtil.getKey("mongo_host")
	val mongoPort = PropertiesUtil.getKey("mongo_port")
	val mongoUser = PropertiesUtil.getKey("mongo_user")
	val mongoPassword = PropertiesUtil.getKey("mongo_password")
	val mongoDB = PropertiesUtil.getKey("mongo_db")
	val mongoCollection = PropertiesUtil.getKey("mongo_collection")


  //预发布
	//val mongoUrl="mongodb://%s:%s/%s.%s".format(mongoHost, mongoPort, mongoDB, mongoCollection)

	// 生产环境
	val mongoUrl="mongodb://%s:%s@%s:%s/%s.%s".format(mongoUser, mongoPassword, mongoHost, mongoPort, mongoDB, mongoCollection)

	// 测试环境：需要认证
	//val mongoUrl= "mongodb://%s:%s@%s:%s/%s.%s?authSource=admin".format(mongoUser, mongoPassword, mongoHost, mongoPort, mongoDB, mongoCollection)

	// 开发环境：无需认证，且版本低于3.0
	//val mongoUrl= "mongodb://%s:%s/%s.%s".format(mongoHost, mongoPort, mongoDB, mongoCollection)



	val MontorMap = new mutable.HashMap[String, String]()
	MontorMap+=("CONTROL_RESTORE_FACTORY" ->"恢复出厂设置")
	MontorMap+=("CONTROL_USB_DEBUG" ->"USB调试")
	MontorMap+=("CONTROL_USB_MTP" ->"USB文件传输")
	MontorMap+=("CONTROL_EXTERNAL_SDCARD_ENABLED"->"外置SD可用")
	MontorMap+=("CONTROL_MULTI_USER_ALLOWED" ->"允许多用户")



}
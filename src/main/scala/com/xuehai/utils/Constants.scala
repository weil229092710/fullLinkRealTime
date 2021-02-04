package com.xuehai.utils

import org.slf4j.LoggerFactory

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
	  * 钉钉机器人
	  */
	val DingDingUrl = PropertiesUtil.getKey("dingding_url")
}
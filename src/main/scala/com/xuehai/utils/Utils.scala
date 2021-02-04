package com.xuehai.utils

import java.security.MessageDigest

import com.alibaba.fastjson.{JSONObject, JSON}
import com.dingtalk.api.DefaultDingTalkClient
import com.dingtalk.api.request.OapiRobotSendRequest
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Administrator on 2019/5/23 0023.
  */
object Utils extends Constants{
	/**
	  * null 转"null"，非空则直接转String，防止空指针异常
	  *
	  * @return
	  */
	def null2Str(x: Any): String ={
		if(x == null) return ""
		else x.toString
	}

	def null2Double(x: Any): Double ={
		try{
			x.toString.replace("MB", "").toDouble
		}catch {
			case e:Exception => 0.toDouble
		}
	}

	/**
	  * null 转0，计算kafka的offset使用
	  *
	  * @param x
	  * @return
	  */
	def null20(x: Any): Int ={
		if(x == null) return 0
		else x.toString.toInt
	}

	/**
	  * null 转负数 -1，计算kafka的offset使用
	  *
	  * @param x
	  * @return
	  */
	def null2Negative(x: Any): Long ={
		if(x == null) return -1
		else x.toString.toLong
	}


	def map2json(map: mutable.Map[String, AnyRef]): JSONObject ={
		val json = JSON.parseObject("{}")
		map.foreach(x => json.put(x._1, x._2))

		json
	}


	/**
	  * MD5加密
	  * @param content 需要被加密的字符串
	  * @return
	  */
	def hashMD5(content: String): String = {
		var contentBytes: Array[Byte] = "".getBytes
		if(null != content) contentBytes=content.getBytes

		val md5 = MessageDigest.getInstance("MD5")
		val encoded = md5.digest(contentBytes)
		encoded.map("%02x".format(_)).mkString
	}


	/**
	  * 钉钉机器人消息推送
	  *
	  * @param user "all"-@所有人，"18810314189,13724612033"-@指定的人，以逗号分割
	  * @param massage 推送的消息
	  */
	def dingDingRobot(user: String, massage: String): Unit ={
		val text = new OapiRobotSendRequest.Text()
		text.setContent(massage)

		val at = new OapiRobotSendRequest.At()
		if(user=="all"){
			at.setIsAtAll("true")
		}else{
			at.setAtMobiles(user.split(",").toList.asJava)
		}

		val request = new OapiRobotSendRequest()
		request.setMsgtype("text")
		request.setText(text)
		request.setAt(at)

		val client = new DefaultDingTalkClient(DingDingUrl)
		client.execute(request)
	}

	def main(args: Array[String]) {
		dingDingRobot("18810314189", "测试")

	}
}

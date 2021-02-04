package com.xuehai.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

import com.alibaba.fastjson.JSON
import org.elasticsearch.client.RestHighLevelClient

object test {
  def main(args: Array[String]): Unit = {
    //val time=Utils.toLoclTime(time_local)

    val value = "{\n\t\"appId\": \"WEB002001\",\n\t\"appVersion\": \"2\",\n\t\"data\": [{\n\t\t\"login_id\": \"29070\",\n\t\t\"account\": \"ls50\",\n\t\t\"school\": \"智通学校\",\n\t\t\"username\": \"蔡瑞琼\",\n\t\t\"login_ip\": \"192.168.40.51\",\n\t\t\"login_type\": \"账号密码登录\",\n\t\t\"login_at\": \"2021-02-01 09:26:14\",\n\t\t\"device\": \"pc\",\n\t\t\"browser_type\": \"Safari 537.36\"\n\t},\n\t\t\t {\n\t\t\"login_id\": \"29070\",\n\t\t\"account\": \"ls50\",\n\t\t\"school\": \"智通学校\",\n\t\t\"username\": \"蔡瑞琼\",\n\t\t\"login_ip\": \"192.168.40.51\",\n\t\t\"login_type\": \"账号密码登录\",\n\t\t\"login_at\": \"2021-02-01 09:26:14\",\n\t\t\"device\": \"pc\",\n\t\t\"browser_type\": \"Safari 537.36\"\n\t},\n\t\t\t {\n\t\t\"login_id\": \"29070\",\n\t\t\"account\": \"ls50\",\n\t\t\"school\": \"智通学校\",\n\t\t\"username\": \"蔡瑞琼\",\n\t\t\"login_ip\": \"192.168.40.51\",\n\t\t\"login_type\": \"账号密码登录\",\n\t\t\"login_at\": \"2021-02-01 09:26:14\",\n\t\t\"device\": \"pc\",\n\t\t\"browser_type\": \"Safari 537.36\"\n\t}\n\t\t\t],\n\t\"name\": \"10285\",\n\t\"time\": 1612142774902,\n\t\"version\": 2\n}"
    val str = JSON.parseObject(value)
    //println(str)
    val array = str.getJSONArray("data")
    val size=array.size()
    for (i <- 0 until size) {
      println(array.get(i))
    }
    println(LocalDate.now())
    val l = System.currentTimeMillis()

    import java.text.SimpleDateFormat
//    val sdf = new SimpleDateFormat("yyyy-MM-dd")
//    System.out.println(sdf.format(new Nothing))
    //获取当前时间戳,也可以是你自已给的一个随机的或是别人给你的时间戳(一定是long型的数据)
    val timeStamp = System.currentTimeMillis
    //这个是你要转成后的时间的格式
    val sdff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeStamp)
    // 时间戳转换成时间
    //val sd = sdff.format(timeStamp)
    System.out.println(sdff) //打印出你要的时间


  }
}



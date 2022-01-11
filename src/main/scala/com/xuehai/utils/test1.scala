package com.xuehai.utils

import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.mongodb.DBObject
import com.mongodb.casbah.Imports.MongoDBObject

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

object test1 extends  Constants {
  def main(args: Array[String]): Unit = {
    //    val accountSet: mutable.Set[Int] = mutable.Set()
    //
    //    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
    //    val watch_account = MongoUtils.getMongoConnection().getCollection("watch_account")
    ////    watch_account.find().iterator().asScala.foreach((obj: DBObject) =>
    ////      //if(obj.get("status").toString.toInt==1)
    ////        accountSet.add(obj.get("userId").toString.toInt)
    ////    )
    //    watch_account.find().iterator().asScala.foreach((obj: DBObject) =>   if(obj.get("status").toString.toInt==1) accountSet.add(obj.get("userId").toString.toInt))
    //
    //    //val queryAccount = MongoDBObject("userId" -> 151366,"status"->1)
    //    val queryAccount = MongoDBObject("status"->1)
    //
    //    val count = watch_account.find(queryAccount).count()
    //    println(count)

    //println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))


//    println(getDeviceInfo("355896052737170"))
//
//    println(getOldBindTimeByDeviceIdAndUserId("355896052737170","32236"))
//

    //equipment_number='R32KC0094JP' and  user_id=152415  and  login_time> '2021-10-15 13:25:35' ORDER BY  login_time asc  limit 1

   // println(getBrushCode("R32M1001G6Y"))

//   val code_time=getBrushCode("R32KC0094JP")
//   println(getPlatLoginTime("R32KC0094JP", 152415, code_time))


//    println(isNotCodeTime("", "2021-10-15 13:25:35", ""))
//    println(isNotCodeTime("", "2021-10-15 13:25:35", "2021-10-16 13:25:35"))
//    println(isNotCodeTime("", "2021-10-15 13:25:35", "2021-10-14 13:25:35"))
//    println(isNotCodeTime("2021-10-16 13:25:35", "2021-10-15 13:25:35", ""))
//    println(isNotCodeTime("2021-10-14 13:25:35", "2021-10-15 13:25:35", ""))
//    println(isNotCodeTime("2021-10-16 13:25:35", "2021-10-15 13:25:35", "2021-10-16 13:25:35"))
//    println(isNotCodeTime("2021-10-14 13:25:35", "2021-10-15 13:25:35", "2021-10-14 13:25:35"))
//    println(isNotCodeTime("2021-10-14 13:25:35", "2021-10-15 13:25:35", "2021-10-16 13:25:35"))
//    println(isNotCodeTime("2021-10-16 13:25:35", "2021-10-15 13:25:35", "2021-10-14 13:25:35"))


    val t: (Int, Int, Int, Int) = (4, 3, 2, 1)


    t._1
   // t.productIterator.foreach{ i =>println("Value = " + i )}

    val tuple = (BigInt("1625991635000"),BigInt("1625991635000"))

   // println(tuple._1)
    val tuple1 = (BigInt("1645991635000"),BigInt("1655991635000"))

    val buffer1: ArrayBuffer[(BigInt, BigInt)] = ArrayBuffer[(BigInt,BigInt)]()
    buffer1 += tuple
    buffer1 += tuple1


    for (elem <- buffer1) {
    //  println(elem._1 + elem._2)
    }

    //t.productIterator.foreach{ i =>println("Value = " + i )}


    val tuples: ArrayBuffer[(BigInt, BigInt)] = getRepairTime("123")
    for (elem <- tuples) {
//      println(elem._1)
//      println(elem._2)
    }


    val tuples1: ArrayBuffer[(BigInt, BigInt)] = getRepairTime("123")
    //println(isRepireTime(tuples1, "2019-06-03 04:00:00"))
    //println(isRepireTime(tuples1, "2021-11-04 14:00:00"))


//     val aa=true
//     val bb=false
//     var cc=0
//    if(aa==false)  cc=3
//
////   (if (bool == true) 2 else 3)
//
//
//    println(isLastDate(1634572800000L))
//    println(isLastDate(1633536000000L))


    println(getBrushCodeTime("R22J80064HM", "2021-06-09 13:51:30"))

  }


  //根据设备获取信息
  def getDeviceInfo(deviceId: String): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")
    var smodel = ""
    var tcreateTime = ""
    var user_id = ""
    val quUserInfoSql = "select sdevicenumber device_id,  smodel,iuserid user_id,  date_format(tcreatedate, '%Y-%m-%d %H:%i:%s')  from  XHSys_AccountDeviceLocked  a\nwhere bdelete=0 and  sdevicenumber=  '" + deviceId + "'" +" order by  tcreatedate desc limit 1"
    val results2: ResultSet = MysqlUtils.select4(quUserInfoSql)
    while (results2.next()) {
      smodel = results2.getString(2)
      tcreateTime = results2.getString(4)
      user_id = results2.getString(3)
    }
    obj.put("smodel", smodel)
    obj.put("tcreateTime", tcreateTime)
    obj.put("user_id", user_id)
    obj
  }


  //通过设备id和用户id获取旧的设备绑定时间
  def getOldBindTimeByDeviceIdAndUserId(deviceId: String, userId: String): String = {
    var time = ""
    val quModelSql = " select   date_format(tcreatedate, '%Y-%m-%d %H:%i:%s') AS createdAt   from  XHSys_AccountDeviceLocked a where   a.sdevicenumber= '" + deviceId + "'" +"and iuserid=" +userId +" ORDER BY tcreatedate asc  limit 1 "
      val results1: ResultSet = MysqlUtils.select4(quModelSql)
      while (results1.next()) {
        time = results1.getString(1)
      }

    time
  }






  //是否是一个月之前的安装数据
  def isLastDate( changeTime: Long): Boolean = {
    var bool: Boolean = false
    if(changeTime-start_time<=0){
      bool=true
    }
    bool
  }


  def getBrushCodeTime(deviceId: String,change_time:String): String = {
    var time = ""
    val quModelSql = "SELECT   date_format(used_date, '%Y-%m-%d %H:%i:%s') AS  used_date from  brush_code where status=1 and  device_id =  '" + deviceId + "'" +" and  used_date< '" + change_time + "' " +  " ORDER BY used_date desc limit 1"
    val results1: ResultSet = MysqlUtils.select5(quModelSql)
    while (results1.next()) {
      time = results1.getString(1)
    }

    time
  }



  //根据设备号和用户id和时间查询出扫码后最近一次登录智通云平台的数据
  def getPlatLoginTime(deviceId: String, userId: Int,login_time:String): String = {
    var time = ""
    val quModelSql = " select  date_format(login_time, '%Y-%m-%d %H:%i:%s') AS  login_time  from   plat_login_device a where equipment_number= '" + deviceId + "'" +" and user_id=" +userId +" and  login_time> '" + login_time + "' " + "ORDER BY  login_time asc  limit 1 "
    val results1: ResultSet = MysqlUtils.select3(quModelSql)
    while (results1.next()) {
      time = results1.getString(1)
    }
    time
  }




  //判断是否在维修时间
  def isNotCodeTime(codeTime: String, changeTime: String,login_time:String): Boolean = {
    var bool: Boolean = false
    //当刷机码为空，且登录平台时间为空或者登录时间大于安装时间
    if(codeTime!=""&&(codeTime<changeTime&&(login_time>changeTime||login_time==""))){
      bool=true
    }
    bool
  }



  def getRepairTime(deviceId: String): ArrayBuffer[(BigInt, BigInt)] = {
    val tuples: ArrayBuffer[(BigInt, BigInt)] = ArrayBuffer[(BigInt,BigInt)]()
    val quModelSql = " SELECT a.start_time,a.end_time FROM yj_repair_time a where a.equipment_number= '" + deviceId + "'"
    val results: ResultSet = MysqlUtils.select3(quModelSql)
    while (results.next()) {
      val   start_time: BigInt = BigInt(results.getLong(1))
      val   end_time = BigInt(results.getLong(2))
      val   timeT: (BigInt, BigInt) = (start_time,end_time)
      tuples+=timeT
    }
    tuples
  }




  //判断是否在维修期间时间
  def isRepireTime(turple:ArrayBuffer[(BigInt, BigInt)],change_time:String): Boolean = {
    var bool: Boolean = false
    val time: Long = DateUtil.Date2Long(change_time)
    //遍历该设备维修数据,看安装时间在维修期间，或者维修未完成时end_time 等于0
    val loop = new Breaks;
    loop.breakable {
      for (elem <- turple) {
        if (time > elem._1 && (time < elem._2 || elem._2 == 0)) {
          bool = true
          loop.break;
        }
      }
    }
    bool
  }





}

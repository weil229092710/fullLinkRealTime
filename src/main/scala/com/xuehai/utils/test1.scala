package com.xuehai.utils

import java.text.SimpleDateFormat
import java.util.Date

import com.mongodb.DBObject
import com.mongodb.casbah.Imports.MongoDBObject

import scala.collection.JavaConverters._
import scala.collection.mutable

object test1 {
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

    println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))

  }

}

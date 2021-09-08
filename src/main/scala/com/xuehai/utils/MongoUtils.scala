package com.xuehai.utils

import java.sql.{Connection, DriverManager, ResultSet}

import com.mongodb.casbah.Imports
//import com.xuehai.utils.MongoTool.{mongoCollection, mongoDB, mongoHost, mongoPassword, mongoPort, mongoUser}

import scala.collection.mutable


/**
  * Created by Administrator on 2019/5/22 0022.
  */
object MongoUtils extends Constants{

    import com.mongodb.casbah.Imports._

    def getMongoConnection():MongoDB ={
        try{
            val uri = MongoClientURI(mongoUrl)
            val   mongoClient=  MongoClient(uri)
            val db = mongoClient(mongoDB)
            db
        }catch {
            case e: Exception => {
                print("mongo connection failed！！！！", e)
                val uri = MongoClientURI(mongoUrl)
                val   mongoClient=  MongoClient(uri)
                val db = mongoClient(mongoDB)
                db
            }
        }
    }






//    def close(): Unit = {
//        try {
//            if (!conn.isClosed() || conn != null) {
//                conn.close()
//            }
//        }catch {
//            case ex: Exception => {
//                ex.printStackTrace()
//            }
//        }
//    }


}

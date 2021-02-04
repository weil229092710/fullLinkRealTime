package com.xuehai.utils

import java.sql.{Connection, DriverManager, ResultSet}

/**
  * Created by Administrator on 2019/5/22 0022.
  */
object MysqlUtils extends Constants{
    var conn: Connection = null
    var conn1: Connection = null
    def getMysqlConnection() ={
        try{
            Class.forName("com.mysql.jdbc.Driver")
            conn = DriverManager.getConnection(mysqlUtilsUrl, mysqlUser, mysqlPassword)
            LOG.info("mysql connection success!")
        }catch {
            case e: Exception => {
                LOG.error("mysql connection failed！！！！", e)
            }
        }

    }

    def select(sql: String): ResultSet ={
        var result: ResultSet = null
        try{
            result = conn.createStatement().executeQuery(sql)
        }catch {
            case e: Exception => {
                getMysqlConnection()
                result = conn.createStatement().executeQuery(sql)
            }
        }
       result
    }


    def getMysqlConnection1() ={
        try{
            Class.forName("com.mysql.jdbc.Driver")
            conn1 = DriverManager.getConnection(Url, User, Password)
            LOG.info("mysql connection success!")
        }catch {
            case e: Exception => {
                LOG.error("从mysql connection failed！！！！", e)
            }
        }

    }

    def select1(sql: String): ResultSet ={
        var result: ResultSet = null
        try{
            result = conn1.createStatement().executeQuery(sql)
        }catch {
            case e: Exception => {
                getMysqlConnection1()
                result = conn1.createStatement().executeQuery(sql)
            }
        }
        result
    }

    def update(sql: String) ={
        try{
            conn.createStatement().executeUpdate(sql)
        }catch {
            case e: Exception => {
                getMysqlConnection()
                conn.createStatement().executeUpdate(sql)
            }
        }
    }

    def close(): Unit = {
        try {
            if (!conn.isClosed() || conn != null) {
                conn.close()
            }
        }catch {
            case ex: Exception => {
                ex.printStackTrace()
            }
        }
    }
}

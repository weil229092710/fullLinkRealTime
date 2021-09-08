package com.xuehai.utils

import java.sql.{Connection, DriverManager, ResultSet}

/**
  * Created by Administrator on 2019/5/22 0022.
  */
object MysqlUtils extends Constants{
    var conn: Connection = null
    var conn1: Connection = null
    var conn2: Connection = null
     var conn3:Connection = null
  var conn4:Connection = null
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


    def getMysqlConnection2() ={
        try{
            Class.forName("com.mysql.jdbc.Driver")
            conn2 = DriverManager.getConnection(Url2, User2, Password2)
            LOG.info("mysql connection success!")
        }catch {
            case e: Exception => {
                LOG.error("从mysql connection failed！！！！", e)
            }
        }

    }

  def getMysqlConnection3() ={
    try{
      Class.forName("com.mysql.jdbc.Driver")
      conn3 = DriverManager.getConnection(Url3, User3, Password3)
      LOG.info("mysql connection success!")
    }catch {
      case e: Exception => {
        LOG.error("从mysql connection failed！！！！", e)
      }
    }

  }

  def getMysqlConnection4() ={
    try{
      Class.forName("com.mysql.jdbc.Driver")
      conn4 = DriverManager.getConnection(Url4, User4, Password4)
      LOG.info("mysql connection success!")
    }catch {
      case e: Exception => {
        LOG.error("从mysql connection failed！！！！", e)
      }
    }

  }



  def select4(sql: String): ResultSet ={
    var result: ResultSet = null
    try{
      result = conn4.createStatement().executeQuery(sql)
    }catch {
      case e: Exception => {
        getMysqlConnection4()
        result = conn4.createStatement().executeQuery(sql)
      }
    }
    result
  }

    def select2(sql: String): ResultSet ={
        var result: ResultSet = null
        try{
            result = conn2.createStatement().executeQuery(sql)
        }catch {
            case e: Exception => {
                getMysqlConnection2()
                result = conn2.createStatement().executeQuery(sql)
            }
        }
        result
    }

  def select3(sql: String): ResultSet ={
    var result: ResultSet = null
    try{
      result = conn3.createStatement().executeQuery(sql)
    }catch {
      case e: Exception => {
        getMysqlConnection3()
        result = conn3.createStatement().executeQuery(sql)
      }
    }
    result
  }

  def update3(sql: String):Unit ={
    try{
      conn3.createStatement().executeUpdate(sql)
    }catch {
      case e: Exception => {
        getMysqlConnection3()
        conn3.createStatement().executeUpdate(sql)
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

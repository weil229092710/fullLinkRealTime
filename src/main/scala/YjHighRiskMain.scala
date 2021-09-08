import java.io.IOException
import java.sql.{Connection, ResultSet}
import java.text.SimpleDateFormat
import java.util.{Arrays, Calendar, Date}

import CouponMain.{schoolInfoMap, schoolNameMap}
import YjHighRiskMain.getUserIdByDeviceid
import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.mongodb.DBObject
import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.casbah.MongoCollection
import com.xuehai.utils._
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object YjHighRiskMain extends Constants {

  var userInfoMap = new mutable.HashMap[Int, JSON]()

  var sModelMap = new mutable.HashMap[String, String]()
  var sBindTimeMap = new mutable.HashMap[String, String]()

  val Utc2Local = new DateUtils()


  val watch_account = MongoUtils.getMongoConnection().getCollection("watch_account")

  val watch_app = MongoUtils.getMongoConnection().getCollection("watch_app")

  val watch_system = MongoUtils.getMongoConnection().getCollection("watch_system")

  val accountSet: mutable.Set[Int] = mutable.Set()
  val appSet: mutable.Set[String] = mutable.Set()
  val systemSet: mutable.Set[String] = mutable.Set()

  try {



    //加载设备型号信息

    val quSmodelSql = "select sDeviceNumber,a.sModel,tCreateDate from XHSys_AccountDeviceLocked a where bDelete=0";

    val results1: ResultSet = MysqlUtils.select4(quSmodelSql)
    while (results1.next()) {
      val sDeviceNumber = results1.getString(1)
      val sModel = results1.getString(2)
      val bind = results1.getString(3)

      sModelMap += (sDeviceNumber -> sModel)
      sBindTimeMap+=(sDeviceNumber->bind)
    }


    import scala.collection.JavaConverters._


    watch_account.find().iterator().asScala.foreach((obj: DBObject) =>   if(obj.get("status").toString.toInt==1) accountSet.add(obj.get("userId").toString.toInt))
    watch_app.find().iterator().asScala.foreach((obj: DBObject) =>  if(obj.get("status").toString.toInt==1) appSet.add(Utils.null2Str(obj.get("packageName")).toString))
    watch_system.find().iterator().asScala.foreach((obj: DBObject) =>  if(obj.get("status").toString.toInt==1) systemSet.add(obj.get("version").toString))

  } catch {
    case e: IOException => {
      e.printStackTrace()
    }
    case e: Exception => {
      // java.lang.OutOfMemoryError
      e.printStackTrace()
      println("数据加载到内存信息时出错")
    }
  }


  def main(args: Array[String]) {

    val kafkaConsumer = new KafkaConsumer[String, String](kafkaParams.asJava)

    kafkaConsumer.subscribe(Arrays.asList(userServerTopic))

    println("starting.......")
    while (true) {
      try {
        nginxStart(kafkaConsumer.poll(3000))
      } catch {
        case e: IOException => {
          e.printStackTrace()
        }
        case e: Exception => {

          e.printStackTrace()

        }
      }
    }
  }

  import org.apache.commons.dbcp2.BasicDataSource

  val dataSource: BasicDataSource = new BasicDataSource
  var conn: Connection = getConnection(dataSource)

  val InsertStmtStatus = conn.prepareStatement("INSERT INTO yj_risk_detail (equipment_number, user_id, class_name, user_name, school_id, school_name, class_id, equipment_type, account, label, detail, label_type,  event_time, report_time,risk_level) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,3)")
  val InsertMontorStatus = conn.prepareStatement("INSERT INTO yj_risk_detail (equipment_number, user_id, user_name, school_id, school_name, class_id, class_name, equipment_type, account, label, detail, label_type, event_time, report_time, platform_version, mdm_version, os_display,risk_level) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,3)")
  val Insertrecord = conn.prepareStatement("INSERT INTO yj_monitor_record ( deviceId, CONTROL_RESTORE_FACTORY, CONTROL_USB_DEBUG, CONTROL_USB_MTP, CONTROL_EXTERNAL_SDCARD_ENABLED, CONTROL_MULTI_USER_EXIST, create_time) VALUES (?,?,?,?,?,?,?)")


  def nginxStart(records: ConsumerRecords[String, String]): Unit = {
    try {

      import scala.collection.JavaConversions._

      for (record <- records) {

        try {
          val json: JSONObject = JSON.parseObject(record.value())
          //println(json)
          val table = json.getString("ns")
          if ((table == "AppService.user_monitor_log" || table == "AppService.user_app_changes_logs")) {
            //  if ((table == "AppService.user_monitor_log" )) {

            val jSONObject = getMongoInfo(json)

            val table = jSONObject.getString("table")

            table match {
              //系统异常
              case "AppService.user_monitor_log" => {
                val userId = jSONObject.getInteger("userId")
                val deviceId = jSONObject.getString("deviceId")
                val platformVersion = jSONObject.getString("platformVersion")
                val mdmVersion = jSONObject.getString("mdmVersion")
                val model = jSONObject.getString("model")
                val osDisplay = jSONObject.getString("osDisplay")
                val monitorItems = JSON.parseArray(jSONObject.getString("monitorItems"))
                val createdDate = jSONObject.getLong("createdDate")
                val time=Utc2Local.MilltoLocal(createdDate)
                //用户维度信息
                val nObject = getUserInfo(userId)
                val iUserType=nObject.getInteger("iUserType")
                val history=getHistory(userId)
                val schoolId = nObject.getString("schoolId")
                val istatus = getIstatus(schoolId.toInt)
                if(iUserType==1&&history==0&&istatus==2) {

                  val userName = nObject.getString("userName")

                  val schoolName = nObject.getString("schoolName")
                  val class_id = nObject.getString("claass_id")
                  val class_name = nObject.getString("claass_name")
                  val account = nObject.getString("account")

                  if ((!systemSet.contains(osDisplay)) && (!getWatchSystem(osDisplay)) && (!getWatchAccount(userId))) {
                    //  if((!getWatchSystem(osDisplay))&&(!getWatchAccount(userId))){
                    // println(json)
                    val userid=getUserIdByDeviceid(deviceId)
                    if(userid==userId) {
                      InsertMontorStatus.setString(1, deviceId)
                      InsertMontorStatus.setInt(2, userId)
                      InsertMontorStatus.setString(3, userName)
                      InsertMontorStatus.setString(4, schoolId)
                      InsertMontorStatus.setString(5, schoolName)
                      InsertMontorStatus.setString(6, class_id)
                      InsertMontorStatus.setString(7, class_name)
                      InsertMontorStatus.setString(8, model)
                      InsertMontorStatus.setString(9, account)
                      InsertMontorStatus.setString(10, "系统版本异常")
                      InsertMontorStatus.setString(11, "当前系统版本" + osDisplay)
                      InsertMontorStatus.setInt(12, 6)
                      InsertMontorStatus.setString(13, time)
                      InsertMontorStatus.setString(14, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))
                      InsertMontorStatus.setString(15, platformVersion)
                      InsertMontorStatus.setString(16, mdmVersion)
                      InsertMontorStatus.setString(17, osDisplay)
                      InsertMontorStatus.execute()
                    }
                  }

                  //系统异常
                  //表中查询到的数据
                  val recordBuffer = getMonitorRecod(deviceId)
                  val count = recordBuffer.size
                  if (count == 1 && (model == "SM-P350" || model == "SM-P355C") && (!getWatchAccount(userId))) { //没有查到直接插进去
                    // if(count==1&&(model=="SM-P350"||model=="SM-P355C")&&(!getWatchAccount(userId))){  //没有查到直接插进去

                    for (a <- 1 to monitorItems.size() - 2) {

                      val data = monitorItems.get(a).asInstanceOf[JSONObject]
                      val abnomal = data.getString("abnormal")
                      if (abnomal == "true") {
                        val key = data.getString("key")
                        val value: String = MontorMap.get(key).get
                        val userid=getUserIdByDeviceid(deviceId)
                        if(userid==userId) {
                          InsertMontorStatus.setString(1, deviceId)
                          InsertMontorStatus.setInt(2, userId)
                          InsertMontorStatus.setString(3, userName)
                          InsertMontorStatus.setString(4, schoolId)
                          InsertMontorStatus.setString(5, schoolName)
                          InsertMontorStatus.setString(6, class_id)
                          InsertMontorStatus.setString(7, class_name)
                          InsertMontorStatus.setString(8, model)
                          InsertMontorStatus.setString(9, account)
                          InsertMontorStatus.setString(10, value) //后面换成标签
                          InsertMontorStatus.setString(11, "") //系统异常详情不用展示
                          InsertMontorStatus.setInt(12, a)
                          InsertMontorStatus.setString(13, time)
                          InsertMontorStatus.setString(14, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))
                          InsertMontorStatus.setString(15, platformVersion)
                          InsertMontorStatus.setString(16, mdmVersion)
                          InsertMontorStatus.setString(17, osDisplay)
                          InsertMontorStatus.execute()
                        }
                      }
                    }
                    ////将设备状态插入进去
                    val userid=getUserIdByDeviceid(deviceId)
                    if(userid==userId) {
                      val k1 = monitorItems.get(1).asInstanceOf[JSONObject].getString("abnormal")
                      val k2 = monitorItems.get(2).asInstanceOf[JSONObject].getString("abnormal")
                      val k3 = monitorItems.get(3).asInstanceOf[JSONObject].getString("abnormal")
                      val k4 = monitorItems.get(4).asInstanceOf[JSONObject].getString("abnormal")
                      val k5 = monitorItems.get(5).asInstanceOf[JSONObject].getString("abnormal")
                      Insertrecord.setString(1, deviceId)
                      Insertrecord.setString(2, k1)
                      Insertrecord.setString(3, k2)
                      Insertrecord.setString(4, k3)
                      Insertrecord.setString(5, k4)
                      Insertrecord.setString(6, k5)
                      Insertrecord.setString(7, time)
                      Insertrecord.execute()
                    }
                  }


                  //设备中已经有该数据
                  if (count > 1 && (model == "SM-P350" || model == "SM-P355C") && (!getWatchAccount(userId))) { //查到数据
                    //   if(count>1&&(model=="SM-P350"||model=="SM-P355C")&&(!getWatchAccount(userId))) { //查到数据

                    var keyBuffer = ArrayBuffer[String]("0")
                    var reportBuffer = ArrayBuffer[Int]()
                    for (a <- 1 to monitorItems.size() - 2) {
                      val data = monitorItems.get(a).asInstanceOf[JSONObject]
                      val abnomal = data.getString("abnormal")

                      keyBuffer += abnomal
                    }

                    // 找出表中状态为false，且这次记录为true的记录

                    for (a <- 1 to 5) {
                      if (recordBuffer(a) == "false" && keyBuffer(a) == "true") {
                        reportBuffer += a
                      }
                    }

                    // 找出仅这次上报异常的
                    for (a <- reportBuffer) {
                      val data = monitorItems.get(a).asInstanceOf[JSONObject]
                      val abnomal = data.getString("abnormal")
                      if (abnomal == "true") {
                        val key = data.getString("key")
                        val value: String = MontorMap.get(key).get
                        val userid=getUserIdByDeviceid(deviceId)
                        if(userid==userId) {
                          InsertMontorStatus.setString(1, deviceId)
                          InsertMontorStatus.setInt(2, userId)
                          InsertMontorStatus.setString(3, userName)
                          InsertMontorStatus.setString(4, schoolId)
                          InsertMontorStatus.setString(5, schoolName)
                          InsertMontorStatus.setString(6, class_id)
                          InsertMontorStatus.setString(7, class_name)
                          InsertMontorStatus.setString(8, model)
                          InsertMontorStatus.setString(9, account)
                          InsertMontorStatus.setString(10, value)
                          InsertMontorStatus.setString(11, "") //系统异常详情不用展示
                          InsertMontorStatus.setInt(12, a)
                          InsertMontorStatus.setString(13, time)
                          InsertMontorStatus.setString(14, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))
                          InsertMontorStatus.setString(15, platformVersion)
                          InsertMontorStatus.setString(16, mdmVersion)
                          InsertMontorStatus.setString(17, osDisplay)
                          InsertMontorStatus.execute()
                        }
                      }

                    }
                    //删除设倍表
                    val userid=getUserIdByDeviceid(deviceId)
                    if(userid==userId) {
                      deleteRecord(deviceId)
                      //处理record表
                      ////将设备状态插入进去
                      val k1 = monitorItems.get(1).asInstanceOf[JSONObject].getString("abnormal")
                      val k2 = monitorItems.get(2).asInstanceOf[JSONObject].getString("abnormal")
                      val k3 = monitorItems.get(3).asInstanceOf[JSONObject].getString("abnormal")
                      val k4 = monitorItems.get(4).asInstanceOf[JSONObject].getString("abnormal")
                      val k5 = monitorItems.get(5).asInstanceOf[JSONObject].getString("abnormal")
                      Insertrecord.setString(1, deviceId)
                      Insertrecord.setString(2, k1)
                      Insertrecord.setString(3, k2)
                      Insertrecord.setString(4, k3)
                      Insertrecord.setString(5, k4)
                      Insertrecord.setString(6, k5)
                      Insertrecord.setString(7, time)
                      Insertrecord.execute()
                    }
                  }
                }
              }


              //应用预警
              case "AppService.user_app_changes_logs" => {
                val packageName=jSONObject.getString("packageName")
                val  type1= jSONObject.getString("type")
                val  userId= jSONObject.getInteger("userId")
                if(type1=="INSTALLED"&&(!appSet.contains(packageName))&&(!getWatchApp(packageName))&&(!getWatchAccount(userId))) {

                  //  if(type1=="INSTALLED"&&(!getWatchApp(packageName))&&(!getWatchAccount(userId))) {

                  val deviceId = jSONObject.getString("deviceId")

                  val sModel = Utils.null2Str(getSmodel(deviceId))
                  val time = Utils.null2Str(getBindTime(deviceId)) //平台安装时间
                  val createdAt = jSONObject.getString("createdAt")
                  val eventTime = Utc2Local.UTCToCST(createdAt)

                  if (time!=""&&time < eventTime) {
                    val packageName = jSONObject.getString("packageName")
                    val appName = jSONObject.getString("appName")
                    val nObject = getUserInfo(userId)
                    val userName = nObject.getString("userName")
                    val schoolId = nObject.getString("schoolId")
                    val schoolName = nObject.getString("schoolName")
                    val claass_id = nObject.getString("claass_id")
                    val claass_name = nObject.getString("claass_name")
                    val account = nObject.getString("account")
                    val iUserType = nObject.getInteger("iUserType")
                    if (iUserType == 1) {
                      // println(json)
                      val userid=getUserIdByDeviceid(deviceId)
                      val history=getHistory(userid)
                      val istatus=getIstatus(schoolId.toInt)
                      if(userid==userId&&history==0&&istatus==2) {
                        InsertStmtStatus.setString(1, deviceId)
                        InsertStmtStatus.setInt(2, userId)
                        InsertStmtStatus.setString(3, claass_name)
                        InsertStmtStatus.setString(4, userName)
                        InsertStmtStatus.setString(5, schoolId)
                        InsertStmtStatus.setString(6, schoolName)
                        InsertStmtStatus.setString(7, claass_id)
                        InsertStmtStatus.setString(8, sModel)
                        InsertStmtStatus.setString(9, account)
                        InsertStmtStatus.setString(10, "应用预警")
                        InsertStmtStatus.setString(11, "安装" + appName + "应用")
                        InsertStmtStatus.setInt(12, 6)
                        InsertStmtStatus.setString(13, eventTime)
                        InsertStmtStatus.setString(14, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date))
                        InsertStmtStatus.execute()
                      }
                    }
                  }
                }

              }


              case _ => {}


            }

          }
        } catch {
          case e: Exception => {
            e.printStackTrace()
            println(record)
          }
        }
      }

    }
  }


  //获取用户信息
  def getUserInfo(userId: Int): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")
    var userName = ""
    var schoolId = 0
    var schoolName = ""
    var claass_id = ""
    var claass_name = ""
    var account = ""
    var iUserType=1

    val quUserInfoSql = "\tSELECT\n\ta.userId,\n\tuserName,\n\tb.schoolId,\n\tb.schoolName,\n\t b.id   AS calss_id,\n    b.name AS calss_name \nFROM\n\t(select * from   XHSchool_ClazzMembers a  where a.archived=0 and departed=0 and a.userId= "+userId+") a\ninner JOIN (select * from XHSchool_Clazzes b  where b.clazzType=0)b ON a.groupId = b.id \nGROUP BY\n\ta.userId  "
    val results2: ResultSet = MysqlUtils.select(quUserInfoSql)
    while (results2.next()) {
      userName = results2.getString(2)
      schoolId = results2.getInt(3)
      schoolName = results2.getString(4)
      claass_id = results2.getString(5)
      claass_name = results2.getString(6)

    }
    if(claass_id==""||claass_id==null)
    {
      val quUserInfoSql2 = "SELECT\n\ta.userId,\n\tuserName,\n\tb.schoolId,\n\tb.schoolName,\n\tGROUP_CONCAT( DISTINCT b.id SEPARATOR ',')  AS calss_id,\n  GROUP_CONCAT( DISTINCT b.name SEPARATOR ',')  AS calss_name\nFROM\n\t(select * from   XHSchool_ClazzMembers a  where a.archived=0 and departed=0 and a.userId= "+userId+ ") a\n inner JOIN (select * from XHSchool_Clazzes b  where b.clazzType=1)b ON a.groupId = b.id \nGROUP BY\n\ta.userId "
      val results3: ResultSet = MysqlUtils.select(quUserInfoSql2)
      while (results3.next()) {
        userName = results3.getString(2)
        schoolId = results3.getInt(3)
        schoolName = results3.getString(4)
        claass_id = results3.getString(5)
        claass_name = results3.getString(6)

      }
    }
    if(userName==""||userName==null){
      val quUserInfoSql4 = "select a.sUserName,iUserId,iSchoolId,school_name,iUserType from  XHSys_User a  where iuserId = " + userId
      val results4: ResultSet = MysqlUtils.select(quUserInfoSql4)
      while (results4.next()) {
        userName = results4.getString(1)
        schoolId = results4.getInt(3)
        schoolName = results4.getString(4)
        iUserType = results4.getInt(5)
      }
    }

    val quUserInfoSql5 = "select account from  user_account  where user_id = " + userId
    val results5: ResultSet = MysqlUtils.select(quUserInfoSql5)
    while (results5.next()) {
      account = results5.getString(1)
    }

    obj.put("userId", userId)
    obj.put("userName", userName)
    obj.put("schoolId", schoolId)
    obj.put("schoolName", schoolName)
    obj.put("claass_id", claass_id)
    obj.put("claass_name", claass_name)
    obj.put("account", account)
    obj.put("iUserType", iUserType)
    obj
  }







  //解析mongo中的数据
  def getMongoInfo(json: JSONObject): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")

    val sql_type = json.getString("op")
    val table = json.getString("ns")
    obj.put("table", table)
    obj.put("sql_type",sql_type)

    val array: JSONArray = JSON.parseArray(json.getString("o"))
    //
    for (a <- 0 to array.size() - 1) {
      val data = array.get(a).asInstanceOf[JSONObject]
      //
      if (table == "AppService.user_app_changes_logs" && sql_type == "i") {
        if (data.getString("Name") == "userId") {
          val userId = data.getString("Value")
          obj.put("userId", userId)
        }
        if (data.getString("Name") == "deviceId") {
          val deviceId = data.getString("Value")
          obj.put("deviceId", deviceId)
        }
        if (data.getString("Name") == "packageName") {
          val packageName = data.getString("Value")
          obj.put("packageName", packageName)
        }
        if (data.getString("Name") == "appName") {
          val appName = data.getString("Value")
          obj.put("appName", appName)
        }
        if (data.getString("Name") == "type") {
          val type1 = data.getString("Value")
          obj.put("type", type1)
        }
        if (data.getString("Name") == "createdAt") {
          val createdAt = data.getString("Value")
          obj.put("createdAt", createdAt)
        }

      }
      if (table == "AppService.user_monitor_log" && sql_type == "i") {
        if (data.getString("Name") == "userId") {
          val userId = data.getString("Value")
          obj.put("userId", userId)
        }
        if (data.getString("Name") == "deviceId") {
          val deviceId = data.getString("Value")
          obj.put("deviceId", deviceId)
        }
        if (data.getString("Name") == "platformVersion") {
          val platformVersion = data.getString("Value")
          obj.put("platformVersion", platformVersion)
        }
        if (data.getString("Name") == "mdmVersion") {
          val mdmVersion = data.getString("Value")
          obj.put("mdmVersion", mdmVersion)
        }
        if (data.getString("Name") == "model") {
          val model = data.getString("Value")
          obj.put("model", model)
        }
        if (data.getString("Name") == "osDisplay") {
          val osDisplay = data.getString("Value")
          obj.put("osDisplay", osDisplay)
        }
        if (data.getString("Name") == "monitorItems") {
          val monitorItems = data.getString("Value")
          obj.put("monitorItems", monitorItems)
        }
        if (data.getString("Name") == "createdDate") {
          val createdDate = data.getString("Value")
          obj.put("createdDate", createdDate)
        }
      }
    }
    obj
  }


  //mongo中是否有该白名单app
  def getWatchApp(packageName: String): Boolean = {
    var boolean = false
    val queryApp = MongoDBObject("packageName" -> packageName,"status"->1)
    val count = watch_app.find(queryApp).count()
    if (count > 0) boolean = true
    boolean
  }

  //mongo中是否有该白名单用户
  def getWatchAccount(userId: Int): Boolean = {
    var boolean = false
    val queryAccount = MongoDBObject("userId" -> userId,"status"->1)
    val count = watch_account.find(queryAccount).count()
    if (count > 0) boolean = true
    boolean
  }
  //判断白名单中是否有系统
  def getWatchSystem(osDisplay: String): Boolean = {
    var boolean = false
    val querySystem = MongoDBObject("version" -> osDisplay,"status"->1)
    val count = watch_system.find(querySystem).count()
    if (count > 0) boolean = true
    boolean
  }


  def getSmodel(deviceId: String): String = {

    var sModel = sModelMap.getOrElse(deviceId, "")
    if (sModel == "") {
      val quModelSql = "select a.sModel from XHSys_AccountDeviceLocked a where bDelete=0 and sDeviceNumber= '"+deviceId +"'"
      val results1: ResultSet = MysqlUtils.select4(quModelSql)
      while (results1.next()) {
        sModel = results1.getString(1)
      }
    }
    sModel
  }



  def getHistory(user_id: Int): Int = {

     var  ihistory =999;

      val quhistorySql = "SELECT historical from  xh_user_service.XHSys_User  a    where iUserId= " +user_id
      val results1: ResultSet = MysqlUtils.select(quhistorySql)
      while (results1.next()) {
        ihistory = results1.getInt(1)
      }

    ihistory
  }

  def getIstatus(school_id: Int): Int = {

    var  iStatus =999;

    val quIstatusSql = "select iStatus from XHSchool_Info  where iSchoolId= " +school_id
    val results1: ResultSet = MysqlUtils.select4(quIstatusSql)
    while (results1.next()) {
      iStatus = results1.getInt(1)
    }

    iStatus
  }



  def getBindTime(deviceId: String): String = {

    var time = sBindTimeMap.getOrElse(deviceId, "")
    if (time == "") {
      val quModelSql = "select tCreateDate,iUserId from XHSys_AccountDeviceLocked a where  bDelete=0 and sDeviceNumber= '"+deviceId +"'"
      val results1: ResultSet = MysqlUtils.select4(quModelSql)
      while (results1.next()) {
        time = results1.getString(1)
      }
    }
    time
  }


  def getUserIdByDeviceid(deviceId: String): Int = {

    var userid=0
      val quModelSql = "select iUserId from XHSys_AccountDeviceLocked a where bDelete=0 and sDeviceNumber= '"+deviceId +"'"
      val results1: ResultSet = MysqlUtils.select4(quModelSql)
      while (results1.next()) {
        userid = results1.getInt(1)
      }

    userid
  }


  //获取系统异常记录信息表
  def getMonitorRecod(deviceId: String): ArrayBuffer[String] = {


    scala.collection.mutable.ArrayBuffer
    var arrayBuffer = ArrayBuffer[String]("0")

    val quModelSql = "select CONTROL_RESTORE_FACTORY,CONTROL_USB_DEBUG,CONTROL_USB_MTP,CONTROL_EXTERNAL_SDCARD_ENABLED,CONTROL_MULTI_USER_EXIST from  yj_monitor_record\nwhere deviceId ='"+deviceId +"'"
    val results1: ResultSet = MysqlUtils.select3(quModelSql)
    while (results1.next()) {
      val  k1 = results1.getString(1)
      val k2 = results1.getString(2)
      val  k3 = results1.getString(3)
      val  k4 = results1.getString(4)
      val  k5 = results1.getString(5)

      arrayBuffer+=k1
      arrayBuffer+=k2
      arrayBuffer+=k3
      arrayBuffer+=k4
      arrayBuffer+=k5

    }
    arrayBuffer
  }





  //删除record
  def deleteRecord(deviceId: String): Unit = {
    try{
      MysqlUtils.update3("delete from yj_monitor_record  WHERE deviceId ='"+deviceId +"'")

    } catch {
      case e: Exception => {
        e.printStackTrace()

      }
    }

  }



  //阿里云数据库连接信息
  def getConnection(dataSource: BasicDataSource): Connection = {
    dataSource.setDriverClassName("com.mysql.jdbc.Driver")
    //注意，替换成自己本地的 mysql 数据库地址和用户名、密码
    dataSource.setUrl(Url3) //test为数据库名

    dataSource.setUsername(User3) //数据库用户名

    dataSource.setPassword(Password3) //数据库密码

    //设置连接池的一些参数
    dataSource.setInitialSize(10)
    dataSource.setMaxTotal(1004)
    dataSource.setMinIdle(10)
    var con: Connection = null
    try {
      con = dataSource.getConnection
      con
    } catch {
      case e: Exception =>
        System.out.println("-----------mysql get connection has exception , msg = " + e.getMessage)
        con
    }

  }

}














import java.io.IOException
import java.sql.{Connection, PreparedStatement, ResultSet}
import java.text.SimpleDateFormat
import java.util.Arrays

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.xuehai.utils._
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.collection.mutable


object OperateMain extends Constants {

  var userInfoMap = new mutable.HashMap[Int, JSON]()
  var schoolNameMap = new mutable.HashMap[Int, String]()
  var teacherNameMap = new mutable.HashMap[Int, String]()


  try {
    //将用户信息加载到内存中
    val quUserInfoSql = "select a.iUserId,a.iSchoolId,a.sUserName,b.sSchoolName,b.scountyname,b.sProvinceName,b.sCityName  from \nxh_user_service.XHSys_User a\nLEFT JOIN \nxh_user_service.XHSchool_Info b \non  a.iSchoolId=b.ischoolid and b.bdelete=0 and b.istatus in (1,2)"

    val results: ResultSet = MysqlUtils.select(quUserInfoSql)

    while (results.next()) {
      val json = JSON.parseObject("{}")
      val user_id = results.getInt(1)
      json.put("student_id", results.getInt(1))
      json.put("iSchoolId", results.getString(2))
      json.put("student_name", results.getString(3))
      json.put("school_name", results.getString(4))
      json.put("county_name", results.getString(5))
      json.put("province_name", results.getString(6))
      json.put("city_name", results.getString(7))
      userInfoMap += (user_id -> json)
    }

    //将教师姓名信息加载到内存中
    val quTeacherSql = "select teacher_id ,teacher_name  from fact_teacher_info GROUP BY teacher_id "
    val results1: ResultSet = MysqlUtils.select1(quTeacherSql)
    while (results1.next()) {
      val teacher_id = results1.getInt(1)
      val teacher_name = results1.getString(2)
      teacherNameMap += (teacher_id -> teacher_name)
    }


    //将学校名信息加载到内存中
    val quschoolSql = "select school_id ,school_name  from fact_teacher_info GROUP BY school_id  "
    val results2: ResultSet = MysqlUtils.select1(quschoolSql)
    while (results2.next()) {
      val school_id = results2.getInt(1)
      val school_name = results2.getString(2)
      schoolNameMap += (school_id -> school_name)
    }

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

    kafkaConsumer.subscribe(Arrays.asList(OperateTopic))

    println("starting.......")
    while (true) {
      try {
        nginxStart(kafkaConsumer.poll(3000))
      } catch {
        case e: IOException => {
          e.printStackTrace()
        }
        case e: Exception => {
          // java.lang.OutOfMemoryError
          e.printStackTrace()
          //	Utils.dingDingRobot("all", "full link: %s, %s".format(index, e.getMessage))
        }
      }
    }

  }

  import org.apache.commons.dbcp2.BasicDataSource

  val dataSource: BasicDataSource = new BasicDataSource
  var conn: Connection = getConnection(dataSource)
  //祝福人次
  val blessCount: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate (\n        parent_bless_count,\n        uptime,school_id,school_name,province,city,county)\n      VALUES\n      (?, ?, ?, ?,?, ?, ?) ON DUPLICATE KEY UPDATE parent_bless_count = parent_bless_count+1,\n      uptime=?")
  //祝福内容
  val blessContent: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate_bless (\n       school_id,school_name,teacher_id,teacher_name,student_name,content,uptime,province,city,county)\n      VALUES\n      (?, ?, ?, ?,?, ?, ?,?, ?, ?) ")
  //祝福人数
  val blessnum: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate_only_bless (\n      user_id, school_id,uptime)\n      VALUES\n      (?, ?, ?) ON DUPLICATE KEY UPDATE uptime=?")

  //家长访问人次
  val parentCount: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate (\n        parent_visit_count,\n        uptime,school_id,school_name,province,city,county)\n      VALUES\n      (?, ?, ?, ?,?, ?, ?) ON DUPLICATE KEY UPDATE parent_visit_count = parent_visit_count+1,\n      uptime=?")
  //家长访问人数
  val parentnum: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate_only_parent (\n      user_id, school_id,uptime)\n      VALUES\n      (?, ?, ?) ON DUPLICATE KEY UPDATE uptime=? ")

  //分享访问人次
  val shareCount: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate (\n  share_visit_count,\n        uptime,school_id,school_name,province,city,county)\n      VALUES\n      (?, ?, ?, ?,?, ?, ?) ON DUPLICATE KEY UPDATE share_visit_count = share_visit_count+1,\n      uptime=?")
  //分享人数
  val sharenum: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate_only_share (\n      user_id, school_id,uptime)\n      VALUES\n      (?, ?, ?) ON DUPLICATE KEY UPDATE uptime=? ")

  //教师访问人次
  val teacherCount: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate (\n  teacher_visit_count,\n        uptime,school_id,school_name,province,city,county)\n      VALUES\n      (?, ?, ?, ?,?, ?, ?) ON DUPLICATE KEY UPDATE teacher_visit_count = teacher_visit_count+1,\n      uptime=?")

  //教师访问人数
  val teachernum: PreparedStatement = conn.prepareStatement("INSERT INTO big_screen_operate_only_teacher (\n      user_id, school_id,uptime)\n      VALUES\n      (?, ?, ?) ON DUPLICATE KEY UPDATE uptime=? ")


  def nginxStart(records: ConsumerRecords[String, String]): Unit = {
    try {

      import scala.collection.JavaConversions._

      for (record <- records) {
        var str = JSON.parseObject("{}")

        try {
          str = JSON.parseObject(record.value())
        } catch {
          case e: Exception => {
            e.printStackTrace()
            println(record.value())
          }
        }
        val name = str.getString("name")
        val timeStamp = str.getString("time")
        val time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeStamp.toLong)


        if (name == "10291" || name == "10292" || name == "10290") { //只保留这次想要的数据
          //println(str)

          name match {
            //送祝福
            case "10292" => {
              val array = str.getJSONArray("data")
              val size = array.size()
              for (i <- 0 until size) {
                val data = array.get(i).asInstanceOf[JSONObject]
                val school_id = data.getString("schoolId")
                val student_id = data.getString("studentId")
                val teacher_id = data.getString("teacherId")
                val teacher_name = getTeacherName(teacher_id.toInt)
                val wishMsg = data.getString("wishMsg")
                val userInfo = getUserInfo(student_id.toInt)
                val school_name = userInfo.getString("school_name")
                val student_name = userInfo.getString("student_name")
                val county_name = userInfo.getString("county_name")
                val city_name = userInfo.getString("city_name")
                val province_name = userInfo.getString("province_name")

                //祝福人次
                blessCount.setInt(1, 1)
                blessCount.setString(2, time)
                blessCount.setString(3, school_id)
                blessCount.setString(4, school_name)
                blessCount.setString(5, province_name)
                blessCount.setString(6, city_name)
                blessCount.setString(7, county_name)
                blessCount.setString(8, time)
                blessCount.addBatch()
                //祝福内容
                blessContent.setInt(1, school_id.toInt)
                blessContent.setString(2, school_name)
                blessContent.setString(3, teacher_id)
                blessContent.setString(4, teacher_name)
                blessContent.setString(5, student_name)
                blessContent.setString(6, wishMsg)
                blessContent.setString(7, time)
                blessContent.setString(8, province_name)
                blessContent.setString(9, city_name)
                blessContent.setString(10, county_name)
                blessContent.addBatch()

                //祝福人数（有时间维度）
                blessnum.setInt(1, student_id.toInt)
                blessnum.setInt(2, school_id.toInt)
                blessnum.setString(3, time)
                blessnum.setString(4, time)
                blessnum.addBatch()

              }

            }
            //家长端访问
            case "10291" => {

                val data = str.getJSONObject("data")
                val school_id = data.getString("schoolId")
                val student_id = data.getString("studentId")
                val shareMode = data.getString("shareMode")
                val userInfo = getUserInfo(student_id.toInt)
                val school_name = userInfo.getString("school_name")
                val county_name = userInfo.getString("county_name")
                val city_name = userInfo.getString("city_name")
                val province_name = userInfo.getString("province_name")

                if (shareMode == "0") {
                  //家长访问人次
                  parentCount.setInt(1, 1)
                  parentCount.setString(2, time)
                  parentCount.setString(3, school_id)
                  parentCount.setString(4, school_name)
                  parentCount.setString(5, province_name)
                  parentCount.setString(6, city_name)
                  parentCount.setString(7, county_name)
                  parentCount.setString(8, time)
                  parentCount.addBatch()
                  //家长访问人数
                  parentnum.setInt(1, student_id.toInt)
                  parentnum.setInt(2, school_id.toInt)
                  parentnum.setString(3, time)
                  parentnum.setString(4, time)
                  parentnum.addBatch()
                }

                if (shareMode == "1") {
                  //分享访问人次
                  shareCount.setInt(1, 1)
                  shareCount.setString(2, time)
                  shareCount.setString(3, school_id)
                  shareCount.setString(4, school_name)
                  shareCount.setString(5, province_name)
                  shareCount.setString(6, city_name)
                  shareCount.setString(7, county_name)
                  shareCount.setString(8, time)
                  shareCount.addBatch()
                  //分享访问人数
                  sharenum.setInt(1, student_id.toInt)
                  sharenum.setInt(2, school_id.toInt)
                  sharenum.setString(3, time)
                  sharenum.setString(4, time)
                  sharenum.addBatch()
                }

            }

            //教师端访问
            case "10290" => {

                val data = str.getJSONObject("data")
                val school_id = data.getString("schoolId")
                val teacher_id = data.getString("teacherId")
                val userInfo = getUserInfo(teacher_id.toInt)
                val school_name = userInfo.getString("school_name")
                val county_name = userInfo.getString("county_name")
                val city_name = userInfo.getString("city_name")
                val province_name = userInfo.getString("province_name")

                //教师访问人次
                teacherCount.setInt(1, 1)
                teacherCount.setString(2, time)
                teacherCount.setString(3, school_id)
                teacherCount.setString(4, school_name)
                teacherCount.setString(5, province_name)
                teacherCount.setString(6, city_name)
                teacherCount.setString(7, county_name)
                teacherCount.setString(8, time)
                teacherCount.addBatch()
                //教师访问人数
                teachernum.setInt(1, teacher_id.toInt)
                teachernum.setInt(2, school_id.toInt)
                teachernum.setString(3, time)
                teachernum.setString(4, time)
                teachernum.addBatch()
              }



            case _ => {}

          }

          //只保留这次想要的数据
        }

        //每条数据循环体结束
      }


      //批量执行
      val count1 = blessCount.executeBatch //祝福语数量
      val count2 = blessContent.executeBatch //祝福语内容
      val count3 = blessnum.executeBatch //祝福语人数
      val count4 = parentCount.executeBatch //家长访问量
      val count5 = parentnum.executeBatch //家长访人数
      val count6 = shareCount.executeBatch // 分享访问量
      val count7 = sharenum.executeBatch // 分享人数
      val count8 = teacherCount.executeBatch // 教师访问量
      val count9 = teachernum.executeBatch // 教师访问人数

      println("祝福语信息插入了" + count1.length + "条数据")



    } catch {
      case e: Exception => {
        e.printStackTrace()
        //println(records)
      }
    }
  }

  //获取用户信息
  def getUserInfo(userId: Int): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")
    val userinfo = userInfoMap.getOrElse(userId, "信息错误")
    var student_name = ""
    var school_name = ""
    var county_name = ""
    var province_name = ""
    var city_name = ""

    if (userinfo != "信息错误") {
      student_name = userinfo.asInstanceOf[JSONObject].getString("student_name")
      school_name = userinfo.asInstanceOf[JSONObject].getString("school_name")
      county_name = userinfo.asInstanceOf[JSONObject].getString("county_name")
      province_name = userinfo.asInstanceOf[JSONObject].getString("province_name")
      city_name = userinfo.asInstanceOf[JSONObject].getString("city_name")
    }
    if (userinfo == "信息错误") {
      val quUserInfoSql = "select a.iUserId,a.iSchoolId,a.sUserName,a.iUserType,b.sSchoolName,b.scountyname,b.sProvinceName,b.sCityName  from \n(select * from xh_user_service.XHSys_User where  iUserId=" + userId + ") a \nLEFT JOIN \nxh_user_service.XHSchool_Info b \non  a.iSchoolId=b.ischoolid and b.bdelete=0 and b.istatus in (1,2)"
      val results2: ResultSet = MysqlUtils.select(quUserInfoSql)
      while (results2.next()) {
        student_name = results2.getString(3)
        school_name = results2.getString(4)
        county_name = results2.getString(5)
        province_name = results2.getString(6)
        city_name = results2.getString(7)
      }
    }

    obj.put("userId", userId)
    obj.put("student_name", student_name)
    obj.put("school_name", school_name)
    obj.put("county_name", county_name)
    obj.put("province_name", province_name)
    obj.put("city_name", city_name)

    obj
  }


  //阿里云数据库连接信息
  def getConnection(dataSource: BasicDataSource): Connection = {
    dataSource.setDriverClassName("com.mysql.jdbc.Driver")
    //注意，替换成自己本地的 mysql 数据库地址和用户名、密码
    dataSource.setUrl(Url1) //test为数据库名

    dataSource.setUsername(User1) //数据库用户名

    dataSource.setPassword(Password1) //数据库密码

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


  //获取教师信息
  def getTeacherName(userId: Int): String = {
    var teacher_name = teacherNameMap.getOrElse(userId, "")
    if (teacher_name == "") {
      val quModelSql = "select teacher_id ,teacher_name  from fact_teacher_info where teacher_id=" + userId + "  GROUP BY teacher_id"
      val results1: ResultSet = MysqlUtils.select1(quModelSql)
      while (results1.next()) {
        teacher_name = results1.getString(2)
      }
    }
    teacher_name
  }


  //获取学校信息
  def getSchoolName(schoolId: Int): String = {
    var School_name = schoolNameMap.getOrElse(schoolId, "")
    if (School_name == "") {
      val quModelSql = "select school_id ,school_name  from fact_teacher_info where school_id=" + schoolId + "  GROUP BY school_id"
      val results1: ResultSet = MysqlUtils.select1(quModelSql)
      while (results1.next()) {
        School_name = results1.getString(2)
      }
    }
    School_name
  }


}








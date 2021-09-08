import java.io.IOException
import java.sql.{Connection, ResultSet}
import java.util.{Arrays, Calendar}

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.xuehai.utils._
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.collection.mutable

object CouponMain extends Constants {

  var userInfoMap = new mutable.HashMap[Int, JSON]()
  var schoolInfoMap = new mutable.HashMap[Int, JSON]()

  var schoolNameMap = new mutable.HashMap[Int, String]()

  var gradeInfoMap = new mutable.HashMap[Int, JSON]()



  try {


    val quUserInfoSql = "select a.userId,b.enrollYear,b.grade\nfrom \nXHSchool_ClazzMembers  a\nleft join XHSchool_Clazzes  b\non a.groupId=b.id";
    val results: ResultSet = MysqlUtils.select(quUserInfoSql)
    while (results.next()) {
      val userid = results.getInt(1)
      val enrollYear = results.getInt(2)
      val grade = results.getString(3)

      val json = JSON.parseObject("{}")
      json.put("student_id", userid)

      json.put("enrollYear", enrollYear)
      json.put("grade", grade)

      userInfoMap += (userid -> json)
    }


    val quUserInfoSq2 = "select student_id,gradeid,grade_name from fact_student_info_distinct_daily ";
    val result: ResultSet = MysqlUtils.select1(quUserInfoSq2)
    while (result.next()) {
      val userid = result.getInt(1)
      val gradeid = result.getInt(2)
      val grade_name = result.getString(3)
      val json = JSON.parseObject("{}")
      json.put("student_id", userid)
      json.put("gradeid", gradeid)
      json.put("grade_name", grade_name)

      gradeInfoMap += (userid -> json)
    }




    //将学校名信息加载到内存中
    val quschoolSql = "select school_id ,school_name  from fact_teacher_info GROUP BY school_id  "
    val results2: ResultSet = MysqlUtils.select1(quschoolSql)
    while (results2.next()) {
      val school_id = results2.getInt(1)
      val school_name = results2.getString(2)
      schoolNameMap += (school_id -> school_name)
    }



    //将学校名信息加载到内存中
    val quschoolInfoSql = "select iuserID ,ischoolId ,school_name,sUserName  from XHSys_User GROUP BY iuserID "
    val results3: ResultSet = MysqlUtils.select(quschoolInfoSql)
    while (results3.next()) {
      val user_id = results3.getInt(1)
      val school_id = results3.getInt(2)
      val school_name = results3.getString(3)
      val student_name = results3.getString(4)
      val json = JSON.parseObject("{}")
      json.put("school_id", school_id)
      json.put("school_name", school_name)
      json.put("student_name", student_name)
      schoolInfoMap += (user_id -> json)
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

    kafkaConsumer.subscribe(Arrays.asList(mysql_binlog))

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


 val updateStmtStatus=conn.prepareStatement("UPDATE baifen_stats_coupon SET  no=?, batch_id=?, batch_no=?, status=?, status_changed_date=?, deleted=?, distribution_channel=?, expire_end=?, expire_start=?, user_id=?, user_name=?, user_grade_id=?, user_grade_name=?, user_school_id=?, user_school_name=?, obtain_time=? WHERE no=?")

  val  insertStmt = conn.prepareStatement("INSERT INTO baifen_stats_coupon ( no, batch_id, batch_no, status, status_changed_date, deleted, distribution_channel, expire_end, expire_start, user_id, user_name, user_grade_id, user_grade_name, user_school_id, user_school_name, obtain_time) VALUES (?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?) ")
  val  updateStmt = conn.prepareStatement("update baifen_stats_coupon set use_time=?,order_no=? where no =?")
  val  insertCouponStatistics = conn.prepareStatement("  INSERT INTO baifen_stats_coupon_summary (batch_id, grant_toatl_num, received_num, no_receive_num, receive_ratio, user_num, no_user_num, effective_use_num, effective_use_ratio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ;")
  val updataCouponStatistics=conn.prepareStatement("update baifen_stats_coupon_summary c\nset  c.user_num=effective_use_num+?, c.no_user_num=c.grant_toatl_num-effective_use_num-?,effective_use_ratio=case when effective_use_num+? =0 then 0 else effective_use_num/(effective_use_num+?) end \n where batch_id=?")
  val updataCoupon=conn.prepareStatement("update baifen_stats_coupon set  display_school_id =?,display_school_name=? where order_no=?")
  val  updateCouponSunny=conn.prepareStatement("UPDATE  baifen_stats_coupon_summary\nset\n  batch_id=?\n , grant_toatl_num =?,\n  received_num =?,\n  no_receive_num =?,\n  receive_ratio =?,\n  user_num =?,\n  no_user_num =?,\n  effective_use_num =?,\n  effective_use_ratio =? where batch_id=?")

  def nginxStart(records: ConsumerRecords[String, String]): Unit = {
    try {

      import scala.collection.JavaConversions._

      for (record <- records) {
        var json = JSON.parseObject("{}")

        try {
          json = JSON.parseObject(record.value())
         // println(json)
        } catch {
          case e: Exception => {
            e.printStackTrace()
            println(record.value())
          }
        }
       // val sql_type=json.getString("type")
        val table=json.getString("table")




        if (table == "coupon" || table == "coupon_use_record"||table == "coupon_batch"||table == "orders") { //只保留这次想要的数据
       // if (table == "coupon_batch") { //只保留这次想要的数据
          val array: JSONArray = JSON.parseArray(json.getString("data"))
          //println(json)

          table match {
            //
            case "coupon" => {

              val size = array.size()
              for (i <- 0 until size) {
                val data = array.get(i).asInstanceOf[JSONObject]
                val userid = data.getString("user_id")
                val no = data.getString("no")
                val batch_no = data.getString("batch_no")
                val batch_id = data.getString("batch_id")
                val modified_date = data.getString("modified_date")
                val status_changed_date = data.getString("status_changed_date")
                val distribution_channel = data.getString("distribution_channel")
                val created_date = data.getString("created_date")
                val expire_start = data.getString("expire_start")
                val expire_end = data.getString("expire_end")
                val deleted = data.getString("deleted")
                val status = data.getString("status")

                //获取信息

                val userInfo = getUserInfo(userid.toInt)
                val schoolInfo=getschoolInfo(userid.toInt)
                val gradeInfo = getGrade(userid.toInt)
                val school_name = schoolInfo.getString("school_name")
                val school_id = schoolInfo.getInteger("school_id")

                val student_name = schoolInfo.getString("student_name")

                var gradeid = gradeInfo.getInteger("gradeid")
                var grade_name = gradeInfo.getString("grade_name")
                if (gradeid ==null){
                   gradeid = Utils.null2Int(userInfo.getInteger("gradeid"))
                   grade_name = userInfo.getString("grade_name")
                }

                updateStmtStatus.setString(1, no)
                updateStmtStatus.setInt(2, batch_id.toInt)
                updateStmtStatus.setString(3, batch_no)
                updateStmtStatus.setInt(4, status.toInt)
                updateStmtStatus.setString(5, status_changed_date)
                updateStmtStatus.setInt(6, deleted.toInt)
                updateStmtStatus.setString(7, distribution_channel)
                updateStmtStatus.setString(8, expire_end)
                updateStmtStatus.setString(9, expire_start)
                updateStmtStatus.setInt(10, userid.toInt)
                updateStmtStatus.setString(11, student_name)
                updateStmtStatus.setInt(12, gradeid)
                updateStmtStatus.setString(13, grade_name)
                updateStmtStatus.setInt(14, school_id)
                updateStmtStatus.setString(15,school_name )
                updateStmtStatus.setString(16, created_date)
                updateStmtStatus.setString(17, no)
                updateStmtStatus.execute()

                //插入
                if(updateStmtStatus.getUpdateCount == 0 ) {
                  insertStmt.setString(1, no)
                  insertStmt.setInt(2, batch_id.toInt)
                  insertStmt.setString(3, batch_no)
                  insertStmt.setInt(4, status.toInt)
                  insertStmt.setString(5, status_changed_date)
                  insertStmt.setInt(6, deleted.toInt)
                  insertStmt.setString(7, distribution_channel)
                  insertStmt.setString(8, expire_end)
                  insertStmt.setString(9, expire_start)
                  insertStmt.setInt(10, userid.toInt)
                  insertStmt.setString(11, student_name)
                  insertStmt.setInt(12, gradeid)
                  insertStmt.setString(13, grade_name)
                  insertStmt.setInt(14, school_id)
                  insertStmt.setString(15, school_name)
                  insertStmt.setString(16, created_date)
                  insertStmt.execute()
                }

                //修改信息

                if(status=="40"){
                 // val batch_id=getBachIdByNo(no.toInt)
                  val no_pay_num=getCouponInfo(batch_no.toInt)
                  updataCouponStatistics.setInt(1,no_pay_num)
                  updataCouponStatistics.setInt(2,no_pay_num)
                  updataCouponStatistics.setInt(3,no_pay_num)

                  updataCouponStatistics.setInt(4,no_pay_num)
                  updataCouponStatistics.setInt(5,batch_no.toInt)
                  updataCouponStatistics.addBatch()
                }
              }
            }


            case "coupon_use_record" => {
              println(json)
              val size = array.size()
              for (i <- 0 until size) {
                val data = array.get(i).asInstanceOf[JSONObject]
                val order_no = data.getString("order_no")
                val coupon_no = data.getString("coupon_no")
                val used_date = data.getString("created_date")

                updateStmt.setString(1, used_date)
                updateStmt.setString(2, order_no)
                updateStmt.setString(3, coupon_no)
                updateStmt.addBatch()
              }

            }


            case "orders" => {
              val size = array.size()
              for (i <- 0 until size) {
                val data = array.get(i).asInstanceOf[JSONObject]
                val no = data.getString("no")
                val fabricated_school_id = Utils.null2Int(data.getString("fabricated_school_id"))
                var fabricated_school_name=""
//              if (fabricated_school_id!=null) fabricated_school_name=getSchoolName(fabricated_school_id)

                updataCoupon.setInt(1, fabricated_school_id)
                updataCoupon.setString(2, fabricated_school_name)
                updataCoupon.setString(3, no)
                updataCoupon.execute()
              }

            }

            case "coupon_batch" => {

              val size = array.size()
              for (i <- 0 until size) {
                val data = array.get(i).asInstanceOf[JSONObject]
                val batch_no = data.getInteger("batch_no")
                val max_count = data.getInteger("max_count")  //最大数量
                val distributed_count = data.getInteger("distributed_count") //已经领取数量
                val no_distributed_count=max_count-distributed_count //未领取数量
                val distributed_radio = (distributed_count.toDouble/max_count.toDouble).formatted("%.4f").toDouble //领取率
                val used_count = data.getInteger("used_count")  //有效使用数量
                val no_pay_num=getCouponInfo(batch_no)
                val used_count_nopay = data.getInteger("used_count") +no_pay_num   //已使用
                val no_userd_count=max_count-used_count_nopay    //未使用
                var eff_use_radio=0.0000
                if(used_count_nopay!=0) {
                   eff_use_radio = (used_count.toDouble / used_count_nopay.toDouble).formatted("%.4f").toDouble //有效使用率
                }



                updateCouponSunny.setInt(1, batch_no)
                updateCouponSunny.setInt(2, max_count)
                updateCouponSunny.setInt(3, distributed_count)
                updateCouponSunny.setInt(4, no_distributed_count)
                updateCouponSunny.setDouble(5, distributed_radio)
                updateCouponSunny.setInt(6, used_count_nopay)
                updateCouponSunny.setInt(7, no_userd_count)
                updateCouponSunny.setInt(8, used_count)
                updateCouponSunny.setDouble(9, eff_use_radio)
                updateCouponSunny.setInt(10, batch_no)
                updateCouponSunny.execute()

                if(updateCouponSunny.getUpdateCount == 0 ) {

                  insertCouponStatistics.setInt(1, batch_no)
                  insertCouponStatistics.setInt(2, max_count)
                  insertCouponStatistics.setInt(3, distributed_count)
                  insertCouponStatistics.setInt(4, no_distributed_count)
                  insertCouponStatistics.setDouble(5, distributed_radio)
                  insertCouponStatistics.setInt(6, used_count_nopay)
                  insertCouponStatistics.setInt(7, no_userd_count)
                  insertCouponStatistics.setInt(8, used_count)
                  insertCouponStatistics.setDouble(9, eff_use_radio)

                  insertCouponStatistics.execute()
                }
              }

            }




            case _ => {}

          }


          //批量执行

      val count2 = updateStmt.executeBatch //祝福语内容
      val count3=    updataCouponStatistics.executeBatch()
          println("优惠券使用记录信息插入了" + count2.length + "条数据")




        } }}catch {
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

    var grade = 0
    var enrollYear = 0

    var  gradeid=0
   var  grade_name=""

    if (userinfo != "信息错误") {
      grade = userinfo.asInstanceOf[JSONObject].getInteger("grade")
      enrollYear = userinfo.asInstanceOf[JSONObject].getInteger("enrollYear")

    }

    if (userinfo == "信息错误") {
      val quUserInfoSql = "select a.userId,b.enrollYear,b.grade\nfrom \nXHSchool_ClazzMembers  a\nleft join XHSchool_Clazzes  b\non a.groupId=b.id where userId =" + userId
      val results2: ResultSet = MysqlUtils.select(quUserInfoSql)
      while (results2.next()) {

        grade = results2.getInt(2)
        enrollYear = results2.getInt(3)
      }
    }

  if(grade>0) {
  val calendar = Calendar.getInstance()
  val year = (calendar.get(Calendar.YEAR)).toInt
  val month = (calendar.get(Calendar.MONTH) + 1).toInt
    var value: Int = year - enrollYear - 1
  if (month > 8) value = year - enrollYear

    val trans: Int = grsMap.get(grade).get
    val cur_gra = trans + value
    gradeid = graidMap.get(cur_gra).get
    grade_name = gMap.get(cur_gra).get
}
    obj.put("userId", userId)
    obj.put("gradeid", gradeid)
    obj.put("grade_name", grade_name)

    obj
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


  //根据批次号id获取优惠券状态为锁定的数量
  def getCouponInfo(no: Int): Int = {

    var num=0
    val quModelSql = " select count(*) as num from coupon b where b.batch_no ="+no+" and b.status=40 "
      val results1: ResultSet = MysqlUtils.select2(quModelSql)
      while (results1.next()) {
         num = results1.getInt(1)
      }
    num
  }


  //根据优惠券id获取优惠券状态为锁定的数量
  def getBachIdByNo(no: Int): Int = {
    var batch_id=0
    val quModelSql = "select batch_no  from coupon b where b.no="+no
    val results1: ResultSet = MysqlUtils.select2(quModelSql)
    while (results1.next()) {
      batch_id = results1.getInt(1)
    }
    batch_id
  }




  //根据学号获取学生信息
  //获取用户信息
  def getschoolInfo(userId: Int): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")
    val userinfo = schoolInfoMap.getOrElse(userId, "信息错误")
    var school_id = 0
    var school_name = ""
    var student_name = ""
    if (userinfo != "信息错误") {
      school_name = userinfo.asInstanceOf[JSONObject].getString("school_name")
      school_id = userinfo.asInstanceOf[JSONObject].getInteger("school_id")
      student_name = userinfo.asInstanceOf[JSONObject].getString("student_name")
    }

    if (userinfo == "信息错误") {
      val quUserInfoSql = "select iuserID ,ischoolId ,school_name,sUserName  from XHSys_User  where iuserId=" + userId
      val results2: ResultSet = MysqlUtils.select(quUserInfoSql)
      while (results2.next()) {
        school_id = results2.getInt(2)
        school_name = results2.getString(3)
        student_name=results2.getString(4)
      }
    }
    obj.put("userId", userId)
    obj.put("school_name", school_name)
    obj.put("school_id", school_id)
    obj.put("student_name", student_name)
    obj
  }




  //获取用户信息
  def getGrade(userId: Int): JSONObject = {
    val obj: JSONObject = JSON.parseObject("{}")
    val userinfo = gradeInfoMap.getOrElse(userId, "信息错误")

    var grade_id = ""
    var grade_name = ""
    if (userinfo != "信息错误") {
      grade_id = userinfo.asInstanceOf[JSONObject].getString("grade_id")
      grade_name = userinfo.asInstanceOf[JSONObject].getString("grade_name")
    }

    if (userinfo == "信息错误") {
      val quUserInfoSql = "select student_id,gradeid,grade_name from fact_student_info_distinct_daily where student_id = " + userId
      val results2: ResultSet = MysqlUtils.select1(quUserInfoSql)
      while (results2.next()) {

        grade_id = results2.getString(2)
        grade_name=results2.getString(3)
      }
    }
    obj.put("userId", userId)
    obj.put("grade_id", grade_id)
    obj.put("grade_name", grade_name)
    obj
  }





  //阿里云数据库连接信息
  def getConnection(dataSource: BasicDataSource): Connection = {
    dataSource.setDriverClassName("com.mysql.jdbc.Driver")
    //注意，替换成自己本地的 mysql 数据库地址和用户名、密码
    dataSource.setUrl(Url) //test为数据库名

    dataSource.setUsername(User) //数据库用户名

    dataSource.setPassword(Password) //数据库密码

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








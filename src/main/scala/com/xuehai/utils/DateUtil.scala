package com.xuehai.utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, TimeZone}

/**
  * Created by Administrator on 2019/6/3 0003.
  */
object DateUtil {
    final val ONE_SECOND_MILLISECONDS = 1000
    final val ONE_HOUR_MILLISECONDS = 1000 * 3600
    final val ONE_DAY_MILLISECONDS = 24*3600*1000
    final val DAY_FORMAT_1 = "yyyyMMdd"
    final val DAY_FORMAT_2 = "yyyy-MM-dd"
    final val DAY_FORMAT_3 = "yyyy.MM.dd"
    final val DAY_SECOND_FORMAT = "yyyy-MM-dd HH:mm:ss"

    /**
      * 2019-06-03 to Date
      * @param dateStr "2019-06-03"
      * @return
      */
    def str2date(dateStr: String) ={
        new SimpleDateFormat(DAY_FORMAT_2).parse(dateStr)
    }

    /**
      * 2019-06-03 04:00:00 to Date
      * @param timeStr "2019-06-03 02:20:0"
      * @return
      */
    def str2time(timeStr: String) ={
        new SimpleDateFormat(DAY_SECOND_FORMAT).parse(timeStr)
    }

    /**
      * 获取当前Date
      * @return date
      */
    def getNowDate() ={
        new Date()
    }

    /**
      * 获取今日日期字符串：yyyyMMdd
      *
      * @return yyyyMMdd
      */
    def getTodayDateStr(): String ={
        val date = new Date()
        val dateFormat = new SimpleDateFormat(DAY_FORMAT_1)
        dateFormat.format(date)
    }

    def getDateStr(): String ={
        val date = new Date()
        val dateFormat = new SimpleDateFormat(DAY_FORMAT_3)
        dateFormat.format(date)
    }

    /**
      * 获取昨天日期字符串：yyyyMMdd
      * @return yyyyMMdd
      */
    def getYesterdayDateStr() ={
        val dateFormat = new SimpleDateFormat(DAY_FORMAT_1)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        dateFormat.format(cal.getTime)
    }

    /**
      * 获取当前时间距离明天某个时间点的毫秒数，如果hour=0，就是求距离明天凌晨的毫秒数
      * @param hour int
      * @return Long
      */
    def getTomorrowHourMilliseconds(hour: Int) ={
        val dateFormat = new SimpleDateFormat(DAY_FORMAT_2)
        val cal1: Calendar = Calendar.getInstance()
        cal1.add(Calendar.DATE, + 1)

        val tomorrowTime: Date = dateFormat.parse(dateFormat.format(cal1.getTime))
        tomorrowTime.setHours(hour)

        tomorrowTime.getTime - System.currentTimeMillis()
    }

    /**
      * 获取今天某个小时的时间戳
      * @param hour int
      * @return
      */
    def getTodayHourMilliseconds(hour: Int): Long ={
        val dateFormat = new SimpleDateFormat(DAY_FORMAT_2)
        val cal1: Calendar = Calendar.getInstance()

        val todayTime: Date = dateFormat.parse(dateFormat.format(cal1.getTime))
        todayTime.setHours(hour)

        todayTime.getTime
    }

    /**
      * 获取当前时间的年月日 时分秒
      * @return
      */
    def getNowTimeStr(): String ={
        val date = new Date()
        val dateFormat = new SimpleDateFormat(DAY_SECOND_FORMAT)
        dateFormat.format(date)
    }

    /**
      * 时间戳转ISODate字符串
      * @param milliseconds long
      * @return
      */
    def long2ISODate(milliseconds: Long): String ={
        val date = new Date(milliseconds)
        val tz = TimeZone.getTimeZone("UTC") // UTC
        val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'")
        df.setTimeZone(tz)
        val nowAsISO = df.format(date)

        return nowAsISO.replace(" ", "T")
    }

    /**
      * 毫秒数生产日期和小时，例如1559615598000-》(20190604, 10)
      * @param x long
      * @return (day, hour)
      */
    def long2DayHour(x: Long) ={
        val date = new Date(x)
        val dateFormat = new SimpleDateFormat("yyyyMMdd HH")
        val dateStr = dateFormat.format(date).split(" ")

        (dateStr(0), dateStr(1))
    }

    def long2DateStr(x: Long): String ={
        val date = new Date(x)
        val dateFormat = new SimpleDateFormat("yyyy.MM.dd")

        dateFormat.format(date)
    }

    def main(args: Array[String]) {

        val date = new Date(1561346915835L)
        val tz = TimeZone.getTimeZone("UTC") // UTC
        val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'")
        df.setTimeZone(tz)
        val nowAsISO = df.format(date)

        println(getDateStr())

    }
}

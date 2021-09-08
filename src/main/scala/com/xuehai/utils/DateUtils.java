package com.xuehai.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by  wlk
 */
public class DateUtils {

    SimpleDateFormat sdf=null;
// UTC时间转换成本地时间
    public  String UTCToCST(String UTCStr) throws ParseException {
        Date date = null;
        int count=UTCStr.split("\\.").length;
        if(count==2){
                sdf    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        }
        if(count==1){
            sdf    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }


        date = sdf.parse(UTCStr);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
        //calendar.getTime() 返回的是Date类型，也可以使用calendar.getTimeInMillis()获取时间戳

        Long shijian=calendar.getTimeInMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(shijian);
        Date date1 = new Date(lt);
       String res = simpleDateFormat.format(date1);
       return   res;
    }

  //时间戳转换成本地时间
    public  String MilltoLocal(Long time) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(time);
        Date date = new Date(lt);
        String res = simpleDateFormat.format(date);
        return   res;
    }

    public static void main(String[] args) {
        String UTCStr="2021-03-24T02:27:08.035Z";
        String UTCStr1= "2021-03-25T18:24:57Z";
        int count=UTCStr.split("\\.")[1].length();

    }
}
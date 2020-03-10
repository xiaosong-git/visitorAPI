package com.goldccm.util;


import com.alibaba.druid.util.StringUtils;
import com.goldccm.model.compose.Constant;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间的处理
 * @Author Linyb
 * @Date 2016/12/14.
 */
public class DateUtil {

    /**
     * 把指定的时间转化成默认格式的时间字符串
     *
     * @Author Linyb
     * @Date 2016/12/14 10:26
     */
    public static String dateFormatDefaul(Date date) {
        return new SimpleDateFormat(Constant.DATE_FORMAT_DEFAULT).format(date);
    }

    public static Integer getAgeByBirthday(String date) {
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date mydate = null;
        try {
            mydate = myFormatter.parse(date);
        } catch (ParseException e) {
        }
        Calendar cal = Calendar.getInstance();
        if (cal.before(mydate)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(mydate);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                // monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                // monthNow>monthBirth
                age--;
            }
        }
        return age;
    }

    public static Date changeToDate(String date) {
        String d = date.substring(0, date.length() - 2);
        String str = date.substring(date.length() - 3, date.length()).trim();
        Date mydate = null;
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            mydate = myFormatter.parse(d);
            if ("pm".equals(str)) {
                Long mytime = mydate.getTime();
                mytime += 12 * 60 * 60 * 1000;
                mydate.setTime(mytime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mydate;
    }

    public static int minutesBetween(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d1 = sdf.parse(startDate);
            Date d2 = sdf.parse(endDate);
            return (int) (d2.getTime() - d1.getTime()) / 1000 / 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static String getCurTime(){
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }

    public static String getCurDate() {
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }
    public static String getCurrentDateTime(String type){
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat(type);
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }

    public static String getSystemTime() {
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }
    public boolean compareDate(String date1,String date2) {
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d1 = dateFormat.parse(date1);
            Date d2 = dateFormat.parse(date2);
            if(d1.equals(d2)||d1.after(d2)){
                return true;
            }else if(d1.before(d2)){
                return false;
            }
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 获取当月第一天
     *
     * @return
     */
    public static String getCurMonthFirstDay() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String first = f.format(c.getTime());
        return first;
    }
    /**
     * 获取某月第一天
     *
     * @return
     */
    public static String getMonthFirstDay(String yearDate) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new SimpleDateFormat("yyyy-MM").parse(yearDate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String first = f.format(c.getTime());
        return first;
    }

    /**
     * 获取当月最后一天
     *
     * @return
     */
    public static String getCurMonthLastDay() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = f.format(ca.getTime());
        return last;
    }
    /**
     * 获取某月最后一天
     *
     * @return
     */
    public static String getMonthLastDay(String yearDate) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new SimpleDateFormat("yyyy-MM").parse(yearDate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = f.format(c.getTime());
        return last;
    }

//    public static void main(String[] args) throws Exception {
//        Integer maxCount = 3;
//        List<Long> list = new ArrayList<Long>();
//        list.add(1L);
//        list.add(1L);
//        list.add(1L);
//
//        list.add(2L);
//        list.add(2L);
//        list.add(2L);
//
//        list.add(3L);
//        list.add(3L);
//        list.add(3L);
//
//        list.add(4L);
//
//        list.add(5L);
//
//        /**
//         * 记录每天的笔数
//         */
//        Map<Long, Integer> map = new HashMap<Long, Integer>();
//        for (int i=0; i<list.size(); i++){
//            Long key = list.get(i);
//            Integer val = map.get(key);
//            if(val == null){
//                map.put(key, 1);
//            }else{
//                map.put(key, ++val);
//            }
//        }
//
//        Iterator<Long> iterator = map.keySet().iterator();
//        while (iterator.hasNext()){
//            Long key = iterator.next();
//            Integer val = map.get(key);
//            if(val >= maxCount){
//                iterator.remove();
//            }
//        }
//
//
//
//        /**
//         * 剔除节假日，存入removeList
//         */
//        List<Long> removeList = new ArrayList<Long>();
//        Iterator<Long> it = list.iterator();
//        while (it.hasNext()){
//            Long value = it.next();
//            if(value == 3L){
//                map.remove(value);
//                removeList.add(value);
//                it.remove();
//            }
//        }
//
//        /**
//         * dayList存放着可供安排的日期
//         */
//        List<Long> dayList = new ArrayList<Long>();
//        for(Map.Entry<Long, Integer> m : map.entrySet()){
//            dayList.add(m.getKey());
//        }
//
//
//        /**
//         * 将节假日的计划重新分配
//         */
//        for (int i=0; i<removeList.size(); i++){
//            list.add(dayList.get(i % dayList.size()));
//        }
//
//        /**
//         * 查看重分配结果
//         */
//        Collections.sort(list);
//        for (int i=0; i<list.size(); i++){
//            System.out.println(list.get(i));
//        }
//    }
    /**
     * 获取银行的账单日、还款日（例如输入20,10 → 2017-05-20, 2017-06-10）
     *
     * @param bday
     * @param pday
     * @return
     */
    public static String[] changeToBillDateAndRepayDate(Integer bday, Integer pday) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String bill_date = "";
        String repay_date = "";
        String[] result = new String[2];
        Calendar cnow = Calendar.getInstance();
        int now = cnow.get(Calendar.DAY_OF_MONTH);//当前日期
        if (bday < pday) {// 同月
            Calendar tem = Calendar.getInstance();
            tem.set(Calendar.DAY_OF_MONTH, bday);
            bill_date = sdf.format(tem.getTime());
            tem.set(Calendar.DAY_OF_MONTH, pday);
            repay_date = sdf.format(tem.getTime());
            result[0] = bill_date;
            result[1] = repay_date;
        } else {// 不同月
            Calendar cbdate = Calendar.getInstance();
            cbdate.setTime(cnow.getTime());
            Calendar cpdate = Calendar.getInstance();
            cpdate.setTime(cnow.getTime());
            if (now < pday) {// 上个月
                cbdate.set(Calendar.DAY_OF_MONTH, bday);
                cbdate.add(Calendar.MONTH, -1);// 加一个月
                cpdate.set(Calendar.DAY_OF_MONTH, pday);
            } else {
                cbdate.set(Calendar.DAY_OF_MONTH, bday);
                cpdate.set(Calendar.DAY_OF_MONTH, pday);
                cpdate.add(Calendar.MONTH, 1);// 加一个月
            }
//            if (cnow.after(cbdate) && cnow.before(cpdate)) {
                bill_date = sdf.format(cbdate.getTime());
                repay_date = sdf.format(cpdate.getTime());
                result[0] = bill_date;
                result[1] = repay_date;
//            } else {
//                return null;
//            }
        }
        return result;
    }

    //计算两个时间之间间隔的天数
    public static int daysBetween(String startdate, String enddate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        long time1 = 0;
        long time2 = 0;

        try {
            cal.setTime(sdf.parse(startdate));
            time1 = cal.getTimeInMillis();
            cal.setTime(sdf.parse(enddate));
            time2 = cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 作用：将时间加上一定的分钟数
     * @param startTime
     * @param minutes
     * @return
     */
    public static String addMinute(String startTime,long minutes){
        String result = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            if(!StringUtils.isEmpty(startTime)){
                Date date = sdf.parse(startTime);
                Date resultDate = new Date((date.getTime()+minutes*60*1000));
                result = sdf.format(resultDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public static boolean isWeekend(String date) throws ParseException{
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date bdate = format1.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(bdate);
        if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
            return true;
        }
        return false;
    }


        /**
        * 根据一个日期，返回是星期几的字符串
        * @param sdate
        * @return
        */
    public static int getWeek(String sdate) throws ParseException {
        // 再转换为时间
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format1.parse(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        new SimpleDateFormat("EEEE").format(c.getTime());
         int hour=c.get(Calendar.DAY_OF_WEEK);
//         hour中存的就是星期几了，其范围 1~7
//         1=星期日 7=星期六，其他类推
//        new SimpleDateFormat("EEEE").format(c.getTime());
        return hour-1;
//        
    }

    public static String getSystemTimeFourteen() {
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }
    //某天的unix值
    public static long StrToUnix(String date) throws ParseException {
        Date unixDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        return unixDate.getTime()/1000;
    }
    //下一天的unix值
    public static long NextStrToUnix(String date) throws ParseException {
        Date unixDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(unixDate);
        c.add(Calendar.DAY_OF_MONTH,1);     //利用Calendar 实现 Date日期+1天
        Date nextDate = c.getTime();
        return nextDate.getTime()/1000;
    }
    public static String getDate(String date) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date strDate = f.parse(date);
        return f.format(strDate);
    }

    public static String NextDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date unixDate = formatter.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(unixDate);
        c.add(Calendar.DAY_OF_MONTH,1);     //利用Calendar 实现 Date日期+1天
        Date nextDate = c.getTime();
        String day=formatter.format(nextDate);
        return day;
    }
    public static String NextMinu(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date minu = formatter.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(minu);
        c.add(Calendar.MINUTE,1);     //利用Calendar 实现 分钟+1分钟
        Date nextMinu = c.getTime();
        String day=formatter.format(nextMinu);
        return day;
    }

    public static void main(String[] args) throws ParseException {
        String x = NextMinu("2019-11-20 00:00:00");
        System.out.println(x.substring(11));
        System.out.println(x.substring(0,10));
//        System.out.println(getDate("2019-11-20 00:00:00").substring(10,14));
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new SimpleDateFormat("yyyy-MM").parse("2020-02");

        Calendar c = Calendar.getInstance();
        c.setTime(date);
//        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = f.format(c.getTime());
//        String first = f.format(c.getTime());
        System.out.println(getMonthFirstDay("2020-02"));
        System.out.println(getMonthLastDay("2020-02"));

    }
}

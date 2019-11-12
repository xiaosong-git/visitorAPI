package com.goldccm.util;

import com.goldccm.model.plan.PurchasePlan;
import com.goldccm.model.plan.RepayPlan;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author linyb
 * @Date 2017/5/4 10:55
 */
public class PlanUtil {
    /**
     * 作用：将时间加上一定的分钟数
     * @param startTime
     * @param minutes
     * @return
     */
    public static String addMinute(String startTime,long minutes){
        String result = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(!StringUtils.isEmpty(startTime)){
                Date date = sdf.parse(startTime);
                Date resultDate = new Date((date.getTime()+minutes*60*1000));
                result = sdf.format(resultDate);
            }
        } catch (Exception e) {
            return null;
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
        return Integer.parseInt(String.valueOf(between_days+1));
    }
    /**
     * 计算两个时间相隔的分钟数
     * @param startDate
     * @param endDate
     * @return
     */
    public static int minutesBetween(String startDate,String endDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = sdf.parse(startDate);
            Date d2 = sdf.parse(endDate);
            int betweenMinutes = (int)(d2.getTime() - d1.getTime())/1000/60;
            return betweenMinutes;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //按笔数随机将金额拆分,将拆分后的金额以List的形式返回
    public static List<BigDecimal> randomSplitMoney(double sumMoney, int num){
        List<BigDecimal> moneys = new ArrayList<BigDecimal>();//存放分配随机后金额
        if(num == 1){
            moneys.add(BigDecimal.valueOf(sumMoney));
            return moneys;
        }
        //计算平均每笔多少钱
        int tempSumMoney = (int)sumMoney;
        BigDecimal tempMoney = BigDecimal.valueOf(sumMoney - tempSumMoney);
        BigDecimal m = new BigDecimal(sumMoney);
        BigDecimal n = new BigDecimal(num);
        BigDecimal averMoney = m.divide(n, 0, BigDecimal.ROUND_DOWN);
        //随机每笔在平均数上下按随机的百分比进行浮动
        for(int i=0; i<num; i++){
            //判断是不是最后一笔
            if(i == num-1){
                //最后一笔
                moneys.add(m.add(tempMoney).setScale(1,BigDecimal.ROUND_HALF_UP));
            }
            else{
                //不是最后一笔
                Double ranUp=Math.random()*3*0.1;//上浮的百分比
                Double ranDown=Math.random()*3*0.1;//下浮的百分比
                Long isZero=Math.round((Math.random()*500000))%2;//0下浮，1上浮
                int round = 0;//保留的小数位数
                if(isZero == 0){
                    BigDecimal temp = averMoney.subtract(averMoney.multiply(BigDecimal.valueOf(ranDown)));
                    moneys.add(temp.setScale(round,BigDecimal.ROUND_HALF_UP));
                    m = m.subtract(temp).setScale(round,BigDecimal.ROUND_HALF_UP);
                }else{
                    BigDecimal temp = averMoney.add(averMoney.multiply(BigDecimal.valueOf(ranUp)));
                    moneys.add(temp.setScale(round,BigDecimal.ROUND_HALF_UP));
                    m = m.subtract(temp).setScale(round,BigDecimal.ROUND_HALF_UP);
                }
            }
        }

        return moneys;
    }

    /**
     * 获取当前系统时间
     * @return
     */
    public static String getSystemTime(){
        String returnStr = null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        returnStr = f.format(date);
        return returnStr;
    }

    /**
     * 将两个时间之间均分
     * @param count 这天的笔数
     * @return
     * @throws ParseException
     * @throws InterruptedException
     */
    public static int findGap(String start,String end,Integer count) throws ParseException, InterruptedException{

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long count1 = sdf.parse(start).getTime();//毫秒 开始
        long count2 = sdf.parse(end).getTime();//毫秒 结束

        long diffTime=count2-count1;//时间差 毫秒

        return (int)diffTime/count;
    }

    /**
     * randomDate调用
     * @param begin
     * @param end
     * @return
     */
    public static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (rtn == begin || rtn == end) {

            return random(begin, end);
        }
        return rtn;
    }

    /**
     * 获取随机时间
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date randomDate(String beginDate, String endDate) {

        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date start = format.parse(beginDate);// 构造开始日期

            Date end = format.parse(endDate);// 构造结束日期

            // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。

            if (start.getTime() >= end.getTime()) {

                return null;

            }
            long date = random(start.getTime(), end.getTime());

            return new Date(date);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return null;
    }

    /**
     * 将还款计划信息转化为还款计划对象
     * @param planDetail
     * @param purchases
     * @return
     */
    public static List<RepayPlan> changeMapToRepayPlan(Map<String, Map<String, BigDecimal>> planDetail, Map<String,Map<String,BigDecimal>> purchases){
        List<RepayPlan> repayPlans = new ArrayList<RepayPlan>();
        boolean isFirstRepay = true;
        boolean isFirstPurchase = true;
        Integer daySize = planDetail.size();
        Integer dayIndex = 0;
        for (Map.Entry<String, Map<String, BigDecimal>> entry : planDetail.entrySet()) {
            dayIndex++;
            Map<String, BigDecimal> m = entry.getValue();
            Integer repaySize = m.size();
            Integer repayIndex = 0;
            for (Map.Entry<String, BigDecimal> entr : m.entrySet()) {
                repayIndex++;
                RepayPlan rp = new RepayPlan();
                String[] s = entr.getKey().split(" ");
                rp.setRepayDetailDate(s[0]);
                rp.setExecTime(s[1].substring(0, s[1].length() - 3));
                rp.setAmount(entr.getValue());
                if (isFirstRepay) {
                    rp.setIsFirst("T");
                    if(daySize == 1 && repaySize == 1){
                        rp.setIsLast("T");
                    }else{
                        rp.setIsLast("F");
                    }

                    isFirstRepay = false;
                } else {
                    rp.setIsFirst("F");
                    if(dayIndex == daySize && repayIndex == repaySize){
                        rp.setIsLast("T");
                    }else{
                        rp.setIsLast("F");
                    }
                }
                Map<String, BigDecimal> p = purchases.get(entr.getKey());
                List<PurchasePlan> pps = new ArrayList<PurchasePlan>();
                //消费
                Integer purSize = p.size();
                Integer purIndex = 0;
                for (Map.Entry<String, BigDecimal> ee : p.entrySet()) {
                    purIndex++;
                    String time = ee.getKey().split(" ")[1];
                    PurchasePlan pp = new PurchasePlan();
                    pp.setExecTime(time.substring(0, time.length() - 3));
                    pp.setAmount(ee.getValue());
                    if (isFirstPurchase) {
                        pp.setIsFirst("T");
                        isFirstPurchase = false;
                        if(dayIndex == daySize && repayIndex == repaySize && purIndex == purSize){
                            pp.setIsLast("T");
                        }else{
                            pp.setIsLast("F");
                        }
                    } else {
                        pp.setIsFirst("F");
                        if(dayIndex == daySize && repayIndex == repaySize && purIndex == purSize){
                            pp.setIsLast("T");
                        }else{
                            pp.setIsLast("F");
                        }

                    }
                    pps.add(pp);
                }
                rp.setPurchaseNum(p.size());
                rp.setPurchasePlans(pps);
                repayPlans.add(rp);
            }
        }
        return repayPlans;
    }

    public static boolean isRoundNum(BigDecimal num){
        if(num != null){
            if(num.divideAndRemainder(new BigDecimal(1000))[1].compareTo(BigDecimal.ZERO) == 0){
                return true;
            }
        }
        return false;
    }
    /**
     * 判断是否是连续递增的数字，如123、1234
     * @param num
     * @return
     */
    public static boolean isAddConsecutiveNum(String num){
        if(num != null){
            char[] c = num.toCharArray();
            if(c.length < 3){
                return false;
            }
            //判断是否是递增连续,如123
            for(int i=0; i<c.length-1; i++){
                if(c[i+1] - c[i] != 1){
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * 判断是否是连续递减的数字，如4321
     * @param num
     * @return
     */
    public static boolean isSubstractConsecutiveNum(String num){
        if(num != null){
            char[] c = num.toCharArray();
            if(c.length < 3){
                return false;
            }
            //判断是否是递减连续,如321
            for(int i=0; i<c.length-1; i++){
                if(c[i] - c[i+1] != 1){
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * 判断是否是同号,如111,2222,33333
     * @param num
     * @return
     */
    public static boolean isSameNum(String num){
        if(num != null){
            char[] c = num.toCharArray();
            if(c.length < 3){
                return false;
            }
            for(int i=0; i<c.length-1; i++){
                if(c[i+1] - c[i] != 0){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isLegalNum(BigDecimal num){
        if(num != null){
            String s = num.toString();
            if(!isAddConsecutiveNum(s) && !isSubstractConsecutiveNum(s) && !isSameNum(s) && !isRoundNum(num)){
                return true;
            }else{
               return false;
            }
        }
        return false;
    }

    public static boolean isLegalList(List<BigDecimal> list){
        if(list != null && list.size() > 0){
            for (int i=0; i<list.size(); i++){
                if(!isLegalNum(list.get(i))){
                    return false;
                }
            }
        }
        return true;
    }
    public static void main(String[] args ){
        BigDecimal a = new BigDecimal(11);
        String num = a.toString();
        if(!isAddConsecutiveNum(num) && !isSubstractConsecutiveNum(num) && !isSameNum(num)){
            System.out.println("可以");
        }else{
            System.out.println("非法");
        }
    }

//    public static void main(String[] args) throws Exception{
//        for (int i=0;i<10;i++) {
//            BigDecimal amount = new BigDecimal("10000");
//            BigDecimal oneMaxRepay = new BigDecimal("1000");
//            BigDecimal charge = new BigDecimal("80");
//            BigDecimal rate = BigDecimal.valueOf(0.35);
//
//
//            BigDecimal[] bigs = new BigDecimal[2];//存储每笔的上限和下限的数组
//            bigs[0] = oneMaxRepay;//上限
//            bigs[1] = oneMaxRepay.subtract((oneMaxRepay.multiply(rate).setScale(0, BigDecimal.ROUND_HALF_UP)));//下限
//            System.out.println("bigs[0]:"+oneMaxRepay+"===bigs[1]:"+bigs[1]);
//            BigDecimal tempSum = amount;//总金额 拿来做减法
//            //1、随机拆分金额 添加进集合
//            List<BigDecimal> moneys = new ArrayList<BigDecimal>();
//            boolean kftFlag = true;
//            BigDecimal tempMoney = BigDecimal.ZERO;//存放小数点后的数
//            while (true) {
//                //随机数 * (上限 - 下限 )四舍五入
//                BigDecimal ran = BigDecimal.valueOf(Math.round(Math.random() * (bigs[0].subtract(bigs[1]).intValue())));
//                if (ran.compareTo(BigDecimal.ZERO) == 0) {
//                    if (kftFlag == true) {
//                        ran = oneMaxRepay.add(charge);
//                    } else {
//                        ran = oneMaxRepay;
//                    }
//                } else {
//                    if (kftFlag == true) {
//                        ran = oneMaxRepay.add(charge);
//                    } else {
//                        ran = ran.add(bigs[1]);
//                    }
//                }
//                //减法
//                if (ran.compareTo(tempSum) != 1 && tempSum.compareTo(BigDecimal.ZERO) != -1) {
//                    moneys.add(ran.setScale(2,BigDecimal.ROUND_HALF_UP));
//                    tempSum = tempSum.subtract(ran);
//                    if (kftFlag == true) {
//                        int m = tempSum.intValue();
//                        tempMoney = tempSum.subtract(BigDecimal.valueOf(m));
//                        tempSum = BigDecimal.valueOf(m);
//                        kftFlag = false;
//                    }
//                } else if (ran.compareTo(tempSum) > 0) {
//                    if (kftFlag == true) {
//                        moneys.add(ran.setScale(2,BigDecimal.ROUND_HALF_UP));
//                        kftFlag = false;
//                    } else {
//                        moneys.add(tempSum.add(tempMoney).setScale(2,BigDecimal.ROUND_HALF_UP));
//                    }
//
//                    break;
//                }
//
//            }
//
//            //----------------判断最后两笔的总还款金额是否大于保证金，小于则合并成一笔-------------------
//            int size = moneys.size()-1;
//            BigDecimal lastAmount = moneys.get(size);
//            BigDecimal lastAmount1 = moneys.get(size-1);
//            BigDecimal last = lastAmount.add(lastAmount1);
//            BigDecimal totalAmount = oneMaxRepay;
//            //System.out.println("lastAmount:"+lastAmount+"===lastAmount1:"+lastAmount1+"--totalAmount:"+totalAmount);
//            if (last.compareTo(totalAmount)<=0){
//                moneys.remove(size);
//                moneys.remove(size-1);
//                moneys.add(last);
//            }
//
//            //--------------------------------
//
//            BigDecimal total = new BigDecimal("0");
//            for (BigDecimal tep : moneys) {
//                System.out.println("aaaaaaaaaaaaaaaaa:" + tep);
//                total = total.add(tep);
//            }
//            System.out.println("==========================================================="+total);
//        }
//    }
}

package com.goldccm.service.checkInWork.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.ResultData;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.impl.BaseDaoImpl;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.checkInWork.CheckInWorkService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: visitor
 * @description:
 * @author: cwf
 * @create: 2019-11-04 10:25
 **/
@Service("CheckInWorkService")
public class CheckInWorkServiceImpl  extends BaseServiceImpl implements CheckInWorkService {
    @Autowired
    private BaseDaoImpl baseDao;
    Logger logger = LoggerFactory.getLogger(CheckInWorkServiceImpl.class);
    /**
     * 1、插入tbl_wk_group规则字段，得到规则id。字段：id，company_id,group_type，group_name，sync_holidays，need_photo，note_can_use_local_pic，allow_checkin_offworkday，allow_apply_offworkday
     * 2、根据规则id插入规则相关打卡日期表tbl_wk_checkindate，打卡时间表tbl_wk_checkintime，打卡日期时间关系表：tbl_wk_date_time_rlat地址表tbl_wk_loc_infos。特殊日期表tbl_wk_spe_days
     * 3、修改旧规则时，需要新增一个新规则，保存旧规则，保存修改时间，以便进行判断
     * 4、同一个用户，只对最新规则进行判断
     *  新增打卡时间限制，并且中午下班打卡时间限制不得与中午上班打卡时间限制有交叉？·
     * @param request
     * @return
     */
    @Transactional(rollbackFor=RuntimeException.class)
    @Override
    public Result saveGroup(HttpServletRequest request) {
        Map<String,Object> saveMap=new HashMap<>();
        Integer companyId = BaseUtil.objToInteger(request.getParameter("company_id"), 0);
        Integer groupType = BaseUtil.objToInteger(request.getParameter("group_type"), 0);
        String groupName = BaseUtil.objToStr(request.getParameter("group_name"), "");
        String syncHolidays = BaseUtil.objToStr(request.getParameter("sync_holidays"), "F");
        String needPhoto = BaseUtil.objToStr(request.getParameter("need_photo"), "F");
        String noteCanUseLocalPic = BaseUtil.objToStr(request.getParameter("note_can_use_local_pic"), "F");
        String allowCheckinOffworkday = BaseUtil.objToStr(request.getParameter("allow_checkin_offworkday"), "F");
        String allowApplyOffworkday = BaseUtil.objToStr(request.getParameter("allow_apply_offworkday"), "F");
        saveMap.put("company_id", companyId);
        saveMap.put("group_type",groupType);
        saveMap.put("group_name",groupName);
        saveMap.put("sync_holidays", syncHolidays);
        saveMap.put("need_photo",needPhoto);
        saveMap.put("note_can_use_local_pic",noteCanUseLocalPic);
        saveMap.put("allow_checkin_offworkday",allowCheckinOffworkday);
        saveMap.put("allow_apply_offworkday",allowApplyOffworkday);
        saveMap.put("effective_time", DateUtil.getCurDate());
        try {
            //更新上一条的失效时间
            deleteOrUpdate("update tbl_wk_group set failure_time ="+DateUtil.getCurDate()+"  where company_id=" + companyId + " ORDER BY id desc  limit 1");

            int id = save(TableList.WK_GROUP,saveMap);
//        System.out.println(id);
        //插入规则相关数据
            Map<String,Object> checkInDateMap=new HashMap<>();
            Map<String,Object> checkInTimeMap=new HashMap<>();
            int remind = Integer.valueOf(request.getParameter("remind"));
            //暂时设置为1测试
//            int id=1;
//            ------------------工作日期----------------------------
            int dateId=0;
            int timeId=0;
            JSONObject dateObject=null;
            JSONArray timeInterval=null;
            StringBuffer dateTimeRlatPrefixSql = new StringBuffer("insert into " + TableList.WK_DATE_TIME_RLAT + "(date_id,time_id) values");
            StringBuffer dateTimeRlatsuffixSql = new StringBuffer();
            //添加规则中打卡日期与时间
            JSONArray datesArray=JSONArray.parseArray(request.getParameter("checkInDate"));
            for(int i=0;i<datesArray.size();i++){
                dateObject = datesArray.getJSONObject(i);
                timeInterval=dateObject.getJSONArray("time_interval");
                checkInDateMap.put("group_id",id);
                checkInDateMap.put("workDays",dateObject.getString("workDays"));
                checkInDateMap.put("flex_time",dateObject.getString("flex_time"));
                checkInDateMap.put("noneed_offwork",dateObject.getString("noneed_offwork"));
                checkInDateMap.put("limit_aheadtime",dateObject.getString("limit_aheadtime"));
                dateId = save(TableList.WK_CHECKINDATE, checkInDateMap);
                //用dateId插入checkInTime
                System.out.println(timeInterval.size());

                if (timeInterval!=null) {
                    for (int j = 0; j < timeInterval.size(); j++) {
                        if (j % 2 == 0) {
                            System.out.println("上班时间："+timeInterval.getInteger(j));
                            System.out.println("上班提醒时间："+(timeInterval.getInteger(j)-Integer.valueOf(request.getParameter("remind"))));
                            checkInTimeMap.put("work_sec",timeInterval.getInteger(j));
                            checkInTimeMap.put("remind_work_sec",(timeInterval.getInteger(j)-Integer.valueOf(request.getParameter("remind"))));

                        }else{
                            System.out.println("下班时间："+timeInterval.getInteger(j));
                            System.out.println("下班提醒时间："+(timeInterval.getInteger(j)-Integer.valueOf(request.getParameter("remind"))));
                            checkInTimeMap.put("off_work_sec",timeInterval.getInteger(j));
                            checkInTimeMap.put("remind_off_work_sec",(timeInterval.getInteger(j)-Integer.valueOf(request.getParameter("remind"))));
                             timeId = save(TableList.WK_CHECKINTIME, checkInTimeMap);
                            //拼接批量插入语句
                            dateTimeRlatsuffixSql.append("("+dateId+","+timeId+"),");
                        }
                    }
                }
                System.out.println(dateObject);

            }
            //插入date与time关系
            int[] ints = baseDao.batchUpdate(dateTimeRlatPrefixSql + dateTimeRlatsuffixSql.substring(0, dateTimeRlatsuffixSql.length() - 1));
//            "loc_title":"福州软件园G区1#楼","loc_detail":"福建省福州市闽侯县","lat":"30547030","lng":"104062890","distance":"300"
            //------------------地址----------------------------
            JSONArray locsArray=JSONArray.parseArray(request.getParameter("loc_infos"));
            JSONObject locObject=null;
            Map<String,Object> locMap=new HashMap<>();
            StringBuffer locsPrefixSql = new StringBuffer("insert into " + TableList.WK_LOC_INFOS + "(group_id,lat,lng,loc_title," +
                    "loc_detail,distance) values");
            StringBuffer locsSuffixSql = new StringBuffer();
            for(int k=0;k<locsArray.size();k++){
                locObject=  locsArray.getJSONObject(k);
                locsSuffixSql.append("("+id+","+locObject.getString("lat")+","+locObject.getString("lng")+",'"
                        +locObject.getString("loc_title")+"','"
                        +locObject.getString("loc_detail")+"','"
                        +locObject.getString("distance")+"'),");
            }
            //批量插入地址信息
            int[] locs = baseDao.batchUpdate(locsPrefixSql + locsSuffixSql.substring(0, locsSuffixSql.length() - 1));
            System.out.println(locs[0]);
//            ------------------特殊日期----------------------------
            String spe_workdays = request.getParameter("spe_workdays");
            if(spe_workdays!=null&&!"".equals(spe_workdays)) {
               JSONArray speDatesArray=JSONArray.parseArray(spe_workdays);
               int speDateId = 0;
               int spetimeId = 0;
               JSONObject speDateObject = null;
               JSONArray speTimeInterval = null;
                Map<String,Object> speInDateMap=new HashMap<>();
                Map<String,Object> speInTimeMap=new HashMap<>();
               StringBuffer speTimeRlatPrefixSql = new StringBuffer("insert into " + TableList.WK_SPE_DAYS_TIME_RLAT + "(spe_id,time_id) values");
               StringBuffer speTimeRlatsuffixSql = new StringBuffer();
//            //添加特殊日期规则中打卡日期与时间
               for (int l = 0; l < speDatesArray.size(); l++) {
                   speDateObject = speDatesArray.getJSONObject(l);
                   speTimeInterval = speDateObject.getJSONArray("time_interval");
                   speInDateMap.put("group_id", id);
                   speInDateMap.put("spe_date", speDateObject.getString("spe_date"));
                   speInDateMap.put("type", speDateObject.getString("type"));
                   speInDateMap.put("notes", speDateObject.getString("notes"));
                   speDateId = save(TableList.WK_SPE_DAYS, speInDateMap);
                   //用dateId插入checkInTime
                   if (speTimeInterval != null) {
                       for (int j = 0; j < speTimeInterval.size(); j++) {
                           if (j % 2 == 0) {
                               logger.info("上班时间：" + speTimeInterval.getInteger(j));
                               logger.info("上班提醒时间：" + (speTimeInterval.getInteger(j) - Integer.valueOf(request.getParameter("remind"))));
                               checkInTimeMap.put("work_sec", speTimeInterval.getInteger(j));
                               checkInTimeMap.put("remind_work_sec", (speTimeInterval.getInteger(j) - Integer.valueOf(request.getParameter("remind"))));

                           } else {
                               logger.info("下班时间：" + speTimeInterval.getInteger(j));
                               logger.info("下班提醒时间：" + (speTimeInterval.getInteger(j) - Integer.valueOf(request.getParameter("remind"))));
                               checkInTimeMap.put("off_work_sec", speTimeInterval.getInteger(j));
                               checkInTimeMap.put("remind_off_work_sec", (speTimeInterval.getInteger(j) - Integer.valueOf(request.getParameter("remind"))));
                               spetimeId = save(TableList.WK_CHECKINTIME, checkInTimeMap);
                               logger.info("tbl_wk_checkintime 并获取spetimeId：{}",spetimeId);
                               //拼接批量插入语句
                               speTimeRlatsuffixSql.append("(" + speDateId + "," + spetimeId + "),");
                           }
                       }
                   }
                   logger.info(speDateObject.toJSONString());
               }
                //插入date与time关系
                int[] spes = baseDao.batchUpdate(speTimeRlatPrefixSql + speTimeRlatsuffixSql.substring(0, speTimeRlatsuffixSql.length() - 1));
                logger.info("批量插入特殊日期关系表语句：\n{}",speTimeRlatPrefixSql + speTimeRlatsuffixSql.substring(0, speTimeRlatsuffixSql.length() - 1));
            }
//            ------------------下发用户----------------------------
            JSONArray userList = JSONArray.parseArray(request.getParameter("userList"));
            StringBuffer userPrefixSql = new StringBuffer("insert into " + TableList.WK_USER_GROUP_RLAT + "(group_id,user_id) values");
            StringBuffer userSuffixSql = new StringBuffer();
            for (Object userId:userList){
                userSuffixSql.append("("+id+","+userId+"),");
            }
            logger.info("批量插入用户规则关系表语句：\n{}",userPrefixSql+userSuffixSql.substring(0,userSuffixSql.length()-1));
            baseDao.batchUpdate(userPrefixSql+userSuffixSql.substring(0,userSuffixSql.length()-1));
            //------------------下发白名单----------------------------
            JSONArray whiteList = JSONArray.parseArray(request.getParameter("whiteList"));
            StringBuffer whitePrefixSql = new StringBuffer("insert into " + TableList.WK_WHITE_LIST+ "(group_id,user_id) values");
            StringBuffer whiteSuffixSql = new StringBuffer();
            for (Object whiteId:whiteList){
                whiteSuffixSql.append("("+id+","+whiteId+"),");
            }
            logger.info("批量插入白名单语句：\n{}",whitePrefixSql+whiteSuffixSql.substring(0,whiteSuffixSql.length()-1));
            baseDao.batchUpdate(whitePrefixSql+whiteSuffixSql.substring(0,whiteSuffixSql.length()-1));
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
            return Result.unDataResult("fail","插入数据错误");
        }
        return Result.unDataResult("test","success");
    }
    /**
     *  判断是否为白名单内，白名单无需打卡
     *  判断是否特殊日期，如果是，则按照特殊日期打卡
     *
     * @param paramMap 传入用户id，用户公司 年月日
     * @return Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/13 17:20
     */
    @Override
    public Result gainWorkOne(Map<String, Object> paramMap) throws ParseException {

        Long userId = BaseUtil.objToLong(paramMap.get("userId"), null);
        Long companyId = BaseUtil.objToLong(paramMap.get("companyId"), null);
        String date = BaseUtil.objToStr(paramMap.get("date"), null);
        if (userId==null||companyId==null||date==null){
            return Result.unDataResult("fail","缺少参数！");
        }
        //获取打卡规则
        String groupSql="select user_id,wg.* from " + TableList.WK_GROUP + " wg \n" +
                "left join " + TableList.WK_USER_GROUP_RLAT+
                " wugr on wugr.group_id=wg.id where user_id=" + userId + " and company_id=" + companyId + " ORDER BY wg.id desc";
        Map<String, Object> groupMap = findFirstBySql(groupSql);
        logger.info("用户{}的规则sql:\n{}",userId,groupSql);
        Long groupId=BaseUtil.objToLong(groupMap.get("id"),null);

        if (groupId==null){
            return Result.unDataResult("fail","未找到用户规则");
        }else{


            //获取白名单,如果用户是白名单内，不需要打卡，返回
            Map<String, Object> white = findFirstBySql("select * from " + TableList.WK_WHITE_LIST + " where user_id=" + userId + " and group_id=" + groupId);
            if (white!=null){
                //返回信息 code=201=白名单
               return Result.ResultCode("success","用户在白名单内","201");
            }
            //获取特殊日期
            String  coloumSql=" select id,spe_date,type,notes ";
            String  fromSql=" from "+TableList.WK_SPE_DAYS+" where group_id="+groupId;
            List<Map<String, Object>> speDaysList =findList(coloumSql,fromSql);
            Map<String, Object> checkDateMap;
//            Map<String, Object> checkTimeMap;
            Object checdDateId = null;
            List<Map<String, Object>> dateList;
            //时间转unix
//            long unixTimestamp = DateUtil.StrToUnix(date);
//            long nextUnixTimestamp = DateUtil.NextStrToUnix(date);
//            System.out.println(unixTimestamp);
//            System.out.println(nextUnixTimestamp);

            //获取地址信息
            coloumSql="select lat,lng,loc_title,loc_detail,distance ";
            fromSql=" from "+TableList.WK_LOC_INFOS+" where group_id="+groupId;
            List<Map<String, Object>> locInfosList = findList(coloumSql, fromSql);

            String startDate = DateUtil.getDate(date);
            String nextDate = DateUtil.NextDate(date);
            //当天打卡记录 有效记录未筛选
            coloumSql="select * ";
            fromSql=" from " + TableList.WK_RECORD +" where group_id=" + groupId + " and user_id=" + userId +
                    " and checkin_time between '"+ startDate + "' and '"+nextDate+"'";
            logger.info("用户{}的打卡记录sql:\n{}",userId,coloumSql+fromSql);
            List<Map<String, Object>>  dayWork = findList(coloumSql,fromSql);
            groupMap.put("dayWork",dayWork);
            //插入地址信息
            groupMap.put("loc_infos",locInfosList);
            for (int i=0 ;i<speDaysList.size();i++ ) {
                //打卡日期数据
                checkDateMap = speDaysList.get(i);
                
                checdDateId = checkDateMap.get("id");
                //需要打卡的日期type=1并且时间为今天
                if ("1".equals(BaseUtil.objToStr(checkDateMap.get("type"),""))&&startDate.equals(BaseUtil.objToLong(checkDateMap.get("timestamp"),null))){
                    //查询需要打卡的时间 当日零点开始的秒数
                    coloumSql="select work_sec,off_work_sec,remind_work_sec,remind_off_work_sec ";
                    fromSql =" from "+TableList.WK_CHECKINTIME+" " +
                            "wt left join "+TableList.WK_SPE_DAYS_TIME_RLAT+" " +
                            "str on wt.id=str.time_id where str.spe_id="+checdDateId;
                    logger.info("用户{}的需要打卡的时间sql:\n{}",userId,coloumSql+fromSql);
                    dateList = findList(coloumSql, fromSql);
//                    speDaysList.get(i).put("interval",dateList);
//                    System.out.println(coloumSql+fromSql);
                    //插入打卡时间区间
                    groupMap.put("interval",dateList);
                    return ResultData.dataResult("success","获取打卡规则成功",groupMap);
                    //如果type=2 当天不需要打卡
                }else if ("2".equals(BaseUtil.objToStr(checkDateMap.get("type"),""))&&nextDate.equals(BaseUtil.objToLong(checkDateMap.get("timestamp"),null))){

                    return Result.ResultCode("success","不需要打卡的日期","202");
                }
            }
            /**
             * 判断今天是星期几，如果星期在workDays之间，则当日为工作日
             * @date 2019/11/20 15:25
             */
            //获取打卡时间
             coloumSql="select id,workdays,flex_time,noneed_offwork,limit_aheadtime";
             fromSql=" from "+TableList.WK_CHECKINDATE+" where group_id="+groupId;

            String week = String.valueOf(DateUtil.getWeek(date));
            List<Map<String, Object>> checkDateList = findList(coloumSql, fromSql);
            for (int i=0 ;i<checkDateList.size();i++ ) {
                //打卡日期数据
                checkDateMap = checkDateList.get(i);
                checdDateId = checkDateMap.get("id");
                String workdays = BaseUtil.objToStr(checkDateMap.get("workdays"), ",");
                String[] weekDay = workdays.split(",");
                for (String s:weekDay){
                    //星期存在日期则查找时间区间
                    if (week.equals(s)){
//                    fromSql = "select GROUP_CONCAT(inter) inter from (\n" +
//                            "select dtr.date_id,CONCAT(work_sec,',',off_work_sec) inter from " + TableList.WK_CHECKINTIME + " wt left join " + TableList.WK_DATE_TIME_RLAT +
//                            " dtr on wt.id=dtr.time_id where dtr.date_id=" + checdDateId+")x";
//                    Map<String, Object> interval = findFirstBySql(fromSql);
//                        logger.info("用户{}的需要打卡的时间sql:\n{}",userId,fromSql);
//                    checkDateList.get(i).put("interval",interval.get("inter"));
                        coloumSql="select work_sec,off_work_sec,remind_work_sec,remind_off_work_sec ";
                        fromSql =" from "+TableList.WK_CHECKINTIME+" " +
                                "wt left join "+TableList.WK_DATE_TIME_RLAT+" " +
                                "dtr on wt.id=dtr.time_id where dtr.date_id="+checdDateId;
                        logger.info("用户{}的需要打卡的时间sql:\n{}",userId,coloumSql+fromSql);
                        dateList = findList(coloumSql, fromSql);
                        groupMap.put("interval",dateList);
                        return ResultData.dataResult("success","获取打卡规则成功",groupMap);
                    }

                }
                }

//            groupMap.put("checkInDate",checkDateList);

            return ResultData.dataResult("success","测试",groupMap);
        }
    }
    /**
     * 打卡
     * @param paramMap
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/15 13:38
     */
    @Override
    public Result saveWork(Map<String, Object> paramMap) {
        Map<String, Object> saveMap=paramMap;
//        System.out.println("--------------");
        saveMap.entrySet().removeIf(m ->
                m.getValue()==null ||"".equals(m.getValue())&&
                        !"user_id".equals(m.getKey())&&
                        !"group_id".equals(m.getKey())&&
                        !"checkin_type".equals(m.getKey())&&
                        !"exception_type".equals(m.getKey())&&
                        !"checkin_date".equals(m.getKey())&&
                        !"checkin_time".equals(m.getKey())&&
                        !"location_title".equals(m.getKey())&&
                        !"location_detail".equals(m.getKey())&&
                        !"wifi_name".equals(m.getKey())&&
                        !"wifi_mac".equals(m.getKey())&&
                        !"checkin_divice".equals(m.getKey())&&
                        !"notes".equals(m.getKey())&&
                        !"mediaids".equals(m.getKey())&&
                        !"lng".equals(m.getKey())&&
                        !"exp1".equals(m.getKey())&&
                        !"exp2".equals(m.getKey())&&
                        !"exp3".equals(m.getKey())
        );
        int save = save(TableList.WK_RECORD, saveMap);
        //判断上一条是否作废 比如更新下班卡  前端+后端判断

        if (save>0){
            return Result.unDataResult("success","打卡成功");
        }
        return Result.unDataResult("fail","打卡失败");
    }

    @Override
    public Result gainMonthStatistics(Map<String, Object> paramMap) {
        return null;
    }


    /**
     * 日历 个人日打卡记录
     * 统计这个月的每一天是否有异常，前端显示红点--有异常，白点--正常
     * @param paramMap	 
     * @return com.goldccm.model.compose.Result 
     * @throws Exception    
     * @author cwf 
     * @date 2019/11/15 15:28
     */
    @Override
    public Result gainCalendarStatistics(Map<String, Object> paramMap) {
        /**
         *         统计某个月每一天的异常情况
         *         1、查询规则
         *         查看规则是否变化，根据规则表的变动时间进行查询
         *         2、比对规则
         *
         */

        return null;
    }
    //生成测试数据
    @Override
    public  Result effective(Map<String, Object> paramMap) throws ParseException {

        StringBuffer dateTimeRlatPrefixSql = new StringBuffer("INSERT INTO `visitor`.`tbl_wk_record`(`user_id`, `group_id`, `group_name`, `checkin_type`, `exception_type`,`checkin_date`, `checkin_time`, `location_title`, `location_detail`, `wifi_name`, `wifi_mac`, `checkin_divice`, `notes`, `mediaids`, `lat`, `lng` ) values");
        StringBuffer dateTimeRlatsuffixSql = new StringBuffer();
        String nextMinu = DateUtil.NextMinu("2019-12-02 00:00:00");

        for (int i=1;i<24*60;i++){
            nextMinu= DateUtil.NextMinu(nextMinu);


            dateTimeRlatsuffixSql.append("(10, 24, '日常考勤', '上班打卡', '地点异常','"+nextMinu.substring(0,10)+"', '"+nextMinu.substring(11)+"', '依澜府', '四川省成都市武侯区益州大道中段784号附近', '办公一区', '3c:46:d8:0c:7a:70', '小米note3', '路上堵车，迟到了5分钟', 'WWCISP_G8PYgRaOVHjXWUWFqchpBqqqUpGj0OyR9z6WTwhnMZGCPHxyviVstiv_2fTG8YOJq8L8zJT2T2OvTebANV-2MQ', 30547645, 104063236),");
        }

        int[] locs = baseDao.batchUpdate(dateTimeRlatPrefixSql + dateTimeRlatsuffixSql.substring(0, dateTimeRlatsuffixSql.length() - 1));
        return Result.unDataResult("test","test");
    }
    /**
     * 管理员 查看的月统计，月报
     * 日统计，一、上下班统计：1、迟到分钟数 2、早退分钟数 3、旷工分钟数 4、缺卡分钟数 5、地点异常数 6设备异常数
     *        二、假勤统计：1、打卡补卡 2、外勤 3、外出 4、出差 、5、年假 6、事假 7、病假 8、调休假 9、婚假 10、产假 11、陪产假 12、其他
     * 备注：当天没有打卡记录为旷工，当天有部分打卡为缺卡。
     * @param date
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/19 10:04
     */
    public Result gainMonthStatistics(String date){

        return null;
    }

//    public static void main(String[] args) throws ParseException {
//        //时间转unix
//        Long timestamp = Long.parseLong("1492617611")*1000;
//        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
//        System.out.println(date);
//        //unix转时间
//        Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
//        long unixTimestamp = date1.getTime()/1000;
//        System.out.println(unixTimestamp);
//
//    }

}

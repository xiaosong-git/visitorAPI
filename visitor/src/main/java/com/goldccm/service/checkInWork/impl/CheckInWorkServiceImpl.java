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
public class CheckInWorkServiceImpl extends BaseServiceImpl implements CheckInWorkService {
    @Autowired
    private BaseDaoImpl baseDao;
    Logger logger = LoggerFactory.getLogger(CheckInWorkServiceImpl.class);

    /**
     * 1、插入tbl_wk_group规则字段，得到规则id。字段：id，company_id,group_type，group_name，sync_holidays，need_photo，note_can_use_local_pic，allow_checkin_offworkday，allow_apply_offworkday
     * 2、根据规则id插入规则相关打卡日期表tbl_wk_checkindate，打卡时间表tbl_wk_checkintime，打卡日期时间关系表：tbl_wk_date_time_rlat地址表tbl_wk_loc_infos。特殊日期表tbl_wk_spe_days
     * 3、修改旧规则时，需要新增一个新规则，保存旧规则，保存修改时间，以便进行判断
     * 4、同一个用户，只对最新规则进行判断
     *
     *
     * @param jsonObject json窜
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Result addGroup(JSONObject jsonObject) {
        Map<String, Object> group = new HashMap<>();
        JSONObject groupJson = jsonObject.getJSONObject("group");
        Integer companyId = BaseUtil.objToInteger(groupJson.get("companyId"), 0);
        Integer groupType = BaseUtil.objToInteger(groupJson.get("groupType"), 0);
        String groupName = BaseUtil.objToStr(groupJson.get("groupName"), "");
//        String syncHolidays = BaseUtil.objToStr(jsonObject.get("sync_holidays"), "F");
//        String needPhoto = BaseUtil.objToStr(jsonObject.get("need_photo"), "F");
//        String noteCanUseLocalPic = BaseUtil.objToStr(jsonObject.get("note_can_use_local_pic"), "F");
//        String allowCheckinOffworkday = BaseUtil.objToStr(jsonObject.get("allow_checkin_offworkday"), "F");
//        String allowApplyOffworkday = BaseUtil.objToStr(jsonObject.get("allow_apply_offworkday"), "F");
        group.put("company_id", companyId);
        group.put("group_type", groupType);
        group.put("group_name", groupName);
//        group.put("sync_holidays", syncHolidays);
//        group.put("need_photo", needPhoto);
//        group.put("note_can_use_local_pic", noteCanUseLocalPic);
//        group.put("allow_checkin_offworkday", allowCheckinOffworkday);
//        group.put("allow_apply_offworkday", allowApplyOffworkday);
        group.put("effective_time", DateUtil.getCurDate());
        try {
            Object groupId = groupJson.get("groupId");
            if (groupId!=null){
                deleteOrUpdate("update "+TableList.WK_GROUP+" set failure_time =now()  where id="+groupId);
            }
            int id = save(TableList.WK_GROUP, group);

//        System.out.println(id);
            //插入规则相关数据
            Map<String, Object> checkInDateMap = new HashMap<>();
            Map<String, Object> checkInTimeMap = new HashMap<>();
            //暂时设置为1测试
//            int id=1;
//            ------------------工作日期----------------------------
            int dateId = 0;
            int timeId = 0;
            JSONObject dateObject = null;
            String timeInterval = null;
            StringBuffer dateTimeRlatPrefixSql = new StringBuffer("insert into " + TableList.WK_DATE_TIME_RLAT + "(date_id,time_id) values");
            StringBuffer dateTimeRlatsuffixSql = new StringBuffer();
            //添加规则中打卡日期与时间
            JSONArray datesArray = jsonObject.getJSONArray("checkInDate");
            for (int i = 0; i < datesArray.size(); i++) {
                dateObject = datesArray.getJSONObject(i);
                timeInterval = dateObject.getString("timeInterval");
                checkInDateMap.put("group_id", id);
                checkInDateMap.put("workdays", dateObject.getString("workDays"));
                checkInDateMap.put("noneed_offwork", dateObject.getString("noneedOffwork"));
//                checkInDateMap.put("limit_aheadtime", dateObject.getString("limitAheadtime"));
                dateId = save(TableList.WK_CHECKINDATE, checkInDateMap);
                //用dateId插入checkInTime

                int preWorkSec;
                String workTime;
//                String remind=jsonObject.getString("remind");
//                String preworkTime;
                String[] timeIntervalSplit = timeInterval.split(",");
                if (timeIntervalSplit != null) {
                    for (int j = 0; j < timeIntervalSplit.length; j++) {
                        workTime = timeIntervalSplit[j];
//                        preWorkSec= DateUtil.timeToSec(workTime)-DateUtil.timeToSec(remind);
//                        preworkTime = DateUtil.secToTime(preWorkSec);
                        if (j % 2 == 0) {
                            System.out.println("上班时间：" + workTime);
//                            System.out.println("上班提醒时间：" + preworkTime);
                            checkInTimeMap.put("work_sec", workTime);
//                            checkInTimeMap.put("remind_work_sec", preworkTime);
                        } else {
                            System.out.println("下班时间：" + workTime);
//                            System.out.println("下班提醒时间：" + preworkTime);
                            checkInTimeMap.put("off_work_sec", workTime);
//                            checkInTimeMap.put("remind_off_work_sec", preworkTime);
                            timeId = save(TableList.WK_CHECKINTIME, checkInTimeMap);
                            //拼接批量插入语句
                            dateTimeRlatsuffixSql.append("(" + dateId + "," + timeId + "),");
                        }
                    }
                }
                System.out.println(dateObject);

            }
            //插入date与time关系
            int[] ints = baseDao.batchUpdate(dateTimeRlatPrefixSql + dateTimeRlatsuffixSql.substring(0, dateTimeRlatsuffixSql.length() - 1));
//            "loc_title":"福州软件园G区1#楼","loc_detail":"福建省福州市闽侯县","lat":"30547030","lng":"104062890","distance":"300"
            //------------------地址----------------------------
            JSONArray locsArray = jsonObject.getJSONArray("locInfos");
            JSONObject locObject = null;
            Map<String, Object> locMap = new HashMap<>();
            StringBuffer locsPrefixSql = new StringBuffer("insert into " + TableList.WK_LOC_INFOS + "(group_id,lat,lng,loc_title," +
                    "loc_detail,distance) values");
            StringBuffer locsSuffixSql = new StringBuffer();
            for (int k = 0; k < locsArray.size(); k++) {
                locObject = locsArray.getJSONObject(k);
                locsSuffixSql.append("(" + id + "," + locObject.getString("lat") + "," + locObject.getString("lng") + ",'"
                        + locObject.getString("locTitle") + "','"
                        + locObject.getString("locDetail") + "','"
                        + locObject.getString("distance") + "'),");
            }
            //批量插入地址信息
            int[] locs = baseDao.batchUpdate(locsPrefixSql + locsSuffixSql.substring(0, locsSuffixSql.length() - 1));
            System.out.println(locs[0]);
//            ------------------特殊日期----------------------------
//            String spe_workdays = jsonObject.getString("spe_workdays");
//            if (spe_workdays != null && !"".equals(spe_workdays)) {
//                JSONArray speDatesArray = JSONArray.parseArray(spe_workdays);
//                int speDateId = 0;
//                int spetimeId = 0;
//                JSONObject speDateObject = null;
//                JSONArray speTimeInterval = null;
//                Map<String, Object> speInDateMap = new HashMap<>();
//                Map<String, Object> speInTimeMap = new HashMap<>();
//                StringBuffer speTimeRlatPrefixSql = new StringBuffer("insert into " + TableList.WK_SPE_DAYS_TIME_RLAT + "(spe_id,time_id) values");
//                StringBuffer speTimeRlatsuffixSql = new StringBuffer();
////            //添加特殊日期规则中打卡日期与时间
//                for (int l = 0; l < speDatesArray.size(); l++) {
//                    speDateObject = speDatesArray.getJSONObject(l);
//                    speTimeInterval = speDateObject.getJSONArray("time_interval");
//                    speInDateMap.put("group_id", id);
//                    speInDateMap.put("spe_date", speDateObject.getString("spe_date"));
//                    speInDateMap.put("type", speDateObject.getString("type"));
//                    speInDateMap.put("notes", speDateObject.getString("notes"));
//                    speDateId = save(TableList.WK_SPE_DAYS, speInDateMap);
//                    //用dateId插入checkInTime
//                    if (speTimeInterval != null) {
//                        for (int j = 0; j < speTimeInterval.size(); j++) {
//                            if (j % 2 == 0) {
//                                logger.info("上班时间：" + speTimeInterval.getInteger(j));
//                                logger.info("上班提醒时间：" + (speTimeInterval.getInteger(j) - jsonObject.getInteger("remind")));
//                                checkInTimeMap.put("work_sec", speTimeInterval.getInteger(j));
//                                checkInTimeMap.put("remind_work_sec", (speTimeInterval.getInteger(j) - jsonObject.getInteger("remind")));
//
//                            } else {
//                                logger.info("下班时间：" + speTimeInterval.getInteger(j));
//                                logger.info("下班提醒时间：" + (speTimeInterval.getInteger(j) - jsonObject.getInteger("remind")));
//                                checkInTimeMap.put("off_work_sec", speTimeInterval.getInteger(j));
//                                checkInTimeMap.put("remind_off_work_sec", (speTimeInterval.getInteger(j) - jsonObject.getInteger("remind")));
//                                spetimeId = save(TableList.WK_CHECKINTIME, checkInTimeMap);
//                                logger.info("tbl_wk_checkintime 并获取spetimeId：{}", spetimeId);
//                                //拼接批量插入语句
//                                speTimeRlatsuffixSql.append("(" + speDateId + "," + spetimeId + "),");
//                            }
//                        }
//                    }
//                    logger.info(speDateObject.toJSONString());
//                }
//                //插入date与time关系
//                int[] spes = baseDao.batchUpdate(speTimeRlatPrefixSql + speTimeRlatsuffixSql.substring(0, speTimeRlatsuffixSql.length() - 1));
//                logger.info("批量插入特殊日期关系表语句：\n{}", speTimeRlatPrefixSql + speTimeRlatsuffixSql.substring(0, speTimeRlatsuffixSql.length() - 1));
//            }
//            ------------------下发用户----------------------------
            String userList=jsonObject.getString("userList");
            String[] userSplit = userList.split(",");
            StringBuffer userPrefixSql = new StringBuffer("insert into " + TableList.WK_USER_GROUP_RLAT + "(group_id,user_id) values");
            StringBuffer userSuffixSql = new StringBuffer();
            for (Object userId : userSplit) {
                userSuffixSql.append("(" + id + "," + userId + "),");
            }
            logger.info("批量插入用户规则关系表语句：\n{}", userPrefixSql + userSuffixSql.substring(0, userSuffixSql.length() - 1));
            baseDao.batchUpdate(userPrefixSql + userSuffixSql.substring(0, userSuffixSql.length() - 1));
            //------------------下发白名单----------------------------
            String whiteList = jsonObject.getString("whiteList");
            if (whiteList!=null) {
                String[] whiteSplit = whiteList.split(",");
                StringBuffer whitePrefixSql = new StringBuffer("insert into " + TableList.WK_WHITE_LIST + "(group_id,user_id) values");
                StringBuffer whiteSuffixSql = new StringBuffer();
                for (Object whiteId : whiteSplit) {
                    whiteSuffixSql.append("(" + id + "," + whiteId + "),");
                }
                logger.info("批量插入白名单语句：\n{}", whitePrefixSql + whiteSuffixSql.substring(0, whiteSuffixSql.length() - 1));
                baseDao.batchUpdate(whitePrefixSql + whiteSuffixSql.substring(0, whiteSuffixSql.length() - 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
            return Result.unDataResult("fail", "插入数据错误");
        }
        return Result.unDataResult("success", "操作成功");
    }
    //管理员获取规则
    @Override
    public Result gainGroupIndex(Map<String, Object> paramMap){
        Object companyId = paramMap.get("companyId");
        if (companyId==null){
            return Result.unDataResult("fail","缺少公司参数");
        }
        String coloumSql="select groupId,groupName,dateId,workdays,wk,time,loc_title locTitle,dateCount,count(*) locCount";
        String fromSql="  from \n" +
                "(select id groupId,dateCount,group_name groupName,date_id dateId,workdays,noneed_offwork noneedOffwork,t,CONCAT(fir,'至',ed) wk,time from(\n" +
                "select *,count(*) dateCount,\n" +
                "case when f='1' then '周一' when f='2' then '周二' when f='3' then '周三' when f='4' then '周四' \n" +
                "when f='5' then '周五' when f='6' then '周六' when f='0' then '周天' else '无' end fir,\n" +
                "case when e='1' then '周一' when e='2' then '周二' when e='3' then '周三' when e='4' then '周四' \n" +
                "when e='5' then '周五' when e='6' then '周六' when e='0' then '周天' else '无' end ed,group_concat(t Separator ' ') time from (\n" +
                "select g.id,g.group_name,date_id,workdays,SUBSTRING(workdays,1,1) f,SUBSTRING(workdays,-1,1) e,noneed_offwork,CONCAT(work_sec,'-',off_work_sec) t from wk_group g left join wk_checkindate d on d.group_id=g.id\n" +
                "left join wk_date_time_rlat r on r.date_id=d.id\n" +
                "left join wk_checkintime t on t.id=r.time_id\n" +
                "where company_id ="+companyId+" and failure_time is null and effective_time is not null \n" +
                ") x\n" +
                "group by x.id,x.date_id)x\n" +
                "group by x.id)x left join wk_loc_infos i on x.groupId=i.group_id\n" +
                "group by x.groupId";
        List<Map<String,Object>> list = findList(coloumSql,fromSql);
        if (list==null&&list.isEmpty()){
            return Result.unDataResult("success","暂无数据");
        }
       return ResultData.dataResult("success","获取成功",list);
    }
    //管理员获取规则详情
    @Override
    public Result gainGroupDetail(Map<String, Object> paramMap){
        Object groupId = paramMap.get("groupId");
        if (groupId==null){
            return Result.unDataResult("fail","缺少规则参数");
        }
        Map<String,Object> resultMap= new HashMap<>();
        String coloumSql="select group_name groupName,group_type groupType,company_id companyId from "+TableList.WK_GROUP+" where id="+groupId;
        Map<String, Object> group = findFirstBySql(coloumSql);

        coloumSql="select  groupId ,workdays workDays, noneedOffwork,group_concat(t Separator ',') timeInterval";
        String fromSql=" from (" +
                "select group_id groupId,workdays,noneed_offwork noneedOffwork,CONCAT(work_sec,',',off_work_sec) t from wk_checkindate c left join wk_date_time_rlat dtr on\n" +
                "                dtr.date_id=c.id left join wk_checkintime t on dtr.time_id=t.id \n" +
                "                where group_id="+groupId+")x\n" +
                "GROUP BY groupId,workdays";
        List<Map<String,Object>> checkInDate = findList(coloumSql, fromSql);
        Map<String, Object> user = findFirstBySql("select DISTINCT group_concat(ugr.user_id Separator ',') userList from wk_group g " +
                "left join wk_user_group_rlat ugr on g.id=ugr.group_id where g.id=" + groupId + "\n");
        resultMap.put("group",group);
        resultMap.put("checkInDate",checkInDate);
        if (user!=null){
            resultMap.put("userList",BaseUtil.objToStr(user.get("userList"),""));
        }else {
            resultMap.put("userList","");
        }
        String locColoumSql="select lat,lng,loc_title locTitle,loc_detail locDetail,distance ";
        String locFromSql= "from  wk_loc_infos where group_id= "+groupId;
        List locList = findList(locColoumSql, locFromSql);
        resultMap.put("locInfos",locList);

        Map<String, Object> whiteList = findFirstBySql("select DISTINCT group_concat(white.user_id Separator ',') whiteList from wk_group g" +
                " left join wk_white_list white on g.id=white.group_id where g.id=" + groupId);
        if (whiteList!=null) {
            resultMap.put("whiteList", BaseUtil.objToStr(whiteList.get("whiteList"),""));
        }else {
            resultMap.put("whiteList", "");
        }
        return ResultData.dataResult("success","获取成功",resultMap);
    }
    /**
     * 获取当天需要打卡记录
     * @param paramMap
     * @return Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/13 17:20
     */
    public Result gainWork(Map<String, Object> paramMap) throws ParseException {

        Long userId = BaseUtil.objToLong(paramMap.get("userId"), null);
        Long companyId = BaseUtil.objToLong(paramMap.get("companyId"), null);
        String date = BaseUtil.objToStr(paramMap.get("date"), null);
        if (userId == null || companyId == null ) {
            return Result.unDataResult("fail", "缺少参数！");
        }
        //插入考勤统计表
//        "insert into wk_day_statistics(user_id,group_id,need_checkin_date,need_checkin_time,checkin_type) \n" ;
        String coloum = "select DISTINCT  x.user_id,x.group_id,x.need_checkin_date,x.need_checkin_time,x.checkin_type";
        String groupSql = " from (\n" +
                "(select ugr.*,DATE_FORMAT(SYSDATE(),'%Y-%m-%d') need_checkin_date,work_sec need_checkin_time, 1 checkin_type  from wk_group g  \n" +
                "left join wk_checkindate cd on g.id=cd.group_id\n" +
                "left join wk_user_group_rlat ugr on ugr.group_id=g.id\n" +
                "left join wk_date_time_rlat dtr on dtr.date_id=cd.id \n" +
                "left join wk_checkintime ct on ct.id=dtr.time_id\n" +
                "where failure_time is null  and effective_time is not null\n" +
                "and  ugr.user_id=" + userId + " and g.company_id=" + companyId + "\n" +
                "and FIND_IN_SET(WEEKDAY(SYSDATE()),workdays)>0 " +
                ")\n" +
                "union\n" +
                "(select ugr.*,DATE_FORMAT(SYSDATE(),'%Y-%m-%d') need_checkin_date,off_work_sec need_checkin_time,2 checkin_type from wk_group g  \n" +
                "left join wk_checkindate cd on g.id=cd.group_id\n" +
                "left join wk_user_group_rlat ugr on ugr.group_id=g.id\n" +
                "left join wk_date_time_rlat dtr on dtr.date_id=cd.id \n" +
                "left join wk_checkintime ct on ct.id=dtr.time_id\n" +
                "where failure_time is null  and effective_time is not null\n" +
                "and  ugr.user_id=" + userId + " and g.company_id=" + companyId + "\n" +
                "and FIND_IN_SET(WEEKDAY(SYSDATE()),workdays)>0 " +
                ")\n" +
                "ORDER BY user_id,need_checkin_time\n" +
                ")x  where not exists(select 1 from wk_day_statistics statistics where statistics.user_id=x.user_id and statistics.group_id=x.group_id and\n" +
                "statistics.need_checkin_date=x.need_checkin_date and statistics.checkin_type =x.checkin_type and statistics.need_checkin_time=x.need_checkin_time ) " +
                "and  not exists(select 1 from wk_white_list white where white.group_id=x.group_id and white.user_id=x.user_id)\n" +
                "and group_id=(select max(g.id) from  wk_group g " +
                "left join wk_user_group_rlat ugr on ugr.group_id=g.id " +
                "where ugr.user_id=" + userId + " and g.company_id="+ companyId +"  and  not exists(select 1 from wk_white_list white where white.group_id=g.id and white.user_id=ugr.user_id)) ";
        logger.info("用户{}的规则sql:\n{}", userId, coloum + groupSql);
        List<Map<String, Object>> list = findList(coloum, groupSql);
        //插入未生成的规则统计
        if (!list.isEmpty()) {
            StringBuffer prefixSql = new StringBuffer("insert into " + TableList.WK_DAY_STATISTICS + "(user_id,group_id,need_checkin_date,need_checkin_time,checkin_type) values");
            StringBuffer suffixSql = new StringBuffer();
            for (Map<String, Object> map : list) {
                suffixSql.append("('" + map.get("user_id") + "','" + map.get("group_id") + "','" + map.get("need_checkin_date") + "','" + map.get("need_checkin_time") + "','" + map.get("checkin_type") + "'),");
            }
            baseDao.batchUpdate(prefixSql + suffixSql.substring(0, suffixSql.length() - 1));
        }
        coloum="select DISTINCT  ds.id  statisticsId,ugr.user_id userId,ugr.group_id groupId,need_checkin_date needCheckinDate,need_checkin_time needCheckinTime,checkin_type checkinType,if(effictive_time is null,'',effictive_time) effictiveTime";
        //查询规则
        groupSql=" from "+TableList.WK_DAY_STATISTICS+" ds left join "+TableList.WK_GROUP+" g on g.id=ds.group_id left join " +
                TableList.WK_USER_GROUP_RLAT+" ugr on ugr.group_id=g.id " +
                "where ugr.user_id= "+userId+"  and g.company_id=" + companyId+" and need_checkin_date='"+DateUtil.getCurDate()+"'";
        List<Map<String, Object>> groupList = findList(coloum,groupSql);
        logger.info("用户{}的规则sql:\n{}", userId, coloum+groupSql);

        if (groupList.isEmpty()) {
            //判断今日是否需要打卡

            return Result.unDataResult("fail", "未找到用户规则");
        } else {
            String sql="select * from wk_group g  \n" +
                    "left join wk_checkindate cd on g.id=cd.group_id\n" +
                    "left join wk_user_group_rlat ugr on ugr.group_id=g.id\n" +
                    "where FIND_IN_SET(WEEKDAY(SYSDATE()),workdays)>0 and ugr.user_id="+userId+" and " +
                    "company_id="+companyId ;
            Map<String, Object> firstBySql = findFirstBySql(sql);
            if (firstBySql==null||firstBySql.isEmpty()){
                return Result.unDataResult("fail", "今日无需打卡");
            }
            //获取白名单,如果用户是白名单内，不需要打卡，返回
            Object groupId = groupList.get(0).get("groupId");
            Map<String, Object> white = findFirstBySql("select * from " + TableList.WK_WHITE_LIST + " where user_id=" + userId + " and group_id=" + groupId);
            if (white != null) {
                //返回信息 code=201=白名单
                return Result.ResultCode("success", "用户在白名单内", "201");
            }
            //获取特殊日期
//            String  coloumSql=" select id,spe_date,type,notes ";
//            String  fromSql=" from "+TableList.WK_SPE_DAYS+" where group_id="+groupId;
//            List<Map<String, Object>> speDaysList =findList(coloumSql,fromSql);
//            Map<String, Object> checkDateMap;
////            Map<String, Object> checkTimeMap;
//            Object checdDateId = null;
//            List<Map<String, Object>> dateList;
            //时间转unix
//            long unixTimestamp = DateUtil.StrToUnix(date);
//            long nextUnixTimestamp = DateUtil.NextStrToUnix(date);
//            System.out.println(unixTimestamp);
//            System.out.println(nextUnixTimestamp);

            //获取地址信息
            String coloumSql = "select lat,lng,loc_title,loc_detail,distance ";
            String fromSql = " from " + TableList.WK_LOC_INFOS + " where group_id=" + groupId;
            List<Map<String, Object>> locInfosList = findList(coloumSql, fromSql);
            Map<String, Object> resultMap =new HashMap<>();
            resultMap.put("group",groupList);
            resultMap.put("locInf",locInfosList);
            resultMap.put("groupId",groupId);
            return ResultData.dataResult("success", "成功",resultMap );
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
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Result saveWork(Map<String, Object> paramMap) {
        Map<String, Object> saveMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();

        Object userId = paramMap.get("userId");
        Object companyId = paramMap.get("companyId");
        Object checkinDate = paramMap.get("checkinDate");
        Object checkinTime = paramMap.get("checkinTime");
//        Integer checkinType = BaseUtil.objToInteger(paramMap.get("checkinType"),null);
        if (userId==null||checkinDate==null||checkinTime==null||companyId==null) {
        return Result.unDataResult("fail","缺少参数");
        }
        saveMap.put("user_id", userId);
        saveMap.put("company_id", companyId);
//        saveMap.put("checkin_type", checkinType);
//        saveMap.put("exception_type",paramMap.get("exceptionType"));
        saveMap.put("checkin_date", checkinDate);
        saveMap.put("checkin_time", checkinTime);
        saveMap.put("location_title",paramMap.get("locationTitle"));
        saveMap.put("location_detail",paramMap.get("locationDetail"));
        saveMap.put("wifi_name",paramMap.get("wifiName"));
        saveMap.put("checkin_divice",paramMap.get("checkinDivice"));
        saveMap.put("notes",paramMap.get("notes"));
        saveMap.put("mediaids",paramMap.get("mediaids"));
        saveMap.put("lng",paramMap.get("lng"));
        saveMap.put("lat",paramMap.get("lat"));
        saveMap.put("exp1",paramMap.get("exp1"));
        saveMap.put("exp2",paramMap.get("exp2"));
        saveMap.put("exp3",paramMap.get("exp3"));
        saveMap.entrySet().removeIf(m ->
                m.getValue() == null || "".equals(m.getValue()) &&
                        !"user_id".equals(m.getKey()) &&
                        !"company_id".equals(m.getKey()) &&
                        !"checkin_type".equals(m.getKey()) &&
                        !"exception_type".equals(m.getKey()) &&
                        !"checkin_date".equals(m.getKey()) &&
                        !"checkin_time".equals(m.getKey()) &&
                        !"location_title".equals(m.getKey()) &&
                        !"location_detail".equals(m.getKey()) &&
                        !"wifi_name".equals(m.getKey()) &&
                        !"wifi_mac".equals(m.getKey()) &&
                        !"checkin_divice".equals(m.getKey()) &&
                        !"notes".equals(m.getKey()) &&
                        !"mediaids".equals(m.getKey()) &&
                        !"lng".equals(m.getKey()) &&
                        !"exp1".equals(m.getKey()) &&
                        !"exp2".equals(m.getKey()) &&
                        !"exp3".equals(m.getKey())
        );
        int save = save(TableList.WK_RECORD, saveMap);
        //更新打卡
        if (save > 0) {
            return Result.unDataResult("success", "打卡成功");
        }
        return Result.unDataResult("fail", "打卡失败");
    }

    /**
     * 外出打卡
     * @param paramMap
     * @return
     */
    @Override
    public Result outWork(Map<String, Object> paramMap){
        Map<String, Object> saveMap = new HashMap<>();
        saveMap.put("location_title",paramMap.get("locationTitle"));
        saveMap.put("location_detail",paramMap.get("locationDetail"));
        saveMap.put("lng",paramMap.get("lng"));
        saveMap.put("lat",paramMap.get("lat"));
        try {

            save(TableList.WK_OUTWORK,saveMap);
        }catch (Exception e){
            logger.error(e.getMessage());
            return Result.unDataResult("fail","缺少参数");
        }
        return Result.success();
    }
    //月统计
    @Override
    public Result gainMonthStatistics(Map<String, Object> paramMap) throws ParseException {
        String date = BaseUtil.objToStr(paramMap.get("date"), "");
        Object userId = paramMap.get("userId");
        if ("".equals(date)){
            return  Result.unDataResult("fail", "缺少参数");
        }
        String monthFirstDay = DateUtil.getMonthFirstDay(date);//月初
        String monthLastDay = DateUtil.getMonthLastDay(date);//月末
        String sql="select count(late>0 or NULL) late ,count(early>0 or NULL) early,count(absent>0 or NULL) absent,count(location>0 or NULL) location,count(equipment>0 or NULL) equipment from (\n" +
                "select user_id,sum(late) late,sum(early) early,sum(absent) absent,sum(location) location,sum(equipment) equipment from wk_day_statistics \n" +
                "where user_id="+userId+" and need_checkin_date BETWEEN "+monthFirstDay+" and "+monthLastDay+" " +
                "group by user_id)x";
        Map<String, Object> firstBySql = findFirstBySql(sql);

        if (firstBySql!=null) {
            return ResultData.dataResult("success", "成功", firstBySql);
        }else{
            return Result.unDataResult("success", "暂无数据");
        }
    }
    //获取公司用户
    @Override
    public Result companyUser(Map<String, Object> paramMap){
        Object companyId = paramMap.get("companyId");
        if (companyId==null){
            return Result.unDataResult("fail","缺少参数");
        }
        List list = findList("select u.id userId, u.realName,u.phone,u.idHandleImgUrl,u.headImgUrl", " from tbl_company_user cu left join" +
                " tbl_user u on cu.userId=u.id where currentStatus='normal' and cu.companyId=" + companyId+" and u.id is not null");
        if (list.isEmpty()){
            return Result.unDataResult("success","暂无数据");
        }
         return ResultData.dataResult("success","获取成功",list);
    };
    /**
     * 日历 个人日打卡记录
     * 统计这个月的每一天是否有异常，前端显示红点--有异常，白点--正常
     *
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
    public Result effective(Map<String, Object> paramMap) throws ParseException {

        StringBuffer dateTimeRlatPrefixSql = new StringBuffer("INSERT INTO `visitor`.`tbl_wk_record`(`user_id`, `group_id`, `group_name`, `checkin_type`, `exception_type`,`checkin_date`, `checkin_time`, `location_title`, `location_detail`, `wifi_name`, `wifi_mac`, `checkin_divice`, `notes`, `mediaids`, `lat`, `lng` ) values");
        StringBuffer dateTimeRlatsuffixSql = new StringBuffer();
        String nextMinu = DateUtil.NextMinu("2019-12-02 00:00:00");

        for (int i = 1; i < 24 * 60; i++) {
            nextMinu = DateUtil.NextMinu(nextMinu);


            dateTimeRlatsuffixSql.append("(10, 24, '日常考勤', '上班打卡', '地点异常','" + nextMinu.substring(0, 10) + "', '" + nextMinu.substring(11) + "', '依澜府', '四川省成都市武侯区益州大道中段784号附近', '办公一区', '3c:46:d8:0c:7a:70', '小米note3', '路上堵车，迟到了5分钟', 'WWCISP_G8PYgRaOVHjXWUWFqchpBqqqUpGj0OyR9z6WTwhnMZGCPHxyviVstiv_2fTG8YOJq8L8zJT2T2OvTebANV-2MQ', 30547645, 104063236),");
        }

        int[] locs = baseDao.batchUpdate(dateTimeRlatPrefixSql + dateTimeRlatsuffixSql.substring(0, dateTimeRlatsuffixSql.length() - 1));
        return Result.unDataResult("test", "test");
    }

    /**
     * 管理员 查看的月统计，月报
     * 日统计，一、上下班统计：1、迟到分钟数 2、早退分钟数 3、旷工分钟数 4、缺卡分钟数 5、地点异常数 6设备异常数
     * 二、假勤统计：1、打卡补卡 2、外勤 3、外出 4、出差 、5、年假 6、事假 7、病假 8、调休假 9、婚假 10、产假 11、陪产假 12、其他
     * 备注：当天没有打卡记录为旷工，当天有部分打卡为缺卡。
     *
     * @param date
     * @return com.goldccm.model.compose.Result
     * @throws Exception
     * @author cwf
     * @date 2019/11/19 10:04
     */
    public Result gainMonthStatistics(String date) {

        return null;
    }
    @Override
    public Result gainDay(Map<String, Object> paramMap) {
        Object userId = paramMap.get("userId");
        Object date = paramMap.get("date");
        Object companyId = paramMap.get("companyId");
        String fromsql= "from "+TableList.WK_RECORD+" where user_id="+userId+" and checkin_date='"+date+"'"+
                " and company_id="+companyId;
        List list = findList("select *", fromsql);
        logger.info("select *"+fromsql);
        return ResultData.dataResult("success","成功",list);
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

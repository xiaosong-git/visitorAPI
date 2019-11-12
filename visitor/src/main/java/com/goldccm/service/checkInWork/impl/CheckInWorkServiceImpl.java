package com.goldccm.service.checkInWork.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.persist.base.impl.BaseDaoImpl;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.checkInWork.CheckInWorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
     * 3、
     * @param request
     * @return
     */
    @Transactional(rollbackFor=RuntimeException.class)
    @Override
    public Result saveGroup(HttpServletRequest request) {
        Map<String,Object> saveMap=new HashMap<>();
        saveMap.put("company_id",request.getParameter("company_id"));
        saveMap.put("group_type",request.getParameter("group_type"));
        saveMap.put("group_name",request.getParameter("group_name"));
        saveMap.put("sync_holidays",request.getParameter("sync_holidays"));
        saveMap.put("need_photo",request.getParameter("need_photo"));
        saveMap.put("note_can_use_local_pic",request.getParameter("note_can_use_local_pic"));
        saveMap.put("allow_checkin_offworkday",request.getParameter("allow_checkin_offworkday"));
        saveMap.put("allow_apply_offworkday",request.getParameter("allow_apply_offworkday"));
        try {
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
                   speInDateMap.put("timestamp", speDateObject.getString("timestamp"));
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
            logger.info("批量插入特殊日期关系表语句：\n{}",userPrefixSql+userSuffixSql.substring(0,userSuffixSql.length()-1));
            baseDao.batchUpdate(userPrefixSql+userSuffixSql.substring(0,userSuffixSql.length()-1));
            //------------------下发白名单----------------------------
            JSONArray whiteList = JSONArray.parseArray(request.getParameter("whiteList"));
            StringBuffer whitePrefixSql = new StringBuffer("insert into " + TableList.WK_WHITE_LIST+ "(group_id,user_id) values");
            StringBuffer whiteSuffixSql = new StringBuffer();
            for (Object whiteId:whiteList){
                whiteSuffixSql.append("("+id+","+whiteId+"),");
            }
            logger.info("批量插入特殊日期关系表语句：\n{}",whitePrefixSql+whiteSuffixSql.substring(0,whiteSuffixSql.length()-1));
            baseDao.batchUpdate(whitePrefixSql+whiteSuffixSql.substring(0,whiteSuffixSql.length()-1));
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//回滚
            return Result.unDataResult("fail","插入数据错误");
        }
        return Result.unDataResult("test","success");
    }

}

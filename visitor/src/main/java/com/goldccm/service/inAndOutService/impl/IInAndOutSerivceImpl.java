package com.goldccm.service.inAndOutService.impl;

import com.goldccm.model.compose.Result;
import com.goldccm.model.compose.TableList;
import com.goldccm.service.base.impl.BaseServiceImpl;
import com.goldccm.service.errorLog.impl.IErrorLogServiceImpl;
import com.goldccm.service.inAndOutService.IInAndOutService;
import com.goldccm.util.BaseUtil;
import com.goldccm.util.FilesUtils;
import com.goldccm.util.MD5Util;
import com.goldccm.util.ParamDef;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Service("inAndOutService")
public class IInAndOutSerivceImpl extends BaseServiceImpl implements IInAndOutService {
    Logger log = LoggerFactory.getLogger(IErrorLogServiceImpl.class);
//    @Autowired
//    private IFileService iFileService;
//
//    @Autowired
//    private TaskExecutor taskExecutor;

    Logger logger = LoggerFactory.getLogger(IErrorLogServiceImpl.class);



    /**
     * 下发进出日志
     * update by cwf  2019/8/26 16:42
     */
    @Override
    public Result getInOutTxt(Map<String, Object> paramMap, HttpServletResponse response) throws Exception {

        String merchantNum = BaseUtil.objToStr(paramMap.get("merchantNum"), null);
        String orgCode = BaseUtil.objToStr(paramMap.get("orgCode"), null);
        String companyCode = BaseUtil.objToStr(paramMap.get("companyCode"), "");
        String timestamp = BaseUtil.objToStr(paramMap.get("timestamp"), null);
        String startDate = BaseUtil.objToStr(paramMap.get("startDate"), null);
        String endDate = BaseUtil.objToStr(paramMap.get("endDate"), null);
        String sign = BaseUtil.objToStr(paramMap.get("sign"), null);
        //获取私钥
        Map<String, Object> merchant = findFirstBySql("select private_key from " + TableList.MERCHANT + " where merchant_num=" + merchantNum);
        String privateKey = BaseUtil.objToStr(merchant.get("privateKey"),null) ;
        String verify = merchantNum + orgCode + companyCode + timestamp + privateKey;
        System.out.println(MD5Util.MD5Encode(verify));
        //验证签名是否正确
        if (!MD5Util.MD5Encode(verify).equals(sign)) {
            return Result.ResultCode("fail", "签名验证失败","-2");
        }
        String sql = "";
        String leftSql = "";
        //根据是否有公司code来拼接sql
        if (StringUtils.isNotBlank(companyCode)) {
            sql = "select org_name,companyName from  " + TableList.ORG + " o " +
                    "left join " + TableList.COMPANY + " c on c.orgId=o.id where org_code ='" + orgCode + "' and " +
                    "companyCode='" + companyCode + "'";
            leftSql = "left join " + TableList.ORG + " o on o.org_code=io.orgCode\n" +
                    "left join " + TableList.COMPANY + " c on c.orgId=o.id";
        } else {
            sql = "select org_name from  " + TableList.ORG + " where org_code ='" + orgCode + "'";
        }
        Map<String, Object> org = findFirstBySql(sql);
        String orgName = (String) org.get("org_name");
        String companyName = BaseUtil.objToStr(org.get("companyName"), "");

        StringBuffer txtStr = new StringBuffer("");
        String path = ParamDef.findDirByName("inOutDir") + orgName + companyName + "进出日志" + startDate + "_" + endDate + ".txt";
        String filename = orgName + companyName + "进出日志" + startDate + "_" + endDate + ".txt";

        int page = 0;
        int size = 10000;
        int totle = size;
        String comlumSql = "select userName,pospCode,scanDate,scanTime,inOrOut ";
        String fromSql = " from " + TableList.IN_OUT + " io " + leftSql + " where orgCode='" + orgCode + "' and scanDate between '" + startDate + "' and '" + endDate + "'";
        //分页获取数据库信息
        String limit = " limit " + page + "," + size;
        Map<String, Object> firstMap = findFirstBySql(comlumSql + fromSql + limit);
        if (firstMap==null||firstMap.isEmpty()){
                return Result.ResultCode("success","暂无数据","0");
            }
        File f = new File(path);
        //如果文件存在，则追加内容；如果文件不存在，则创建文件
        FileWriter fw = new FileWriter(f);
        PrintWriter pw = new PrintWriter(fw);
        try {
        while (true) {
            List<Map<String, Object>> list = findList(comlumSql, fromSql + limit);
            if (list==null||list.size()==0||list.isEmpty()){
                return Result.ResultCode("success","暂无数据","0");
            }
            page = totle;
            totle = (totle / size + 1) * size;
            limit = " limit " + page + "," + size;
            if (list != null && !list.isEmpty()) {
                for (Map<String, Object> map : list) {
                    txtStr.append(map.get("userName")).append("|")
                            .append(map.get("pospCode")).append("|")
                            .append(map.get("scanDate")).append("|")
                            .append(map.get("scanTime")).append("|")
                            .append(map.get("inOrOut"));
                    pw.println(txtStr);
                    txtStr = new StringBuffer("");
                }
            } else {
                break;
            }
        }
        }catch (Exception e){
            e.printStackTrace();
            return Result.ResultCode("fail","系统异常","-1");
        }finally {
            pw.flush();
            fw.flush();
            if (pw!=null){
                pw.close();
            }
            if (fw!=null){
                fw.close();
            }
        }

        FilesUtils.sendFile(path, filename, response);
        f.delete();
        return Result.ResultCode("success","获取数据成功","100");
    }


}

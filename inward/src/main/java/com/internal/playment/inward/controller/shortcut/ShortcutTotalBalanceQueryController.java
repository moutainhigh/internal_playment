package com.internal.playment.inward.controller.shortcut;

import com.alibaba.dubbo.common.json.JSON;
import com.internal.playment.common.dto.BusinessTotalBalanceQueryDTO;
import com.internal.playment.common.enums.ResponseCodeEnum;
import com.internal.playment.common.inner.InnerPrintLogObject;
import com.internal.playment.common.inner.NewPayException;
import com.internal.playment.common.inner.ParamRule;
import com.internal.playment.common.table.merchant.MerchantInfoTable;
import com.internal.playment.common.table.system.SystemOrderTrackTable;
import com.internal.playment.inward.component.Md5Component;
import com.internal.playment.inward.controller.NewAbstractCommonController;
import com.internal.playment.inward.service.shurtcut.ShortcutTotalBalanceQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/shortcut")
public class ShortcutTotalBalanceQueryController  extends NewAbstractCommonController {

    private final ShortcutTotalBalanceQueryService ShortcutTotalBalanceQueryService;
    private final Md5Component md5Component;

    /**
     *
     * @param request
     * @param param
     * @return
     */
    @PostMapping(value = "/queryBalance", produces = "text/html;charset=UTF-8")
    public String totalBalanceQuery(HttpServletRequest request, @RequestBody(required = false) String param){
        final String bussType = "【余额查询】";
        BusinessTotalBalanceQueryDTO businessTotalBalanceQueryDTO = null;
        String errorMsg,errorCode,printErrorMsg,respResult=null;
        SystemOrderTrackTable sotTable = null;
        InnerPrintLogObject ipo = null;
        MerchantInfoTable merInfoTable = null;
        try{
            //解析 以及 获取SystemOrderTrackTable对象
            sotTable = this.getSystemOrderTrackTable(request,param,bussType);
            //类型转换
            businessTotalBalanceQueryDTO = JSON.parse(sotTable.getRequestMsg(), BusinessTotalBalanceQueryDTO.class);
            sotTable.setMerId(businessTotalBalanceQueryDTO.getMerId());
            //创建日志打印对象
            ipo = new InnerPrintLogObject(businessTotalBalanceQueryDTO.getMerId(),businessTotalBalanceQueryDTO.getTerMerId(),bussType);
            //获取商户信息
            merInfoTable = ShortcutTotalBalanceQueryService.getOneMerInfo(ipo);
            //获取必要参数
            Map<String, ParamRule> paramRuleMap = ShortcutTotalBalanceQueryService.getParamMapByBusinessTotalBalanceQuery();
            //参数校验
            this.verify(paramRuleMap,ShortcutTotalBalanceQueryService,ipo);
            //验证签名
            md5Component.checkMd5(sotTable.getRequestMsg(),merInfoTable.getSecretKey(),ipo);
            //查询
            Map<String,Object> map = ShortcutTotalBalanceQueryService.query(businessTotalBalanceQueryDTO,ipo);
            //生成返回结果
            respResult = ShortcutTotalBalanceQueryService.responseMsg(map,merInfoTable);
        }catch (Exception e){
            if(e instanceof NewPayException){
                NewPayException npe = (NewPayException) e;
                errorMsg = npe.getResponseMsg();
                printErrorMsg = npe.getInnerPrintMsg();
                errorCode = npe.getCode();
                log.info(printErrorMsg);
            }else{
                e.printStackTrace();
                errorMsg = ResponseCodeEnum.RXH99999.getMsg();
                printErrorMsg = isBlank(e.getMessage()) ? "" : (e.getMessage().length()>=512 ? e.getMessage().substring(0,526) : e.getMessage());
                errorCode = ResponseCodeEnum.RXH99999.getCode();
            }
            sotTable.setPlatformPrintLog(printErrorMsg);
            respResult = ShortcutTotalBalanceQueryService.responseMsg(businessTotalBalanceQueryDTO,merInfoTable,errorCode,errorMsg);
        }finally {
            sotTable.setResponseResult(respResult).setCreateTime(new Date());
            ShortcutTotalBalanceQueryService.saveSysLog(sotTable);
            return  null == respResult ? "系统内部错误！" : respResult;
        }
    }

}

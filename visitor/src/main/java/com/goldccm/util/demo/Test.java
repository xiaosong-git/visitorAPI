package com.goldccm.util.demo;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/6/29.
 */
public class Test {
    public static void main(String[] args) {
        BigDecimal leftCharge = new BigDecimal(109);
        BigDecimal userLeftAmount = new BigDecimal(1467);
        BigDecimal cardLeftAmount = BigDecimal.ZERO;
        BigDecimal leftRepayAmount = new BigDecimal(13576);

        String[] plan = {"1358","p955",
                "754","p420","p300",
                "727","p489","p613",
                "1006","p831",
                "694","p280","p399",
                "963","p713",
                "729","p1132",
                "807","p296","p492",
                "719","p752",
                "775","p891",
                "1006","p769",
                "730","p683",
                "741","p818",
                "840","p851",
                "628","p945"



        };
        String s = null;
        BigDecimal amount = null;
        BigDecimal charge = null;
        for(int i=0; i<plan.length; i++){
            s = plan[i];
            if(s.contains("p")){
                s = s.replace("p","");
                amount = new BigDecimal(s);
                //卡上余额-
               /* if(cardLeftAmount.compareTo(amount) < 0){
                    System.out.println("消费失败，卡上余额："+cardLeftAmount+"金额："+amount);
                    return;
                }*/
                cardLeftAmount = cardLeftAmount.subtract(amount);

                //账户余额+
                charge = amount.multiply(new BigDecimal(0.008)).setScale(2,BigDecimal.ROUND_HALF_UP);
                userLeftAmount = userLeftAmount.add(amount).subtract(charge);
                //手续费-
                leftCharge = leftCharge.subtract(charge);
            }else{
                amount = new BigDecimal(s);
                //剩余还款
                leftRepayAmount = leftRepayAmount.subtract(amount);
                //卡上余额+
                cardLeftAmount = cardLeftAmount.add(amount);
                /*if(userLeftAmount.compareTo(amount) < 0){
                    System.out.println("还款失败，金额："+amount);
                    return;
                }*/
                //账户余额-
                userLeftAmount = userLeftAmount.subtract(amount);

            }
        }
        System.out.println("剩余还款："+leftRepayAmount+" 账户余额："+userLeftAmount+" 剩余手续费："+leftCharge+" 卡上余额:"+cardLeftAmount);
    }
}

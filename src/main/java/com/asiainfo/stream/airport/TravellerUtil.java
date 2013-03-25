package com.asiainfo.stream.airport;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-3-7
 * Time: 上午9:47
 */
public class TravellerUtil {
    private Random random = new Random();

    /**
     * 生成旅客信令数据
     * @param imsi
     * 旅客用户Imsi
     * @param startDate
     * 开始时间，正点对应毫秒数
     * @param endDate
     * 结束时间，毫秒数
     * @param generateRate
     * 生成信令的速率，条/秒
     *
     * 数据格式：“imsi,eventType,time,loc,cell”
     * cell字段为"airport"时客户在机场
     */
    void generateTravellerData(String imsi, long startDate, long endDate, long generateRate){

    }

    int getNotZeroRandomInt (int ceiling){
        int i = random.nextInt(ceiling);
        while (i == 0){
            i = random.nextInt(ceiling);
        }
        return i;
    }
}

package com.asiainfo.stream.wlan;

import com.asiainfo.stream.util.TimeUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-3-7
 * Time: 上午9:46
 */
public class WlanUserUtil {
    private Random random = new Random();

    /**
     * 生成wlan用户信令数据
     * @param imsi
     * wlan用户Imsi
     * @param startDate
     * 开始时间，正点对应毫秒数
     * @param endDate
     * 结束时间，毫秒数
     * @param generateRate
     * 生成信令的速率，条/秒
     *
     * 数据格式：“imsi,eventType,time,cause,lac,cell”
     *
     */
    void generateTouristData(String imsi, long startDate, long endDate, long generateRate){
        String filePath = GenWlanApp.fileDir + File.separator +"tmp" + File.separator;
        String fileName = imsi + ".csv";

        File file = createFile(filePath, fileName);
        if(file != null && file.canWrite()){
            try{
                BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(file));
                String content = genSigContent(imsi, startDate, endDate, generateRate);
                buff.write(content.getBytes());
                buff.flush();
                buff.close();
            } catch (FileNotFoundException e1){
                e1.printStackTrace();
            } catch (IOException e2){
                e2.printStackTrace();
            }
        }
    }

    String genSigContent(String imsi, long startDate, long endDate, long generateRate){
        StringBuilder content = new StringBuilder();

        long[] times = null;

        long minutes = endDate / (60 * 1000) - startDate / (60 * 1000) + 1;
        int connMin = random.nextInt((int)minutes - 16);
        int endMin = (int)minutes - random.nextInt((int)minutes - connMin - 16) - 1;

        long connTime = startDate + connMin * 60 * 1000;
        long endTime = startDate + endMin * 60 * 1000;

//            String info = String.format("统计生成%d分钟数据；连接WAP时长%d分钟，为第%d至%d分钟即：%s~%s",
//                    minutes,
//                    (endMin - connMin),
//                    connMin,
//                    endMin,
//                    GenWlanApp.getTime(connTime),
//                    GenWlanApp.getTime(endTime));
//            System.out.print(info);
            String info = null;
            info = String.format("%s: wlan\t%d min: %s~%s\r\n", imsi, (endMin - connMin), TimeUtil.getTime(connTime), TimeUtil.getTime(endTime));
            GenWlanApp.summaryInfo.append(info);
            System.out.println(info);

        System.out.println();

        for(int i = 0; i < minutes; i++){
            times = genRandomTime(generateRate);
            for (int j = 0; j < generateRate && startDate + times[j] <= endDate; j++){
                long signalTime = startDate + times[j];
                String timeCheck = new String();

                    timeCheck = TimeUtil.getTime(signalTime);

                if (i == connMin && j == 0) {
                    content.append(imsi + ",02," + signalTime + ",cause,ft,home,calling,called,apn,sgsnIp,res2," + timeCheck + "\r\n");
                } else if (i == endMin && j == generateRate - 1) {
                    content.append(imsi + ",04," + signalTime + ",cause,ft,home,calling,called,apn,sgsnIp,res2," + timeCheck + "\r\n");
                } else if ((i >= connMin) && (i <= endMin)) {
                    content.append(imsi + ",03," + signalTime + ",cause,ft,home,calling,called,apn,sgsnIp,res2," + timeCheck + "\r\n");
                } else {
                    content.append(imsi + ",99," + signalTime + ",cause,ft,home,calling,called,apn,sgsnIp,res2," + timeCheck + "\r\n");
                }
                if (j == generateRate - 1){
                    startDate += 60 * 1000;
                }
            }
        }
        return content.toString();
    }

    int getNotZeroRandomInt (int ceiling){
        int i = random.nextInt(ceiling);
        while (i == 0){
            i = random.nextInt(ceiling);
        }
        return i;
    }

    long[] genRandomTime(long genRate){ // 随机生成1分钟内的genRate个时间点
        long[] times = null;
        if(genRate > 0){
            times = new long[(int)genRate];
            for (int i = 0; i < genRate; i++){
                times[i] = (long)random.nextInt(60 * 1000);
            }
            Arrays.sort(times);
        }
        return times;
    }

    /**
     * create dir and file
     * @param filePath
     * 路径
     * @param fileName
     * 文件名
     * @return
     * 是否成功
     */
    File createFile(String filePath, String fileName){
        File userFilePath = new File(filePath);
        if (!userFilePath.exists()){
            boolean success = userFilePath.mkdirs();
            System.out.println("create dirs: " + userFilePath.getAbsolutePath());
        }
        File userFile = new File(filePath + fileName);
        if(!userFile.exists()){
            try{
                boolean created = userFile.createNewFile();
                System.out.println("new wlan file: " + userFile.getAbsolutePath() + ": " + created);
            } catch (IOException e){
                e.printStackTrace();
                userFile = null;
            }
        } else {
            System.out.println("File already exists: " + userFile.getAbsolutePath() + ": false");
            userFile = null;
        }
        return userFile;
    }

    public static void main(String[] args){
//        new TouristUtil().generateTouristData("100001000002829", (System.currentTimeMillis() - 3600 * 1000), System.currentTimeMillis(), 6L);
//        long[] test = new TouristUtil().genRandomTime(System.currentTimeMillis(), 10L);
//        for(long l: test){
//            System.out.println(l);
//        }
//        String[][] testLocations = new TouristUtil().genLocation(10L);
//        for(String[] str: testLocations){
//            System.out.println(str[0] + "\t" + str[1]);
//        }
//        long hours = System.currentTimeMillis() / (60 * 60 * 1000) - (System.currentTimeMillis() - 3600 * 1000) / (60 * 60 * 1000);
//        System.out.println(hours);


        String startDateStr = "2013-01-11 08:00:00.000", endDateStr = "2013-01-11 08:30:59.999";
        long startDate = 0L, endDate = 0L;
        try {
            startDate = TimeUtil.getTime(startDateStr);
            endDate = TimeUtil.getTime(endDateStr);
            System.out.println(startDateStr + "\t"+startDate+"\t" + TimeUtil.getTime(startDate));
            System.out.println(endDateStr + "\t"+endDate+"\t" + TimeUtil.getTime(endDate));
        } catch (ParseException e){
            e.printStackTrace();
        }

//        System.out.println(new TouristUtil().genSigContent("100001000002829", 1357833599999L, 1359647999999L, 2L));

        new WlanUserUtil().generateTouristData("100001000002829", 1357891200000L, 1357893059999L, 2L);

//        System.out.println((long)30 * 24 * 60 * 60 * 1000);
    }
}

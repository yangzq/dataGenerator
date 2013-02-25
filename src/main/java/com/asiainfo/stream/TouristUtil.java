package com.asiainfo.stream;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-2-18
 * Time: 下午3:36
 */
public class TouristUtil {
    private Random random = new Random();

    /**
     * 生成游客信令数据
     * @param imsi
     * 游客用户Imsi
     * @param startDate
     * 开始时间，正点对应毫秒数
     * @param endDate
     * 结束时间，毫秒数
     * @param generateRate
     * 生成信令的速率，条/秒
     *
     * 数据格式：“imsi,time,loc,cell”
     * cell字段为"tourist"时客户在景区
     */
     void generateTouristData(String imsi, long startDate, long endDate, long generateRate){
        String filePath = "files" + File.separator +"tmp" + File.separator;
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
        String[][] locations = null;

        long hours = endDate / (60 * 60 * 1000) - startDate / (60 * 60 * 1000) + 1;
        long days = hours / 24;
        int tourDays = getNotZeroRandomInt(5); // 在景区天数，限制少于5天
        int[] tourDaysArr = getRandomIntArr(tourDays, 10); // 最后十天内哪几天在景区
        for (int i = 0; i < tourDaysArr.length; i++){
            tourDaysArr[i] = (int)days - tourDaysArr[i];
        }
        Arrays.sort(tourDaysArr);

        System.out.print("统计生成" + hours + "小时；" + days + "天；在景区" + tourDays + "天，为");
        for(int i : tourDaysArr){
            System.out.print("第" + i +"天，");
        }
        System.out.println();

        long[] tourMillisArr = new long[tourDays];
        for(int i = 0; i < tourDays; i++){
            tourMillisArr[i] = startDate + (long)(tourDaysArr[i] - 1) * 24 * 60 * 60 * 1000;
            System.out.println("时间：" + new Date(tourMillisArr[i]));
        }

//        long threeHours = 3 * 60 * 60 * 1000;
//        long fiveHours = 5 * 60 * 60 * 1000;

        for(int i = 0; i < hours; i++){
            times = genRandomTime(generateRate);
            locations = genLocation(startDate, endDate, generateRate);
            for (int j = 0; j < generateRate && startDate + times[j] <= endDate; j++){
                long signalTine = startDate + times[j];
                if (isInDaytimeInterval(tourMillisArr, signalTine)){
                    content.append(imsi + "," + signalTine + "," + locations[j][0] + "," + "tourist," + new Date(signalTine) + "\r\n");
                } else if (isInNightInterval(tourMillisArr, signalTine)){
                    content.append(imsi + "," + signalTine + "," + locations[j][0] + "," + "tourist," + new Date(signalTine) + "\r\n");
                } else {
                    if (i == hours -1 && j == generateRate - 1){
                        content.append(imsi + "," + signalTine + "," + locations[j][0] + "," + "tourist," + new Date(signalTine) + "\r\n");
                    } else {
                        content.append(imsi + "," + signalTine + "," + locations[j][0] + "," + locations[j][1] + "," + new Date(signalTine) + "\r\n");
                    }
                }
                if (j == generateRate - 1){
                    startDate += 60 * 60 * 1000;
                }
            }
        }
        return content.toString();
    }

    boolean isInDaytimeInterval(long[] timeArr, long time){
        boolean isIn = false;
        for(int i = 0 ; i < timeArr.length; i++){
            if(time >= (timeArr[i] + (long)8 * 60 * 60 * 1000) && time <= (timeArr[i] + (long)18 * 60 * 60 * 1000)){
                isIn = true;
                return isIn;
            }
        }
        return isIn;
    }

    boolean isInNightInterval(long[] timeArr, long time){
        boolean isIn = false;
        for(int i = 0 ; i < timeArr.length; i++){
            if(time >= (timeArr[i] + (long)18 * 60 * 60 * 1000) && time <= (timeArr[i] + (long)(24 + 8) * 60 * 60 * 1000)){
                isIn = true;
                return isIn;
            }
        }
        return isIn;
    }

    int[] getRandomIntArr(int len, int maxValue){
        int[] arr = new int[len];
        for (int i = 0; i < len; i++){
            arr[i] = 0;
        }
        if(maxValue > len){
            for (int i = 0; i < len; i++){
                int tmpInt = random.nextInt(maxValue);
                for(int j = 0; j < len; j++){
                    if(tmpInt == arr[j]){
                        tmpInt = random.nextInt(maxValue);
                        j = -1; // 重新从0开始循环，赋值为-1是由于还要先运行j++
                    }
                }
                arr[i] = tmpInt;
//                System.out.println(tmpInt);
            }
            Arrays.sort(arr);
            return arr;
        } else {
            System.err.println("maxValue <= len, cannot generate random int Array.");
            return null;
        }
    }

    int getNotZeroRandomInt (int ceiling){
        int i = random.nextInt(ceiling);
        while (i == 0){
            i = random.nextInt(ceiling);
        }
        return i;
    }

    String[][] genLocation(long startDate, long endDate, long genRate){
        String[][] locations = null;
        if(genRate > 0){
            locations = new String[(int)genRate][2];
            String[] lac = {"hd", "chy", "chp", "xc", "dc", "shy"};
            String[] cell = {"home", "stadium", "airport", "mall" ,"company"}; // 生成不在景区的信令数据
            int llac = lac.length, lcell = cell.length;
            for(int i = 0; i < genRate; i++){
                locations[i][0] = lac[random.nextInt(llac)];
                locations[i][1] = cell[random.nextInt(lcell)];
            }
        }
        return locations;
    }

    long[] genRandomTime(long genRate){ // 随机生成一小时内的genRate个时间点
        long[] times = null;
        if(genRate > 0){
            times = new long[(int)genRate];
            for (int i = 0; i < genRate; i++){
                times[i] = (long)random.nextInt(60 * 60 * 1000);
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
                System.out.println("new tourist file: " + userFile.getAbsolutePath() + ": " + created);
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


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String startDateStr = "2013-01-01 00:00:00.000", endDateStr = "2013-01-12 23:59:59.999";
        long startDate = 0L, endDate = 0L;
        try {
            startDate = sdf.parse(startDateStr).getTime();
            endDate = sdf.parse(endDateStr).getTime();
            System.out.println(startDateStr+"\t"+sdf.parse(startDateStr)+"\t"+startDate+"\t"+new Date(startDate));
            System.out.println(endDateStr+"\t"+sdf.parse(startDateStr)+"\t"+endDate+"\t"+new Date(endDate));
        } catch (ParseException e){
            e.printStackTrace();
        }

//        System.out.println(new TouristUtil().genSigContent("100001000002829", 1357833599999L, 1359647999999L, 2L));

        new TouristUtil().generateTouristData("100001000002829", 1356969600000L, 1358006399999L, 2L);

//        System.out.println((long)30 * 24 * 60 * 60 * 1000);
    }
}

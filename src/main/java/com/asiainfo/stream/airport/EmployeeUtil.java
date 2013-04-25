package com.asiainfo.stream.airport;

import com.asiainfo.stream.util.TimeUtil;

import java.io.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-3-7
 * Time: 上午9:47
 */
public class EmployeeUtil {
    private Random random = new Random();

    /**
     * 生成机场工作人员信令数据
     *
     * @param imsi         旅客用户Imsi
     * @param startDate    开始时间，正点对应毫秒数
     * @param endDate      结束时间，毫秒数
     * @param generateRate 生成信令的速率，条/秒
     *                     <p/>
     *                     数据格式：“imsi,eventType,time,lac,cell”
     *                     cell字段为"airport"时客户在机场
     */
    void generateEmployeeData(String imsi, long startDate, long endDate, long generateRate) {
        String filePath = GenTravellerApp.fileDir + File.separator + "tmp" + File.separator;
        String fileName = imsi + ".csv";

        File file = createFile(filePath, fileName);
        if (file != null && file.canWrite()) {
            try {
                BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(file));
                String content = genSigContent(imsi, startDate, endDate, generateRate);
                buff.write(content.getBytes());
                buff.flush();
                buff.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    String genSigContent(String imsi, long startDate, long endDate, long generateRate) {
        StringBuilder content = new StringBuilder();

        long[] times = null;
        String[][] locations = null;
        String[] events = null;

        long hours = endDate / (60 * 60 * 1000) - startDate / (60 * 60 * 1000) + 1;
        long days = hours / 24;
        int travelDays = getNotZeroRandomInt(10, 15); // 出现在机场的天数，限制不少于10天，不多于15天
        int[] travelDaysArr = getRandomIntArr(travelDays, 30); // 最后30天内哪几天出现在机场
        for (int i = 0; i < travelDaysArr.length; i++) {
            travelDaysArr[i] = (int) days - travelDaysArr[i];
        }
        Arrays.sort(travelDaysArr);

        System.out.print(String.format("统计生成%d小时，%d天；在机场出现%d天，为：", hours, days, travelDays));

        GenTravellerApp.summaryInfo.append(imsi + ": employee" + "\t");
        long[] tourMillisArr = new long[travelDays];
        for (int i = 0; i < travelDays; i++) { // 打印可视的在机场信令日期
            tourMillisArr[i] = startDate + (long) (travelDaysArr[i] - 1) * 24 * 60 * 60 * 1000;
            System.out.print(TimeUtil.getDate(tourMillisArr[i]) + "\t");
            GenTravellerApp.summaryInfo.append(TimeUtil.getDate(tourMillisArr[i]) + "\t");

        }
        System.out.println();
        GenTravellerApp.summaryInfo.append("\r\n");

        final int stayHoursPerDay = 5;
        for (int d = 0; d < days; d++) { // 按天循环
            int dayth = Arrays.binarySearch(travelDaysArr, d + 1);
            if (!(dayth < 0)) { // 当天出现在机场
                int hourInAirport = random.nextInt(18); // 几时信令在机场区域
                for (int i = 0; i < 24; i++) { // 每天按小时生成信令
                    times = genRandomTime(generateRate);
                    locations = genLocation(generateRate);
                    events = genEvents(generateRate);
                    for (int j = 0; (j < generateRate) && !(startDate + times[j] > endDate); j++) {
                        long signalTime = startDate + times[j];
                        String timeCheck = TimeUtil.getTime(signalTime);

                        if (isInAirportMillis(tourMillisArr, hourInAirport, signalTime, stayHoursPerDay)) { // 在机场
                            if ((dayth == travelDays - 1) && (i == hourInAirport + stayHoursPerDay - 1) && (j == generateRate - 1)) { // 最后一天最后一个在机场的信令
                                content.append(imsi + "," + "05" + "," + signalTime + "," + locations[j][0] + "," + "airport," + timeCheck + "\r\n");
                            } else {
                                content.append(imsi + "," + events[j] + "," + signalTime + "," + locations[j][0] + "," + "airport," + timeCheck + "\r\n");
                            }
                        } else {
                            content.append(imsi + "," + events[j] + "," + signalTime + "," + locations[j][0] + "," + locations[j][1] + "," + timeCheck + "\r\n");
                        }
                        if (j == generateRate - 1) {
                            startDate += 60 * 60 * 1000;
                        }
                    }
                }
            } else { // 当天不出现在机场
                for (int i = 0; i < 24; i++) { // 每天按小时生成信令
                    times = genRandomTime(generateRate);
                    locations = genLocation(generateRate);
                    events = genEvents(generateRate);
                    for (int j = 0; (j < generateRate) && !(startDate + times[j] > endDate); j++) {
                        long signalTime = startDate + times[j];
                        String timeCheck = TimeUtil.getTime(signalTime);

                        content.append(imsi + "," + events[j] + "," + signalTime + "," + locations[j][0] + "," + locations[j][1] + "," + timeCheck + "\r\n");

                        if (j == generateRate - 1) {
                            startDate += 60 * 60 * 1000;
                        }
                    }
                }
            }


        }

        return content.toString();
    }

    /**
     * genEvents
     *
     * @param generateRate
     * @return String[] 非开关机的其他事件数组
     *         枚举值 "01"; // 语音主叫
     *         "02"; // 语音被叫
     *         "03"; // 短信接收
     *         "04"; // 短信发送
     *         "05"; // 开机
     *         "06"; // 关机
     *         "99"; // 其他事件
     */
    private String[] genEvents(long generateRate) {
        String[] events = null;
        if (generateRate > 0) {
            events = new String[(int) generateRate];
            String[] etypes = {"01", "02", "03", "04", "99"};
            int letypes = etypes.length;
            for (int i = 0; i < generateRate; i++) {
                events[i] = etypes[random.nextInt(letypes)];
            }
        }
        return events;
    }

    boolean isInAirportMillis(long[] timeArr, int hourInAirport, long time, int stayHours) {
        boolean isIn = false;
        for (int i = 0; i < timeArr.length; i++) {
            long startMillisIn = timeArr[i] + hourInAirport * TimeUtil.ONE_HOUR;
            if (!(time < startMillisIn) && time < (startMillisIn + stayHours*TimeUtil.ONE_HOUR)) {
                isIn = true;
                return isIn;
            }
        }
        return isIn;
    }

    int[] getRandomIntArr(int len, int maxValue) {
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = 0;
        }
        if (maxValue > len) {
            for (int i = 0; i < len; i++) {
                int tmpInt = random.nextInt(maxValue);
                for (int j = 0; j < len; j++) {
                    if (tmpInt == arr[j]) {
                        tmpInt = random.nextInt(maxValue);
                        j = -1; // 重新从0开始循环，赋值为-1是由于还要先运行j++
                    }
                }
                arr[i] = tmpInt;
            }
            Arrays.sort(arr);
            return arr;
        } else {
            System.err.println("maxValue <= len, cannot generate random int Array.");
            return null;
        }
    }

    /**
     * @param floor
     * @param ceiling
     * @return [floor, ceiling]区间内的随机整数
     */
    int getNotZeroRandomInt(int floor, int ceiling) {
        int i = random.nextInt(ceiling + 1);
        while (i == 0 || i < floor) {
            i = random.nextInt(ceiling + 1);
        }
        return i;
    }

    int getNotZeroRandomInt(int ceiling) {
        int i = random.nextInt(ceiling);
        while (i == 0) {
            i = random.nextInt(ceiling);
        }
        return i;
    }

    String[][] genLocation(long genRate) {
        String[][] locations = null;
        if (genRate > 0) {
            locations = new String[(int) genRate][2];
            String[] lac = {"ft", "hd", "chy", "chp", "xc", "dc", "shy"};
            String[] cell = {"home", "stadium", "tourist", "mall", "company"}; // 生成不在机场的信令数据
            int llac = lac.length, lcell = cell.length;
            for (int i = 0; i < genRate; i++) {
                locations[i][0] = lac[random.nextInt(llac)];
                locations[i][1] = cell[random.nextInt(lcell)];
            }
        }
        return locations;
    }

    long[] genRandomTime(long genRate) { // 随机生成一小时内的genRate个时间点
        long[] times = null;
        if (genRate > 0) {
            times = new long[(int) genRate];
            for (int i = 0; i < genRate; i++) {
                times[i] = (long) random.nextInt(60 * 60 * 1000);
            }
            Arrays.sort(times);
        }
        return times;
    }

    /**
     * create dir and file
     *
     * @param filePath 路径
     * @param fileName 文件名
     * @return 是否成功
     */
    File createFile(String filePath, String fileName) {
        File userFilePath = new File(filePath);
        if (!userFilePath.exists()) {
            boolean success = userFilePath.mkdirs();
            System.out.println("create dirs: " + userFilePath.getAbsolutePath());
        }
        File userFile = new File(filePath + fileName);
        if (!userFile.exists()) {
            try {
                boolean created = userFile.createNewFile();
//                System.out.println("new worker file: " + userFile.getAbsolutePath() + ": " + created);
            } catch (IOException e) {
                e.printStackTrace();
                userFile = null;
            }
        } else {
            System.out.println("File already exists: " + userFile.getAbsolutePath() + ": false");
            userFile = null;
        }
        return userFile;
    }

    public static void main(String[] args) {

        String startDateStr = "2013-01-01 00:00:00.000", endDateStr = "2013-01-30 23:59:59.999";

        try {
            new EmployeeUtil().generateEmployeeData("100001000002830", TimeUtil.getTime(startDateStr), TimeUtil.getTime(endDateStr), 2L);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

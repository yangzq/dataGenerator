package com.asiainfo.stream.airport;

import com.asiainfo.stream.util.TimeUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-3-7
 * Time: 上午9:46
 */
public class GenTravellerApp {
    Random random = new Random();
    static StringBuilder summaryInfo = new StringBuilder();
    static final String fileDir = "airportfiles";

    /**
     * 按用户类型生成信令数据
     *
     * @param amount        用户数据规模
     * @param travellerRate 抵港旅客比率
     * @param employeeRate  工作人员比率
     * @param startDate     开始时间，毫秒数
     * @param endDate       结束时间，毫秒数
     * @param genetateRate  信令数据生成速率，条/时
     * @param disorderRate  乱序比率
     */
    void generateData(long amount, double travellerRate, double employeeRate, long startDate, long endDate, long genetateRate, double disorderRate) {
        System.out.println("Generate data, user amount: " + amount +
                ", travellerRate: " + travellerRate + ", employeeRate: " + employeeRate +
                ", startDate: " + TimeUtil.getTime(startDate) + ", endDate: " + TimeUtil.getTime(endDate) +
                ", genetateRate: " + genetateRate + ", disorderRate: " + disorderRate);
        EmployeeUtil employeeUtil = new EmployeeUtil();
        TravellerUtil travellerUtil = new TravellerUtil();
        CommonUserUtil commonUserUtil = new CommonUserUtil();
//        StringBuilder imsiInfo = new StringBuilder();

        long startImsi = 300001000000001L;
        String imsi;

        System.out.println("***************按imsi生成单独的信令数据文件***************");
        for (int i = 0; i < amount; i++) {
            startImsi += (long) (travellerUtil.getNotZeroRandomInt(10000));
            imsi = Long.toString(startImsi);
            double tRate = Math.random();
            if (tRate >= 0 && tRate <= travellerRate) { // 旅客
                System.out.println(imsi + "\t" + "traveller" + "\t");
                travellerUtil.generateTravellerData(imsi, startDate, endDate, genetateRate);
//                imsiInfo.append(imsi + ": traveller" + "\r\n");
            } else if (tRate > travellerRate && tRate <= (employeeRate + travellerRate)) { // 工作人员
                System.out.println(imsi + "\t" + "employee" + "\t");
                employeeUtil.generateEmployeeData(imsi, startDate, endDate, genetateRate);
//                imsiInfo.append(imsi + ": employee" + "\r\n");
            } else { // 普通用户
//                System.out.println(imsi + "\t" + "common user");
                commonUserUtil.generateCommonUserData(imsi, startDate, endDate, genetateRate);
//                imsiInfo.append(imsi + ": commonUser" + "\r\n");
            }
        }
        System.out.println("***************旅客/工作人员信息***************");
        System.out.println(summaryInfo);
        System.out.println("*********************************************");
        String sumFile = fileDir + File.separator + "summary.csv";
        File summaryFile = new File(sumFile);
        BufferedOutputStream buff = null;
        if (!summaryFile.exists()) {
            try {
                boolean created = summaryFile.createNewFile();
                System.out.println("new summary file: " + summaryFile.getAbsolutePath() + ": " + created);
                buff = new BufferedOutputStream(new FileOutputStream(summaryFile));
                buff.write(summaryInfo.toString().getBytes());
                buff.flush();
                buff.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Summary file already exists: " + summaryFile.getAbsolutePath() + ": false");
            summaryFile = null;
        }
        System.out.println("***************mergeFile***************");
        String sourcePath = fileDir + File.separator + "tmp" + File.separator;
        String destFile = fileDir + File.separator + "data.csv";
        mergeFile(sourcePath, destFile);
    }

    File mergeFile(String sourceDir, String destFile) {
        File file = new File(destFile);
        File tmpDir = new File(sourceDir);
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            FilenameFilter selector = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".csv");
                }
            };
            File[] fileArr = tmpDir.listFiles(selector);
//            for(File f: fileArr){
//                System.out.println(f.getAbsolutePath());
//            }
            int amount = fileArr.length;
            System.out.println(String.format("源文件总数：%d", amount));
            int numLimit = 1000; // 多余10000文件时，递归处理
            if (amount > numLimit) {
                System.out.println(String.format("amount > numLimit: %d > %d", amount, numLimit));
                System.out.println("***************文件数目过多，合并生成中间文件***************");
                String intermediateDataPath = fileDir + File.separator + "intermediate" + File.separator;
                File intmDataDir = new File(intermediateDataPath);
                if (!intmDataDir.exists()) {
                    intmDataDir.mkdirs();
                }
                if (intmDataDir.isDirectory()) {
                    File[] iFileArr = new File[numLimit];
                    int nloop = (amount % numLimit == 0) ? (amount / numLimit) : (amount / numLimit + 1);
                    for (int i = 0; i < nloop; i++) {
                        System.out.println("loop num: " + i);
                        for (int j = 0; j < numLimit; j++) {
                            iFileArr[j] = fileArr[i * numLimit + j];
//                            System.out.println("inner loop :" + j + ": " + iFileArr[j].getAbsolutePath());
                        }
                        String destIntmFile = intermediateDataPath + i + ".csv";
                        mergeFile(iFileArr, destIntmFile);
                    }
                    mergeFile(intermediateDataPath, destFile);
                }
            } else {
                if (file.exists()) {
                    System.out.print("file: " + file.getAbsolutePath() + " already exists. Now delete it: ");
                    boolean deleted = file.delete();
                    System.out.println(deleted ? "success" : "fail");
                }
                try {
                    boolean created = file.createNewFile();
                    System.out.println("create new target file: " + file.getAbsolutePath() + ": " + (created ? "success" : "fail"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader[] buffs = new BufferedReader[amount];
                String[] records = new String[amount];
                long[] lrecords = new long[amount];
                try {
                    for (int i = 0; i < amount; i++) { // 初始化
                        buffs[i] = new BufferedReader(new InputStreamReader(new FileInputStream(fileArr[i])));
                        records[i] = buffs[i].readLine();
                        if (records[i] != null) {
                            lrecords[i] = Long.parseLong(records[i].split(",")[2]);
                        } else {
                            lrecords[i] = 0L;
                        }
                    }
                    BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file));
                    int index = findSmallest(lrecords);
                    while (index >= 0 && lrecords[index] > 0) {
//                        System.out.println("index :" + index);
//                        System.out.println("Write data: " + records[index] + " , file: " + fileArr[index].getName());
//                        try {
//                            System.out.println(String.format("%s with time: %s \t%s", records[index], getTime(lrecords[index]), index));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                        buffOut.write((records[index] + "\r\n").getBytes());
                        records[index] = buffs[index].readLine();
                        if (records[index] != null) {
                            lrecords[index] = Long.parseLong(records[index].split(",")[2]);
                        } else {
                            lrecords[index] = 0L;
                        }
//                        System.out.println("Read data: " + records[index]);
                        index = findSmallest(lrecords);
                    }

                    buffOut.flush();
                    buffOut.close();
                    for (int i = 0; i < amount; i++) {
                        if (buffs[i] != null) {
                            buffs[i].close();
                            System.out.println(String.format("Close BufferedReader streams: %s", i));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.err.println("Source dir: " + tmpDir.getAbsolutePath() + " does not exists. System exits.");
        }
        return file;
    }

    File mergeFile(File[] sourceFileArr, String destFile) {
        File file = new File(destFile);
        if (file.exists()) {
            System.out.print("file: " + file.getAbsolutePath() + " already exists. Now delete it: ");
            boolean deleted = file.delete();
            System.out.println(deleted ? "succeed" : "fail");
        }
        try {
            boolean created = file.createNewFile();
            System.out.println("create new target file: " + file.getAbsolutePath() + ": " + (created ? "success" : "fail"));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for(File f: sourceFileArr){
//            System.out.println(f.getAbsolutePath());
//        }
        int amount = sourceFileArr.length;
        System.out.println(String.format("******本次合并文件数目：%d******", amount));
        BufferedReader[] buffs = new BufferedReader[amount];
        String[] records = new String[amount];
        long[] lrecords = new long[amount];
        try {
            for (int i = 0; i < amount; i++) {
                buffs[i] = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFileArr[i])));
                records[i] = buffs[i].readLine();
                if (records[i] != null) {
                    lrecords[i] = Long.parseLong(records[i].split(",")[2]);
                } else {
                    lrecords[i] = 0L;
                }
//                System.out.println(records[i] + " with time: " + new Date(lrecords[i]) + "\t" + i);
            }
            BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file));
            int index = findSmallest(lrecords);
            while (index >= 0 && lrecords[index] > 0) {
//                System.out.println("index :" + index);
//                System.out.println("Write data: " + records[index] + " , file: " + sourceFileArr[index].getName());
                buffOut.write((records[index] + "\r\n").getBytes());
                records[index] = buffs[index].readLine();
                if (records[index] != null) {
                    lrecords[index] = Long.parseLong(records[index].split(",")[2]);
                } else {
                    lrecords[index] = 0L;
                }
//                System.out.println("Read data: " + records[index]);
                index = findSmallest(lrecords);
            }

            buffOut.flush();
            buffOut.close();
            for (int i = 0; i < amount; i++) {
                if (buffs[i] != null) {
                    buffs[i].close();
                    System.out.println(String.format("Close BufferedReader streams: %d", i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    int findSmallest(long[] arr) {
        int index = 0;
        long value = arr[0];
        boolean allZero = true;
        if (arr.length > 1) {
            for (int i = 1; i < arr.length && value == 0; i++) {
                if (arr[i] > 0) {
                    allZero = false;
                    value = arr[i];
                    index = i;
                    break;
                }
            }
            if (index == arr.length) {
                index = -1;
                return index;
            }
            for (int i = index; i < arr.length; i++) {
                if (arr[i] < value && arr[i] > 0) {
                    value = arr[i];
                    index = i;
                }
                if (arr[i] > 0) {
                    allZero = false;
                }
            }
        } else {
            if (arr[0] > 0) {
                allZero = false;
            }
        }
        if (allZero) {
            index = -1;
        }
        return index;
    }

    public static void main(String[] args) {
        long timebegin = System.currentTimeMillis();

        long amount = 100L;
        double travellerRate = 0.10D;
        double employeeRate = 0.05D;
        String startDateStr = "2013-01-01 00:00:00.000";
        String endDateStr = "2013-01-30 23:59:59.999";
        long genetateRate = 2L;
        double disorderRate = 0D;
        if (args.length >= 7){
            amount = Long.parseLong(args[0]);
            travellerRate = Double.parseDouble(args[1]);
            employeeRate = Double.parseDouble(args[2]);
            startDateStr = args[3] + " 00:00:00.000";
            endDateStr = args[4] + " 23:59:59.999";
            genetateRate = Long.parseLong(args[5]);
            disorderRate = Double.parseDouble(args[6]);
        }

        long startDate = 0L, endDate = 0L;
        try {
            startDate = TimeUtil.getTime(startDateStr);
            endDate = TimeUtil.getTime(endDateStr);
            System.out.println(startDateStr+"\t"+"\t"+startDate+"\t"+TimeUtil.getTime(startDate));
            System.out.println(endDateStr+"\t"+"\t"+endDate+"\t"+TimeUtil.getTime(endDate));
        } catch (ParseException e){
            e.printStackTrace();
        }
        new GenTravellerApp().generateData(amount, travellerRate, employeeRate, startDate, endDate, genetateRate, disorderRate);


//        new GenTravellerApp().mergeFile(fileDir + File.separator + "tmp" + File.separator, fileDir + File.separator + "data.csv");

//        File[] fileArr = {new File(fileDir + File.separator +"tmp" + File.separator + "100001000008494.csv"),
//                new File(fileDir + File.separator +"tmp" + File.separator + "100001000015771.csv"),
//                new File(fileDir + File.separator +"tmp" + File.separator + "100001005052764.csv"), };
//        new GenApp().mergeFile(fileArr, fileDir + File.separator +"test.csv");

//        long[] testArr = {3L, 3L, 4L, 2L, 5L};
//        long[] zeros = {0L, 0L, 0L, 0L, 0L};
//        int index = new GenApp().findSmallest(zeros);
//        System.out.println(index);
//        System.out.println(index + ": " + testArr[index]);


/*
        formatTime("2013-01-01 00:00:00.000");
        formatTime("2013-01-04 00:00:00.000");
        formatTime("2013-01-05 00:00:00.000");
        formatTime("2013-01-06 00:00:00.000");
        formatTime("2013-01-07 00:00:00.000");
        formatTime("2013-01-08 00:00:00.000");
        formatTime("2013-01-09 00:00:00.000");
        formatTime("2013-01-10 17:59:59.999");
        formatTime("2013-01-11 00:00:00.000");
        formatTime("2013-01-12 01:00:00.000");
        formatTime("2013-01-13 02:00:00.000");
        formatTime("2013-01-14 02:00:00.000");
        formatTime("2013-01-14 17:00:00.000");
        formatTime("2013-01-14 19:00:00.000");
        formatTime("2013-01-14 22:00:00.000");
        formatTime("2013-01-16 07:00:00.000");
*/
        /*
        // 生成测试样例数据
        int amount = 1 * 24 * 31 - 1;
//        long startTime = 1357891261000L;
        long startTime = 0L;
        try {
            startTime = getTime("2013-01-01 00:00:00.000");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Random random1 = new Random();
        int randomDelta = 3600 * 1000; // 1hour 1signal
        int time = random1.nextInt(randomDelta);
        try {
            System.out.println(String.format("%d\t%d\t%s", 1, startTime + time, getTime(startTime + time)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < amount; i++){
            startTime += randomDelta;
            time = random1.nextInt(randomDelta);
            try {
                System.out.println(String.format("%d\t%d\t%s", i+2, startTime + time, getTime(startTime + time)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        */

        long timeend = System.currentTimeMillis();
        System.out.println("耗时：" + (timeend - timebegin));
    }

}

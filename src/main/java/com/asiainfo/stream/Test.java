package com.asiainfo.stream;

import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.*;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-2-18
 * Time: 下午3:28
 * 数据生成主程序，分别调用普通用户、游客、工作人员信令数据生成程序。
 */
public class Test {
    Random random =  new Random();

    /**
     * 按用户类型生成信令数据
     *
     * @param amount
     * 用户数据规模
     * @param touristRate
     * 游客比率
     * @param workerRate
     * 工作人员比率
     * @param startDate
     * 开始时间，毫秒数
     * @param endDate
     * 结束时间，毫秒数
     * @param genetateRate
     * 信令数据生成速率，条/时
     * @param disorderRate
     * 乱序比率
     */
    void generateData(long amount, double touristRate, double workerRate, long startDate, long endDate, long genetateRate, double disorderRate){
        System.out.println("Generate data, user amount: " + amount +
                ", touristRate: " + touristRate +", workerRate: " + workerRate +
                ", startDate: " + new Date(startDate) + ", endDate: " + new Date(endDate) +
                ", genetateRate: " + genetateRate + ", disorderRate: " + disorderRate);
        TouristUtil touristUtil = new TouristUtil();
        WorkerUtil workerUtil = new WorkerUtil();
        CommonUserUtil commonUserUtil = new CommonUserUtil();
        StringBuilder imsiInfo = new StringBuilder();

        long startImsi = 100001000000001L;
        String imsi;

        for(int i = 0; i < amount; i++){
            startImsi += (long)(touristUtil.getNotZeroRandomInt(10000));
            imsi = Long.toString(startImsi);
            double tRate = Math.random();
            if(tRate >= 0 && tRate <=touristRate){ // 游客
                System.out.println(imsi + "\t" + "tourist" + "\t" + tRate);
                touristUtil.generateTouristData(imsi, startDate, endDate, genetateRate);
                imsiInfo.append(imsi + ": tourist" + "\r\n");
            } else if(tRate > touristRate && tRate <= (workerRate + touristRate)){ // 工作人员
                System.out.println(imsi + "\t" + "worker" + "\t" + tRate);
                workerUtil.generateWorkerData(imsi, startDate, endDate, genetateRate);
                imsiInfo.append(imsi + ": worker" + "\r\n");
            } else { // 普通用户
                System.out.println(imsi + "\t" + tRate);
                commonUserUtil.generateCommonUserData(imsi, startDate, endDate, genetateRate);
//                imsiInfo.append(imsi + ": commonUser" + "\r\n");
            }
        }
        System.out.println("*********************************\r\n" + imsiInfo);
        String sumFile = "files" + File.separator +"summary.csv";
        File summaryFile = new File(sumFile);
        BufferedOutputStream buff = null;
        if(!summaryFile.exists()){
            try{
                boolean created = summaryFile.createNewFile();
                System.out.println("new summary file: " + summaryFile.getAbsolutePath() + ": " + created);
                buff = new BufferedOutputStream(new FileOutputStream(summaryFile));
                buff.write(imsiInfo.toString().getBytes());
                buff.flush();
                buff.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("Summary file already exists: " + summaryFile.getAbsolutePath() + ": false");
            summaryFile = null;
        }
        String sourcePath = "files" + File.separator +"tmp" + File.separator;
        String destFile = "files" + File.separator +"data.csv";
        mergeFile(sourcePath, destFile);
    }

    File mergeFile(String sourceDir, String destFile){
        File file = new File(destFile);
        File tmpDir = new File(sourceDir);
        if(tmpDir.exists() && tmpDir.isDirectory()){
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
            System.out.println("源文件总数：" + amount);
            int numLimit = 1000; // 多余10000文件时，递归处理
            if (amount > numLimit){
                System.out.println("amount > numLimit: " + amount + " > " + numLimit);
                String intermediateDataPath = "files" + File.separator +"intermediate" + File.separator;
                File intmDataDir = new File(intermediateDataPath);
                if (!intmDataDir.exists()){
                    intmDataDir.mkdirs();
                }
                if (intmDataDir.isDirectory()){
                    File[] iFileArr = new File[numLimit];
                    int nloop = (amount % numLimit == 0) ? (amount / numLimit) : (amount / numLimit + 1);
                    for (int i = 0; i < nloop; i++){
                        System.out.println("loop num: " + i);
                        for (int j = 0; j < numLimit; j++){
                            iFileArr[j] = fileArr[i * numLimit + j];
//                            System.out.println("inner loop :" + j + ": " + iFileArr[j].getAbsolutePath());
                        }
                        String destIntmFile = intermediateDataPath + i + ".csv";
                        mergeFile(iFileArr, destIntmFile );
                    }
                    mergeFile(intermediateDataPath, destFile);
                }
            } else {
                if(file.exists()){
                    System.out.print("file: " + file.getAbsolutePath() + " already exists. Now delete it: ");
                    boolean deleted = file.delete();
                    System.out.println(deleted ? "success" : "fail");
                }
                try{
                    boolean created = file.createNewFile();
                    System.out.println("create new target file: " + file.getAbsolutePath() + ": " + created);
                } catch (IOException e){
                    e.printStackTrace();
                }
                BufferedReader[] buffs = new BufferedReader[amount];
                String[] records = new String[amount];
                long[] lrecords = new long[amount];
                try{
                    for(int i = 0; i < amount; i++){
                        buffs[i] = new BufferedReader(new InputStreamReader(new FileInputStream(fileArr[i])));
                        records[i] = buffs[i].readLine();
                        if (records[i] != null){
                            lrecords[i] = Long.parseLong(records[i].split(",")[1]);
                        } else {
                            lrecords[i] = 0L;
                        }
                        try {
                            System.out.println(records[i] + " with time: " + GenApp.getTime(lrecords[i]) + "\t" + i);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file));
                    int index = findSmallest(lrecords);
                    while (index >= 0 && lrecords[index] > 0){
                        System.out.println("index :" + index);
//                        System.out.println("Write data: " + records[index] + " , file: " + fileArr[index].getName());
                        buffOut.write((records[index] + "\r\n").getBytes());
                        records[index] = buffs[index].readLine();
                        if (records[index] != null){
                            lrecords[index] = Long.parseLong(records[index].split(",")[1]);
                        } else {
                            lrecords[index] = 0L;
                        }
//                        System.out.println("Read data: " + records[index]);
                        index = findSmallest(lrecords);
                    }

                    buffOut.flush();
                    buffOut.close();
                    for (int i = 0; i < amount; i++){
                        if (buffs[i] != null){
                            buffs[i].close();
                            System.out.println("Close BufferedReader streams: " + i);
                        }
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

        } else {
            System.err.println("Source dir: " + tmpDir.getAbsolutePath() + " does not exists. System exits.");
        }
        return file;
    }

    File mergeFile(File[] sourceFileArr, String destFile){
        File file = new File(destFile);
        if(file.exists()){
            System.out.print("file: " + file.getAbsolutePath() + " already exists. Now delete it: ");
            boolean deleted = file.delete();
            System.out.println(deleted ? "succeed" : "fail");
        }
        try{
            boolean created = file.createNewFile();
            System.out.println("create new target file: " + file.getAbsolutePath() + ": " + created);
        } catch (IOException e){
            e.printStackTrace();
        }

//        for(File f: sourceFileArr){
//            System.out.println(f.getAbsolutePath());
//        }
        int amount = sourceFileArr.length;
        System.out.println("本次合并文件数目：" + amount);
        BufferedReader[] buffs = new BufferedReader[amount];
        String[] records = new String[amount];
        long[] lrecords = new long[amount];
        try{
            for(int i = 0; i < amount; i++){
                buffs[i] = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFileArr[i])));
                records[i] = buffs[i].readLine();
                if (records[i] != null){
                    lrecords[i] = Long.parseLong(records[i].split(",")[1]);
                } else {
                    lrecords[i] = 0L;
                }
//                System.out.println(records[i] + " with time: " + new Date(lrecords[i]) + "\t" + i);
            }
            BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file));
            int index = findSmallest(lrecords);
            while (index >= 0 && lrecords[index] > 0){
//                System.out.println("index: " + index);
//                System.out.println("Write data: " + records[index] + " , file: " + sourceFileArr[index].getName());
                buffOut.write((records[index] + "\r\n").getBytes());
                records[index] = buffs[index].readLine();
                if (records[index] != null){
                    lrecords[index] = Long.parseLong(records[index].split(",")[1]);
                } else {
                    lrecords[index] = 0L;
                }
//                System.out.println("Read data: " + records[index]);
                index = findSmallest(lrecords);
            }

            buffOut.flush();
            buffOut.close();
            for (int i = 0; i < amount; i++){
                if (buffs[i] != null){
                    buffs[i].close();
                    System.out.println("Close BufferedReader streams: " + i);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return file;
    }

    int findSmallest(long[] arr){
        int index = 0;
        long value = arr[0];
        boolean allZero = true;
        if (arr.length > 1){
            for (int i = 1; i < arr.length && value == 0; i++){
                if (arr[i] > 0){
                    allZero = false;
                    value = arr[i];
                    index = i;
                    break;
                }
            }
            if (index == arr.length){
                index = -1;
                return index;
            }
            for (int i = index; i < arr.length; i++){
                if (arr[i] < value && arr[i] > 0){
                    value = arr[i];
                    index = i;
                }
                if (arr[i] > 0){
                    allZero = false;
                }
            }
        } else {
            if (arr[0] > 0){
                allZero = false;
            }
        }
        if (allZero){
            index = -1;
        }
        return index;
    }
    public static void main(String[] args){
        long timebegin = System.currentTimeMillis();

        String startDateStr = "2013-01-01 00:00:00.000", endDateStr = "2013-01-10 23:59:59.999";
        long startDate = 0L, endDate = 0L;
        try {
            startDate = getTime(startDateStr);
            endDate = getTime(endDateStr);
            System.out.println(startDateStr+"\t"+"\t"+startDate+"\t"+getTime(startDate));
            System.out.println(endDateStr+"\t"+"\t"+endDate+"\t"+getTime(endDate));
        } catch (ParseException e){
            e.printStackTrace();
        }
        new Test().generateData(10L, 1D, 0D, startDate, endDate, 2L, 0D);


//        new GenApp().mergeFile("files" + File.separator +"tmp" + File.separator, "files" + File.separator +"data.csv");
//        File[] fileArr = {new File("files" + File.separator +"tmp" + File.separator + "100001000008494.csv"),
//                new File("files" + File.separator +"tmp" + File.separator + "100001000015771.csv"),
//                new File("files" + File.separator +"tmp" + File.separator + "100001005052764.csv"), };
//        new GenApp().mergeFile(fileArr, "files" + File.separator +"test.csv");

//        long[] testArr = {3L, 3L, 4L, 2L, 5L};
//        long[] zeros = {0L};
//        int index = new Test().findSmallest(zeros);
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
//        formatTime("2013-01-11 01:01:01.000");
        long timeend = System.currentTimeMillis();
        System.out.println("耗时：" + (timeend - timebegin));

    }

    static void formatTime(String timeStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        try {
            long ltime = getTime(timeStr);
            System.out.println(String.format("%s\t%s", ltime, getTime(ltime)));
        } catch (ParseException e){
            e.printStackTrace();
        }
    }
    private static long getTime(String s) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").parse(s + " +0000").getTime();
    }
    private static String getTime(long s) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(s- TimeZone.getDefault().getRawOffset()));
    }
}

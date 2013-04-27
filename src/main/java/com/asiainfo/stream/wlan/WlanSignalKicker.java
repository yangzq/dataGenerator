package com.asiainfo.stream.wlan;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: yangzq2
 * Date: 13-2-21
 * Time: 下午6:47
 */
public class WlanSignalKicker {

    public static void main(String[] args) {
        final long time1 = System.currentTimeMillis();
        final String filePath = args[0];
//        final String filePath = "d:\\svn\\storm\\dataGenerator\\files\\tmp\\100001000008870.csv";
        final long sleepTime = Long.parseLong(args[1]); // 指定发送1000条信令暂停多少毫秒
        final long kickCount = Long.parseLong(args[2]); // 指定发送信令条数，大于零时生效
        final int paramLength = args.length - 3;
        final String[] destIp = new String[paramLength];
        final int[] destPorts = new int[paramLength];
        if (paramLength >= 1) {
            for (int i = 0; i < paramLength; i++) {
                destIp[i] = args[i + 3].split(":")[0];
                destPorts[i] = Integer.parseInt(args[i + 3].split(":")[1]);
                System.out.println("destination IP: [" + i + "]: " + destIp[i] + ":" + destPorts[i]);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("To read file: " + filePath);
                if (filePath != null && !filePath.equals("")) {
                    File file = new File(filePath);
                    if ((file.exists()) && (file.canRead())) {
                        try {
                            Socket[] sockets = new Socket[paramLength];
                            PrintWriter[] out = new PrintWriter[paramLength];
//                            PrintStream[] out = new PrintStream[paramLength];
                            if (paramLength >= 1) {
                                for (int i = 0; i < paramLength; i++) {
                                    sockets[i] = new Socket(destIp[i] != null ? destIp[i] : "localhost", destPorts[i]);
                                    out[i] = new PrintWriter(sockets[i].getOutputStream());
//                                    out[i] = System.out;
                                    System.out.println("create socksts: " + sockets[i].getInetAddress());
                                }
                            }
                            BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                            String rec = buff.readLine();
                            long timea = System.currentTimeMillis();
                            int i = 1;
                            if (kickCount > 0) {
                                System.out.println("指定发送： " + kickCount + " 条信令");
                                while (rec != null && i <= kickCount) {
                                    int index = ((i - 1) / 100) % paramLength;
                                    out[index].println(rec);
                                    out[index].flush();
//                                    System.out.println(String.format("Send data: %s to: %s:%s, num: %d", rec, sockets[index].getInetAddress(), sockets[index].getPort(), i));
                                    if (i % 1000 == 0) {
                                        long timeb = System.currentTimeMillis();
                                        System.out.println("本次发送1000条数据耗时：" + (timeb - timea));
                                        Thread.sleep(sleepTime);
                                        System.out.println("--------------------------------------------");
                                        System.out.println("Thread.sleep(" + sleepTime + ")");
                                        System.out.println("--------------------------------------------");
                                        timea = System.currentTimeMillis();
                                    }
                                    rec = buff.readLine();
                                    i++;
                                }
                            } else {
                                System.out.println("指定发送所有信令");
                                while (rec != null) {
                                    int index = ((i - 1) / 100) % paramLength;
                                    out[index].println(rec);
                                    out[index].flush();
//                                    System.out.println(String.format("Send data: %s to: %s:%s, num: %d", rec, sockets[index].getInetAddress(), sockets[index].getPort(), i));
                                    if (i % 1000 == 0) {
                                        long timeb = System.currentTimeMillis();
                                        System.out.println("本次发送1000条数据耗时：" + (timeb - timea));
                                        Thread.sleep(sleepTime);
                                        System.out.println("--------------------------------------------");
                                        System.out.println("Thread.sleep(" + sleepTime + ")");
                                        System.out.println("--------------------------------------------");
                                        timea = System.currentTimeMillis();
                                    }
                                    rec = buff.readLine();
                                    i++;
                                }
                            }

                            buff.close();
                            for (int j = 0; j < paramLength; j++) {
                                System.out.println("close socksts: " + sockets[j].getInetAddress());
                                out[j].close();
                                sockets[j].close();
                            }
                            long time2 = System.currentTimeMillis();
                            System.out.println("发送数据耗时：" + (time2 - time1));
                        } catch (UnknownHostException e1) {
                            e1.printStackTrace();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        } catch (InterruptedException e3) {
                            e3.printStackTrace();
                        }
                    } else {
                        System.out.println("file.exists(): " + file.exists() + ", path: " + filePath);
                    }
                }
            }
        }).start();

    }
}

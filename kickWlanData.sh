nohup java -client -Xmx128m -Xms64m -cp dataGenerator-1.0-SNAPSHOT.jar com.asiainfo.stream.wlan.WlanSignalKicker data.csv 0 0 10.1.253.29:5002 10.1.253.29:5003 > kick.out 2>&1 &
#tail -f kick.out
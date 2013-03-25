nohup java -cp dataGenerator-1.0-SNAPSHOT.jar com.asiainfo.stream.tourist.SignalKicker files/data.csv 0 10000 10.1.253.93 10.1.253.92 10.1.253.91 > kick.out 2>&1 &
# java -cp dataGenerator-1.0-SNAPSHOT.jar com.asiainfo.stream.tourist.SignalKicker files/data.csv 10.1.253.92 10
tail -f kick.out

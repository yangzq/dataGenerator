nohup java -server -Xmx512m -Xms64m -cp dataGenerator-1.0-SNAPSHOT.jar com.asiainfo.stream.airport.AirportSignalKicker data.csv 0 0 10.1.253.29:5001 > kick.out 2>&1 &
#tail -f kick.out
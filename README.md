dataGenerator
=============

景区游客案例参数说明：
/**
 * 按用户类型生成信令数据
 *
 * @param amount       用户数
 * @param touristRate  游客比率，double
 * @param workerRate   工作人员比率，double
 * @param startDate    开始时间，yyyy-MM-dd
 * @param endDate      结束时间，yyyy-MM-dd
 * @param genetateRate 信令数据生成速率，条/时
 * @param disorderRate 乱序比率
 */
数据生成在files/文件夹中，data.csv为数据文件，summary.csv为测试数据统计信息。

--------------------------
wlan热点提醒案例参数说明：
/**
 * 按用户类型生成信令数据
 *
 * @param amount       用户数
 * @param wlanRate     WLAN用户比率，double
 * @param startDate    开始时间，HH:mm:ss，日期为2013-01-11
 * @param endDate      结束时间，HH:mm:ss，日期为2013-01-11
 * @param generateRate 信令数据生成速率，条/分钟
 * @param disorderRate 乱序比率
 */
数据生成在wlanfiles/文件夹中，data.csv为数据文件，summary.csv为测试数据统计信息。

--------------------------
机场来港客户案例参数说明：
/**
 * 按用户类型生成信令数据
 *
 * @param amount        用户数
 * @param travellerRate 抵港旅客比率，double
 * @param employeeRate  工作人员比率，double
 * @param startDate     开始时间，yyyy-MM-dd
 * @param endDate       结束时间，yyyy-MM-dd
 * @param genetateRate  信令数据生成速率，条/时
 * @param disorderRate  乱序比率
 */
数据生成在airportfiles/文件夹中，data.csv为数据文件，summary.csv为测试数据统计信息。

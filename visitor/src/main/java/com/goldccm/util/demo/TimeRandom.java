package com.goldccm.util.demo;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimeRandom {
	
	public static int LEN = 10000;

	/**
	 * 次数
	 */
	private int time;
	
	/**
	 * 结果
	 */
	private List<String> results;
	/**
	 * 开始小时数
	 */
	private int startHour;
	/**
	 * 结束小时数
	 */
	private int endHour;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	
	public int getTime() {
		return time;
	}
	public TimeRandom setTime(int time) {
		this.time = time;
		return this;
	}
	public List<String> getResults() {
		return results;
	}
	public TimeRandom setResults(List<String> results) {
		this.results = results;
		return this;
	}
	
	public int getStartHour() {
		return startHour;
	}
	public TimeRandom setStartHour(int startHour) {
		this.startHour = startHour;
		return this;
	}
	public int getEndHour() {
		return endHour;
	}
	public TimeRandom setEndHour(int endHour) {
		this.endHour = endHour;
		return this;
	}
	
	
	public String getStartTime() {
		return startTime;
	}
	public TimeRandom setStartTime(String startTime) {
		this.startTime = startTime;
		return this;
	}
	public String getEndTime() {
		return endTime;
	}
	public TimeRandom setEndTime(String endTime) {
		this.endTime = endTime;
		return this;
	}
	public static TimeRandom build(){
		return new TimeRandom();
	}
	
	public static List<String> getPayTime(int time,int start,int end,String startTime,String endTime)
			throws Exception{
		
		if(end <= start){
			throw new Exception("还款允许结束时间小于等于还款允许开始时间");
		}
		if(startTime.compareTo(endTime) >= 0){
			throw new Exception("还款时间小于等于还款开始时间");
		}
		
		TimeRandom timeRandom = TimeRandom.build()
			.setTime(time)
			.setStartHour(start)
			.setEndHour(end)
			.setStartTime(startTime)
			.setEndTime(endTime);
		
		List<String> results = new ArrayList<String>();
		int totalHours = getHourlen(timeRandom.getEndTime(), timeRandom.getStartTime(), timeRandom.getStartHour(), timeRandom.getEndHour());
		
		if(totalHours == 0){
			throw new Exception("开始时间到结束时间至少要有一个小时间隔");
		}
		
		double perHour = totalHours * 1.0/timeRandom.getTime();
		for(int i = 0;i < timeRandom.getTime();i++){
			
			Random hourRandom = new Random();
			double randomHour = hourRandom.nextInt((int)Math.floor(perHour*1000000));
			String result = getNextTime(timeRandom.getStartTime(), randomHour/1000000.0,timeRandom.getStartHour(), timeRandom.getEndHour());
			results.add(result);
			
			timeRandom.setStartTime(getNextTime(timeRandom.getStartTime(), perHour,timeRandom.getStartHour(), timeRandom.getEndHour()));
		}
		
		timeRandom.setResults(results);
		return timeRandom.getResults();
	}
	
	public static void main(String[] args)throws Exception{
		System.out.println(JSONObject.toJSONString(getPayTime(14,8,20,"2017-06-18 15:00:00","2017-06-19 18:00:00")));
	}
	
	private static int getHourlen(String endTime,String startTime,int startHour,int endHour)throws Exception{
		GregorianCalendar startGc = new GregorianCalendar();
		startGc.setTime(toDate(startTime));
		GregorianCalendar endGc = new GregorianCalendar();
		endGc.setTime(toDate(endTime));
		int hours = 0;
		while(startGc.before(endGc)){
			startGc.add(Calendar.HOUR_OF_DAY, 1);
			if(startGc.before(endGc) && startGc.get(Calendar.HOUR_OF_DAY) >= startHour && startGc.get(Calendar.HOUR_OF_DAY) < endHour){
				hours++;
			}
		}
		return hours;
	}
	
	private static String getNextTime(String startTime,double hours,int startHour,int endHour)throws Exception{
		Date startTimeDate = toDate(startTime);
		Date nowTimeDate = new Date(startTimeDate.getTime()+(long)(hours*3600*1000));
		while(nowTimeDate.getHours() < startHour || nowTimeDate.getHours() >= endHour){
			nowTimeDate = new Date(nowTimeDate.getTime()+(1*3600*1000));
		}
		nowTimeDate = new Date(nowTimeDate.getTime());
		return toDateStr(nowTimeDate);
	}
	
	public static String toDateStr(Date date){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}
	public static Date toDate(String dateStr)throws Exception{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
	}
	
	
}

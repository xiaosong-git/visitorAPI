package com.goldccm.util.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class WeightRandom {
	//存放权重的树
	private TreeMap<Double, String> weightMap = new TreeMap<Double, String>();

	public WeightRandom(List<WeightCategory> list) {
		for (WeightCategory wc : list) {
			double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey().doubleValue();//统一转为double
			this.weightMap.put(wc.getWeight() + lastWeight, wc.getChannel());//权重累加
		}
	}

	public String random() {
		double randomWeight = this.weightMap.lastKey() * Math.random();
		SortedMap<Double, String> tailMap = this.weightMap.tailMap(randomWeight, false);
		return this.weightMap.get(tailMap.firstKey());
	}

	public static List<WeightCategory> initWeightList(){
		List<WeightCategory> list = new ArrayList<WeightCategory>();

		WeightCategory wc1 = new WeightCategory();
		wc1.setChannel("A");
		wc1.setWeight(1);

		WeightCategory wc2 = new WeightCategory();
		wc2.setChannel("B");
		wc2.setWeight(2);

		WeightCategory wc3 = new WeightCategory();
		wc3.setChannel("C");
		wc3.setWeight(3);

		list.add(wc1);
		list.add(wc2);
		list.add(wc3);

		return list;
	}

	public static void main(String[] args){

		List<WeightCategory> list = initWeightList();
		WeightRandom wr = new WeightRandom(list);
		Long aCount = 0L;
		Long bCount = 0L;
		Long cCount = 0L;
		String s = null;
		long total = 1000000;
		for(long i=0; i<total; i++){
			s = wr.random();
			if("C".equals(s)){
				cCount++;
			}else if("B".equals(s)){
				bCount++;
			}
			else if("A".equals(s)){
				aCount++;
			}
		}
		System.out.println("总次数："+total);
		System.out.println("A出现次数："+aCount);
		System.out.println("B出现次数："+bCount);
		System.out.println("C出现次数："+cCount);
	}
	

}

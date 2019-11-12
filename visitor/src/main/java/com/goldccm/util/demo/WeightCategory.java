package com.goldccm.util.demo;

import java.util.Random;

/**
 * Created by Administrator on 2017/11/9.
 */
public class WeightCategory {

    public static void main(String[] args) {
        Random random = new Random();
        for(int i=0; i<100; i++){
            int num = random.nextInt(10)+1;
            if(num == 10){
                System.out.println("xxxxxxx");
            }
        }
    }
    private String channel;
    private double weight;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}

package com.goldccm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class Test implements Comparable {

    private Integer param;

    public Integer getParam() {
        return param;
    }

    public void setParam(Integer param) {
        this.param = param;
    }

    public Test(Integer param){
        this.param = param;
    }

    public static void main(String[] args) {
        List<Test> list = new ArrayList<Test>();
        Test t2 = new Test(2);
        Test t1 = new Test(1);
        list.add(t1);
        list.add(t2);
        Collections.sort(list);
        Iterator i = list.iterator();
        while (i.hasNext()){
            Test temp = (Test)i.next();
            System.out.println(temp.getParam());
        }
    }


    @Override
    public int compareTo(Object o) {
        Test t = (Test)o;
        return (this.param - t.getParam());
    }
}

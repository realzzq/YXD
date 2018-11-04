package com.zzq.simple.control;

public class Test {
    public static void main(String[] args){
        System.out.println("欢迎来到雅信达自动代刷习题,本系统适合于东电理学院的，如其他学院或学校的请联系作者Lucien:q256598791");
        String itemType = "getSpecialWorkByParam.do?ruletype=";
        String username = "";
        String password = "";
        QuickYxd Listen = new QuickYxd(username,password,itemType + "1");
        for (int i = 0; i < 2; i++) {
            Listen.beginAnswer();
            Listen.getCorrectAnswers();
            Listen.submitCorrectAnswers();
        }
        QuickYxd Read = new QuickYxd(username, password, itemType + "2");
        for (int i = 0; i < 2; i++) {
            Read.beginAnswer();
            Read.getCorrectAnswers();
            Read.submitCorrectAnswers();
        }
    }
}

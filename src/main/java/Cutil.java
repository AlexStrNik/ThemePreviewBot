package main.java;

/**
 * Created by Никита on 31.07.2017.
 */
public class Cutil {
    public static void main(String[] args){
        System.out.println(getHEX("-16777216"));
    }
    public static String getHEX(String s) {
        String hex;
        if(s.equals("0")){
            return "#fff"+"\" fill-opacity=\"0";
        }
        if(!s.startsWith("#")){
            hex = Integer.toHexString(Integer.parseInt(s));
        }
        else {
            hex = s.substring(1);
        }
        if(hex.length()>6){
            float a = Integer.parseInt(hex.substring(0,2),16);
            return "#"+hex.substring(2)+"\" fill-opacity=\""+a/255.0+"";
        }
        else {
            if(hex.length()<6){
                return "#ffffff";
            }
            return "#"+hex+"\" fill-opacity=\"1";
        }
    }


}

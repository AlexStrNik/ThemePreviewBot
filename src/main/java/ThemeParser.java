package main.java;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by Никита on 31.07.2017.
 */
public class ThemeParser {
    public static Theme Default;
    public static void inicialize(){
        URL z = ThemeParser.class.getResource("default.attheme");
        File f = new File(z.getPath());
        try {
            Default = parser(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {

        File f = new File("C:\\Users\\Никита\\Downloads\\Telegram Desktop\\default.attheme");
        try {
            for(String line : parser(f).settings.keySet()){
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Theme parser(File file) throws Exception {
        FileInputStream stream = new FileInputStream(file);
        HashMap<String,String> setts = new HashMap<>();
        int read;
        byte[] bytes = new byte[1024];
        int currentPosition = 0;
        int themedWallpaperFileOffset = -1;
        boolean finished = false;
        Image image = null;
        while ((read = stream.read(bytes)) != -1) {

            int previousPosition = currentPosition;
            int start = 0;
            for (int a = 0; a < read; a++) {
                if (bytes[a] == '\n') {
                    int len = a - start + 1;
                    String line = new String(bytes, start, len - 1, "UTF-8");
                    if (line.startsWith("WPS")) {
                        themedWallpaperFileOffset = currentPosition + len;
                        finished = true;
                        break;
                    } else {
                        setts.put(line.split("=")[0],line.split("=")[1]);
                    }
                    start += len;
                    currentPosition += len;
                }
            }
            if (previousPosition == currentPosition) {
                break;
            }
            stream.getChannel().position(currentPosition);
            if (finished) {
                break;
            }

        }
        if(themedWallpaperFileOffset!=-1){
            stream = new FileInputStream(file);
            stream.getChannel().position(themedWallpaperFileOffset);
            image = ImageIO.read(stream);
        }
        return new Theme(image, setts);
    }
}
class Theme{
    public Image image;
    public HashMap<String,String> settings;
    public Theme(Image im, HashMap<String,String> set){
        settings= set;
        image=im;
    }
}

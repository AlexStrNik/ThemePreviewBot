package main.java;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Document;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;

/**
 * Created by Никита on 04.08.2017.
 */

public class ThemePreviewBot extends TelegramLongPollingBot {
    static String help = "Send me an .attheme file to create its preview!";
    static String welcome = "Welcome {user}";
    public ThemePreviewBot(){
        super();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()){
            Message user = update.getMessage();
            if(user.hasText()){
                if (user.getText().equals("/start")){
                    SendMessage m = new SendMessage();
                    m.setChatId(user.getChatId().toString());
                    m.setText(welcome.replace("{user}",user.getFrom().getUserName())+"\n"+help);
                    try {
                        sendMessage(m);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                else if(user.getText().startsWith("/start ")){
                    String id = user.getText().split("/start ")[1];
                    SendMessage m = new SendMessage();
                    m.setChatId(user.getChatId().toString());
                    m.setText(welcome.replace("{user}",user.getFrom().getUserName())+"\n"+help+"\nyour id = "+id);
                    try {
                        sendMessage(m);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                else if(user.getText().equals("/help")){
                    SendMessage m = new SendMessage();
                    m.setChatId(user.getChatId().toString());
                    m.setText(help);
                    try {
                        sendMessage(m);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            if(user.hasDocument()){
                Document d = user.getDocument();
                if(d.getFileName().endsWith(".attheme")){
                    try {
                        GetFile fq = new GetFile();
                        fq.setFileId(d.getFileId());
                        File f = downloadFile(getFile(fq));
                        Theme th = ThemeParser.parser(f);
                        Image img = PreviewParser.getPreview(th);
                        f.delete();
                        SendPhoto ph = new SendPhoto();
                        SendDocument doc = new SendDocument();
                        f = File.createTempFile("img",".tmp");
                        ImageIO.write((RenderedImage) img,"jpg",f);
                        ph.setNewPhoto(f);
                        doc.setNewDocument(f);
                        doc.setChatId(user.getChatId().toString());
                        doc.setCaption("preview created by @ThemePreviewBot");
                        ph.setChatId(user.getChatId().toString());
                        ph.setCaption("preview created by @ThemePreviewBot");
                        sendDocument(doc);
                        sendPhoto(ph);
                        f.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "ThemePreviewBot";
    }

    @Override
    public String getBotToken() {
        return "419395725:AAGFE5XK3kgcTCmPLxj388ETbGeI9KEDyfk";
    }
    public static void main(String[] args) {

        ApiContextInitializer.init();
        ThemeParser.inicialize();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new ThemePreviewBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

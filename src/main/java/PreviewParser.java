package main.java;



import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;


public class PreviewParser {
    public static void main(String[] args){
        try {
            File f = new File("C:\\Users\\Никита\\Downloads\\Telegram Desktop\\misd.attheme");
            getPreview(ThemeParser.parser(f));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    public static Image getPreview(Theme theme) throws IOException, TranscoderException {
        SVG left = parse("list.svg");
        SVG right = parse("chat.svg");
        left.applyTheme(ThemeParser.Default);
        right.applyTheme(ThemeParser.Default);
        left.applyTheme(theme);
        right.applyTheme(theme);
        if(theme.image!=null){
            theme.image=createResizedCopy(theme.image,480*3,720*3);
            right.classes.get("IMG").get(0).setImage(imgToBase64String(theme.image));
            right.layers.remove(right.classes.get("chat_wallpaper").get(0));
        }
        //System.out.println(right);
        Image l = createImageFromSVG(left);
        Image r = createImageFromSVG(right);
        BufferedImage lr = new BufferedImage(960,782,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = lr.createGraphics();
        g2d.drawImage(l,0,0,null);
        g2d.drawImage(r,480,0,null);
        g2d.dispose();
        //ImageIO.write(lr,"jpg",new File("C:\\Users\\Никита\\Downloads\\Telegram Desktop\\misd.jpg"));
        return lr;
    }
    static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight)
    {
        int imageType = BufferedImage.TYPE_INT_RGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }
    public static String imgToBase64String(final Image img)
    {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            ImageIO.write((RenderedImage) img, "jpeg", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        }
        catch (final IOException ioe)
        {
            throw new UncheckedIOException(ioe);
        }
    }
    public static BufferedImage createImageFromSVG(SVG svg) throws IOException, TranscoderException {
        final BufferedImage[] imagePointer = new BufferedImage[1];

        // Rendering hints can't be set programatically, so
        // we override defaults with a temporary stylesheet.
        // These defaults emphasize quality and precision, and
        // are more similar to the defaults of other SVG viewers.
        // SVG documents can still override these defaults.
        String css = "svg {" +
                "shape-rendering: geometricPrecision;" +
                "text-rendering:  geometricPrecision;" +
                "color-rendering: optimizeQuality;" +
                "image-rendering: optimizeQuality;" +
                "}";
        File cssFile = File.createTempFile("batik-default-override-", ".css");
        Files.write(cssFile.toPath(), css.getBytes());

        TranscodingHints transcoderHints = new TranscodingHints();
        transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                SVGConstants.SVG_NAMESPACE_URI);
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        transcoderHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssFile.toURI().toString());

        try {

            TranscoderInput input = new TranscoderInput(new StringReader(svg.toString()));

            ImageTranscoder t = new ImageTranscoder() {

                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out)
                        throws TranscoderException {
                    imagePointer[0] = image;
                }
            };
            t.setTranscodingHints(transcoderHints);
            t.transcode(input, null);
        }
        catch (TranscoderException ex) {
            // Requires Java 6
            ex.printStackTrace();
            throw new IOException("Couldn't convert " + "1");
        }
        finally {
            cssFile.delete();
        }

        return imagePointer[0];
    }
    public static SVG parse(String name) throws IOException {
        SVG svg = new SVG();
        String end = ">";
        String context = "";
        for(String line : Files.readAllLines(new File(PreviewParser.class.getResource(name).getPath()).toPath(), Charset.defaultCharset())){
            if(!line.startsWith("<svg") && !line.startsWith("</svg")){
                if(line.startsWith("<")&&end.equals("-1")){
                    end=">";
                }
                if(line.startsWith("<g")&&end.equals("-1")){
                    end="g>";
                }
                if(line.endsWith(end)){
                    Layer l = new Layer();
                    l.context = context;
                    svg.layers.add(l);
                    if(l.isClass()){
                        svg.add(l.getClassName(),l);
                    }
                    context="";
                    end="-1";
                }
                context+=line+"\n";
            }
            else{
                Layer l = new Layer();
                l.context=line+"\n";
                svg.layers.add(l);
            }
        }
        return svg;
    }
}
class SVG{
    ArrayList<Layer> layers = new ArrayList<>();
    HashMap<String,ArrayList<Layer>> classes = new HashMap<>();
    public String toString(){
        String res="";
        for(Layer l : layers){
            res+=l.context;
        }
        return res;
    }
    public void add(String name,Layer layer){
        if (classes.containsKey(name)){
            classes.get(name).add(layer);
        }
        else{
            classes.put(name,new ArrayList<Layer>());
            classes.get(name).add(layer);
        }
    }
    public void applyTheme(Theme theme){
        for(String cclass : classes.keySet()){
            if(theme.settings.containsKey(cclass)){
                for(Layer rt : classes.get(cclass)){
                    rt.setColor(Cutil.getHEX(theme.settings.get(cclass)));
                }
            }
        }
    }
}
class Layer{
    String context;
    boolean isClass(){
        return context.split("\n")[0].contains("class=");
    }
    public String getClassName(){
        if(isClass()){
            return context.split("\n")[0].split("class=\"")[1].split("\"")[0];
        }
        return null;
    }
    public void setImage(String base64){
        context = context.replaceAll("AAAdataAAA","data:image/jpeg;base64,"+base64);
    }
    public void setColor(String color){
        if(isClass()){
            try {
                if(context.contains("fill-opacity")){
                    String todel = "fill-opacity=\""+context.split("fill-opacity=\"")[1].split("\"")[0]+"\"";
                    context = context.replace(todel,"");
                }
                String oldc = context.split("fill=\"")[1].split("\"")[0];
                context = context.replaceAll(oldc,color);
            }
            catch (Exception e){}
        }
    }

}
class BufferedImageTranscoder extends ImageTranscoder
{
    @Override
    public BufferedImage createImage(int w, int h)
    {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return bi;
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output)
    {
        this.img = img;
    }

    public BufferedImage getBufferedImage()
    {
        return img;
    }
    private BufferedImage img = null;
}

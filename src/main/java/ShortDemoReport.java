import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.commons.compress.utils.IOUtils;

public class ShortDemoReport {

    private static InputStream candara;
    private static InputStream candarabold;

    private static InputStream lato;
    private static InputStream latobold;
    private static InputStream latoblack;
    private static String datadir = "";
    private static String[] filesindatadir = new String[0];
    private static String indesignfile = "";
    private static LinkedHashMap<String, LinkedHashMap<String,String>> data = new LinkedHashMap<>();
    private static File outputFolderfile = new File("");
    private static boolean isDemo = true;

    public static void main(String[] args) throws Exception {
        if(isDemo){
            datadir = "E:/PCC/R Scripts/Vaginal_Microbiome/Output/PCC_Vaginal_2023-11-24-14-13-13"; // Do not change this as it is specific to demo report.
        }else{
            datadir = "E:/PCC/R Scripts/Vaginal_Microbiome/Output/PCC_Vaginal_2023-11-24-14-13-13";
        }
        //datadir = args[0];
        filesindatadir = new File(datadir).list();

        for(int i=0;i<filesindatadir.length;i++){
            if(filesindatadir[i].startsWith("InDesignInput_Vaginal")){
                indesignfile = filesindatadir[i];
                break;
            }
        }

        if(indesignfile.equals("")){
            JOptionPane.showMessageDialog(null,"The input data file cannot be found in the input folder path you have provided.");
            System.exit(1);
        }

        data = readResultsData();
        ArrayList<String> barcodes = new ArrayList<>(data.keySet());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy:MM:dd:HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);
        String[] dates = date.split(":");
        String outputcode = dates[0] +"-"+ dates[1] +"-"+ dates[2] +"-"+ dates[3] +"-"+ dates[4] +"-"+ dates[5];
        String outputFolder = "Vaginal_Reports_"+outputcode;
        String outputPath = "E:/PCC/Report Outputs/Vaginal_Reports/"+outputFolder;
        //String outputPath = args[1]+"/"+outputFolder;
        outputFolderfile = new File(outputPath);
        outputFolderfile.mkdir();

        for(int i=0;i<barcodes.size();i++) {
            generateReport(data.get(barcodes.get(i)));
        }
    }

    public static void generateReport(LinkedHashMap<String,String> dat) throws Exception{
        String barcode = dat.get("1.1_Text_Barcode");
        String coldate = dat.get("1.1_Date_samplearrivaldate");

        if(isDemo){
            barcode = "vaNGSXX";
            coldate = "TT.MM.JJJJ";
        }

        ArrayList<String> keys = new ArrayList<>(dat.keySet());

        candara = new Main().getClass().getResourceAsStream("CenturyGothicPaneuropeanRegular.ttf");
        candarabold = new Main().getClass().getResourceAsStream("CenturyGothicPaneuropeanBold.ttf");

        lato = new Main().getClass().getResourceAsStream("Lato-Regular.ttf");
        latobold = new Main().getClass().getResourceAsStream("Lato-Bold.ttf");
        latoblack = new Main().getClass().getResourceAsStream("Lato-Black.ttf");

        File out = new File(outputFolderfile.getAbsolutePath()+"/"+barcode+(isDemo?"_short_demo":"")+".pdf");

        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(out));
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        byte[] bytes = IOUtils.toByteArray(candara);
        BaseFont CANDARA_REGULAR = BaseFont.createFont("CenturyGothicPaneuropeanRegular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);

        byte[] bytes2 = IOUtils.toByteArray(candarabold);
        BaseFont CANDARA_BOLD = BaseFont.createFont("CenturyGothicPaneuropeanBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes2, null);

        byte[] bytes3 = IOUtils.toByteArray(lato);
        BaseFont LATO_REGULAR = BaseFont.createFont("Lato-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes3, null);

        byte[] bytes4 = IOUtils.toByteArray(latobold);
        BaseFont LATO_BOLD = BaseFont.createFont("Lato-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes4, null);

        byte[] bytes5 = IOUtils.toByteArray(latoblack);
        BaseFont LATO_BLACK = BaseFont.createFont("Lato-Black.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes5, null);

        // Load existing PDF

        PdfReader reader = new PdfReader(new Main().getClass().getResource("Vagina_German.pdf"));
        PdfImportedPage page;

        page = writer.getImportedPage(reader, 1);
        document.newPage();
        cb.addTemplate(page, 0, 0);

        cb.beginText();
        cb.setFontAndSize(CANDARA_BOLD, 12.5f);
        cb.setColorFill(new BaseColor(255, 255, 255));
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, barcode, 570f, 58.8f, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, coldate, 570f, 37f, 0);

        cb.setFontAndSize(LATO_BLACK, 29f);
        cb.setColorFill(new BaseColor(255, 255, 255));
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, isDemo ? "MUSTERBERICHT" : "Ergebnisbericht", 44f, 390f, 0);

        cb.endText();

        page = writer.getImportedPage(reader, 2);
        document.newPage();
        cb.addTemplate(page, 0, 0);
        addWatermark(cb,CANDARA_BOLD);

        page = writer.getImportedPage(reader, 3);
        document.newPage();
        cb.addTemplate(page, 0, 0);

        float xpos = 440f;
        float ypos = 677f;
        float coef = 0.5f;
        float width = 155f*coef;
        float height = 20f*coef;
        float radius = height/2f;
        float[] yposadj = new float[]{40f,24f,72f,24f,24f,24f};

        int item = 2;

        for(int i=2;i<8;i++){
            Image img = Image.getInstance(datadir+"/"+dat.get(keys.get(i)));
            ypos = ypos - yposadj[i-2];
            img.setAbsolutePosition(xpos, ypos);

            drawGraphSmall(cb,img,xpos,ypos,width,height,radius);
            item=i;
        }

        item++;
        addWatermark(cb,CANDARA_BOLD);

        for(int i=19;i<21;i++){
            page = writer.getImportedPage(reader, i);
            document.newPage();
            cb.addTemplate(page, 0, 0);
            addWatermark(cb,CANDARA_BOLD);
        }

        document.close();
    }

    public static void drawGraphSmall(PdfContentByte cb,Image img,float xpos,float ypos,float width, float height,
                                      float radius) throws Exception{
        cb.saveState();
        cb.roundRectangle(xpos, ypos,
                width, height, radius);
        cb.clip();
        cb.newPath(); // Clear the path for future drawings
        cb.setColorFill(new BaseColor(245,245,245,255));
        cb.rectangle(xpos, ypos,
                width, height);
        cb.fill();
        cb.addImage(img, width*1.004f, 0, 0, height*1.02f,
                xpos-0.002f*width, ypos-0.01f*height);
        cb.restoreState();
    }

    public static void drawGraphLarge(PdfContentByte cb,Image img,float xpos,float ypos,float width, float height,
                                      float radius) throws Exception{
        cb.saveState();
        cb.roundRectangle(xpos, ypos,
                width, height, radius);
        cb.clip();
        cb.newPath(); // Clear the path for future drawings
        cb.setColorFill(new BaseColor(245,245,245,255));
        cb.rectangle(xpos, ypos,
                width, height);
        cb.fill();
        cb.addImage(img, width*1.002f, 0, 0, height*1.02f,
                xpos-0.001f*width, ypos-0.01f*height);
        cb.restoreState();
    }

    public static LinkedHashMap<String, LinkedHashMap<String,String>> readResultsData() throws Exception{
        LinkedHashMap<String, LinkedHashMap<String,String>> res = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(datadir+"/"+indesignfile));
        String s = br.readLine();
        String[] headers = s.split("\t",-1);
        int barcodepos = Arrays.asList(headers).indexOf("1.1_Text_Barcode");

        while((s=br.readLine())!=null){
            String[] s2 = s.split("\t",-1);
            LinkedHashMap<String,String> sample = new LinkedHashMap<>();
            for(int i=0;i<s2.length;i++){
                sample.put(headers[i],s2[i]);
            }
            res.put(s2[barcodepos],sample);
        }

        br.close();
        return(res);
    }

    public static void addParagraph(String text, PdfContentByte cb,float x1,float y1, float x2, float y2,
                                    BaseFont font,float size) throws Exception{
        Rectangle rect = new Rectangle(x1, y1, x2, y2);

        ColumnText columnText = new ColumnText(cb);

        columnText.setSimpleColumn(rect);

        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setFont(new Font(font,size));
        paragraph.setLeading(18f);
        paragraph.add(text);

        columnText.addElement(paragraph);
        columnText.go();
    }

    public static void addWatermark(PdfContentByte cb, BaseFont font){
        if(isDemo){
            cb.beginText();
            cb.setFontAndSize(font, 70f);
            cb.setColorFill(new BaseColor(0, 0, 0,70));
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "MUSTERBERICHT", 325f, 400f, 45);
            cb.endText();
        }
    }

}

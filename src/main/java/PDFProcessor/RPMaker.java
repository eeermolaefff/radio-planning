package PDFProcessor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.File;
import java.io.IOException;

public class RPMaker {
    private static final String patternName = "E:\\YandexDisk\\!!! Проектирование Омск (1)\\Саша\\Новые проекты\\Шаблоны проектов\\РП образец.pdf";
    private static final String reportName2 = "2.pdf";
    private static final String reportName5 = "5.pdf";

    private static PDDocument sortPages(PDDocument document) {
        PDPageTree allPages = document.getPages();
        PDDocument sorted = new PDDocument();

        for (int i = 1; i < document.getNumberOfPages(); i+=2) {
            PDPage page = allPages.get(i);
            sorted.addPage(page);
        }
        for (int i = 0; i < document.getNumberOfPages(); i+=2) {
            PDPage page = allPages.get(i);
            sorted.addPage(page);
        }

        return sorted;
    }
    private static void addHeadings(PDDocument document, int numberOfFloors) throws IOException {
        int fontSize = 16;
        int marginTop = 10;
        int lastPage =  document.getNumberOfPages() - 1;

        PDFProcessor.addTextUpCenter(document, "Приложение № 2 Тепловая схема уровня сигнала RSSI 2.4 GHz (dBm)",
                0, fontSize, marginTop);
        PDFProcessor.addTextUpCenter(document, "Приложение № 3 Тепловая схема уровня сигнала RSSI 5 GHz (dBm)",
                numberOfFloors, fontSize, marginTop);
        PDFProcessor.addTextUpCenter(document, "Приложение № 6 Список точек доступа",
                numberOfFloors * 2, fontSize, marginTop);

        fontSize = 14;
        PDFProcessor.addTextWithBottomMargin(document, "Радиопланирование выполнил:",
                lastPage, fontSize, 37, 110);
        PDFProcessor.addTextWithBottomMargin(document, "Инженер ООО \"БАУМАСТЕР\"                        ________________ /Богданов В. С.",
                lastPage, fontSize, 37, 90);
    }
    private static void addTextToScheme(PDDocument document) throws IOException {
        int fontSize = 18;
        int marginTop = 30;

        PDFProcessor.addTextWithTopMargin(document, "Приложение № 1 План расположения точек",
                0, fontSize, 80, marginTop);
        marginTop += fontSize + 5;
        PDFProcessor.addTextWithTopMargin(document, "доступа, коммутаторов доступа и кабельных",
                0, fontSize, 80, marginTop);
        marginTop += fontSize + 5;
        PDFProcessor.addTextWithTopMargin(document, "трасс (с указанием масштаба планировки)",
                0, fontSize, 80, marginTop);
    }
    private static PDDocument extractSchemes(File file, int numberOfFloors) throws IOException {
        PDDocument document = Loader.loadPDF(file);
        PDPageTree allPages = document.getPages();
        PDDocument schemes = new PDDocument();

        for (int i = 1; i <= numberOfFloors; i++) {
            PDPage page = allPages.get(i);
            schemes.addPage(page);
        }

        return schemes;
    }
    private static PDDocument combine(PDDocument pattern, PDDocument RP) {
        PDDocument combined = new PDDocument();

        PDPageTree patPages = pattern.getPages();
        PDPageTree rpPages = RP.getPages();

        for (int i = 0; i < 4; i++) {
            PDPage page = patPages.get(i);
            combined.addPage(page);
        }

        for (int i = 0; i < RP.getNumberOfPages()/2; i++) {
            PDPage page = rpPages.get(i);
            combined.addPage(page);
        }

        for (int i = 4; i < 6; i++) {
            PDPage page = patPages.get(i);
            combined.addPage(page);
        }

        for (int i = RP.getNumberOfPages()/2; i < RP.getNumberOfPages(); i++) {
            PDPage page = rpPages.get(i);
            combined.addPage(page);
        }

        return combined;
    }
    private static PDDocument rebuild(PDDocument source, PDDocument sorted, int numberOfFloors) {
        PDDocument rebuilded = new PDDocument();

        PDPageTree srcPages = source.getPages();
        PDPageTree sortedPages = sorted.getPages();

        int numOfSrcPages = srcPages.getCount();

        for (int i = 0; i < 4; i++) {
            PDPage page = srcPages.get(i);
            rebuilded.addPage(page);
        }

        if (numOfSrcPages == 6 + numberOfFloors * 5)
            for (int i = 4; i < 4 + numberOfFloors; i++) {
                PDPage page = srcPages.get(i);
                rebuilded.addPage(page);
            }

        for (int i = 0; i < numberOfFloors * 2; i++) {
            PDPage page = sortedPages.get(i);
            rebuilded.addPage(page);
        }

        int tablePage = rebuilded.getPages().getCount();
        for (int i = tablePage; i < tablePage + 2; i++) {
            PDPage page = srcPages.get(i);
            rebuilded.addPage(page);
        }

        for (int i = numberOfFloors * 2; i < sortedPages.getCount(); i++) {
            PDPage page = sortedPages.get(i);
            rebuilded.addPage(page);
        }

        return rebuilded;
    }
    private static String newRPVersionName(String oldName) {    //oldName = 530 РП v3.4.pdf -> 530 РП v3 -> {530 РП , 3.4}
        oldName = oldName.substring(0, oldName.length() - 4);

        System.out.println(oldName);
        String[] tokens = oldName.split("v");
        String newVersion = "";
        String versionStr = tokens[tokens.length-1];

        if (versionStr.contains(".")) {
            String[] versionTokens = versionStr.split("\\.");   //{3, 4}
            int lastIdx = versionTokens.length - 1;

            Integer version = Integer.parseInt(versionTokens[lastIdx]) + 1; //4 + 1
            versionTokens[versionTokens.length - 1] = version.toString();   //{3, 5}

            for (String token : versionTokens)
                newVersion += token + ".";
            newVersion = newVersion.substring(0, newVersion.length()-1);
        }
        else {
            Integer version = Integer.parseInt(tokens[1]) + 1;
            newVersion = version.toString();
        }

        System.out.println(newVersion);

        return tokens[0] + "v" + newVersion + ".pdf";
    }
    public static void remakeRP(String filePath) throws IOException {
        String dir = filePath.substring(0, filePath.lastIndexOf('\\') + 1);

        File reportSrc2 = new File(dir + reportName2);
        File reportSrc5 = new File(dir + reportName5);
        File oldFile = new File(filePath);

        PDDocument[] files = {PDFProcessor.deleteFirstPage(reportSrc2), PDFProcessor.deleteFirstPage(reportSrc5)};
        int numberOfFloors = files[0].getNumberOfPages() / 2;

        PDDocument merged = PDFProcessor.mergeFiles(files);
        PDDocument sorted = sortPages(merged);
        addHeadings(sorted, numberOfFloors);

        PDDocument oldRP = Loader.loadPDF(oldFile);
        PDDocument newRP = rebuild(oldRP, sorted, numberOfFloors);

        System.out.println(newRPVersionName(oldFile.getName()));
        newRP.save(dir + newRPVersionName(oldFile.getName()));

        for (PDDocument file : files)
            file.close();
        merged.close();
        sorted.close();
        oldRP.close();
        newRP.close();
        System.out.println(reportSrc2.delete());
        System.out.println(reportSrc5.delete());
    }
    public static void makeRP(String dir) throws IOException {
        File reportSrc2 = new File(dir + reportName2);
        File reportSrc5 = new File(dir + reportName5);
        File patternSrc = new File(patternName);

        String schoolID = reportSrc2.getParentFile().getName().split(" ")[0];   // parentName = "388 СОШ"

        PDDocument[] files = {PDFProcessor.deleteFirstPage(reportSrc2), PDFProcessor.deleteFirstPage(reportSrc5)};
        int numberOfFloors = files[0].getNumberOfPages() / 2;

        PDDocument merged = PDFProcessor.mergeFiles(files);
        PDDocument sorted = sortPages(merged);

        addHeadings(sorted, numberOfFloors);

        PDDocument pattern = Loader.loadPDF(patternSrc);
        PDDocument result = combine(pattern, sorted);

        result.save(dir + schoolID + " РП v1.pdf");

        for(PDDocument file : files)
            file.close();
        merged.close();
        sorted.close();
        pattern.close();
        result.close();
        System.out.println(reportSrc2.delete());
        System.out.println(reportSrc5.delete());
    }

    public static void main(String[] args) {

    }




}

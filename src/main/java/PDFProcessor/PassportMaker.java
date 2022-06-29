package PDFProcessor;

import org.apache.pdfbox.Loader;
import java.io.*;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PassportMaker {
    private static final String schemeName = "S.pdf";
    private static final String EGURLName = "E.pdf";

    public static void makePassport(String dir) throws IOException {
        File EGRULSrc = new File(dir + EGURLName);
        File schemeSrc = new File(dir + schemeName);

        PDDocument photos = PDFProcessor.imagesToPDF(dir, true, "Приложение № 9", 18, 7);
        PDDocument scheme = Loader.loadPDF(schemeSrc);
        PDDocument EGURL = Loader.loadPDF(EGRULSrc);

        PDFProcessor.addTextUpCenter(EGURL, "Приложение № 10", 0, 18, 7);

        PDDocument[] documents = {scheme, photos, EGURL};
        PDDocument passport = PDFProcessor.mergeFiles(documents);

        passport.save(dir + "Паспорт.pdf");

        photos.close();
        scheme.close();
        EGURL.close();
        passport.close();
        System.out.println(EGRULSrc.delete());
    }
    public static void combinePhotos(String dir) throws IOException {
        PDDocument photos = PDFProcessor.imagesToPDF(dir, true, "Приложение № 9", 18, 7);
        photos.save(dir + "ФОТО.pdf");
        photos.close();
    };

    public static void main(String[] args) throws IOException {
        String path = "E:\\Clean\\TEST\\ФОТО.pdf";
        PDDocument document = Loader.loadPDF(new File(path));
        PDFProcessor.addTextUpCenter(document, "Приложение № 9", 0, 18, 5);
        document.save("E:\\Clean\\TEST\\ФОТО2.pdf");
        document.close();
    }

}

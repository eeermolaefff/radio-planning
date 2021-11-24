package PDFProcessor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PDF2Image {
    public static int splitPDF(String pdfFileName) throws IOException {
        String format = ".jpeg";
        PDDocument document = PDDocument.loadNonSeq(new File(pdfFileName), null);
        List<PDPage> pdPages = document.getDocumentCatalog().getAllPages();
        int pageNumber = 0;
        for (PDPage pdPage : pdPages) {
            ++pageNumber;
            BufferedImage BI = pdPage.convertToImage(BufferedImage.TYPE_BYTE_BINARY, 100);
            String dir = "./" + "Floor " + pageNumber;
            new File(dir).mkdirs();
            ImageIOUtil.writeImage(BI, dir +"/floor" + format, 100);
        };

        document.close();
        return pageNumber;
    }
    public static void main(String[] args) throws IOException {
        int k = PDF2Image.splitPDF("test.pdf");
    }
}

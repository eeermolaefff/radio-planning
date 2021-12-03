package PDFProcessor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDF2Image {
    public static int splitPDF(String pdfFileName, int dpi, String format) throws IOException {
        PDDocument document = Loader.loadPDF(new File(pdfFileName));
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        int pageNumber;
        for (pageNumber = 0; pageNumber < document.getNumberOfPages(); pageNumber++) {
            BufferedImage BI = pdfRenderer.renderImageWithDPI(pageNumber, dpi, ImageType.BINARY);

            String dir = "./" + "Floor " + (pageNumber + 1);
            new File(dir).mkdirs();

            ImageIO.write(BI, format.substring(1), new File(dir + "/floor" + format));
        };

        document.close();
        return pageNumber;

    }
    public static void main(String[] args) throws IOException {

    }
}

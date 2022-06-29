package PDFProcessor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;

public class PDFProcessor {
    private static final String fontPath = "D:\\MyPerfectApp\\src\\fonts\\arial.ttf";

    private static class Dimensions {
        public int startX, startY;
        public float factor;
        public boolean isFlipped;
        public Dimensions() {}
        public Dimensions(int startX, int startY, float factor, boolean isFlipped) {
            this.startX = startX;
            this.startY = startY;
            this.factor = factor;
            this.isFlipped = isFlipped;
        }
    }
    private static File[] listOfImages(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".jpeg") || name.endsWith(".jpg")||
                name.endsWith(".JPG") || name.endsWith(".PNG") || name.endsWith(".png"));
        return files;
    }
    private static Dimensions adjustImage(File file, float pageWidth, float pageHeight) {
        Dimensions dimensions = new Dimensions();
        javaxt.io.Image image = new javaxt.io.Image(file.getAbsolutePath());

        dimensions.isFlipped = image.getHeight() < image.getWidth();
        int offset = 40;

        if (dimensions.isFlipped) {
            dimensions.factor = Math.min((pageHeight - offset * 2) / image.getWidth(),
                    (pageWidth - offset * 2) / image.getHeight());
            dimensions.startX = (int) (pageWidth - image.getHeight() * dimensions.factor) / 2;
            dimensions.startY = (int) ((pageHeight - image.getWidth() * dimensions.factor) / 2 +
                    image.getWidth() * dimensions.factor);
        } else {
            dimensions.factor = Math.min((pageHeight - offset * 2) / image.getHeight(),
                    (pageWidth - offset * 2) / image.getWidth());
            dimensions.startX = (int) (pageWidth - image.getWidth() * dimensions.factor) / 2;
            dimensions.startY = (int) (pageHeight - image.getHeight() * dimensions.factor) / 2;
        }

        return dimensions;
    }
    private static void addHeader(PDPageContentStream contentStream, String header, PDFont font,
                                  int fontSize, int offsetX, int offsetY) throws IOException {
        contentStream.beginText();

        contentStream.setStrokingColor(0,0,0);
        contentStream.newLineAtOffset(offsetX, offsetY);
        contentStream.setFont(font, fontSize);
        contentStream.showText(header);

        contentStream.endText();
    }
    public static PDDocument imagesToPDF(String dir, boolean deleteImages, String header, int fontSize,
                                         int marginTop) throws IOException {
        File[] images = listOfImages(dir);
        PDDocument photos = new PDDocument();

        for (int i = 0; i < images.length; i++) {
            String imgPath = images[i].getAbsolutePath();
            System.out.println(imgPath);

            photos.addPage(new PDPage(PDRectangle.A4));
            PDPage page = photos.getPage(i);

            PDImageXObject pdImage = PDImageXObject.createFromFile(imgPath, photos);
            PDPageContentStream contentStream = new PDPageContentStream(photos, page);

            if (i == 0) {
                PDFont font = PDType0Font.load(photos, new File(fontPath));
                float txtWidth = font.getStringWidth(header) / 1000 * fontSize;
                float txtHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

                int offsetX = (int) (page.getMediaBox().getWidth() -  txtWidth)/ 2;
                int offsetY = (int) (page.getMediaBox().getHeight() - txtHeight - marginTop);

                addHeader(contentStream, header, font, fontSize, offsetX, offsetY);
            }

            Dimensions imageDims = adjustImage(images[i], page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            Matrix transformation = new Matrix();
            transformation.translate(imageDims.startX, imageDims.startY);

            if (imageDims.isFlipped) transformation.rotate(Math.toRadians(-90));

            transformation.scale(imageDims.factor, imageDims.factor);
            contentStream.transform(transformation);
            contentStream.drawImage(pdImage, 0, 0);
            contentStream.close();
        }

        if (deleteImages)
            for (File image : images)
                image.delete();

        return photos;
    }
    public static PDDocument imagesToPDF(String dir, boolean deleteImages) throws IOException {
        File[] images = listOfImages(dir);
        PDDocument photos = new PDDocument();

        for (int i = 0; i < images.length; i++) {
            String imgPath = images[i].getAbsolutePath();
            System.out.println(imgPath);

            photos.addPage(new PDPage(PDRectangle.A4));
            PDPage page = photos.getPage(i);

            PDImageXObject pdImage = PDImageXObject.createFromFile(imgPath, photos);
            PDPageContentStream contentStream = new PDPageContentStream(photos, page);

            Dimensions imageDims = adjustImage(images[i], page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            Matrix transform = new Matrix();
            transform.translate(imageDims.startX, imageDims.startY);

            if (imageDims.isFlipped) transform.rotate(Math.toRadians(-90));

            transform.scale(imageDims.factor, imageDims.factor);
            contentStream.transform(transform);
            contentStream.drawImage(pdImage, 0, 0);
            contentStream.close();
        }

        if (deleteImages)
            for (File image : images)
                image.delete();

        return photos;
    }
    public static PDDocument deleteFirstPage(File file) throws IOException {
        PDDocument document = Loader.loadPDF(file);
        document.removePage(0);
        return document;
    }
    public static PDDocument mergeFiles(PDDocument[] documents) throws IOException {
        PDDocument merged = new PDDocument();
        PDFMergerUtility ut = new PDFMergerUtility();
        for (PDDocument document : documents)
            ut.appendDocument(merged, document);
        return merged;
    }
    public static void addTextUpCenter(PDDocument document, String txt, int pageNum, int fontSize,
                                       int marginTop) throws IOException {
        PDPage page =  document.getPages().get(pageNum);
        PDPageContentStream contentStream = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, false);

        contentStream.beginText();

        contentStream.setStrokingColor(0,0,0);

        PDFont font = PDType0Font.load(document, new File(fontPath));
        float txtWidth = font.getStringWidth(txt) / 1000 * fontSize;
        float txtHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        contentStream.newLineAtOffset((page.getMediaBox().getWidth() -  txtWidth)/ 2,
                page.getMediaBox().getHeight() - txtHeight - marginTop);
        contentStream.setFont(font, fontSize);
        contentStream.showText(txt);

        contentStream.endText();
        contentStream.close();
    }
    public static void addTextWithBottomMargin(PDDocument document, String txt, int pageNum, int fontSize,
                                               int marginLeft, int marginBottom) throws IOException {
        PDPage page =  document.getPages().get(pageNum);
        PDPageContentStream contentStream = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, false);

        contentStream.beginText();

        contentStream.setStrokingColor(0,0,0);

        PDFont font = PDType0Font.load(document, new File(fontPath));

        contentStream.newLineAtOffset(marginLeft, marginBottom);
        contentStream.setFont(font, fontSize);
        contentStream.showText(txt);

        contentStream.endText();
        contentStream.close();
    }
    public static void addTextWithTopMargin(PDDocument document, String txt, int pageNum, int fontSize,
                                            int marginLeft, int marginTop) throws IOException {
        PDPage page =  document.getPages().get(pageNum);
        PDPageContentStream contentStream = new PDPageContentStream(document, page,
                PDPageContentStream.AppendMode.APPEND, false);

        contentStream.beginText();

        contentStream.setStrokingColor(0,0,0);

        PDFont font = PDType0Font.load(document, new File(fontPath));
        float txtHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        contentStream.newLineAtOffset(marginLeft, page.getMediaBox().getHeight() - txtHeight - marginTop);
        contentStream.setFont(font, fontSize);
        contentStream.showText(txt);

        contentStream.endText();
        contentStream.close();
    }
}

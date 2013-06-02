import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageComparison {
    File originalFile;
    File modifiedFile;
    BufferedImage originalImage;
    BufferedImage modifiedImage;
    Object colorData;
    ColorModel model;
    final Color colorForDifferences = Color.RED;
    int colorTolerance;
    public static final int thresholdOfDifferentObjectsPixelSize = 15;
    public static final int aroundObjectPixelSize = 45;

    public ImageComparison(File originalFile, File modifiedFile, int colorTolerance) {
        this.originalFile = originalFile;
        this.modifiedFile = modifiedFile;
        this.colorTolerance = colorTolerance;
        constructOriginalAndModifiedImage(originalFile, modifiedFile);
        this.model = originalImage.getColorModel();
    }

    private void constructOriginalAndModifiedImage(File originalFile, File modifiedFile) {
        try {
            originalImage = ImageIO.read(originalFile);
            originalImage.getType();

            modifiedImage = ImageIO.read(modifiedFile);
            modifiedImage.getType();
        } catch (IOException e) {
            System.out.println("There was an error with the image, please try again");
            e.printStackTrace();
        }
    }

    public void compareImages() {
        BufferedImage resultImage =
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster resultRaster = resultImage.getRaster();

        ArrayList<Integer> maxWidthDifference = new ArrayList<Integer>();
        ArrayList<Integer> maxHeightDifference = new ArrayList<Integer>();

        for (int widthPixel = 0; widthPixel < originalImage.getWidth(); widthPixel++) {
            for (int heightPixel = 0; heightPixel < originalImage.getHeight(); heightPixel++) {
                analyzePixel(resultRaster, maxWidthDifference, maxHeightDifference, widthPixel, heightPixel);
            }
        }
        processObjectsDetection(maxWidthDifference, maxHeightDifference, resultRaster);
        createOutputImage(resultImage);
    }

    private void analyzePixel(WritableRaster resultRaster,
                              ArrayList<Integer> maxWidthDifference,
                              ArrayList<Integer> maxHeightDifference,
                              int widthPixel,
                              int heightPixel) {
        if (Math.abs(originalImage.getRGB(widthPixel, heightPixel) - modifiedImage.getRGB(widthPixel, heightPixel)) > 256 * colorTolerance / 100) {
            colorData = model.getDataElements(colorForDifferences.getRGB(), null);

            maxWidthDifference.add(widthPixel);
            maxHeightDifference.add(heightPixel);
        } else {
            colorData = model.getDataElements(originalImage.getRGB(widthPixel, heightPixel), null);
        }
        resultRaster.setDataElements(widthPixel, heightPixel, colorData);
    }

    private void processObjectsDetection(ArrayList<Integer> maxWidthDiff,
                                         ArrayList<Integer> maxHeightDiff,
                                         WritableRaster resultRaster) {
        ArrayList<Integer> avgHPoint = detectHorizontalCenterPixelsForObjects(maxWidthDiff);
        ArrayList<Integer> avgVPoint = detectVerticalCenterPixelsForObjects(maxHeightDiff);

        drawRectangularAroundObjects(resultRaster, avgHPoint, avgVPoint);
    }

    private void drawRectangularAroundObjects(WritableRaster resultRaster, ArrayList<Integer> avgHPoint, ArrayList<Integer> avgVPoint) {
        colorData = model.getDataElements(colorForDifferences.getRGB(), null);
        for (int numberOfObjects = 0; numberOfObjects < avgHPoint.size(); numberOfObjects++) {
            for (int i = 0; i < aroundObjectPixelSize; i++) {
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) + i, avgVPoint.get(numberOfObjects) + aroundObjectPixelSize, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) - i, avgVPoint.get(numberOfObjects) + aroundObjectPixelSize, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) + i, avgVPoint.get(numberOfObjects) - aroundObjectPixelSize, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) - i, avgVPoint.get(numberOfObjects) - aroundObjectPixelSize, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) + aroundObjectPixelSize, avgVPoint.get(numberOfObjects) + i, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) + aroundObjectPixelSize, avgVPoint.get(numberOfObjects) - i, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) - aroundObjectPixelSize, avgVPoint.get(numberOfObjects) + i, colorData);
                resultRaster.setDataElements(avgHPoint.get(numberOfObjects) - aroundObjectPixelSize, avgVPoint.get(numberOfObjects) - i, colorData);
            }
        }
    }

    private ArrayList<Integer> detectHorizontalCenterPixelsForObjects(ArrayList<Integer> maxWidthDifference) {
        int hSum = 0;
        int previousHIndex = 0;
        ArrayList<Integer> averageHPoint = new ArrayList<>();

        for (int hIndex = 1; hIndex < maxWidthDifference.size(); hIndex++) {

            if (Math.abs((maxWidthDifference.get(hIndex) - maxWidthDifference.get(hIndex - 1))) < thresholdOfDifferentObjectsPixelSize &&
                                                                                     hIndex + 1 != maxWidthDifference.size()) {
                hSum += maxWidthDifference.get(hIndex);
            } else {
                averageHPoint.add(hSum / (hIndex - previousHIndex));
                hSum = 0;
                previousHIndex = hIndex;
            }
        }
        return averageHPoint;
    }

    private ArrayList<Integer> detectVerticalCenterPixelsForObjects(ArrayList<Integer> maxHeightDifference) {
        int vSum = 0;
        int previousVIndex = 0;
        ArrayList<Integer> averageVPoint = new ArrayList<>();

        for (int vIndex = 1; vIndex < maxHeightDifference.size(); vIndex++) {
            if (Math.abs((maxHeightDifference.get(vIndex) - maxHeightDifference.get(vIndex - 1))) < thresholdOfDifferentObjectsPixelSize &&
                                                                                       vIndex + 1 != maxHeightDifference.size()) {
                vSum += maxHeightDifference.get(vIndex);
            } else {
                averageVPoint.add(vSum / (vIndex - previousVIndex));
                vSum = 0;
                previousVIndex = vIndex;
            }
        }
        return averageVPoint;
    }

    private void createOutputImage(BufferedImage resultImage) {
        try {
            ImageIO.write(resultImage, "png", new File("third.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
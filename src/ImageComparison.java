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
    public static final int rectangularAroundObjectPixelSize = 45;

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

    private void analyzePixel(WritableRaster resultRaster, ArrayList<Integer> maxWidthDifference, ArrayList<Integer> maxHeightDifference, int widthPixel, int heightPixel) {
        if (Math.abs(originalImage.getRGB(widthPixel, heightPixel) - modifiedImage.getRGB(widthPixel, heightPixel)) > 256 * colorTolerance / 100) {
            colorData = model.getDataElements(colorForDifferences.getRGB(), null);

            maxWidthDifference.add(widthPixel);
            maxHeightDifference.add(heightPixel);
        } else {
            colorData = model.getDataElements(originalImage.getRGB(widthPixel, heightPixel), null);
        }
        resultRaster.setDataElements(widthPixel, heightPixel, colorData);
    }

    private void processObjectsDetection(ArrayList<Integer> maxWidthDifference, ArrayList<Integer> maxHeightDifference, WritableRaster resultRaster) {
        ArrayList<Integer> averageHorizontalPoint = detectHorizontalCenterPixelsForObjects(maxWidthDifference);
        ArrayList<Integer> averageVerticalPoint = detectVerticalCenterPixelsForObjects(maxHeightDifference);

        drawRectangulartAroundObjects(resultRaster, averageHorizontalPoint, averageVerticalPoint);
    }

    private void drawRectangulartAroundObjects(WritableRaster resultRaster, ArrayList<Integer> averageHorizontalPoint, ArrayList<Integer> averageVerticalPoint) {
        colorData = model.getDataElements(colorForDifferences.getRGB(), null);
        for (int numberOfObjects = 0; numberOfObjects < averageHorizontalPoint.size(); numberOfObjects++) {
            for (int i = 0; i < rectangularAroundObjectPixelSize; i++) {
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) + i, averageVerticalPoint.get(numberOfObjects) + rectangularAroundObjectPixelSize, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) - i, averageVerticalPoint.get(numberOfObjects) + rectangularAroundObjectPixelSize, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) + i, averageVerticalPoint.get(numberOfObjects) - rectangularAroundObjectPixelSize, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) - i, averageVerticalPoint.get(numberOfObjects) - rectangularAroundObjectPixelSize, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) + rectangularAroundObjectPixelSize, averageVerticalPoint.get(numberOfObjects) + i, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) + rectangularAroundObjectPixelSize, averageVerticalPoint.get(numberOfObjects) - i, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) - rectangularAroundObjectPixelSize, averageVerticalPoint.get(numberOfObjects) + i, colorData);
                resultRaster.setDataElements(averageHorizontalPoint.get(numberOfObjects) - rectangularAroundObjectPixelSize, averageVerticalPoint.get(numberOfObjects) - i, colorData);
            }
        }
    }

    private ArrayList<Integer> detectHorizontalCenterPixelsForObjects(ArrayList<Integer> maxWidthDifference) {
        int horizontalSum = 0;
        int previousHorizontalIndex = 0;
        ArrayList<Integer> averageHorizontalPoint = new ArrayList<>();

        for (int horizontalIndex = 1; horizontalIndex < maxWidthDifference.size(); horizontalIndex++) {

            if (Math.abs((maxWidthDifference.get(horizontalIndex) - maxWidthDifference.get(horizontalIndex - 1))) < thresholdOfDifferentObjectsPixelSize && horizontalIndex + 1 != maxWidthDifference.size()) {
                horizontalSum += maxWidthDifference.get(horizontalIndex);
            } else {
                if (previousHorizontalIndex == 0) {
                    averageHorizontalPoint.add(horizontalSum / (horizontalIndex));

                } else {

                    averageHorizontalPoint.add(horizontalSum / (horizontalIndex - previousHorizontalIndex));
                }
                horizontalSum = 0;
                previousHorizontalIndex = horizontalIndex;
            }
        }
        return averageHorizontalPoint;
    }

    private ArrayList<Integer> detectVerticalCenterPixelsForObjects(ArrayList<Integer> maxHeightDifference) {
        int verticalSum = 0;
        int previousVerticalIndex = 0;
        ArrayList<Integer> averageVerticalPoint = new ArrayList<>();

        for (int verticalIndex = 1; verticalIndex < maxHeightDifference.size(); verticalIndex++) {
            if (Math.abs((maxHeightDifference.get(verticalIndex) - maxHeightDifference.get(verticalIndex - 1))) < thresholdOfDifferentObjectsPixelSize && verticalIndex + 1 != maxHeightDifference.size()) {
                verticalSum += maxHeightDifference.get(verticalIndex);
            } else {
                if (previousVerticalIndex == 0) {
                    averageVerticalPoint.add(verticalSum / (verticalIndex));
                } else {
                    averageVerticalPoint.add(verticalSum / (verticalIndex - previousVerticalIndex));
                }
                verticalSum = 0;
                previousVerticalIndex = verticalIndex;
            }
        }
        return averageVerticalPoint;
    }

    private void createOutputImage(BufferedImage resultImage) {
        try {
            ImageIO.write(resultImage, "png", new File("third.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
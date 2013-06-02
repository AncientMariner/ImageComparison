import java.io.File;

public class ImageProcessing {
    public static void main(String [] args){
        System.out.println("\n This is image comparison tool, please use the following syntax to work with it:\n" +
                "    <toolname> <first image> <second image> <colorTolerance threshold>");

//        System.out.println(args[0]);

        File originalFile = new File(args[0]);
        File modifiedFile = new File(args[1]);
        int tolerance = Integer.parseInt(args[2]);

        ImageComparison imageComparison = new ImageComparison(originalFile, modifiedFile, tolerance);
        imageComparison.compareImages();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package documentrecognition;

import static documentrecognition.Svm_train.svmTrain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;
import libsvm.svm;
import libsvm.svm_model;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
/**
 *
 * @author mudig
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        //Denoising
       // Loading the OpenCV core library
       System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
       // Reading the Image from the file and storing it in to a Matrix object
       String file ="C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Pictures\\tech.png";
       Mat src = Imgcodecs.imread(file);
       // Creating an empty matrix to store the result
       Mat dst = new Mat();
       // Applying Bilateral filter on the Image
       Imgproc.bilateralFilter(src, dst, 15, 80, 80, Core.BORDER_DEFAULT);
       // Writing the image
       //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Pictures\\
       Imgcodecs.imwrite("sport_denoised.jpg", dst);
       System.out.println("Image Processed");
       
       
       //Image Sharpening
       try{
           System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
           //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Pictures\\
           Mat source = Imgcodecs.imread("sport_denoised.jpg",
           Imgcodecs.CV_LOAD_IMAGE_COLOR);
           Mat destination = new Mat(source.rows(),source.cols(),source.type());
           Imgproc.GaussianBlur(source, destination, new Size(0,0), 10);
           Core.addWeighted(source, 1.5, destination, -0.5, 0, destination);
           //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Pictures\\
           Imgcodecs.imwrite("sport_sharpen.jpg", destination);
       }catch (Exception e) {
       }
       
       
       //Extracting text
       ExtractText app = new ExtractText();
       //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\
       PrintStream myconsole = new PrintStream(new File("sports.txt"));
       System.setOut(myconsole);
       //"C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Pictures\\
       myconsole.println(app.getImgText("sport_sharpen.jpg"));
       
       
       
       
       //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\
       //Bag of Word for the extracted text
       Path fileDir=Paths.get("sports.txt"); //("UTF-8");
       MyFileVisitor visitor=new MyFileVisitor();
       Files.walkFileTree(fileDir, visitor);
       
       
       //Testing with SVM
       Path fileDir2=Paths.get("bbc"); //("UTF-8");
       MyFileVisitor2 visitor2=new MyFileVisitor2();
       Files.walkFileTree(fileDir2, visitor2);
       
       Svm_train svm_obj = new Svm_train();
       boolean retrain = false;
            svm_model m;
            //if(!retrain)
                m = svm.svm_load_model("tam_model");
                double[][] xtrain = new double[2000][4157];
                double[] ytrain = new double[2000];
                double[][] xtest = new double[1][4157];
                double[] ytest = new double[1];
                
                
                PrintStream consoleStream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        System.setOut(consoleStream);
        
        
                //Read the features file to xtrain, and assign the class label ytrain
                System.out.println("Training data");
                int count = 0;
                int []number_of_files_in_each_category = {510 - 45,386-45,417-45,511-45,401-45};
                for(int i = 1;i<=5;i++){
                    for (int j = 1;j<=number_of_files_in_each_category[i-1];j++)
                    {
                        System.out.println(count);
                        StringBuilder sbuf = new StringBuilder();
                        Formatter fmt = new Formatter(sbuf);
                        fmt.format("%03d", j);
                        String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        File file2 = new File(filename);
                        FileReader fileReader = new FileReader(file2);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                                xtrain[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                        }
                        fileReader.close();
                        ytrain[count] = i;
                        count++;

                }


                }

                System.out.println("Testing data");
                count = 0;
                //for(int i = 1;i<=5;i++)
                //{
                  //  for (int j = number_of_files_in_each_category[i-1]+1;j<=number_of_files_in_each_category[i-1]+45;j++)
                  //  {
                        System.out.println(count);

                        //StringBuilder sbuf = new StringBuilder();
                        //Formatter fmt = new Formatter(sbuf);
                        //fmt.format("%03d", j);
                        
                        //String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        
                        //C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\
                        String filename = "sports.txt.bow";
                       
                        System.out.println(filename);
                        File file1 = new File(filename);
                        FileReader fileReader = new FileReader(file1);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                               xtest[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                            if(line_id == 4158)
                                break;
                        }
                        fileReader.close();
                        //ytest[count] = i;
                        count++;
                    //}

                //}

                //Normalize the features for training data
                for(int i = 0; i<2000; i++) 
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtrain[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {
                        xtrain[i][j] /= sum;
                    }
                }

                //Normalize the features for testing data
                for(int i = 0; i<1; i++)
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtest[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {                        
                        xtest[i][j] /= sum;
                    }
                }
                
            if(retrain){
                m = svmTrain(xtrain,ytrain);
                svm.svm_save_model("tam_model",m);
            }
            double[] ypred = svm_obj.svmPredict(xtest, m); 
            String [] labels = {"business","entertainment","politics","sports","tech"};
      for (int i = 0; i < xtest.length; i++){
           
      System.out.println(" Prediction:" + ypred[i] + " "  + labels[(int)(ypred[i])-1] );
      }  

    
    }
}

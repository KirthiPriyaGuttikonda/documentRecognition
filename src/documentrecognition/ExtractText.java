/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package documentrecognition;

import net.sourceforge.tess4j.*;
import java.io.*;

public class ExtractText {
    public String getImgText(String imageLocation) {
      ITesseract instance = new Tesseract();
      try 
      {
         String imgText = instance.doOCR(new File(imageLocation));
         return imgText;
      } 
      catch (TesseractException e) 
      {
         e.getMessage();
         return "Error while reading image";
      }
   }
   
}
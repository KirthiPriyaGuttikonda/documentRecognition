
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package documentrecognition;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
//import java.util.Arrays;

/**
 *
 * @author gkirt
 */
public class MyFileVisitor extends SimpleFileVisitor<Path> {
   @Override
   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
       System.out.println("About to visit: "+dir);
       return FileVisitResult.CONTINUE;
   } 
   @Override
   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
       System.out.println("Just visited: "+dir);
       return FileVisitResult.CONTINUE;
   }
   @Override
   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
       
       String filename = file + ".bow";
        
       PrintStream myconsole = new PrintStream(new File(filename));
       System.setOut(myconsole);
       if(attrs.isRegularFile())
       {
           System.out.print("Regular File:");
           
       }
       System.out.println(file);
       
       HashMap<String, Integer> wordcount = new HashMap<String, Integer>();
       String str;
       
       BufferedReader bufferedReader = Files.newBufferedReader(file, Charset.forName("ISO-8859-1"));
      //  String line = bufferedReader.readLine();
        //String stopWords[]={"and","the","a", "if","of","that","its","as","for","at","it","has","had","to","from","which","in","by","were","but","in","on","when"};
        
       try { 
        while ((str = bufferedReader.readLine()) != null) {
            
            str= str.toLowerCase();
            int idx1 = -1;
            for (int i = 0; i < str.length(); i++) { 
                if ((!Character.isLetterOrDigit(str.charAt(i))) || (i + 1 == str.length())) {
                    if (i - idx1 > 1) { 
                        if (Character.isLetter(str.charAt(i))) 
                                i++;
                        String word = str.substring(idx1 + 1, i);
                        if (wordcount.containsKey(word)) { 
                            wordcount.put(word, wordcount.get(word) + 1);
                            } else { 
                            wordcount.put(word, 1);
                            } 
                        }
                    idx1 = i;
                    }
                    
                }
        } 
        } catch (Exception e) { 
            e.printStackTrace();
            System.exit(1);
        } 
        ArrayList<Integer> values = new ArrayList<Integer>();
        // String stopWords[]={"and","the","a", "if", "an", "of", "to"};
        values.addAll(wordcount.values());
        // and sorting it (in reverse order) 
        Collections.sort(values, Collections.reverseOrder());
        
        
        
        FileReader fr = new FileReader("dictionary.txt");
        BufferedReader br = new BufferedReader(fr);
        String temp;
        ArrayList<String> content = new ArrayList<>();
        while((temp = br.readLine()) != null)
        {
            Scanner scan = new Scanner(temp);
            while(scan.hasNext())
            {
                content.add(scan.nextLine());
            }
        }
        
        String[] dictArray = new String[content.size()];
        dictArray = content.toArray(dictArray);
        
        int[] freq = new int[dictArray.length];
        
        
        
        
        int last_i = -1;
        // Now, for each value  
        for (Integer i : values) { 
            if (last_i == i) // without dublicates  
                continue;
            last_i = i;
            // we print all hash keys  
            for (String s : wordcount.keySet()) { 
                if (wordcount.get(s) == i) // which have this value  
                    
                    for(int j = 0; j<dictArray.length;j++)
                    {
                        if(s.equalsIgnoreCase(dictArray[j]))
                        {
                            freq[j]=i;
                        }
                    }
                
            } 
            // pretty inefficient, but works  
        } 
        
        for(int k =0; k<dictArray.length;k++)
        {
            myconsole.println(freq[k]);
        }
            //str = bufferedReader.readLine();
       // }
         
       
       return FileVisitResult.CONTINUE;
   }
   @Override
   public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
       System.out.println(exc.getMessage());
        return FileVisitResult.CONTINUE;
    }
}


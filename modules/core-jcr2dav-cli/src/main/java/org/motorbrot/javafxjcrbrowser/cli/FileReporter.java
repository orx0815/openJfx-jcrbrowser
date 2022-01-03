package org.motorbrot.javafxjcrbrowser.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.*;

/**
 * Report the results to file
 */
public class FileReporter {
  
  private static final Logger LOG = Logger.getLogger(FileReporter.class.getName());
  
  public static File toFile(JcrMigrationResult result, String serverUrl, JcrMigrationParameters parameters, String log) {
    
    try {
      File tempFileDirectory = new File("./content-update-reports");
      FileUtils.forceMkdir(tempFileDirectory);
      
      Date date = new Date() ;
      SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") ;
      String name = parameters.jobName 
              + "_" + fileNameDateFormat.format(date) 
              + "_" + result.getStatus().toUpperCase() + "_";
      String filePrefix = sanitizeFilename(name);
      File file = File.createTempFile(filePrefix, ".txt", tempFileDirectory);
      
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm:ss") ;
      
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      try (PrintWriter pw = new PrintWriter(bw)) {
        if (JcrMigrationResult.STARTED_STATE.equals(result.getStatus()) ){
          pw.append("\n\n=== preliminary Jcr Migration Result Report ===\n");
        } else {
          pw.append("\n\n=== Jcr Migration Result Report ===\n");
        }
        
        pw.append("\n")
          .append("Name:      " + parameters.jobName + "\n")
          .append("on server: " + serverUrl + "\n")         
          .append("changed    " + result.getNumberOfChanges() + " nodes.\n\n")  
                
          .append("Started: " + dateFormat.format(result.getStartDate()) + "\n")
          .append("Ended:   " + dateFormat.format(new Date()) + "\n")
          .append("with status: " + result.getStatus() + "\n\n")
          .append("Jcr rootpath: " + result.getPath() + "\n");
        
        pw.append("_______________________________________________________________\n")
          .append("\n");

        pw.append(log);
        
      }
      
      return file;
    }
    catch (IOException ex) {
      LOG.log(Level.SEVERE, "Couldn't write report file.", ex);
    }
    
    return null;
  }
  
  
  private static String sanitizeFilename(String inputName) {
    return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
  }
  
}

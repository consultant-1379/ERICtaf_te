package com.ericsson.cifwk.taf.executor.cleanup;

import com.ericsson.cifwk.taf.executor.TAFExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.StringReader;
import com.google.common.io.ByteStreams;



/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 25/01/2017
 */
public class OldFileFilter implements FilenameFilter {
     private static final Logger LOGGER = Logger.getLogger(OldFileFilter.class.getName());
    private final int maxLifeTimeInHours;

    public OldFileFilter(int maxLifeTimeInHours) {
        this.maxLifeTimeInHours = maxLifeTimeInHours;
    }

    public boolean accept(File dir, String fileName) {
        if (!isTestRunWorkspace(dir) ) {
            File target = new File(dir, fileName);
            return isOld(target);
        }
        else
            return false;
    }

    protected boolean isTestRunWorkspace(File dir) {
        return dir.getName().equals(TAFExecutor.TEST_RUN_WORKSPACE_SUBDIR);
    }

    /**
     * Delete tmp/te_maven_runs/**-0 directory only after job gets finished.
     * @param dir
     * @return
     */
    protected boolean isFinishexists(File dir) {
        String OS = System.getProperty("os.name");
        String path;
        StringBuffer output = new StringBuffer();
        String delims = "//";
        path = dir.getAbsolutePath();
        String[] tokens = path.split(delims);
        LOGGER.log(Level.INFO," Looking for xml.finish inside " + path );
        Process p;
           try {
               String command = "";
               if(OS.contains("Windows"))
                   command = "dir " + path + " *.xml.finish";
               else
                   command = "find " + path + " -name *.xml.finish";
               p = Runtime.getRuntime().exec(command);
               p.waitFor();
               String stdOut = new String(ByteStreams.toByteArray(p.getInputStream()));
               BufferedReader bufReader = new BufferedReader(new StringReader(stdOut));
               String line = null;
               while ((line = bufReader.readLine())!= null) {
                   output.append(line + "\n");
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
           if(output.toString().contains("xml.finish") ){
               return true;
           }
           else {
               // if Xml.finish not found and last modified more than 5*15 hours remove the test maven dir
               LOGGER.log(Level.INFO, "Job not finished, " + path + " shall be deleted if lastmodified time is > " + 5*maxLifeTimeInHours + "hours");
               return (System.currentTimeMillis() - dir.lastModified() > 5*maxLifeTimeInHours * 3600 * 1000 );

           }
    }

    protected boolean isOld(File target) {
             return (System.currentTimeMillis() - target.lastModified() > maxLifeTimeInHours * 3600 * 1000 );
    }


}

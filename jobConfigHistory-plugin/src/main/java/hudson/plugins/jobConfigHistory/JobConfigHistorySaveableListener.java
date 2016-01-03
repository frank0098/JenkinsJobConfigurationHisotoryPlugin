/*
 * The MIT License
 *
 * Copyright 2013 Stefan Brausch.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.jobConfigHistory;

import static hudson.init.InitMilestone.COMPLETED;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import static java.util.logging.Level.FINEST;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Hudson;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Saves the job configuration at {@link SaveableListener#onChange(Saveable, XmlFile)}.
 *
 * @author Stefan Brausch
 */
@Extension
public class JobConfigHistorySaveableListener extends SaveableListener {

    /** Our logger. */
    private static final Logger LOG = Logger.getLogger(JobConfigHistorySaveableListener.class.getName());
    

    /** {@inheritDoc} */
    @Override
    public void onChange(final Saveable o, final XmlFile file) {
        final JobConfigHistory plugin = getPlugin();
        LOG.log(FINEST, "In onChange for {0}", o);
        if (plugin.isSaveable(o, file)) {
            final HistoryDao configHistoryListenerHelper = getHistoryDao(plugin);
            configHistoryListenerHelper.saveItem(file);
            
            if(file!=null &&file.getFile().getName().equals("config.xml"))
            {
            //info provided by system
        	File jobConfiglibfolder = new File(System.getenv("HOME")+"/jobConfiglib");
            if(!jobConfiglibfolder.exists())
            	jobConfiglibfolder.mkdir();
                
            String username = this.getPlugin().getScmRepoUser();
            String password = this.getPlugin().getScmRepoPass();
            String inputurl = this.getPlugin().getScmRepoUrl();
            
            //svn info
            if (inputurl == null) {
                inputurl = "none";
            }
            String svnurl = inputurl+"_jobConfigHistory_"+System.getenv("LOGNAME");
		    SVNService newsvn = new SVNService(username,password,svnurl);//authenicate the svn server
		    
            //create a folder local to store the jobconfig info
            String repodir = System.getenv("HOME")+"/jobConfiglib";
            String srcdir = System.getenv("HOME")+"/.jenkins/config-history/jobs";
           

            newsvn.sync_repo(svnurl, repodir);
            
            File srcFolder = new File(srcdir);
            File destFolder = new File(repodir);
            
//            
//            if(!destFolder.exists())
//            {
//            	newsvn.commitFiletoSvn(repodir, svnurl); //commit file to svn
//            	newsvn.checkoutfromSvn(svnurl, repodir);
//            }
//            else
//            	newsvn.sync_repo(svnurl, repodir);
            
            //copy all the files into repo foler
            if(!srcFolder.exists()){

             }else{

                try{
             	copyFolder(srcFolder,destFolder);
                }catch(IOException e){
             	e.printStackTrace();
                }
             }
            
            newsvn.commitFiletoSvn(repodir, svnurl); //commit file to svn
           
                //add 'changed' to a new file
                String filename = "record.txt";
                try{
                	FileWriter fileWriter = new FileWriter(filename);
                	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                	bufferedWriter.write("changed\n");
                	bufferedWriter.close();
                }catch(Exception e){
                	
                }
            }
            
        }
        LOG.log(FINEST, "onChange for {0} done.", o);
    }
    /**  
     * Copy file from one location to another.
     * @param src file to copy from
     * @param dest file to copy to
     */
    public static void copyFolder(File src, File dest)
        	throws IOException{
        	
        	if(src.isDirectory()){
        		
        		//if directory not exists, create it
        		if(!dest.exists()){
        		   dest.mkdir();
        		}
        		
        		//list all the directory contents
        		String files[] = src.list();
        		
        		for (String file : files) {
        		   //construct the src and dest file structure
        		   File srcFile = new File(src, file);
        		   File destFile = new File(dest, file);
        		   //recursive copy
        		   copyFolder(srcFile,destFile);
        		}
        	   
        	}else{
        		//if file, then copy it
        		//Use bytes stream to support all file types
        		if(!dest.exists())
        		{
        		InputStream in = new FileInputStream(src);
        	        OutputStream out = new FileOutputStream(dest); 
        	                     
        	        byte[] buffer = new byte[1024];
        	    
        	        int length;
        	        //copy the file content in bytes 
        	        while ((length = in.read(buffer)) > 0){
        	    	   out.write(buffer, 0, length);
        	        }
     
        	        in.close();
        	        out.close();
        	    }
        	}
        }

    /**
     * For tests only.
     *
     * @return plugin
     */
    JobConfigHistory getPlugin() {
        return PluginUtils.getPlugin();
    }

    /**
     * For tests only.
     *
     * @return helper.
     */
    @Deprecated
    HistoryDao getHistoryDao() {
        return getHistoryDao(PluginUtils.getPlugin());
    }

    /**
     * Return the helper, making sure its anonymous while Jenkins is still
     * initializing.
     * @return helper
     */
    HistoryDao getHistoryDao(JobConfigHistory plugin) {
        return (COMPLETED == Hudson.getInstance().getInitLevel())
                ? PluginUtils.getHistoryDao(plugin)
                : PluginUtils.getAnonymousHistoryDao(plugin);
    }
}

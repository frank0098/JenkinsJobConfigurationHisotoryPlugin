package hudson.plugins.jobConfigHistory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

import org.apache.log4j.Logger;  
import org.tmatesoft.svn.core.SVNCommitInfo;  
import org.tmatesoft.svn.core.SVNDepth;  
import org.tmatesoft.svn.core.SVNException;  
import org.tmatesoft.svn.core.SVNNodeKind;  
import org.tmatesoft.svn.core.SVNURL;  
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;  
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;  
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;  
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;  
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;  
import org.tmatesoft.svn.core.io.SVNRepository;  
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;  
import org.tmatesoft.svn.core.wc.SVNClientManager;  
import org.tmatesoft.svn.core.wc.SVNRevision;  
import org.tmatesoft.svn.core.wc.SVNStatus;  
import org.tmatesoft.svn.core.wc.SVNUpdateClient;  
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCommit;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRemoteDelete;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.core.wc.CommitEventHandler;
import hudson.model.Project;


/**
 * Implements the setup to sync config files to a svn repo.
 *
 * @author 
 */
public class SVNService{
	private Logger logger = Logger.getLogger(SVNService.class);  
    private String username = null;  
    private String password = null; 
    private String repourl = null; //assume this will not change, // need another method to handle for changing
    
    /**  
     * Setting up svn repo details
     * @param username of svn repo
     * @param password of svn repo
     * @param  svn repo to sync config files to
     */
    public SVNService(String username,String password, String repourl)
    {
    	this.username = username;
    	this.password = password;
    	this.repourl = repourl;
    }
    
    /**  
     * given a url and a folder path, if url does not exist, 
     * create the url,import. If current dir exists, svn up
     * if not, checkout the repo
     * @param svnUrl repo to sync with
     * @param filepath of file that needs to be synced into
     * @return true if successful 
     */
    public boolean sync_repo(String svnurl, String file_path)
    {
    	SVNClientManager clientManager = xxxx(svnurl);
        SVNURL repositoryURL = null;  
        try {  
            repositoryURL = SVNURL.parseURIEncoded(svnurl);
        } catch (SVNException e) {  
            logger.error(e.getMessage(),e);  
            return false;  
        }  
        File cr = new File(file_path);
        
        
    	if(!SVNUtil.isURLExist(repositoryURL,username,password))
    	{
    		
    		String commitMessage = "Created job config history folder.";
    		SVNUtil.makeDirectory(clientManager, repositoryURL, commitMessage);
    		
    	}
    	File file = new File(file_path);
    	//if(!file.exists())
    		checkoutfromSvn(svnurl,file_path);
//    	else
//    		SVNUtil.update(clientManager, cr, SVNRevision.HEAD, SVNDepth.INFINITY);  
    		
    	return true;
    }

	private SVNClientManager xxxx(String svnurl) {
		SVNClientManager clientManager = initialSVNManager(svnurl);
		return clientManager;
	}

	private SVNClientManager initialSVNManager(String svnurl) {
		SVNClientManager clientManager = SVNUtil.authSvn(svnurl, username, password);  
        clientManager.getCommitClient().setEventHandler(new CommitEventHandler());
		return clientManager;
	}
    
    /**  
     * checkout file from svn
     * @param svnUrl to checkout file from
     * @param filepath of where new file will be saved
     * @return true if successful 
     */
    public boolean checkoutfromSvn(String svnurl, String file_path)
    {
    	SVNClientManager clientManager = xxxx(svnurl);
    	 SVNURL repositoryURL = null;  
         try {  
             repositoryURL = SVNURL.parseURIEncoded(svnurl);
         } catch (SVNException e) {  
             logger.error(e.getMessage(),e);  
             return false;  
         }  
          File project = new File(file_path);
//    	if(SVNUtil.isURLExist(repositoryURL,username,password))
//    	{
//    		SVNUtil.checkout(clientManager, repositoryURL, SVNRevision.HEAD, project, SVNDepth.INFINITY); 
//    	}
    	File ws = new File(file_path);  
        if(!SVNWCUtil.isVersionedDirectory(ws)){  
            SVNUtil.checkout(clientManager, repositoryURL, SVNRevision.HEAD, ws, SVNDepth.INFINITY);  
        }else{  
            SVNUtil.update(clientManager, ws, SVNRevision.HEAD, SVNDepth.INFINITY);  
        } 
        
        
    	return false;
    }
    /**  
     * commit file to svn
     * @param svnUrl to sync to
     * @param filepath of file that needs to be synced into
     * @return true if successful 
     */  
    public boolean commitFiletoSvn(String filepath, String svnurl) {  
        SVNClientManager clientManager = xxxx(svnurl);
          
        File wc_project = new File(filepath);  

        checkVersiondDirectory(clientManager,wc_project);  
        
        String messageString =
		PluginUtils.getPlugin().
		getCommitString(); 
        SVNUtil.commit(clientManager, wc_project, false, messageString);  
          
        return true;  
    }  
    /**  
     * called when first run, create the job_config_history folder in repo if it doesnt exist, and do svn import;
     *if it exist, update local file by calling svn up
     * @param svnUrl to sync wtih
     * @param  file to sync into svn
     * @return true if sync successful, false otherwise
     */  
    public boolean sync_repo(String svnurl, File cr)
    {
    	SVNClientManager clientManager = xxxx(svnurl);
        SVNURL repositoryURL = null;  
        try {  
            repositoryURL = SVNURL.parseURIEncoded(svnurl);
        } catch (SVNException e) {  
            logger.error(e.getMessage(),e);  
            return false;  
        }  
    	if(!SVNUtil.isURLExist(repositoryURL,username,password))
    	{
    		
    		String commitMessage = "create job config history folder";
    		SVNUtil.makeDirectory(clientManager, repositoryURL, commitMessage);
    		commitMessage = "import existing config file";
    		SVNUtil.importDirectory(clientManager, cr, repositoryURL, commitMessage, true);
    	}
    	else
    		SVNUtil.update(clientManager, cr, SVNRevision.HEAD, SVNDepth.INFINITY);  
    		
    	return true;
    }
    
    private void checkVersiondDirectory(SVNClientManager clientManager,File wc){  
        if(!SVNWCUtil.isVersionedDirectory(wc)){  
            SVNUtil.addEntry(clientManager, wc);  
        }  
        if(wc.isDirectory()){  
            for(File sub:wc.listFiles()){  
                if(sub.isDirectory() && sub.getName().equals(".svn")){  
                    continue;  
                }  
                checkVersiondDirectory(clientManager,sub);  
            }  
        }  
    }  
    public boolean check_if_exists(String svnurl)
    {
    	initialSVNManager(svnurl);
        SVNURL repositoryURL = null;  
        try {  
            repositoryURL = SVNURL.parseURIEncoded(svnurl);
        } catch (SVNException e) {  
            logger.error(e.getMessage(),e);  
            return false;  
        }  
    	return SVNUtil.isURLExist(repositoryURL,username,password);
    }
    
    public boolean delete_file(String svnurl)
    {
    	final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
    	SVNURL fileUrl;
    	try {  
    		fileUrl = SVNURL.parseURIEncoded(svnurl);
        } catch (SVNException e) {  
            logger.error(e.getMessage(),e);  
            return false;  
        }  
        try {
            final SvnRemoteDelete remoteDelete = svnOperationFactory.createRemoteDelete();
            remoteDelete.setSingleTarget(SvnTarget.fromURL(fileUrl));
            remoteDelete.setCommitMessage("Delete a file from the repository");
            SVNCommitInfo commitInfo = null;
			try {
				commitInfo = remoteDelete.run();
			} catch (SVNException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if (commitInfo != null) {
                final long newRevision = commitInfo.getNewRevision();
                System.out.println("Removed a file, revision " + newRevision + " created");
            }
        } finally {
            svnOperationFactory.dispose();
        }
		return true;
    }
    
    
}

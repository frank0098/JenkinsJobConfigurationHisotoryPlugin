package hudson.plugins.jobConfigHistory;
import static org.junit.Assert.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SVNServiceSpecTest{
	
	private String username = "uiuc_cs427_testing";
	private String password = "123456";
	private String svnurl = "https://svn.riouxsvn.com/uiuc_cs427_test/";
	
	public SVNServiceSpecTest(){
		
		
		
	}
	//Test for sync
	//This test try to sync with an existing repo
	//This test will test whether a file in a existing repo is checked out as desired
	@Test
	public void test_svn_1(){
		SVNService svn_service = new SVNService(username,password,svnurl);
		try {
			
			//create a new folder
			String repodirpath = System.getenv("HOME")+"/svntestrepofolder";
			 File repodir = new File(repodirpath);
			String svnrepourl = svnurl+"test1";
			if(repodir.exists())
			{
				FileUtils.deleteDirectory(repodir);
			}
			repodir.mkdir();
			svn_service.sync_repo(svnrepourl, repodirpath);
			
			//these are the file contents for comparison
			String verify_file_name = System.getenv("HOME")+"/svntestrepofolder/testfile";
			String verify_content = new String("newtestfile");
			String read_content = null;
			while(true)
			{
				read_content = new String(Files.readAllBytes(Paths.get(verify_file_name)));
				if(read_content!=null)
					break;
			}
			//delete the folder
			repodir.delete();
			FileUtils.deleteDirectory(repodir);
			assertTrue(read_content.equals(verify_content));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	//Test for sync
	//This test try to sync with an repo that does not exist yet
	//it will verify that the sync will create the repo as desired on the svn repository
	@Test
	public void test_svn_2()
	{
		//create new folder
		SVNService svn_service = new SVNService(username,password,svnurl);
		String repodirpath = System.getenv("HOME")+"/svntestrepofolder2";
		 File repodir = new File(repodirpath);
		String svnrepourl = svnurl+"test2";
		try {
			
			
			if(repodir.exists())
			{
				FileUtils.deleteDirectory(repodir);
			}
			repodir.mkdir();
			svn_service.sync_repo(svnrepourl, repodirpath);
		}
		catch (Exception e)
		{
			
		}
		assertTrue(svn_service.check_if_exists(svnrepourl));
		svn_service.delete_file(svnrepourl);
		try {
			FileUtils.deleteDirectory(repodir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//this test check the behaviour of checkout
	//the file check out will be compared with desired content
	@Test
	public void test_svn_3()
	{
		SVNService svn_service = new SVNService(username,password,svnurl);
		try {
			
			String repodirpath = System.getenv("HOME")+"/svntestrepofolder3";
			 File repodir = new File(repodirpath);
			String svnrepourl = svnurl+"test1";
			if(repodir.exists())
			{
				FileUtils.deleteDirectory(repodir);
			}
			repodir.mkdir();
			//checkout the folder from svn
			svn_service.checkoutfromSvn(svnrepourl, repodirpath);
			
			String verify_file_name = System.getenv("HOME")+"/svntestrepofolder/testfile";
			String verify_content = new String("newtestfile");
			String read_content = null;
			while(true)
			{
				read_content = new String(Files.readAllBytes(Paths.get(verify_file_name)));
				if(read_content!=null)
					break;
			}
			//delete the folder
			repodir.delete();
			assertTrue(read_content.equals(verify_content));
			FileUtils.deleteDirectory(repodir);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		}
}
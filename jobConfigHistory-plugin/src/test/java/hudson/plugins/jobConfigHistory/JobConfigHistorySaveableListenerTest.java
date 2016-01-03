package hudson.plugins.jobConfigHistory;

import hudson.XmlFile;
import hudson.model.Saveable;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Mirko Friedenhagen
 */
public class JobConfigHistorySaveableListenerTest {

    private final HistoryDao mockedConfigHistoryListenerHelper = mock(HistoryDao.class);

    private final JobConfigHistory mockedPlugin = mock(JobConfigHistory.class);

    /**
     * Test of onChange method, of class JobConfigHistorySaveableListener.
     */
    @Test
    public void testOnChangeNotSaveable() {
        when(mockedPlugin.isSaveable(any(Saveable.class), any(XmlFile.class))).thenReturn(false);
        JobConfigHistorySaveableListener sut = new JobConfigHistorySaveableListenerImpl();
        sut.onChange(null, null);
        verifyZeroInteractions(mockedConfigHistoryListenerHelper);
    }

    /**
     * Test of onChange method, of class JobConfigHistorySaveableListener.
     */
    @Test
    public void testOnChangeSaveable() {
        when(mockedPlugin.isSaveable(any(Saveable.class), any(XmlFile.class))).thenReturn(true);
        JobConfigHistorySaveableListener sut = new JobConfigHistorySaveableListenerImpl();
        sut.onChange(null, null);
        verify(mockedConfigHistoryListenerHelper).saveItem(any(XmlFile.class));
    }

    private class JobConfigHistorySaveableListenerImpl extends JobConfigHistorySaveableListener {

        @Override
        JobConfigHistory getPlugin() {
            return mockedPlugin;
        }

        @Override
        HistoryDao getHistoryDao(JobConfigHistory plugin) {
            return mockedConfigHistoryListenerHelper;
        }
    }


    
    @Test
    public void testCopyFile(){
        // create src adn dst dir
        File src = new File(System.getenv("HOME") + "/src");
        File dst = new File(System.getenv("HOME") + "/dst");
        
        src.mkdir();
        
        // create a child dir
        File childDir = new File(System.getenv("HOME") + "/src/child");
        childDir.mkdir();
        
        // add some files to src
        for(int i = 1; i<4; i++){
            try {
                // write some content to file in src for testing
                FileWriter fileWriter = new FileWriter(System.getenv("HOME") + "/src/file" + String.valueOf(i));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("This is File" + String.valueOf(i));
                bufferedWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        // add some files to childDir
        for(int i = 1; i<4; i++){
            try {
                // write some content to file in src for testing
                FileWriter fileWriter = new FileWriter(System.getenv("HOME") + "/src/child/childFile" + String.valueOf(i));
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write("This is childFile" + String.valueOf(i));
                bufferedWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        try {
            JobConfigHistorySaveableListener.copyFolder(src, dst);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        // relevant dir and files in dst
        File dstChild =  new File(System.getenv("HOME") + "/dst/child");
        File dstFile1 = new File(System.getenv("HOME") + "/dst/file1");
        File dstChildFile1 = new File(System.getenv("HOME") + "/dst/child/childFile1");
        
        assertTrue(dst.isDirectory());
        assertTrue(dstChild.isDirectory());
        
        // read content from file
        try {
            assertTrue(Files.toString(dstFile1, Charsets.UTF_8).equals("This is File1"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            assertTrue(Files.toString(dstChildFile1, Charsets.UTF_8).equals("This is childFile1"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}

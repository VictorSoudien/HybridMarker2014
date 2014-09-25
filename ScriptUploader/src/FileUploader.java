/* This class is used to upload a given file to a remote directory via an sftp connectionn
 * Author: Victor Soudien
 * Date: 28 July 2014
 * Student Number: SDNVIC001
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.FileObject;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class FileUploader 
{
	// Uploads the file to the specified directory
	public boolean uploadFileToServer (String pathOnServer, File fileToUpload)
	{
		Properties connectionProperties = new Properties();
		StandardFileSystemManager systemManager = new StandardFileSystemManager();
		
		try
		{
			connectionProperties.load(new FileInputStream("ConnectionProperties.txt"));
			String username = connectionProperties.getProperty("username");
			String password = connectionProperties.getProperty("password");
			
			// Check whether the file exist at the given path
			if (fileToUpload.exists())
			{
				// Adapted from documentation provided with the library used
				systemManager.init();
				
				FileSystemOptions systemOptions = new FileSystemOptions();
				SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(systemOptions, "no");
				SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(systemOptions, true);
				SftpFileSystemConfigBuilder.getInstance().setTimeout(systemOptions, 1000);
				
				// Create the URI to connect to
				String uri = "sftp://" + username + ":" + password + "@nightmare.cs.uct.ac.za/Honours_Project/" + pathOnServer;/* + filename*/;
				
				// Create the file objects
				FileObject uploadObject = systemManager.resolveFile(fileToUpload.getAbsolutePath());
				FileObject remoteObject = systemManager.resolveFile(uri, systemOptions);
				
				// Upload the file
				remoteObject.copyFrom(uploadObject, Selectors.SELECT_SELF);
				
				return true;
			}
			else
			{
				System.out.println ("The file that is trying to be uploaded, does not exist");
				return false;
			}
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while trying to upload the file");
			System.out.println (e.getMessage());
			return false;
		}
		finally
		{
			systemManager.close();
		}
	}
	
	// Uploads the file specified as a path to the specified directory
	public boolean uploadFileToServer (String pathOnServer, String filePath)
	{
		Properties connectionProperties = new Properties();
		StandardFileSystemManager systemManager = new StandardFileSystemManager();
		
		try
		{
			connectionProperties.load(new FileInputStream("ConnectionProperties.txt"));
			String username = connectionProperties.getProperty("username");
			String password = connectionProperties.getProperty("password");
			
			File fileToUpload = new File(filePath);
			
			// Check whether the file exist at the given path
			if (fileToUpload.exists())
			{
				// Adapted from documentation provided with the library used
				systemManager.init();
				
				FileSystemOptions systemOptions = new FileSystemOptions();
				SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(systemOptions, "no");
				SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(systemOptions, true);
				SftpFileSystemConfigBuilder.getInstance().setTimeout(systemOptions, 1000);
				
				// Create the URI to connect to
				String uri = "sftp://" + username + ":" + password + "@nightmare.cs.uct.ac.za/Honours_Project/" + pathOnServer;/* + filename*/;
				
				// Create the file objects
				FileObject uploadObject = systemManager.resolveFile(fileToUpload.getAbsolutePath());
				FileObject remoteObject = systemManager.resolveFile(uri, systemOptions);
				
				// Upload the file
				remoteObject.copyFrom(uploadObject, Selectors.SELECT_SELF);
				
				return true;
			}
			else
			{
				System.out.println ("Could not locate file at " + filePath);
				return false;
			}
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while trying to upload the file");
			System.out.println (e.getMessage());
			return false;
		}
		finally
		{
			systemManager.close();
		}
	}
	
	// Gets the number of files already uploaded in this location on the server
	public String getNumberOfFiles (String path)
	{
		String commandToExecute = "ls " + path + " | wc -l";
		
		try
		{
			// Set up an ssh connection to nightmare.cs.uct.ac.za
			Properties connectionProperties = new Properties();
			
			// Get the username and password from the file and remove it from memory after using
			connectionProperties.load(new FileInputStream("ConnectionProperties.txt"));
			String username = connectionProperties.getProperty("username");
			String password = connectionProperties.getProperty("password");
			connectionProperties.remove("username");
			connectionProperties.remove("password");
			
			connectionProperties.put("StrictHostKeyChecking", "no");
			
			JSch jsch = new JSch();
			Session sshSession = jsch.getSession(username, "nightmare.cs.uct.ac.za", 22);
			sshSession.setPassword(password);
			sshSession.setConfig(connectionProperties);
			sshSession.connect();
			
			// Create a communication channel with the server and execute the command
			Channel commChannel = sshSession.openChannel("exec");
			((ChannelExec)commChannel).setCommand(commandToExecute);
			commChannel.setInputStream(null);
			
			InputStream inStream = commChannel.getInputStream();
			commChannel.connect();
			
			String result = "";
			int nextIn;
			
			while ((nextIn = inStream.read()) != -1)
			{
				result += Character.toString((char)nextIn);
			}
			
			return result.trim();
 		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to retrieve amount of uploaded files");
			System.out.println (e.getMessage());
			return null;
		}
	}
}

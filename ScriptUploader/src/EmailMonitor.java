/* Monitors an email inbox so that emailed scripts can be processed
 * Author: Victor Soudien
 * Date: 11 August 2014
 * Student Number: SDNVIC001
 */

import java.util.Properties;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class EmailMonitor
{
	private IMAPFolder inbox;
	private int lastMessageRead;
	
	public EmailMonitor()
	{
		
	}
	
	public void createConnectionToMailbox()
	{
		// Define protocol
		Properties properties = new Properties();
		properties.setProperty("mail.store.protocol", "imap");
		
		// Create a session
		Session session = Session.getInstance(properties, null);
		
		try
		{
			// Connect to mailbox
			IMAPStore mailStore = (IMAPStore)session.getStore();
			mailStore.connect("imap.cs.uct.ac.za", "vsoudien", "compsci2");
			inbox = (IMAPFolder)mailStore.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			inbox.addMessageCountListener(new countChangeListener());
			
			lastMessageRead = inbox.getMessageCount(); // Set the index to the current amount of emails in the inbox
		}
		catch (Exception e)
		{
			System.out.println ("Error while connecting to mailbox");
			e.printStackTrace();
		}
	}
	
	private void startThread()
	{
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				while (true)
				{	
					try
					{
						Thread.sleep(5000);
						inbox.idle();
					}
					catch (Exception e)
					{
						System.out.println ("Error while sending idle message");
						e.printStackTrace();
					}
				}
			}
		});
		
		t.start();
	}
	
	private void processLatestMessages ()
	{
		try
		{
			// Read all messages that were received since the last read
			for (int i = lastMessageRead + 1; i <= inbox.getMessageCount(); i++)
			{
				// Get the latest email
				Message message = inbox.getMessage(i);
		
				Address[] in = message.getFrom();
		        for (Address address : in) 
		        {
		            System.out.println("FROM:" + address.toString());
		        }
		        Multipart mp = (Multipart) message.getContent();
		        BodyPart bp = mp.getBodyPart(0);
		        System.out.println("SENT DATE:" + message.getSentDate());
		        System.out.println("SUBJECT:" + message.getSubject());
		        System.out.println("CONTENT:" + bp.getContent());
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while processing latest email");
			e.printStackTrace();
		}
	}
	
	// This inner class monitors changes to the amount of message in the mailbox
	private class countChangeListener implements MessageCountListener
	{
		@Override
		public void messagesAdded(MessageCountEvent arg0) 
		{
			System.out.println ("Message Added");
			processLatestMessages();
		}

		@Override
		public void messagesRemoved(MessageCountEvent arg0) 
		{
			System.out.println ("Message Removed");
		}
	}
	
	public static void main (String [] args)
	{
		EmailMonitor monitor = new EmailMonitor();
		monitor.createConnectionToMailbox();
		monitor.startThread();
		System.out.println ("Inbox monitoring started...");
	}
}

/**
 *
 * @author Zahraa Mathews
 * 
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 
public class SendMail 
{
        String receiver = "";
        String fileExtention = "";
        String results = "";
        
        public SendMail(String receiver_, String fileExtention_, String results_)
        {
            receiver = receiver_;
            fileExtention = fileExtention_;
            results = results_;
        }
	public void sendMail() throws FileNotFoundException, IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, GeneralSecurityException 
        {
                //set properties
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");
 
                //login to gmail account
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() 
                {
                    protected PasswordAuthentication getPasswordAuthentication() 
                    {
                            return new PasswordAuthentication("scriptviewuct@gmail.com","ScriptView2014");
                    }
		});
 
		try 
                {
			MimeMessage message = new MimeMessage(session);
                        //address from
			message.setFrom(new InternetAddress("scriptviewuct@gmail.com"));
			//address to
                        message.setRecipients(Message.RecipientType.TO,	InternetAddress.parse(receiver));
                        String subject = "Test Results";
			//set subject line
                        message.setSubject(subject);
		
                      
                        // Create the message part
                        BodyPart messageBodyPart = new MimeBodyPart();
                        // Now set the actual message
                        String setcontent = results;
                        if(results.contains("+"))
                        {
                            setcontent = content(results);
                        }
                        messageBodyPart.setText(setcontent);
                        // Create a multipar message
                        Multipart multipart = new MimeMultipart();
                        // Set text message part
                        multipart.addBodyPart(messageBodyPart);
                        
                        // Part two is attachment
                        messageBodyPart = new MimeBodyPart();
                        //String filename = "C:/Users/Game/Desktop/Class_Test_2_Memo.pdf";
                        String filename = fileExtention;
                        DataSource source = new FileDataSource(filename);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName("Test Script.pdf");
                        multipart.addBodyPart(messageBodyPart);
         
         
                        
                        // Send the complete message parts
                        message.setContent(multipart);
                        //send email
			Transport.send(message);
                        //confirmation
			
                        //System.out.println();
                        //message.writeTo(System.out);
                        
		} 
                catch (MessagingException e) 
                {
                        System.out.println("There was an error when sending email to: "+receiver);
			throw new RuntimeException(e);
		}
	}
        public String content(String Content)
        {
            //Content = "+*4*+*1*1**1*2*1*+*3*+*1*1*2*2*+*1*1*1*1*+*3*+26";
            String[] question = Content.split("\\+");
            int[] sumAnswer = new int[question.length-1];
            for(int i =1;i<question.length; i++)
            {
                String Q = question[i].replaceAll("\\*\\*", "*0*");
                String[] SUBquestion = Q.split("\\*");
                int sum = 0;
                for(String SQ : SUBquestion)
                {
                    if(!SQ.equals(""))
                    {
                        sum = sum +Integer.parseInt(SQ);
                    }
                }
                sumAnswer[i-1] = sum;
                sum =0; 
            }
            int questionNum = 1;
            String Answer = "";
            for(int i : sumAnswer)
            {
                if(sumAnswer.length != questionNum)
                {
                Answer = Answer+"Question "+questionNum+" : "+i+"\n";
                }
                else
                {
                 Answer = Answer+"Total Mark : "+i+"\n";
                }
                questionNum++;
            }
            return Answer;
        }

}
package application;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;

import data.Database;

public class HandleImage {
	
    public static boolean DownloadWebPage(String webpage, Book_Booklist eintrag)  {
        
    	BufferedInputStream in;
		try {
			URL url = new URL(webpage);
			in = new BufferedInputStream(url.openStream());
	    	
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	byte[] buf = new byte[1024];
	    	int n = 0;
	    	while (-1!=(n=in.read(buf)))
	    	{
	    	   out.write(buf, 0, n);
	    	}
	    	out.close();
	    	in.close();
	    	byte[] response = out.toByteArray();
	    	String path = "tmp.jpg";
	    	FileOutputStream fos = new FileOutputStream(path);
	    	fos.write(response);
	    	fos.close();
	    	InputStream photoStream = new BufferedInputStream( new FileInputStream(path));
	    	Database.addPic(eintrag.getAutor(), eintrag.getTitel(), photoStream);
	    	photoStream.close();
	    	out.close();
	    	in.close();
	    	File file = new File(path);
	    	if(file.exists()) {
	    		file.delete();
	    		System.out.println("File deleted");
	    	}
	    	JOptionPane.showMessageDialog(null, "Bild erfolgreich importiert. Das Bild wird nach dem Neustart angezeigt.");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			webpage=JOptionPane.showInputDialog(null, "Kein Bild gefunden. Bitte manuell einfügen");
			if(webpage!="") {
				DownloadWebPage(webpage, eintrag);
			}
			
		}
		return false;
		
		

    } 
    
    public static boolean deletePic(String autor, String titel) {
    	return Database.delPic(autor, titel);
    	
    }	    
	    
}

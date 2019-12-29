package anwendung;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import datenhaltung.Datenbank;

public class HandleImage {
	
    public static boolean DownloadWebPage(String webpage, Buch eintrag)  {
        
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
	    	Datenbank.addPic(eintrag.getAutor(), eintrag.getTitel(), photoStream);
	    	photoStream.close();
	    	out.close();
	    	in.close();
	    	File file = new File(path);
	    	if(file.exists()) {
	    		file.delete();
	    		System.out.println("File deleted");
	    	}
	    	JOptionPane.showMessageDialog(null, "Bild erfolgreich importiert");
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
    	return Datenbank.delPic(autor, titel);
    	
    }
    
	public static void getImage(Buch eintrag) {
		String url1 = "https://www.thalia.de/suche?filterPATHROOT=&sq=";
		String url2 = eintrag.getAutor().replaceAll(" ","+") + "+" + eintrag.getTitel().replaceAll(" ","+");
		url2 = url2.toUpperCase().replaceAll("Ä","AE").replaceAll("Ö","OE").replaceAll("Ü","UE").replaceAll("ß","ss");
		String url = url1 + url2;
        String input = "";
		try {
			input = getURLSource(url);
			System.out.println(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        String[] output = input.split("https://assets.thalia.media/img");
        
        String ID = "";
        for(int i = 0;i<output.length;i++) {
        	if (output[i].substring(0, 30).contains("00.jpg")) {
        		ID = output[i].substring(0, 30).split(".jpg")[0];
        		break;
        	}
        }
        String link="https://assets.thalia.media/img" + ID + ".jpg";   
        System.out.println(link);
        boolean success = DownloadWebPage(link, eintrag);
        if(success) {
        	JOptionPane.showMessageDialog(null, "Bild erfolgreich geladen", "Bild Import", JOptionPane.INFORMATION_MESSAGE);
        }
     
        
	}

	public static String getURLSource(String url) throws IOException {
	        URL urlObject = new URL(url);
	        URLConnection urlConnection = urlObject.openConnection();
	        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

	        return toString(urlConnection.getInputStream());
	    }

	private static String toString(InputStream inputStream) throws IOException {
	        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")))
	        {
	            String inputLine;
	            StringBuilder stringBuilder = new StringBuilder();
	            while ((inputLine = bufferedReader.readLine()) != null)
	            {
	                stringBuilder.append(inputLine);
	            }

	            return stringBuilder.toString();
	        }
	    }
	    
	    
}

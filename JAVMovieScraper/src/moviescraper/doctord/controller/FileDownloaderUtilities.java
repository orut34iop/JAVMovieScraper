package moviescraper.doctord.controller;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.awt.image.BufferedImage;

import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import moviescraper.doctord.model.dataitem.Thumb;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
/**
 * Wrapper class around standard methods to download images from urls or write a url to a file 
 * so that set up a custom connection that allows us to set a user agent, etc.
 * This is necessary because some servers demand a user agent to download from them or a 403 error will be encountered.
 */
public class FileDownloaderUtilities {
	
	private static URLConnection getDefaultUrlConnection(URL url) throws IOException
	{
		final URLConnection connection = (URLConnection) url
		        .openConnection();
		connection.setRequestProperty(
		    "User-Agent",
		    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
		return connection;
	}
	
	public static Image getImageFromUrl(URL url) throws IOException
	{
		return null;
		//return getImageFromUrl(url, null);
	}
	
	@SuppressWarnings("deprecation")
	public static Image getImageFromUrl(URL url, URL viewerURL) throws IOException
	{
		URLConnection urlConnectionToUse = FileDownloaderUtilities.getDefaultUrlConnection(url);
		//urlConnectionToUse.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
		urlConnectionToUse.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
		

		
		
		if (viewerURL != null){
        	urlConnectionToUse.setRequestProperty("Referer",viewerURL.toString());   
        }
		try (InputStream inputStreamToUse = urlConnectionToUse.getInputStream();) {
			Image imageFromUrl = ImageIO.read(inputStreamToUse);
			return imageFromUrl;
		}

		/*
        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
		
		String downloadFilepath = "c:\\";
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);//设置为禁止弹出下载窗口
        chromePrefs.put("download.default_directory", downloadFilepath);
        
        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
        options.setExperimentalOption("prefs",chromePrefs);
        options.addArguments("--test-type");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);        
        

        WebDriver driver = new ChromeDriver(cap);
        
        driver.get(url.toString());
        
        driver.
        
        System.out.printf("dowload image file done");	

        driver.quit();	
        
        FileDownloader fileDownloader=new FileDownloader(driver); 
        
        return null;
		*/
		
	}
	
	public static Image getImageFromThumb(Thumb thumb)
	{

		return null;
/*		
		if (thumb != null)
		{
			try {
				return getImageFromUrl(thumb.getThumbURL(), thumb.getReferrerURL());
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		return null;
		*/
	}
	
	public static void writeURLToFile(URL url, File file) throws IOException
	{
		FileUtils.copyInputStreamToFile(FileDownloaderUtilities.getDefaultUrlConnection(url).getInputStream(), file);
	}

	public static void writeURLToFile(URL url, File file, URL viewerUrl) throws IOException
	{
                try {
                    URLConnection imageConnection = url.openConnection();
                    imageConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
                    if (viewerUrl != null){
                        imageConnection.setRequestProperty("Referer",viewerUrl.toString());   
                    }
                    BufferedImage pictureLoaded = ImageIO.read(imageConnection.getInputStream());
                    ImageIO.write(pictureLoaded, "jpg", file);
                 
                } catch(Throwable t) {
                    System.out.println("Error: " + t.getMessage());
                }            

	}
        
        
}

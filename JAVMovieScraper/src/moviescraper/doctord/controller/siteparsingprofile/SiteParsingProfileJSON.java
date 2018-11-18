package moviescraper.doctord.controller.siteparsingprofile;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public abstract class SiteParsingProfileJSON extends SiteParsingProfile {

	public String getJSONStringFromURL(String url) throws IOException
	{
		String json = Jsoup.connect(url).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).ignoreContentType(true).execute().body();
		return json;
	}
	
	public JSONObject getJSONObjectFromString (String jsonString) throws JSONException
	{
		JSONObject jsonObject =  new JSONObject(jsonString);
		return jsonObject;
	}
	
	public JSONObject getJSONObjectFromURL(String url) throws JSONException, IOException{
		return getJSONObjectFromString(getJSONStringFromURL(url));
	}
	
	public static Document getDocument(String url) throws IOException
	{
		
        System.setProperty("webdriver.chrome.driver","C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get(url);

        String html = driver.getPageSource();
        
        driver.quit();

        return  Jsoup.parse(html);

	}
	
	
}

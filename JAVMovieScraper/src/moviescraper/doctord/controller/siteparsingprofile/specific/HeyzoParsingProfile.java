package moviescraper.doctord.controller.siteparsingprofile.specific;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import moviescraper.doctord.controller.languagetranslation.Language;
import moviescraper.doctord.controller.siteparsingprofile.SiteParsingProfile;
import moviescraper.doctord.model.SearchResult;
import moviescraper.doctord.model.dataitem.Actor;
import moviescraper.doctord.model.dataitem.Director;
import moviescraper.doctord.model.dataitem.Genre;
import moviescraper.doctord.model.dataitem.ID;
import moviescraper.doctord.model.dataitem.MPAARating;
import moviescraper.doctord.model.dataitem.OriginalTitle;
import moviescraper.doctord.model.dataitem.Outline;
import moviescraper.doctord.model.dataitem.Plot;
import moviescraper.doctord.model.dataitem.Rating;
import moviescraper.doctord.model.dataitem.ReleaseDate;
import moviescraper.doctord.model.dataitem.Runtime;
import moviescraper.doctord.model.dataitem.Set;
import moviescraper.doctord.model.dataitem.SortTitle;
import moviescraper.doctord.model.dataitem.Studio;
import moviescraper.doctord.model.dataitem.Tagline;
import moviescraper.doctord.model.dataitem.Thumb;
import moviescraper.doctord.model.dataitem.Title;
import moviescraper.doctord.model.dataitem.Top250;
import moviescraper.doctord.model.dataitem.Trailer;
import moviescraper.doctord.model.dataitem.Votes;
import moviescraper.doctord.model.dataitem.Year;

public class HeyzoParsingProfile extends SiteParsingProfile implements SpecificProfile {

	private String englishPage;
	private String japanesePage;
	
	Document japaneseDocument;
	
	@Override
	public String getParserName() {
		return "HEYZO";
	}
	
	public HeyzoParsingProfile()
	{
		super();
	}
	

	@Override
	public Title scrapeTitle() {
		Element titleElement = document.select("div#movie h1").first();
		if(titleElement != null)
		{
			String titleElementText = titleElement.text().trim().replaceAll("[ ]+", " ");
			return new Title(titleElementText);
		}
		return new Title("");
	}

	@Override
	public OriginalTitle scrapeOriginalTitle() {
		if(scrapingLanguage == Language.JAPANESE)
			return new OriginalTitle(scrapeTitle().getTitle());
		else
		{
			Document originalDocument = document;
			document = japaneseDocument;
			OriginalTitle originalTitle = new OriginalTitle(scrapeTitle().getTitle());
			document = originalDocument;
			return originalTitle;
		}
	}

	@Override
	public SortTitle scrapeSortTitle() {
		return SortTitle.BLANK_SORTTITLE;
	}

	@Override
	public Set scrapeSet() {
		return Set.BLANK_SET;
	}

	@Override
	public Rating scrapeRating() {
		//This used to be scrapable, but this now requires javascript to parse the page to get the rating
		//TODO: If I ever replace jsoup with a javascript enabled parser, rewrite this function
		return Rating.BLANK_RATING;
		/*this was the code that should work if I had javascript enabled parser*/
		/*
		Element ratingValueElement = japaneseDocument.select("#review-value").first();
		if(ratingValueElement != null)
		{
			return new Rating(5.0, ratingValueElement.text().trim());
		}
		else return Rating.BLANK_RATING;
		*/
	}

	@Override
	public Year scrapeYear() {
		return scrapeReleaseDate().getYear();
	}
	
	@Override
	public ReleaseDate scrapeReleaseDate()
	{
		Element releaseDateElement = japaneseDocument.select("div.movieInfo span.release-day + span.dataInfo").first();
		if(releaseDateElement != null)
		{
			//System.out.println("year = " + yearElement.text());
			String yearText = releaseDateElement.text().trim();
			if(yearText.length() > 4)
			{
				return new ReleaseDate(yearText);
			}
			
		}
		return ReleaseDate.BLANK_RELEASEDATE;
	}

	@Override
	public Top250 scrapeTop250() {
		return Top250.BLANK_TOP250;
	}

	@Override
	public Votes scrapeVotes() {
		return Votes.BLANK_VOTES;
	}

	@Override
	public Outline scrapeOutline() {
		return Outline.BLANK_OUTLINE;
	}

	@Override
	public Plot scrapePlot() {
		return Plot.BLANK_PLOT;
	}

	@Override
	public Tagline scrapeTagline() {
		return Tagline.BLANK_TAGLINE;
	}

	@Override
	public Runtime scrapeRuntime() {
		Element runtimeElement = document.select("tbody:contains(Whole Movie File Download) tr:contains(:) td").first();
		if(runtimeElement != null)
		{
			String[] runtimeTextSplit = runtimeElement.text().trim().split((":"));
			if(runtimeTextSplit.length == 3)
			{
				int hours = Integer.parseInt(runtimeTextSplit[0]);
				int minutes = Integer.parseInt(runtimeTextSplit[1]);
				int totalMinutes = (hours * 60) + minutes;
				if(totalMinutes > 0)
					return new Runtime(new Integer(totalMinutes).toString());
			}
		}
		return Runtime.BLANK_RUNTIME;
	}

	@Override
	public Thumb[] scrapePosters() {
		ArrayList<Thumb> thumbList = new ArrayList<>();
		String scrapedId = scrapeID().getId();
		try {
			//gallery links
			for(int i = 1; i <= 21; i++)
			{
				String potentialGalleryImageURL = "http://en.heyzo.com/contents/3000/" + scrapedId + "/gallery/0" + String.format("%02d",i) + ".jpg";
				String potentialGalleryPreviewImageURL = "http://en.heyzo.com/contents/3000/" + scrapedId + "/gallery/thumbnail_0" + String.format("%02d",i) + ".jpg";
				if(SiteParsingProfile.fileExistsAtURL(potentialGalleryImageURL))
				{
					Thumb thumbToAdd = new Thumb(potentialGalleryImageURL);
					thumbToAdd.setPreviewURL(new URL(potentialGalleryPreviewImageURL));
					thumbList.add(thumbToAdd);
						
				}
			}
			//image that is the preview of the trailer
			Thumb trailerPreviewThumb = new Thumb("http://www.heyzo.com/contents/3000/" + scrapedId + "/images/player_thumbnail_450.jpg");
			thumbList.add(trailerPreviewThumb);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return thumbList.toArray(new Thumb[thumbList.size()]);
		}
		// TODO Auto-generated method stub
		return thumbList.toArray(new Thumb[thumbList.size()]);
	}

	@Override
	public Trailer scrapeTrailer(){
		String scrapedId = scrapeID().getId();
		String trailerURL = "http://sample.heyzo.com/contents/3000/" + scrapedId + "/heyzo_hd_0194_sample.mp4";
		if(SiteParsingProfile.fileExistsAtURL(trailerURL))
			return new Trailer(trailerURL);
		return Trailer.BLANK_TRAILER;
	}
	
	@Override
	public Thumb[] scrapeFanart() {
		return scrapePosters();
	}

	@Override
	public Thumb[] scrapeExtraFanart() {
		return scrapePosters();
	}

	@Override
	public MPAARating scrapeMPAA() {
		return MPAARating.RATING_XXX;
	}

	@Override
	public ID scrapeID() {
		//Just get the ID from the page URL by doing some string manipulation
		String baseUri = document.baseUri();
		if(baseUri.length() > 0 && baseUri.contains("heyzo.com"))
		{
			baseUri = baseUri.replaceFirst("/index.html", "");
			String idFromBaseUri = baseUri.substring(baseUri.lastIndexOf('/')+1);
			return new ID(idFromBaseUri);
		}
		return ID.BLANK_ID;
	}

	@Override
	public ArrayList<Genre> scrapeGenres() {
		ArrayList<Genre> genreList = new ArrayList<>();
		Elements genreElements = document.select("div.movieInfo a[href*=/listpages/category");
		if(genreElements != null)
		{
			for(Element currentGenre : genreElements)
			{
				if(currentGenre.text().trim().length() > 0)
					genreList.add(new Genre(currentGenre.text().trim()));
			}
		}
		return genreList;
	}

	@Override
	public ArrayList<Actor> scrapeActors() {
		Elements actorElements = document.select("div.movieInfo span.dataInfo a[href*=/listpages/actor");
		ArrayList<Actor> actorList = new ArrayList<>();
		for(Element currentActor : actorElements)
		{
			String actorName = currentActor.text().trim();
			String actorHref = currentActor.attr("href");
			String actorNumber = null;
			String actorThumbUrl = null;
			if(actorHref != null && actorHref.length() > 0)
			{
				String [] splitHrefByUnderScore = actorHref.split("_");
				if(splitHrefByUnderScore.length > 0)
				{
					actorNumber = splitHrefByUnderScore[1];
					actorThumbUrl = "http://en.heyzo.com/actorprofile/3000/" + String.format("%04d", Integer.parseInt(actorNumber)) + "/profile.jpg";
				}
			}
			//we found a thumbnail image for this actor
			if(actorThumbUrl != null && SiteParsingProfile.fileExistsAtURL(actorThumbUrl))
			{
				try {
					actorList.add(new Actor(actorName, "", new Thumb(actorThumbUrl)));
				} catch (MalformedURLException e) {
					e.printStackTrace();
					actorList.add(new Actor(actorName, "", null));
				}
			}
			//we didn't find a thumbnail image for this actor
			else
			{
				actorList.add(new Actor(actorName, "", null));
			}	
		}
		return actorList;
	}

	@Override
	public ArrayList<Director> scrapeDirectors() {
		return new ArrayList<>();
	}

	@Override
	public Studio scrapeStudio() {
		// TODO Auto-generated method stub
		return new Studio("HEYZO");
	}

	@Override
	public String createSearchString(File file) {
		scrapedMovieFile = file;
		String fileID = findIDTagFromFile(file).toLowerCase();

		if (fileID != null) {
			
			englishPage = "http://en.heyzo.com/moviepages/" + fileID + "/index.html";
			japanesePage = "http://www.heyzo.com/moviepages/" + fileID + "/index.html";
			try {
				japaneseDocument = Jsoup.connect(japanesePage)
						.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
						.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
						.header("Accept-Encoding", "gzip, deflate")
						.header("Accept-Language", "zh-CN,zh;q=0.9")
						.header("Upgrade-Insecure-Requests", "1")
						.header("Connection", "keep-alive")
						.header("Cookie", "rtt=xjmx6Q9Vt6cd7LiT05x3WdEBrtMrQ7MVeWjc148jYhO3%2BosfSwmuqxhlrkoknBAUvxWwAv5Iy8er2lpw3xTdElvtKtUsQKBXi73vOj6SweT3HseCRr9URNuFiljDBSPuzk59rVsZSHE8jhctw0pDB67kszWWPEKVNPtRRvHQjBAMbKRs18LVDwsN78PKB1HGHEOVgqJXy6eXhO6gLN8JbpeXdLk%3D; lg=zh; ab=a; ex=USD; gid=UoFfNDEto5dgH7%2BiACzCLBYq538L1KA3gnTf187N5w2KXUhs4ytvPv39SRvm23wvx%2Fk0f26ZosBy38PWrdZjW3DOQmE%3D; _ga=GA1.2.161111165.1541821215; _gid=GA1.2.2040351927.1541821215; i3_ab=8833; bh=eyJwcHBkMDA2NzdkbDYiOiJtb3ZpZXMiLCJwcHBkMDA2NzZkbDYiOiJtb3ZpZXMifQ%3D%3D")
						.timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(scrapingLanguage == Language.ENGLISH)
			{
				return englishPage;
			}
			else
			{
				return japanesePage;
			}
		}

		return null;
	}

	@Override
	public SearchResult[] getSearchResults(String searchString)
			throws IOException {
		SearchResult searchResult = new SearchResult(searchString);
		SearchResult[] searchResultArray = {searchResult};
		return searchResultArray;
	}
	
	public static String findIDTagFromFile(File file) {
		return findIDTag(FilenameUtils.getName(file.getName()));
	}
	
	public static String findIDTag(String fileName) {
		Pattern pattern = Pattern.compile("[0-9]{4}");
		Matcher matcher = pattern.matcher(fileName);
		if (matcher.find()) {
			String searchString = matcher.group();
			return searchString;
		}
		return null;
	}

	@Override
	public SiteParsingProfile newInstance() {
		return new HeyzoParsingProfile();
	}

}

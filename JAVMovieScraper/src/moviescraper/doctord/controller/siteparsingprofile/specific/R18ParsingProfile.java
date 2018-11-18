package moviescraper.doctord.controller.siteparsingprofile.specific;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

public class R18ParsingProfile extends SiteParsingProfile implements SpecificProfile {
	
	private static final SimpleDateFormat r18ReleaseDateFormat = new SimpleDateFormat("MMM. dd,yyyy", Locale.ENGLISH);
	private static final SimpleDateFormat r18ReleaseDateFormatAlternate = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
	@Override
	public String getParserName() {
		return "R18.com";
	}
	
	@Override
	public List<ScraperGroupName> getScraperGroupNames()
	{
		if(groupNames == null)
			groupNames = Arrays.asList(ScraperGroupName.JAV_CENSORED_SCRAPER_GROUP);
		return groupNames;
	}
	
	@Override
	public Title scrapeTitle() {
		Element titleElement = document.select("cite[itemprop=name]").first();
		if(titleElement != null)
			return new Title(titleElement.text());
		else return new Title("");
	}

	@Override
	public OriginalTitle scrapeOriginalTitle() {
		// r18 does not have a title in japanese :(
		return OriginalTitle.BLANK_ORIGINALTITLE;
	}

	@Override
	public SortTitle scrapeSortTitle() {
		//no SortTitle - the user usually provides their own
		return SortTitle.BLANK_SORTTITLE;
	}

	@Override
	public Set scrapeSet() {
		Element setElement = document.select("div.product-details dl dt:contains(Series:) + dd a").first();

		if(setElement != null)
		{
			String setText = setElement.text().trim();
			if(setText.endsWith("..."))
			{
				System.out.println("Visiting set page to get full text");
				try
				{
					Document setDocument = SiteParsingProfile.downloadDocumentFromURLString(setElement.attr("href"));
					Element setElementFullText = setDocument.select("div.cmn-ttl-tabMain01 div.txt01").first();
					if(setElementFullText != null)
					{
						return new Set(setElementFullText.text());
					}
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return new Set(setText);
				}
			}
			return new Set(setText);
		}
		return Set.BLANK_SET;
	}

	@Override
	public Rating scrapeRating() {
		//this site doesn't have ratings
		return Rating.BLANK_RATING;
	}

	@Override
	public Year scrapeYear() {
		return scrapeReleaseDate().getYear();
	}
	
	@Override
	public ReleaseDate scrapeReleaseDate() {
//		Element releaseDateElement = document.select("div.product-details dl dt:contains(Release Date) ~ dd").first();
		
		Element releaseDateElement = document.select("dd[itemprop=dateCreated]").first();

/*
 *		Elements genreElements = document.select("a[itemprop=genre]");
		if(genreElements != null)
		{
			for(Element currentGenre : genreElements)
			{
				String genreText = currentGenre.text(); 
 * */		
		
		if(releaseDateElement != null && releaseDateElement.text().length() > 4)
		{
			String releaseDateText = releaseDateElement.text().trim();

			if (releaseDateText.contains("/")) {
				releaseDateText = releaseDateText.replace('/', '-');
				ReleaseDate releaseDate = new ReleaseDate(releaseDateText);
				return releaseDate;
			}
			
			
			//gah why is this site so inconsistent. September should be Sep., not "Sept.". 
			//They randomly decide how many letters they want each month to take.
			if(releaseDateText.contains("Sept.")) {
				releaseDateText = releaseDateText.replaceFirst(Pattern.quote("Sept."), "Sep.");
			}
			
			//months abbreviated e.g.: "Oct."
			SimpleDateFormat formatToUse = r18ReleaseDateFormat;
			//month did not get abreviated
			if (!releaseDateText.contains(".")) {
				formatToUse = r18ReleaseDateFormatAlternate;
			}

			ReleaseDate releaseDate = new ReleaseDate(releaseDateText, formatToUse);
			return releaseDate;	
		}
		return ReleaseDate.BLANK_RELEASEDATE;
	}

	@Override
	public Top250 scrapeTop250() {
		return Top250.BLANK_TOP250;
	}
	
	@Override
	public Trailer scrapeTrailer() {
		Element element = document.select("object#FreeViewPlayer>param[name=flashvars]").first();
		if (element != null){
			String flashvars = element.attr("value");
			Pattern pattern = Pattern.compile("^.*&fid=(.+)&.*&bid=(\\d)(w|s)&.*$");
			Matcher matcher = pattern.matcher(flashvars);
			if (matcher.matches()){
				String cid = matcher.group(1);
				int bitrates = Integer.parseInt(matcher.group(2));
				String ratio = matcher.group(3);
				String quality = (bitrates & 0b100) != 0 ? "dmb" : (bitrates & 0b010) != 0 ? "dm" : "sm";
				String firstLetterOfCid = cid.substring(0,1);
				String threeLetterCidCode = cid.substring(0,3);
				
				String trailerURL = String.format("http://cc3001.r18.com/litevideo/freepv/%1$s/%2$s/%3$s/%3$s_%4$s_%5$s.mp4", 
			 			firstLetterOfCid, threeLetterCidCode, cid, quality, ratio);
				
				return new Trailer(trailerURL);
			}
			
		}
		return Trailer.BLANK_TRAILER;
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
		Element plotElement = document.select("div.cmn-box-description01 h1 ~ p").first();
		if(plotElement != null)
			return new Plot(plotElement.text());
		return Plot.BLANK_PLOT;
	}

	@Override
	public Tagline scrapeTagline() {
		return Tagline.BLANK_TAGLINE;
	}

	@Override
	public Runtime scrapeRuntime() {
		Element runtimeElement = document.select("div.product-details dl dt:contains(Runtime:) ~ dd").first();
		if(runtimeElement != null && runtimeElement.text().length() > 0)
		{
			String runtimeText = runtimeElement.text();
			runtimeText = runtimeText.replace(" min.", "");
			return new Runtime(runtimeText);
		}
		return Runtime.BLANK_RUNTIME;
	}

	@Override
	public Thumb[] scrapePosters() {
		return scrapePostersAndFanart(true);
	}
	
	private Thumb[] scrapePostersAndFanart(boolean doCrop)
	{
		Element dvdBoxartElement = document.select("section div.box01.mb10.detail-view.detail-single-picture img").first();
		if(dvdBoxartElement != null)
		{
			String imgSrc = dvdBoxartElement.attr("src");
			if(imgSrc.length() > 0)
			{
				try {
					Thumb poster = new Thumb(imgSrc, doCrop);
					Thumb [] posterArray = {poster};
					return posterArray;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new Thumb[0];
	}

	@Override
	public Thumb[] scrapeFanart() {
		return scrapePostersAndFanart(false);
	}

	@Override
	public Thumb[] scrapeExtraFanart() {
		List<Thumb> thumbList = new LinkedList<>();
		
		Elements previewProductGalleryImgLinks = document.select(".product-gallery li a img");
		
		if(previewProductGalleryImgLinks != null)
		{
			for(Element currentPreviewImage : previewProductGalleryImgLinks)
			{
				String imgThumbnailSrc = currentPreviewImage.attr("data-original");
				if(imgThumbnailSrc != null && imgThumbnailSrc.length() > 0)
				{
					int indexOfLastDash = imgThumbnailSrc.lastIndexOf('-');
					String fullImagePath = imgThumbnailSrc.substring(0,indexOfLastDash)+ "jp" + imgThumbnailSrc.substring(indexOfLastDash);
					try {
						thumbList.add(new Thumb(fullImagePath));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		//we could save time by not doing this check, but we might get broken images if the site changes their format
		//to speed things up a bit, we will check one image and assume the rest is OK
		if(thumbList.size() > 0 && !fileExistsAtURL(thumbList.get(thumbList.size()/2).getThumbURL().toString()))  {
			System.err.println("We expected to find extra fanart and did not at: " + document.location());
			return new Thumb[0];
		}
		
		return thumbList.toArray(new Thumb[thumbList.size()]);
	}

	@Override
	public MPAARating scrapeMPAA() {
		return MPAARating.RATING_XXX;
	}

	@Override
	public ID scrapeID() {		
		Element idElement = document.select("div.product-details dl dt:contains(Content ID:) ~ dd").first();
		if(idElement != null && idElement.text().length() > 0 )
		{
			// Some h.m.p titles does not feature the correct Content ID. We need to get it from another location.
			
			if (idElement.text().startsWith("41hodv") && scrapeStudio().getStudio().equals("h.m.p")) {
				Element wishListElement = document.select("div.js-add-to-wishlist[data-wishlist-id]").first();
				if (wishListElement != null) {
					return new ID(DmmParsingProfile.fixUpIDFormatting(wishListElement.attr("data-wishlist-id")));
				}
			}
			
			String r18ID = idElement.text();
			return new ID(DmmParsingProfile.fixUpIDFormatting(r18ID));
		}

		return new ID("");
	}

	@Override
	public ArrayList<Genre> scrapeGenres() {
		ArrayList<Genre> genreList = new ArrayList<>();
	//	Elements genreElements = document.select("div.product-details dl dt:contains(Categories:) ~ dd a");
		Elements genreElements = document.select("a[itemprop=genre]");
		if(genreElements != null)
		{
			for(Element currentGenre : genreElements)
			{
				String genreText = currentGenre.text();
				if(genreText.length() > 0 && !genreText.equals("Hi-Def") && !genreText.equals("Featured Actress") && !(genreText.toLowerCase().startsWith("featured")))
				{
					genreList.add(new Genre(genreText));
				}
					
			}
		}
		return genreList;
	}

	@Override
	public ArrayList<Actor> scrapeActors() {
		ArrayList<Actor> actorList = new ArrayList<>();
		Elements actorElementTabs = document.select("div.js-tab-contents div[id]");
		if(actorElementTabs != null)
		{
			for(Element currentActor : actorElementTabs)
			{
				String actorName = currentActor.select("div.txt01 div").first().text();
				String actorThumbUrl = currentActor.select("img").first().attr("src");
				if(actorName != null && actorName.length() > 0)
				{
					if(actorThumbUrl != null && actorThumbUrl.length() > 0 && !actorThumbUrl.contains("nowprinting"))
					{
						Thumb actorThumb;
						try {
							actorThumb = new Thumb(actorThumbUrl);
							Actor actorWithThumb = new Actor(actorName,"",actorThumb);
							actorList.add(actorWithThumb);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							Actor actorWithoutThumb = new Actor(actorName,"",null);
							actorList.add(actorWithoutThumb);
						}
						
					}
					else
					{
						Actor actorWithoutThumb = new Actor(actorName,"",null);
						actorList.add(actorWithoutThumb);
					}
				}
			}
		}
		return actorList;
	}

	@Override
	public ArrayList<Director> scrapeDirectors() {
		ArrayList<Director> directorList = new ArrayList<>();
		Element studioElement = document.select("div.product-details dl dt:contains(Director:) + dd").first();
		if(studioElement != null)
		{
			String directorText = studioElement.text();
			if(directorText.length() > 0 && !directorText.startsWith("-"))
				directorList.add(new Director(directorText,null));
		}
		return directorList;
	}

	@Override
	public Studio scrapeStudio() {
		Element studioElement = document.select("div.product-details dl dt:contains(Studio:) + dd a").first();
		if(studioElement != null)
		{
			String studioText = studioElement.text();
			if(studioText.length() > 0)
				return new Studio(studioText);
		}
		return Studio.BLANK_STUDIO;
	}

	@Override
	public String createSearchString(File file) {
		
		scrapedMovieFile = file;

		// The general approach is search for 'tag' + '5-digit 0-padded number'.
		// This gets pretty good results, usually a perfect match, 
		// or 2 to 5 results for clashing ids - still good for manual picking.
		
		String baseId = findIDTagFromFile(file, isFirstWordOfFileIsID()).replace("-", "");
		Pattern patternID = Pattern.compile("([0-9]*\\D+)(\\d+)");
		Matcher matcher = patternID.matcher(baseId);
		String groupOne = "";
		String groupTwo = "";
		while (matcher.find()) {
		    groupOne = matcher.group(1);
		    groupTwo = matcher.group(2);
		}

		if (groupOne == null || groupOne.isEmpty() || groupTwo == null || groupTwo.isEmpty())
			return null;
		
		int number = Integer.parseInt(groupTwo);

		// some h.m.p. titles need extra padding
		
		if (groupOne.toUpperCase().equals("HODV")) {
			return String.format("%s+%05d", groupOne, number);
		}
				
		return String.format("%s%05d", groupOne, number);
	}
	
	private SearchResult[] searchResultOnR18(String searchWord)	{
		URLCodec codec = new URLCodec();
		String searchWordURLEncoded;
		try {
			searchWordURLEncoded = codec.encode(searchWord);
			String searchPattern = "http://www.r18.com/common/search/floor=movies/searchword=" + searchWordURLEncoded + "/";
			System.out.println("Searching on R18 with this URL:" + searchPattern);
//			Document searchResultsPage = Jsoup.connect(searchPattern).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
			
			Document searchResultsPage = Jsoup.connect(searchPattern)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
					.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate")
					.header("Accept-Language", "zh-CN,zh;q=0.9")
					.header("Upgrade-Insecure-Requests", "1")
					.header("Connection", "keep-alive")
					.header("Cookie", "rtt=xjmx6Q9Vt6cd7LiT05x3WdEBrtMrQ7MVeWjc148jYhO3%2BosfSwmuqxhlrkoknBAUvxWwAv5Iy8er2lpw3xTdElvtKtUsQKBXi73vOj6SweT3HseCRr9URNuFiljDBSPuzk59rVsZSHE8jhctw0pDB67kszWWPEKVNPtRRvHQjBAMbKRs18LVDwsN78PKB1HGHEOVgqJXy6eXhO6gLN8JbpeXdLk%3D; lg=zh; ab=a; ex=USD; gid=UoFfNDEto5dgH7%2BiACzCLBYq538L1KA3gnTf187N5w2KXUhs4ytvPv39SRvm23wvx%2Fk0f26ZosBy38PWrdZjW3DOQmE%3D; _ga=GA1.2.161111165.1541821215; _gid=GA1.2.2040351927.1541821215; i3_ab=8833; bh=eyJwcHBkMDA2NzdkbDYiOiJtb3ZpZXMiLCJwcHBkMDA2NzZkbDYiOiJtb3ZpZXMifQ%3D%3D")					
					.ignoreHttpErrors(true).timeout(60000).get();			
			
			System.out.println("whole  R18 html :" + searchResultsPage);			
			
/*
 * 
 <li class="item-list">
	<a href="http://www.r18.com/videos/vod/movies/detail/-/id=1sdmu00152/?i3_ref=search&amp;i3_ord=1">
		<p>
					<img 
							class='lazy' src="http://www.r18.com/assets/img/common/img-135x188.gif" data-original="http://pics.r18.com/digital/video/1sdmu00152/1sdmu00152ps.jpg" 
						width="135" height="188">
				</p>
						<dl>
			<dt>無辜的夫婦 x 真正 nakadashi &times; 連續射精遊戲&quot;男友不工作，多次給予魷魚讓？ &quot;後射擊在最後 60 分鐘五次 &#039;不證明任何男朋友&#039; 100 萬日元 ！ 2</dt>
							<dd>~2.66&nbsp;USD&nbsp;-</dd>
					</dl>
	</a>

<div class="cmn-btn-imgHover01">

					 <div class="js-add-to-wishlist wishlist-inline" data-wishlist-id="1sdmu00152"></div>
			
		<p>
		<a class="js-view-sample"
							data-id="1sdmu00152"
										data-video-low="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu152/1sdmu152_sm_w.mp4"
										data-video-med="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu152/1sdmu152_dm_w.mp4"
										data-video-high="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu152/1sdmu152_dmb_w.mp4"
												data-product-page-url="http://www.r18.com/videos/vod/movies/detail/-/id=1sdmu00152/?i3_ref=search&amp;i3_ord=1"
			data-title="無辜的夫婦 x 真正 nakadashi &times; 連續射精遊戲&quot;男友不工作，多次給予魷魚讓？ &quot;後射擊在最後 60 分鐘五次 &#039;不證明任何男朋友&#039; 100 萬日元 ！ 2"
			data-description=""
							data-price="~2.66&nbsp;USD&nbsp;-"
										data-poster="http://pics.r18.com/digital/video/1sdmu00152/1sdmu00152ps.jpg"
									>		</a>
	</p>
	</div>
</li>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
<li class="item-list is-sale">
	<a href="http://www.r18.com/videos/vod/movies/detail/-/id=1sdmu00526/?i3_ref=search&amp;i3_ord=1">
		<p>
					<img 
							class='lazy' src="http://www.r18.com/assets/img/common/img-135x188.gif" data-original="http://pics.r18.com/digital/video/1sdmu00526/1sdmu00526ps.jpg" 
						width="135" height="188">
				</p>
					<span class="saleRibbon ">
				特價			</span>
						<dl>
			<dt><span>特價</span>第一次蜜月夫妻有限魔術鏡子子宮按摩達到高潮 20 放手 ！ 在遙遠的態度比丈夫 Moo 真正 nakadashi 毛 ！</dt>
							<dd>~1.86&nbsp;USD&nbsp;-</dd>
					</dl>
	</a>

<div class="cmn-btn-imgHover01">

					 <div class="js-add-to-wishlist wishlist-inline" data-wishlist-id="1sdmu00526"></div>
			
		<p>
		<a class="js-view-sample"
							data-id="1sdmu00526"
										data-video-low="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu00526/1sdmu00526_sm_w.mp4"
										data-video-med="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu00526/1sdmu00526_dm_w.mp4"
										data-video-high="http://awspv3001.r18.com/litevideo/freepv/1/1sd/1sdmu00526/1sdmu00526_dmb_w.mp4"
												data-product-page-url="http://www.r18.com/videos/vod/movies/detail/-/id=1sdmu00526/?i3_ref=search&amp;i3_ord=1"
			data-title="第一次蜜月夫妻有限魔術鏡子子宮按摩達到高潮 20 放手 ！ 在遙遠的態度比丈夫 Moo 真正 nakadashi 毛 ！"
			data-description=""
							data-price="~1.86&nbsp;USD&nbsp;-"
										data-poster="http://pics.r18.com/digital/video/1sdmu00526/1sdmu00526ps.jpg"
									>		</a>
	</p>
	</div>
</li>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * */			
			
			
			Elements moviesFound = searchResultsPage.select(/*".cmn-list-product01 li"*/"li[class^=item-list]");
			if(moviesFound != null && moviesFound.size() > 0)
			{
				SearchResult [] foundResults = new SearchResult[moviesFound.size()];
				int i = 0;
				for(Element searchResult : moviesFound)
				{
					String urlPath = searchResult.select("a").attr("href");
					String label = searchResult.select("img").first().attr("alt");
					Thumb previewImage = new Thumb(searchResult.select("img").first().attr("data-original"));
					SearchResult searchResultToAdd = new SearchResult(urlPath, label, previewImage);
					foundResults[i] = searchResultToAdd;
					i++;
				}
				return foundResults;
			}
			
		} catch (EncoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	

	@Override
	public SearchResult[] getSearchResults(String searchString)
			throws IOException {
		
		SearchResult[] results = null;
		
		if (searchString != null) {
			
			results = searchResultOnR18(searchString);
				
			if (results == null) {
				
				// lots of old Moodyz titles are listed by their VHS tag, 
				// those starting with 'MD' may get a good match removing the trailing 'D',
				// (MDED -> MDE, MDID -> MDI, MDLD -> MDL...)
				// result will be filtered during the amalgamation process though, need to fix that
				
				Pattern patternID = Pattern.compile("^(MD.)D(\\d+)$", Pattern.CASE_INSENSITIVE);
				Matcher matcher = patternID.matcher(searchString);
				
				if (matcher.matches()){
					String moodyzSearchPattern = matcher.replaceAll("$1$2");
					results = searchResultOnR18(moodyzSearchPattern);
				}
			}
		}

		if (results == null) {
			// results = getLinksFromGoogle(searchString, "r18.com");
			results = new SearchResult[0];
		}
		
		return results;
	}

	@Override
	public SiteParsingProfile newInstance() {
		return new R18ParsingProfile();
	}
	
	@Override
	public String toString(){
		return "R18.com";
	}

}

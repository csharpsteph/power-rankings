package update;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import parse.NBACleaner;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A runnable class which downloads the HTML source of NBA game data (with PageUpdater) and then passes it 
* to the NBACleaner to create an object file of game data.
*/
public class NBAUpdater implements Runnable {

	public static void main(String[] args) {
		new NBAUpdater().run();
	}

	@Override
	public void run() {
		updateHtml();
		updateGameData();
	}
	
	private void updateGameData()
	{
		new NBACleaner().run();
	}
	
	private void updateHtml()
	{
		String[] monthNames = {"october", "november", "december", "january", "february", "march", "april", 
				"may", "june"};
		URL source = null;
		File destination;
		String urlName;
		
		File directory = new File("resources/nba/");
		if (!directory.exists())
		{
			directory.mkdirs();
		}
		
		for (String month: monthNames)
		{
			try {
				source = new URL(String.format("https://www.basketball-reference.com/leagues/"
						+ "NBA_2018_games-%s.html", month));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			urlName = source.getFile();
			destination = new File(directory.getPath() + "/nba201718" + 
					Character.toUpperCase(month.charAt(0)) + month.substring(1, 3) + ".html");
			try {
				System.out.print("Creating file " + destination.getCanonicalPath());
				PageUpdater updater = new PageUpdater(source, destination);
				updater.update();
				System.out.println(" -- DONE.");
			}
			catch (IOException e) {
				System.out.println(" -- Could not open URL stream.");
				continue;
			}
		}
	}

}

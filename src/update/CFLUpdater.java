package update;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import parse.CFLCleaner;
//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A runnable class which downloads the HTML source of CFL game data and then passes it to the CFLCleaner
* to create an object file of game data.
*/
public class CFLUpdater implements Runnable {

	@Override
	public void run() {
		updateHtml();
		updateGameData();
	}
	
	private void updateGameData()
	{
		new CFLCleaner().run();
	}
	
	private void updateHtml()
	{
		URL source = null;
		File destination;
		
		File directory = new File("resources/cfl/");
		if (!directory.exists())
		{
			directory.mkdirs();
		}
		
		try {
			source = new URL("https://stats.cfldb.ca/league/cfl/schedule/2017");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		destination = new File(directory.getPath() + "/cfl2017.html");
		try {
			System.out.print("Creating file " + destination.getCanonicalPath());
			PageUpdater updater = new PageUpdater(source, destination);
			updater.update();
			System.out.println(" -- DONE.");
		}
		catch (IOException e) {
			System.out.println(" -- Could not open URL stream.");
		}
	}

	public static void main(String[] args) {
		new CFLUpdater().run();
	}

}

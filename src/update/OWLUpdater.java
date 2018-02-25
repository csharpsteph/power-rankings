package update;

import parse.OWLCleaner;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A runnable class which downloads the HTML source of Overwatch League game data (with PageUpdater) 
* and then passes it to the OWLCleaner to create an object file of game data.
*/
public class OWLUpdater implements Runnable {

	public static void main(String[] args) {
		new OWLUpdater().run();

	}
	@Override
	public void run() {
		updateGameData();
	}
	
	public void updateGameData()
	{
		new OWLCleaner().run();
	}
}

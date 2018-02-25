package update;
import java.io.*;
import java.net.URL;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A class which downloads a given HTML source of game data.
*/
public class PageUpdater {
	
	private URL source;
	File destination;
	public PageUpdater(URL source, File destination)
	{
		this.source = source;
		this.destination = destination;
	}
	
	
	
	public void update() throws IOException
	{
		InputStream stream = null;
		try {
			stream = source.openStream();
		} catch (IOException e) {
			throw new IOException(String.format("Could not open stream for URL %s", source));
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		
		FileWriter writer = new FileWriter(destination);
		String line;
		while((line = reader.readLine()) != null)
		{
			writer.write(line);
		}
		writer.close();
		reader.close();
		stream.close();
	}
}

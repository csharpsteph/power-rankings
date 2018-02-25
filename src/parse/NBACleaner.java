package parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A class representing a process of cleaning a group of HTML files and creating an object file of 
* NBA game information.
*/
public class NBACleaner implements Runnable {
	
	private File[] htmlSources;
	private File output;
	
	private static SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy");
	
	void writeOutput() throws FileNotFoundException, IOException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(new FileOutputStream(this.output));
		Date date = null;
		String team1, team2;
		short team1Score, team2Score;
		String teamScoreText;
		String csk;
		Element tempElem;
		
		for (int i = 0; i < htmlSources.length; i++)
		{
			System.out.printf("Writing game data for month %d...", i);
			if (!htmlSources[i].exists())
			{
				System.out.println(String.format("File %s does not exist.", 
						htmlSources[i].getName()));
				continue;
			}
			Elements rows = getRows(i);
			for (Element row: rows)
			{
				tempElem = row.selectFirst("td[data-stat=visitor_team_name]");
				csk = tempElem.attr("csk");
				team1 = csk.substring(0, csk.indexOf('.'));
				if (team1.equals("CHO")) { team1 = "CHA"; } // Correct abbr. for Charlotte Hornets
					
				
				tempElem = row.selectFirst("td[data-stat=home_team_name]");
				csk = tempElem.attr("csk");
				team2 = csk.substring(0, csk.indexOf('.'));
				if (team2.equals("CHO")) { team2 = "CHA"; }
				
				tempElem = row.selectFirst("a");
				try 
				{
					date = format.parse(tempElem.text());
				}
				catch (ParseException ex)
				{
					ex.printStackTrace();
				}
				
				tempElem = row.selectFirst("td[data-stat=visitor_pts]");
				teamScoreText = tempElem.text();
				team1Score = teamScoreText.isEmpty() ? Short.MIN_VALUE : Short.parseShort(teamScoreText);
				
				tempElem = row.selectFirst("td[data-stat=home_pts]");
				teamScoreText = tempElem.text();
				team2Score = teamScoreText.isEmpty() ? Short.MIN_VALUE : Short.parseShort(teamScoreText);
				
				ostream.writeObject(date);
				ostream.writeUTF(team1);
				ostream.writeShort(team1Score);
				ostream.writeUTF(team2);
				ostream.writeShort(team2Score);
			}
			ostream.reset();
			System.out.println(" -- DONE.");
		}
		
		ostream.writeObject(null); // EOF marker
		ostream.close();
	}
	
	Elements getRows(int sourceIndex) throws IOException 
	{
		Document doc = Jsoup.parse(htmlSources[sourceIndex], "UTF-8");
		Elements rows = doc.select("table#schedule tbody tr");
		return rows;
	}

	public static void main(String[] args) throws IOException, ParseException {
		new NBACleaner().run();
	}

	@Override
	public void run() {
		File output = new File("resources/nba/nba-clean.dat");
		String months[] = {"Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun"};
		
		String prefix = "resources/nba/nba201718", suffix = ".html";
		File sourceFiles[] = new File[months.length];
		for (int i = 0; i < months.length; i++)
		{
			sourceFiles[i] = new File(prefix + months[i] + suffix);
		}
			
		this.htmlSources = sourceFiles;
		this.output = output;
		for (File f: htmlSources)
		{
			if (!f.exists()) {
				System.out.println(String.format(
						"Could not find specified file: %s", f.getName()));
			}
		}
		
		try {
			writeOutput();
		} catch (FileNotFoundException e) {
			System.out.println(" " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Could not start writing data to stream.");
		}
	}

}

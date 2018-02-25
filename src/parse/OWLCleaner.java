package parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

//@author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
* A class representing a process of parsing a text file and creating an object file of 
* Overwatch League game information.
*/
public class OWLCleaner implements Runnable {
	File source, output;
	private static SimpleDateFormat format = new SimpleDateFormat("EEE MMM d yyyy");
		
	void writeOutput() throws IOException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(new FileOutputStream(this.output));
		
		Scanner scan = new Scanner(source);
		String line;
		Date date = null;
		String[] lineTokens;
		String team1 = null, team2 = null;
		int stage = -1, week = -1;
		int team1Score, team2Score;
		boolean isPlayoff;
		
		while (scan.hasNext())
		{
			line = scan.nextLine();
			if (line.startsWith("Stage"))
			{
				lineTokens = line.split("[\\s-]+");
				try {
					stage = Integer.parseInt(lineTokens[1]);
					week = Integer.parseInt(lineTokens[3]);
					System.out.printf("Writing stage %d, week %d...\n", stage, week);
				} catch (NumberFormatException ex)
				{
					ex.printStackTrace();
				}
				continue;
			}
			
			if (line.startsWith("Date"))
			{	
				try {
					date = format.parse(line.substring(6));
				} catch (ParseException ex) {
					ex.printStackTrace();
				}
				continue;
			}
			
			lineTokens = line.split("\\s");
			team1 = lineTokens[0];
			team2 = lineTokens[2];
			if (lineTokens[1].charAt(0) == '-')
			{
				team1Score = -1;
			} else 
			{
				team1Score = Integer.parseInt(lineTokens[1]);
			}
			if (lineTokens[3].charAt(0) == '-')
			{
				team2Score = -1;
			} else 
			{
				team2Score = Integer.parseInt(lineTokens[3]);
			}
			if (lineTokens.length < 5)
			{
				isPlayoff = false;
			} else
			{
				isPlayoff = true;
			}
			
			ostream.writeObject(date);
			ostream.writeByte(stage);
			ostream.writeByte(week);
			ostream.writeUTF(team1);
			ostream.writeByte(team1Score);
			ostream.writeUTF(team2);
			ostream.writeByte(team2Score);
			ostream.writeBoolean(isPlayoff);
		}
		ostream.writeObject(null);	// EOF marker
		
		scan.close();
		ostream.close();
	}
	

	public static void main(String[] args) throws FileNotFoundException {
		new OWLCleaner().run();
	}


	@Override
	public void run() {
		File source = new File("resources/owl/owlSeason1.txt");
		File output = new File("resources/owl/owl-clean.dat");
		
		try 
		{
			this.source = source;
			this.output = output;
			if (!source.exists())
			{
				throw new FileNotFoundException(String.format(
						"Could not find specified file: %s", source.getName()));
			}
			writeOutput();
		}
		catch (FileNotFoundException ex) 
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

}

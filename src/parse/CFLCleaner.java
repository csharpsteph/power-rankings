package parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

// @author Cody J. Stephens ({@code https://github.com/csharpsteph/})

/*
 * A class representing a process of cleaning an HTML file and creating an object file of 
 * CFL game information.
 */
public class CFLCleaner implements Runnable {
	
	File htmlSource, output;
	static SimpleDateFormat format = new SimpleDateFormat("EEE. MMM d");
	static {
		java.text.DateFormatSymbols symbols = format.getDateFormatSymbols();
		symbols.setShortMonths(new String[]{"Jan.", "Feb.", "Mar.", "Apr.", "May", "June", "July", 
				"Aug.", "Sept.", "Oct.", "Nov.", "Dec."});
		format.setDateFormatSymbols(symbols);
	}
	
	void output() throws IOException, ParseException
	{
		writeOutput(getRows());
	}
	
	/*
	 *  Retrieves the table rows corresponding to regular-season games and playoff games,
	 *  including the Grey Cup. 
	 */
	Elements getRows() throws IOException
	{	
		Document doc = null;
		doc = Jsoup.parse(htmlSource, "UTF-8");
		
		Elements rows = doc.select("tbody tr.reg");
		rows.addAll(doc.select("tbody tr.ply"));
		rows.addAll(doc.select("tbody tr.gc"));
		return rows;
	}
	
	// Writes pased table rows to file.
	void writeOutput(Elements rows) throws IOException, ParseException
	{
		ObjectOutputStream ostream = new ObjectOutputStream(new FileOutputStream(this.output));
		String weekNumber = null;
		String team1, team2;
		int team1Score, team2Score;
		Element tempElem;
		String tempText;
		Date date = null;
		
		for (Element row: rows)
		{
			tempText = row.child(0).text().trim();
			if (!tempText.isEmpty())
			{
				weekNumber = tempText;
				System.out.println("Writing week " + weekNumber + ".");
			}
			
			tempElem = row.select("div[class$=text-left]").first();
			tempText = tempElem.text().trim();
			if (!tempText.isEmpty())
			{
				date = parse(tempText);
			}
			tempElem = row.select("a").first();
			tempText = tempElem.text();
			String result[] = tempText.split("\\s");
			team1 = result[0];
			team1Score = Integer.parseInt(result[1]);
			team2 = result[3];
			team2Score = Integer.parseInt(result[4]);
			
			ostream.writeUTF(weekNumber);
			ostream.writeObject(date);
			ostream.writeUTF(team1);
			ostream.writeShort(team1Score);
			ostream.writeUTF(team2);
			ostream.writeShort(team2Score);
		}
		
		ostream.writeUTF("~");
		
		ostream.close();
	}
	
	// Parses a String and returns a date.
	private Date parse(String s) throws ParseException
	{
		Date date = null;
		try {
			date = format.parse(s);
		} catch (ParseException e) {
			throw new ParseException("Could not parse date from string " + s + ".", e.getErrorOffset());
		}
		// Create a Calendar object to set the year to 2017.
		java.util.Calendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, 2017);
		// Return a Date.
		date = calendar.getTime();
		return date;
	}
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		new CFLCleaner().run();
	}

	@Override
	public void run() {
		File source = new File("resources/cfl/cfl2017.html");
		File output = new File("resources/cfl/cfl-clean.dat");
		this.htmlSource = source;
		this.output = output;
		if (!htmlSource.exists())
		{
			System.out.println(String.format(
					"Could not find specified file: %s", htmlSource.getName()));
		}
		try {
			output();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	

}
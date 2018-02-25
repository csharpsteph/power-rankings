package metrics;

import nodes.Competitor;
import nodes.Division;
import nodes.Graph;
import nodes.Node;
import nodes.Team;
import parse.AbstractGame;
import parse.CFLGame;
import parse.CFLReader;
import parse.GameReader;
import parse.NBAGame;
import parse.NBAReader;
import parse.OWLGame;
import parse.OWLReader;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Cody J. Stephens ({@code https://github.com/csharpsteph/})
 */


/*
 * The project's primary script. Receives user input to output elo ratings, team records, and lists 
 * of games for the Canadian Football League (CFL), National Basketball Association (NBA), and
 * Overwatch League (OWL). 
 */
public class MetricsScript implements Runnable {
	
	// Types of information that can be requested
	private enum InfoType { ELO, RECORDS, GAMES, OPPONENT_RECORDS; }
	private enum LeagueType { CFL, NBA, OWL; }
	
	private boolean isRunning;
	
	// Main search state variables
	private InfoType infoType;
	private GameList<?> gameList = null;
	private LeagueType league;
	
	// Readers
	private Scanner scan;
	private CFLReader cflReader;
	private NBAReader nbaReader;
	private OWLReader owlReader;
	
	// Commands
	private Map<String, LeagueType> leagueCommands;
	private Map<String, InfoType> infoCommands;
	private Set<String> gameParams, recordParams, ratingParams, recordSortTypes;
	
	public void run()
	{
		init();
	}

	// Entry point
	private void init()
	{
		// Load data
		scan = new Scanner(System.in);
		File teamDataSource;
		File gameDataSource;
		
		try {
			teamDataSource = new File("resources/cfl/cfl-graph.dat");
			gameDataSource = new File("resources/cfl/cfl-clean.dat");
			cflReader = new CFLReader(teamDataSource, gameDataSource);
			teamDataSource = new File("resources/nba/nba-graph.dat");
			gameDataSource = new File("resources/nba/nba-clean.dat");
			nbaReader = new NBAReader(teamDataSource, gameDataSource); 
			teamDataSource = new File("resources/owl/owl-graph.dat");
			gameDataSource = new File("resources/owl/owl-clean.dat");
			owlReader = new OWLReader(teamDataSource, gameDataSource);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set up commands
		leagueCommands = new HashMap<>();
		leagueCommands.put("CFL", LeagueType.CFL);
		leagueCommands.put("NBA", LeagueType.NBA);
		leagueCommands.put("OWL", LeagueType.OWL);
		infoCommands = new HashMap<>();
		infoCommands.put("ELO", InfoType.ELO);
		infoCommands.put("RATINGS", InfoType.ELO);
		infoCommands.put("GAMES", InfoType.GAMES);
		infoCommands.put("MATCHES", InfoType.GAMES);
		infoCommands.put("RECORDS", InfoType.RECORDS);
		
		// Set up parameters; useful for identifying mistyped parameters
		
		
		/* The 'same-list' parameter really only applies to the secondary opponent records game search, 
		 * but is included here for all game searches. 
		 */
		gameParams = new HashSet<>(Arrays.asList(new String[]{"start", "end", "away", "home", "team",
				"playoffs", "regular", "complete", "incomplete", "same-list"}));
		recordParams = new HashSet<>(Arrays.asList(new String[] {"type", "sort"}));
		ratingParams = new HashSet<>(Arrays.asList(new String[] 
				{"stage", "week", "date", "game", "sort", "initial"}));
		recordSortTypes = new HashSet<>(Arrays.asList(new String[] 
				{"record", "team", "division", "conference"}));
		
		System.out.println("Metrics for teams in various sports leagues.");
		System.out.println("Type \"help\" for more information. Type \"exit\" to end program.\n");
		
		isRunning = true;
		while (isRunning)
		{
			seekInput();
		}
	}
	
	// The main control function of the program. Progresses only when the user provides a valid query.
	@SuppressWarnings("unchecked")
	private void seekInput()
	{
		if (league == null)
		{
			// Sets the league for user queries.
			handleLeagueInput(seekLeagueInput());
		} 
		else if (gameList == null) 
		{
			// Returns a list of games to be used in a subsequent query.
			gameList = handleGameInput(seekGameInput());
		} 
		else if (infoType == null)
		{
			// Sets the type of information to be requested.
			handleInfoInput(seekInfoInput());
		}
		else if (infoType != null)
		{
			String input;
			
			switch (infoType)
			{
				// Prints Elo ratings up to a specified point in time.
				case ELO:
				{
					input = seekEloInput();
					if (input == null) { return; }
					if (handleEloInput(input) == null) { return; }
					break;
				}
				// No input needed for 'games'. Simply, print the list.
				case GAMES:
				{
					printGames(gameList);
					break;
				}
				// Prints a specified set of records.
				case RECORDS:
				{
					input = seekRecordInput();
					boolean recordsFound = false;
					switch (league)
					{
						case CFL:
						{
							recordsFound = 
									handleRecordInput(input, (GameList<CFLGame>) gameList, cflReader);
							break;
						}
						case NBA:
						{
							recordsFound = 
									handleRecordInput(input, (GameList<NBAGame>) gameList, nbaReader);
							break;
						}
						case OWL:
						{
							recordsFound = 
									handleRecordInput(input, (GameList<OWLGame>) gameList, owlReader);
							break;
						}
					}
					if (!recordsFound)
					{
						return;
					}
					break;
				}
				default:
				{
					System.out.println("Error: Reached default");
				}
			}			
			/* TODO: Allow the program to run persistently. */
			close();
		}
	}
	
	// Requests input from user.
	private String seekInput(String message)
	{
		System.out.println(message);
		System.out.print(">>> ");
		return scan.nextLine();
	}

	/* Returns commands and parameters specified by user. Also handles request to exit and requests 
	 * for information on how to compose a search.  
	 */
	private String[] handleInput(String inputLine)
	{
		/* Splits input across whitespace and any hyphens (to account for inputs like '--start'.)*/
		String[] lineParts = inputLine.split("[\\s]+-*");
		String command = lineParts[0];
		if (command.equals("exit"))
		{
			close();
			return null;	/* Returning null after closing ends the 'while(isRunning)' loop,
			 				 * which ends the program. */
		}
		else if (command.equals("help"))
		{
			printHelp();
			return null;	// Returning null causes seekInput() to again ask for input.
		}
		// Prints list of team codes for use in constraining a game list.
		else if (command.equals("teams"))
		{
			if (league == null)
			{
				System.out.println("No league has been specified yet, so the program "
						+ "cannot yet provide team codes.");
			}
			else {
				printGraph(getCurrentReader().teamGraph());
			}
			return null;
		}
		return lineParts;
	}

	private String seekLeagueInput()
	{
		return seekInput("Enter the abbreviation for the league you'd like to learn about.");
	}
	
	// Sets the value of 'league'.
	private void handleLeagueInput(String input)
	{
		String[] paramList = handleInput(input);
		if (paramList == null) return;	// Happens after "exit" or "help" command.
		
		String command = paramList[0]; 
		command = command.toUpperCase();
		league = leagueCommands.get(command);
		if (league == null)
		{
			System.out.println("Couldn't find a league with that abbreviation.");
			System.out.println("Type \"help\" for more information.");
		}
		else
			System.out.println(league);
	}
	
	private String seekGameInput()
	{
		return seekInput("Please define the set of games you'd like to learn about. Type "
				+ "\'teams\' to learn the codes to use in queries which constrain by team.\n" + 
				"Or, if you'd just like to see all the completed regular-season games, press Enter " +
				"without entering a command.");
	}
	
	/* Notifies users of empty parameter lists and invalid parameters (e.g. 'homr' as a misspelling of 
	 * valid parameter 'home'). 
	 */
	private boolean flagInvalidParameters(Set<String> passedParams, Set<String> legalParams)
	{
		boolean flagged = false;
		if (passedParams.isEmpty())
		{
			System.out.println("No parameters found. Applying default settings.");
		}
		else
		{
			for (String param: passedParams)
			{
				if (!legalParams.contains(param))
				{
					System.out.printf("\'%s\' is an invalid parameter and will be disregarded.\n", param);
					flagged = true;
				}
			}
		}
		return flagged;
	}
	
	// Returns a GameList for use in the next stage of the query.
	private GameList<?> handleGameInput(String input)
	{
		String[] paramList = handleInput(input);
		if (paramList == null) return null;	// Happens after "exit" or "help" command.
		
		Date startDate = null, endDate = null;
		int startStage = -1, endStage = -1;
		int startWeek = -1, endWeek = -1;
		boolean includePlayoffs, includeRegularSeason = true,
				includeComplete = true, includeIncomplete;
		
		Map<String, String> paramMap = getParameters(paramList);
		
		flagInvalidParameters(paramMap.keySet(), gameParams);
		
		/* 
		 * Specify the start point of the games we'd like to see, either by date or (for leagues in which games are organized by week)
		 * by week number.
		 */
		if (paramMap.containsKey("start"))
		{
			String startString = paramMap.get("start");
			Object startValue = parseDateOrString(startString);
			if (startValue instanceof Date)
			{
				startDate = (Date)startValue;
			} 
			else
			{
				switch (league)
				{
					case CFL:
					{
						// Note: doesn't prevent week 0 as an input.
						try { startWeek = Integer.parseInt(startString); }
						catch (NumberFormatException ex)
						{
							System.out.println("Couldn't detect either a start date or a week number.");
							System.out.println("Type \"help\" to learn how the program receives dates.\n");
							seekGameInput();
						}
						break;
					}
					// NBA doesn't do weeks, so date is the only way to specify start.
					case NBA:
					{
						System.out.println("Couldn't detect a start date.");
						System.out.println("Type \"help\" to learn how the program receives dates.\n");
						return null;
					}
					// OWL does stages and weeks, so if no date is found, resort to stage/week combination.
					case OWL:
					{
						int[] stageAndWeek = parseStageAndWeek(startString);
						startStage = stageAndWeek[0];
						startWeek = stageAndWeek[1];
						// parseStageAndWeek returns -1 in cases of invalid (non-integer) input.
						if (startStage < 0)
						{
							System.out.println("Couldn't detect a start date or stage number.");
							System.out.println("Type \"help\" to learn how the program receives dates" +
									"and stage/week information.");
							System.out.println("Applying default start setting.");
						}
						break;
					}
					default:
						System.out.println("Reached START default");
						break;
				}
			}
		}
		
		// Perform the same operation as above for end dates/weeks.
		if (paramMap.containsKey("end"))
		{
			String endString = paramMap.get("end");
			Object endValue = parseDateOrString(endString);
			if (endValue instanceof Date)
			{
				endDate = (Date)endValue;
			} else
			{
				switch (league)
				{
					case CFL:
					{
						try { endWeek = Integer.parseInt(endString); }
						catch (NumberFormatException ex)
						{
							System.out.println("Couldn't detect either an end date or a week number.");
							System.out.println("Type \"help\" to learn how the program receives dates.\n");
							seekGameInput();
						}
						break;
					}
					
					case NBA:
					{
						System.out.println("Couldn't detect an end date.");
						System.out.println("Type \"help\" to learn how the program receives dates.\n");
						return null;
					}
					
					case OWL:
					{
						int[] stageAndWeek = parseStageAndWeek(endString);
						endStage = stageAndWeek[0];
						endWeek = stageAndWeek[1];
						if (endStage < 0)
						{
							System.out.println("Couldn't detect an end date or stage number.");
							System.out.println("Type \"help\" to learn how the program receives dates" +
									"and stage/week information.");
							System.out.println("Applying default end setting.");
						}
						break;
					}
					default:
						System.out.println("Reached END default");;
						break;
				}
			}
			
		}
		
		/* By default, include all games, whether regular season or playoffs,
		 * complete or incomplete */
		includePlayoffs = paramMap.getOrDefault("playoffs", "y").equalsIgnoreCase("n") ?
				false : true;
		includeRegularSeason = paramMap.getOrDefault("regular", "y").equalsIgnoreCase("n") ? 
				false : true;
		includeComplete = paramMap.getOrDefault("complete", "y").equalsIgnoreCase("n") ? 
				false : true;
		includeIncomplete = paramMap.getOrDefault("incomplete", "y").equalsIgnoreCase("n") ?
				false: true;
		
		// Team parameters: filters games by whether any of the specified teams play.  
		String[] team1Strs = null, team2Strs = null, generalTeamStrs = null;
		Set<String> roadStrSet = new HashSet<>();
		Set<String> awayStrSet = new HashSet<>();
		if (paramMap.containsKey("away"))	// Away team must be one of the teams specified here.
		{
			awayStrSet.addAll(Arrays.asList(paramMap.get("away").split(",+")));
		}
		if (paramMap.containsKey("road"))
		{
			roadStrSet.addAll(Arrays.asList(paramMap.get("road").split(",+")));
		}
		awayStrSet.addAll(roadStrSet);	// 'away' and 'road' parameters behave the same
		team1Strs = awayStrSet.toArray(new String[awayStrSet.size()]);
		if (paramMap.containsKey("home"))	// Home team must be one of the teams ID'd here.
		{
			team2Strs = paramMap.get("home").split(",+");
		}
		if (paramMap.containsKey("team"))	// Either team must be one of the teams ID's here.
		{
			generalTeamStrs = paramMap.get("team").split(",+");
		}
		
		GameList<?> newGameList;
		// Return a game list	
		switch (league)
		{
			case CFL:
			{
				newGameList = cflGameList(startDate, endDate, startWeek, endWeek, includePlayoffs,
						includeRegularSeason, includeComplete, includeIncomplete, team1Strs, 
						team2Strs, generalTeamStrs);
				break;
			}  
			case OWL:
			{
				newGameList = owlGameList(startDate, endDate, startStage, startWeek, endStage, endWeek,
						includePlayoffs, includeRegularSeason, includeComplete, includeIncomplete,
						team1Strs, team2Strs, generalTeamStrs);
				break;
			} 
			case NBA:
			{
				newGameList = nbaGameList(startDate, endDate, includePlayoffs, includeRegularSeason, 
						includeComplete, includeIncomplete, team1Strs, team2Strs, generalTeamStrs);
				break;
			}
			default:
			{
				System.out.println("Reached LEAGUE default at end of handleGameInput");
				newGameList = null;
				break;
			}
		}
		
		// Feedback to user input: number of games found; asks again for input if search comes up empty.
		System.out.printf("Found %d games.\n", newGameList.size());	
		if (newGameList.size() == 0)
		{
			System.out.println("Please try different search criteria.");
			newGameList = null;
		}
		return newGameList;

	}
	
	private String seekInfoInput()
	{
		return seekInput("Please specify which type of information you seek.");
	}
	
	private void handleInfoInput(String input)
	{
		String[] paramList = handleInput(input);
		if (paramList == null) return;	// Happens after "exit" or "help" command.
		
		String command = paramList[0]; 
		command = command.toUpperCase();
		infoType = infoCommands.get(command);
		if (infoType == null)
		{
			System.out.println("Your input did not match any information types.");
			System.out.println("Type \"help\" for more information.\n");			
		}
		else
			System.out.println(infoType);		
	}
	
	// Prints game information along with the game's index in gameList.
	private <Game extends AbstractGame<?>> void printGames(GameList<Game> gameList)
	{
		int i = 0;
		for (Game game: gameList)
		{
			System.out.printf("%d: %s\n", ++i, game);
		}
	}
	
	/* Prints divisions and teams in the league - for ID'ing teams/divisions
	 * to filter a game list. 
	 */
	public static <N extends Comparable<N>> void printGraph(Graph<N> graph)
	{
		System.out.println("A list of keys:");
		Stack<Node<N>> stack = new Stack<>();
		stack.add(graph.getRoot());
		int tabLevel = 0;
		Map<Node<N>, Integer> levelsToNodes = new HashMap<>();
		Node<N> currentNode, parentNode;
		while (!stack.isEmpty())
		{
			currentNode = stack.pop();
			parentNode = currentNode.getParent();
			if (parentNode != null && currentNode.getParent() == parentNode)
			{
				tabLevel = levelsToNodes.get(parentNode) + 1;
			}	// Prints child node (e.g. team) tabbed below its parent node (e.g. division)
			levelsToNodes.put(currentNode, tabLevel);
			
			for (int i = 0; i < tabLevel; i++) { System.out.print("  "); }
			System.out.printf("%s (%s)\n", currentNode, currentNode.getKey());
			
			stack.addAll(currentNode.getChildren());
		}
	}
	
	private String seekEloInput()
	{
		return seekInput("Please provide a time point for the power ratings, plus any sort settings." + 
				" Or, if you want the latest applicable ratings, simply press Enter without specifying "
				+ "any commands.");
	}
		
	/* Returns a map of competitiors to their Elo ratings.
	 * Takes either a game number, a week number, or a date to determine the point for which
	 * the program creates power ratings based on the game list.
	 */
	/* TODO: Delete the game index search and perhaps even shorten nba/owl/cflGameList functions
	 * by adding functions which constrain by date, week, and game number.
	 */
	private Map<Competitor, Double> handleEloInput(String input)
	{
		String[] paramList = handleInput(input);
		if (paramList == null) return null;
		Map<String, String> paramMap = getParameters(paramList);
		flagInvalidParameters(paramMap.keySet(), ratingParams);
		int endGameIndex = 0;
		
		if (paramMap.containsKey("game"))
		{	// Takes an index of gameList
			try 
			{
				endGameIndex = Integer.parseInt(paramMap.get("game"));
				// Constrain game number to [1, gameList.size()]
				if (endGameIndex > gameList.size())
				{
					System.out.printf("Could not find game #%d, as the game list "
							+ "contains %d elements\n", endGameIndex, gameList.size());
					return null;
				}
				else if (endGameIndex < 1)
				{
					System.out.printf("Game numbers cannot be less than 1.\n", endGameIndex);
					return null;
				}
			} 
			catch (NumberFormatException ex)
			{
				System.out.println("Couldn't detect a valid game number.\n");
				return null;
			}
		}
		else if (paramMap.containsKey("date"))
		{	
			DateFormat df = new SimpleDateFormat("M/d/yy");
			try 
			{
				Date endDate = df.parse(paramMap.get("date"));
				// Try to preempt the linear search, in case of an extreme value
				if (gameList.get(gameList.size() - 1).getDate().compareTo(endDate) < 0)
				{
					endGameIndex = gameList.size();
				}
				else 
				{
					while (endGameIndex < gameList.size() &&
							gameList.get(endGameIndex).getDate().compareTo(endDate) <= 0)
					{
						++endGameIndex;
					}
					System.out.printf("Searching all games up to and including %s\n", df.format(endDate));
				}
			} 
			catch (ParseException e) 
			{
				System.out.println("Your input was not recognized as a date. Please enter dates " +
						"in the format MM/DD/YY (or MM/DD/YYYY)");
				return null;				
			}
		}
		else if (paramMap.containsKey("week"))
		{
			String weekStr = paramMap.get("week");
			
			switch(league)
			{
				// Disregard, in the case of the NBA
				case NBA:
					System.out.println("The NBA does not identify games by week. Disregarding "
							+ "this parameter...");
					break;
				case CFL:
				{
					int endWeekNumber = -1;
					try 
					{
						endWeekNumber = Integer.parseInt(weekStr);
					} 
					catch (NumberFormatException ex)
					{
						System.out.println("No week number detected.\n");
						return null;
					}
					if (endWeekNumber < 1)
					{
						System.out.println("Invalid week number. Week must be at least 1.\n");
						return null;
					}
					
					CFLGame game = (CFLGame)gameList.get(gameList.size() - 1);
					if (game.getWeekNumber() < endWeekNumber)
					{
						endGameIndex = gameList.size();
					}
					else {
						while (endGameIndex < gameList.size())
						{
							game = (CFLGame)gameList.get(endGameIndex);
							if (game.getWeekNumber() > endWeekNumber) { break; }
							else { endGameIndex++; }
						}
					}
					break;
				}
				case OWL:
				{
					/* Allows two methods of stage/week entry for Overwatch League... */
					int stageNumber = -1 , weekNumber = -1;
					
					// First, test for the composite [stage-week] form.
					int[] stageAndWeek = parseStageAndWeek(weekStr);
					int n1 = stageAndWeek[0], n2 = stageAndWeek[1];
					if (n1 >= 0)	// The composite form MIGHT be used. Could be only the week number
					{
						if (n2 >= 0) // The composite form is used
						{
							stageNumber = n1;
							weekNumber = n2;
							if (stageNumber < 1 || weekNumber < 1)
							{
								System.out.println("Stage and week number must be at least 1.");
								return null;
							}
						}
						else { weekNumber = n1; }
					}
					
					// Composite form is not used. Only the week number. Look to stage parameter.
					if (stageNumber < 0)
					{
						if (paramMap.containsKey("stage"))
						{
							try 
							{
								stageNumber = Integer.parseInt(paramMap.get("stage"));
								/* TODO: How does this work with the playoffs, if the playoffs are
								 * here represented by stageNumber > 4? Will there be week numbers?
								 */
							} 
							catch (NumberFormatException ex)
							{
								System.out.println("No stage number detected.\n");
								return null;
							}
						}
						else	// There is no stage parameter 
						{
							System.out.println("No stage number detected.\n");
							return null;
						}
					}
					/*
					 *  TODO: Generalize these search functions with a method that takes a predicate.
					 *  One for each game type
					 */
					OWLGame game = (OWLGame)gameList.get(gameList.size() - 1);
					// Attempt to preempt search
					if (game.getStageNumber() < stageNumber)
					{
						endGameIndex = gameList.size();
					}
					else if (game.getStageNumber() == stageNumber && game.getWeekNumber() <= weekNumber)
					{
						endGameIndex = gameList.size();
					}
					else 
					{
						while (endGameIndex < gameList.size())
						{
							game = (OWLGame)gameList.get(endGameIndex);
							if (game.getStageNumber() > stageNumber) break;
							else if (game.getStageNumber() == stageNumber && 
									game.getWeekNumber() > weekNumber) break;
							else { endGameIndex++; }
						}
					}
					break;
				}
			}	
		}
		// When there is no week parameter, search to the last week of the given stage.
		// TODO: This branch could be combined (via an or-condition) with the above condition, to save space.
		else if (paramMap.containsKey("stage") && league == LeagueType.OWL)
		{	
			
				int endStageNumber = -1;
				try 
				{
					endStageNumber = Integer.parseInt(paramMap.get("stage"));
				} 
				catch (NumberFormatException ex)
				{
					System.out.println("No stage number detected.\n");
					return null;
				}
				
				if (endStageNumber < 1)
				{
					System.out.println("Invalid stage number. Stage must be at least 1.\n");
					return null;
				}
				
				OWLGame game = (OWLGame)gameList.get(gameList.size() - 1);
				if (game.getStageNumber() < endStageNumber)
				{
					endGameIndex = gameList.size();
				}
				else 
				{
					while (endGameIndex < gameList.size())
					{
						game = (OWLGame)gameList.get(endGameIndex);
						if (game.getStageNumber() > endStageNumber) { break; }
						else { endGameIndex++; }
					}
				}			
		}
		else { endGameIndex = gameList.size(); }
		
		/* Make the map */
		Map<Competitor, Double> ratings = null;
		
		/* An initial value for Elo ratings. Many organizations use a default value of 1500. I use 0,
		 * as this clearly shows which competitors are above and below average.
		 */
		int initial = 0;
		if (paramMap.containsKey("initial"))
		{
			try 
			{
				initial = Integer.parseInt(paramMap.get("initial"));
			} 
			catch (NumberFormatException ex)
			{
				System.out.println("No integer detected for initial value. Please use an integer.\n");
				return null;
			}
		}
		
		// Return a map, using the Elo function for the current league.
		switch (league)
		{
			case CFL:
			{
				@SuppressWarnings("unchecked")
				PowerRating<CFLGame> eloReader = new PowerRating<CFLGame>(null, (double)initial, 
						new CFLEloFunction(), (GameList<CFLGame>) gameList, true);
				eloReader.process();
				ratings = eloReader.getRatingsBeforeGame(endGameIndex);
				break;
			}
			case NBA:
			{
				@SuppressWarnings("unchecked")
				PowerRating<NBAGame> eloReader = new PowerRating<NBAGame>(null, (double)initial, 
						new NBAEloFunction(), (GameList<NBAGame>) gameList, true);
				eloReader.process();
				ratings = eloReader.getRatingsBeforeGame(endGameIndex);
				break;
			}
			case OWL:
			{
				@SuppressWarnings("unchecked")
				PowerRating<OWLGame> eloReader = new PowerRating<OWLGame>(null, (double)initial, 
						new OWLEloFunction(), (GameList<OWLGame>) gameList, true);
				eloReader.process();
				ratings = eloReader.getRatingsBeforeGame(endGameIndex);
				break;
			}
		}
		
		// Notify user of the search result's bounds.
		System.out.printf("Searching up to game %d\n---\n", endGameIndex);
		
		// Sort ratings by team or division or value (which is the default)
		String compareStr = paramMap.get("sort");
		if (compareStr != null)
		{ 	/* If the string identifying the sort criterion is invalid, 
			   notify the user and set to null (and thus to default value).
			*/		
			Set<String> nonceSet = new HashSet<>();
			nonceSet.add(compareStr);
			if (flagInvalidParameters(nonceSet, recordSortTypes))
			{
				compareStr = null;
			}
		}
		printRecords(ratings, compareStr, getCurrentReader().competitorGraph(), true);
		return ratings;
	}
	
	private String seekRecordInput()
	{
		return seekInput("Please specify any settings for reported records." + 
				" Or, if you want the latest team records, simply press Enter without specifying any commands.");
	}

	/* Takes in parameters for searches of overall team records, home and away records, and opponent
	 * records.
	 */
	private <Game extends AbstractGame<?>, C extends Competitor> boolean handleRecordInput
		(String input, GameList<Game> gameList, GameReader<Game, C, ?> teamDataReader)
	{
			
		Map<String, C> teamMap = teamDataReader.teamMap();
		String[] params = handleInput(input);
		if (params == null) { return false; }
		Map<String, String> paramMap = getParameters(params);
		flagInvalidParameters(paramMap.keySet(), recordParams);
		
		/* This object stores team, home, and away records and can later search for opponent records
		 * via a static method. */
		TeamRecords<Game, C> recordsReader = new TeamRecords<>(teamMap, gameList);
		recordsReader.readGames();
		Map<C, Record> recordsMap = null;
		
		if (paramMap.isEmpty())
		{
			System.out.println("No parameters found. Constructing default game list.");
		}
		else
		{
			HashSet<String> legalParams = new HashSet<>(recordParams);
			for (String param: paramMap.keySet())
			{
				if (!legalParams.contains(param))
				{
					System.out.printf("%s is an invalid parameter and will be disregarded.\n", param);
				}
			}
			
		}
		
		String typeStr = paramMap.getOrDefault("type", "team");
		if (typeStr.equals("team"))
		{
			System.out.println("Team records");
			recordsMap = recordsReader.teamRecords();
		}
		else if (typeStr.equals("home"))
		{
			System.out.println("Home records");
			recordsMap = recordsReader.homeRecords();
		}
		else if (typeStr.equals("away") || typeStr.equals("road"))
		{
			System.out.println("Road records");
			recordsMap = recordsReader.awayRecords();
		}
		else if (typeStr.equals("opponent"))
		{	/* Opponent records is a special type of record, necessitating a further gameList query.
			 * To account for this, I assign opponent records its own InfoType 
			 * for use in control statements.
		 	 */
			infoType = InfoType.OPPONENT_RECORDS;
			System.out.println("Opponent records");
			String oppRecordGameInput = seekOpponentRecordInput();
			GameList<?> oppGameList = handleOpponentRecordInput(oppRecordGameInput);
			if (oppGameList == null)
			{	// In cases where the user asks for help, return null and restart function.
				return false;
			}
			// Uses the already calculate team records to calculate opponent strength.
			recordsMap = TeamRecords.opponentRecords(recordsReader.teamRecords(), oppGameList);
		}
		else	// TODO: Notify user that no valid parameter value was found 
		{
			System.out.printf("\'%s\' is not a valid record type. Type \'help\' for a list of "
					+ "permissible parameters.\n", typeStr);
			return false;
		}
		
		String compareStr = paramMap.get("sort");
		if (compareStr != null)
		{ 	/* If the string identifying the sort criterion is invalid, 
			   notify the user and set to null (and thus to default value).
			*/		
			Set<String> nonceSet = new HashSet<>();
			nonceSet.add(compareStr);
			if (flagInvalidParameters(nonceSet, recordSortTypes))
			{
				compareStr = null;
			}
		}
		printRecords(recordsMap, compareStr, teamDataReader.competitorGraph(), false);
		return true;
	}
	
	private String seekOpponentRecordInput()
	{
		return seekInput("Please enter information specifying the set of games " +
				"for which you wish to learn opponent strength.");
	}
	
	/* Returns a new list of games. These games will be evaluated for how strong each team's set
	 * of opponents are, with strength judged by the opponents' performance in the INITIAL GameList's 
	 * games. 
	 */
	private GameList<?> handleOpponentRecordInput(String input)
	{
		String[] paramList = handleInput(input);
		if (paramList == null) return null;
		Map<String, String> paramMap = getParameters(paramList);
		String sameListStr = paramMap.getOrDefault("same-list", "y");
		if (sameListStr.equalsIgnoreCase("y"))
		{
			return gameList;
		}
		return handleGameInput(input);
	}
	
	// Prints records/ratings according to sort criteria.
	private <C extends Competitor, V extends Comparable<V>> boolean
		printRecords(Map<C, V> recordsMap, String compareStr, Graph<?> teamGraph, boolean isDouble)
	{
		// By default, sort all competitors by record, in descending order.
		if (compareStr == null || compareStr.equals("record"))
		{
			List<CompetitorComparablePair<C, V>> list = new ArrayList<>();
			for (Map.Entry<C, V> entry: recordsMap.entrySet())
			{
				list.add(new CompetitorComparablePair<>(entry.getKey(), entry.getValue()));	
			}
			sortRecordsList(list, true);
			printRecordsAux(list, isDouble);
		}
		// Sorts competitors alphabetically.
		else if (compareStr.equals("team"))
		{
			List<CompetitorComparablePair<C, V>> list = new ArrayList<>();
			for (Map.Entry<C, V> entry: recordsMap.entrySet())
			{
				list.add(new CompetitorComparablePair<>(entry.getKey(), entry.getValue()));	
			}
			sortRecordsList(list, false);
			printRecordsAux(list, isDouble);
		}
		// Organizes competitors by division, and sorts by record.
		else if (compareStr.equals("division"))
		{
			List<Division> divisionList = new ArrayList<>();
			switch (league)
			{
				/* CFL and OWL share a division structure */
				case CFL:
				case OWL:
				{	
					for (Node<?> n: teamGraph.getNodesAtLevel(1))
					{
						divisionList.add((Division)n);
					}
					Division division;
					for (int div = 0; div < divisionList.size(); div++)
					{
						division = divisionList.get(div);
						List<CompetitorComparablePair<Team, V>> list = new ArrayList<>();
						for (Node<String> n: division.getChildren())
						{
							Team t = (Team)n;
							list.add(new CompetitorComparablePair<>(t, recordsMap.get(t)));
						}
						
						sortRecordsList(list, true);
						System.out.println(division);
						for (CompetitorComparablePair<Team, V> pair: list)
						{
							System.out.printf("%2s%s\n", "", pair);
						}
					}
					break;
				}
				case NBA:
				{	// In the NBA's case, print out the names of conferences, which contain the divisions.	
					List<Node<?>> conferenceList = new ArrayList<>(teamGraph.getNodesAtLevel(1));
					conferenceList.sort(Comparator.comparing(Node::getKey));
					divisionList = new ArrayList<>();
					Division conference, division;
					for (int conf = 0; conf < conferenceList.size(); conf++)
					{
						conference = (Division)conferenceList.get(conf);
						System.out.println(conference.getFullName());
						
						divisionList = new ArrayList<>();
						for (Node<?> n: conferenceList.get(conf).getChildren())
						{
							divisionList.add((Division)n);
						}
						for (int div = 0; div < divisionList.size(); div++)
						{
							division = divisionList.get(div);
							List<CompetitorComparablePair<Team, V>> list = new ArrayList<>();
							for (Node<?> n: division.getChildren())
							{
								Team t = (Team)n;
								list.add(new CompetitorComparablePair<>(t, recordsMap.get(t)));
							}
							sortRecordsList(list, true);
							System.out.printf("%2s%s\n", "", division);
							for (CompetitorComparablePair<Team, V> pair: list)
							{
								System.out.printf("%4s%s\n", "", pair);
							}							
						}
					}
					break;
				}
			}
		}
		// Organizes competitors by conference, and sorts by record.
		else if (compareStr.equals("conference"))
		{
			switch (league)
			{
				case NBA:
				{
					ArrayList<Node<?>> conferenceList = new ArrayList<>(teamGraph.getNodesAtLevel(1));
					conferenceList.sort(Comparator.comparing(Node::getKey));
					
					Team t;
					for (int conf = 0; conf < conferenceList.size(); conf++)
					{
						ArrayList<CompetitorComparablePair<Team, V>> list = new ArrayList<>();
						Division conference = (Division)conferenceList.get(conf);
						for (Node<String> teamLeaf: conference.getLeaves())
						{
							t = (Team)teamLeaf;
							list.add(new CompetitorComparablePair<>(t, recordsMap.get(t)));
						}
						sortRecordsList(list, true);
						
						System.out.println(conference.getFullName());
						for (CompetitorComparablePair<Team, V> pair: list)
						{
							System.out.printf("%2s%s\n", "", pair);
						}
					}
					break;
				}
				default:
				{
					System.out.printf("The league %s does not use conferences." + 
							"Please sort by division or another criterion.\n\n", league);
					return false;
				}					
			}
		}
		
		return true;
	}
	
	// Sorts lists either by the competitor's name or by the associated numerical value.
	private <C extends Competitor, V extends Comparable<V>> void sortRecordsList
		(List<CompetitorComparablePair<C, V>> recordsList, boolean sortByValue)
	{	
		Comparator<CompetitorComparablePair<C, V>> cmp;
		if (sortByValue)
		{
			cmp = Comparator.comparing(CompetitorComparablePair::value);
			cmp = cmp.reversed();
		}
		else
		{
			cmp = Comparator.comparing(CompetitorComparablePair::competitor);
		}
		
		recordsList.sort(cmp);
	}

	private <C extends Competitor, V extends Comparable<V>> void printRecordsAux
		(List<CompetitorComparablePair<C, V>> recordsList, boolean isDouble)
	{	// String format depends on whether this prints a floating-point number or a String.
		String formatStr = isDouble ? "%s: %.1f\n" : "%s: %s\n";
		for (CompetitorComparablePair<C, V> pair: recordsList)
		{
			System.out.printf(formatStr, pair.competitor(), pair.value());
		}
		
	}
		
	// TODO: implement a back or reset function
	

	private void close()
	{
		scan.close();
		System.out.println("----");
		System.out.println("Exited program.");
		isRunning = false;
	}
	
	private void printHelp()
	{
		if (league == null)
		{
			System.out.println("Choose an abbreviation from the list below:");
			System.out.printf("%4s - CFL (Canadian Football League)\n", "");
			System.out.printf("%4s - NBA (National Basketball Association)\n", "");
			System.out.printf("%4s - OWL (Overwatch League)\n", "");
		}
		else if (gameList == null)
		{
			System.out.println("Example usage:\n\tstart=8/7/17 end=10/9/2017 team=TEAM_OTT,TEAM_SSK" +
					" road=DIV_East");
			Map<String, String> descriptionMap = new LinkedHashMap<>();
			// Adds start and end options to the help list. Implementation varies by league.
			switch (league)
			{
				case CFL:
					descriptionMap.put("start=<[date]/[week]>", 
						"Determines the start of the game list. Parameter value can be either a date "
							+ "in the format MM/DD/YY (or MM/DD/YYYY) or a week number.\n\t"
							+ "For example, start=9/7/17 or start=7");
					descriptionMap.put("end=<[date]/[week]>", 
						"Determines the end of the game list. Parameter value can be either a date "
							+ "in the format MM/DD/YY (or MM/DD/YYYY) or a week number.\n\t"
							+ "For example, end=9/7/17 or end=7");
					break;
				case NBA:
					descriptionMap.put("start=<[date]>", 
						"Determines the start of the game list. Parameter value must be a date "
							+ "in the format MM/DD/YY (or MM/DD/YYYY).\n\t"
							+ "For example, start=12/7/17.");
					descriptionMap.put("end=<[date]>", 
						"Determines the end of the game list. Parameter value must be a date.\n\t"
							+ "For example, end=12/7/17.");
					break;
				case OWL:
					descriptionMap.put("start=<[date]/[week]/[stage]>", 
						"Determines the start of the game list. Parameter value can be either a date "
							+ "in the format MM/DD/YY (or MM/DD/YYYY) or a stage and/or week number.\n\t"
							+ "For example, start=2/1/18 or start=1 (for stage 1) or start=1-2 "
							+ "(for stage 1, week 2).");
					descriptionMap.put("end=<[date]/[week]/[stage]>", 
						"Determines the end of the game list. Parameter value can be either a date "
					+ "in the format MM/DD/YY (or MM/DD/YYYY) or a stage and/or week number.\n\t"
					+ "For example, end=2/1/18 or end=1 (for stage 1) or end=1-2 "
					+ "(for stage 1, week 2).");	
					break;
			}
			
			descriptionMap.put("playoffs=<y/n>", 
				"Determines whether the game list includes playoff games. "
					+ "By default, this value is set to y (for yes).");
			descriptionMap.put("regular=<y/n>", 
				"Determines whether the game list includes regular-season games. By default, "
					+ "this value is set to y (for yes).");
			descriptionMap.put("complete=<y/n>", 
					"Determines whether the game list includes completed games. By default, "
						+ "this value is set to y (for yes).");
			descriptionMap.put("incomplete=<y/n>", 
				"Determines whether the game list includes games which have not ended. By default, "
					+ "this value is set to y (for yes).");
			
			switch (league)
			{
				case CFL:
				case OWL:
					descriptionMap.put("away=[team/div1,team/div2,team/div3,...]",
						"Limits the game list to game in which the away team (or team 1) "
							+ "is a team passed to the parameter. Teams may be identified by their team ID "
							+ "or by the ID of their division.\n\t"
							+ "For example, away=TEAM_LND,DIV_Pacific specifies the London team "
							+ "and all the teams of the Pacific Division.");
					descriptionMap.put("road=[team/div1,team/div2,team/div3,...]", 
						"Same as the \'away\' parameter. Both can be used at once.");
					descriptionMap.put("home=[team/div1,team/div2,team/div3,...]",
							"Limits the game list to game in which the home team (or team 2) "
								+ "is a team passed to the parameter. Teams may be identified by their team ID "
								+ "or by the ID of their division.\n\t"
								+ "For example, home=TEAM_LND,DIV_Pacific specifies the London team "
								+ "and all the teams of the Pacific Division.");
					descriptionMap.put("team=[team/div/conf1,team/div/conf2,team/div/conf3,...]",
							"Limits the game list to game in which either team is a team passed "
								+ "to the parameter. Teams may be identified in the same way "
								+ "specified for the away/road and home parameters.");
					break;
				case NBA:
					descriptionMap.put("away=[team/div/conf1,team/div/conf2,team/div/conf3,...]",
						"Limits the game list to game in which the either team (or team 1) "
							+ "is a team passed to the parameter. Teams may be identified by their team ID "
							+ "or by the ID of their division or conference.\n\t"
							+ "For example, away=TEAM_POR,DIV_Atlantic,CON_West specifies the "
							+ "Portland team, all teams of the Atlantic Division, and all teams of "
							+ "the Western Conference.");
					descriptionMap.put("away=[team/div/conf1,team/div/conf2,team/div/conf3,...]",
						"Same as the \'away\' parameter. Both can be used at once.");
					descriptionMap.put("home=[team/div/conf1,team/div/conf2,team/div/conf3,...]",
							"Limits the game list to game in which the home team (or team 1) "
								+ "is a team passed to the parameter. Teams may be identified by their team ID "
								+ "or by the ID of their division or conference.\n\t"
								+ "For example, home=TEAM_POR,DIV_Atlantic,CON_West specifies the "
								+ "Portland team, all teams of the Atlantic Division, and all teams of "
								+ "the Western Conference.");
					descriptionMap.put("team=[team/div/conf1,team/div/conf2,team/div/conf3,...]",
							"Limits the game list to game in which either team is a team passed "
								+ "to the parameter. Teams may be identified in the same way "
								+ "specified for the away/road and home parameters.");
					break;
			}
					
			// TODO: Keep descriptions within fixed-width columns
			System.out.println("Options:");
			for (Map.Entry<String, String> entry: descriptionMap.entrySet())
			{
				System.out.printf("%2s%-10s\n\t%s\n", "", entry.getKey(), entry.getValue());
			}
		}
		else if (infoType == null)
		{
			System.out.println("Choose an abbreviation from the list below:");
			System.out.println("\t - elo (Elo ratings, or power ratings)");
			System.out.println("\t - ratings (Same as \'elo\'");
			System.out.println("\t - records (Team records: overall, home, road/away, "
					+ "and opponent records)");
			System.out.println("\t - games (A list of the previously specified games/matches)");
			System.out.println("\t - matches (A list of the previously specified games/matches)");
		}
		else 
		{
			Map<String, String> descriptionMap = new LinkedHashMap<>();
			switch (league)
			{
				case CFL:
				case OWL:
					descriptionMap.put("sort=<team/division/record>", 
						"A criteria by which to sort Elo ratings.");
					break;
				case NBA:
					descriptionMap.put("sort=<team/division/conference/record>", 
							"A criteria by which to sort Elo ratings.");
			}
			
			switch (infoType)
			{	
				case ELO:
					descriptionMap.put("date", 
						"Determines the point at which Elo ratings are taken. Parameter value must be "
							+ "in the format MM/DD/YY (or MM/DD/YYYY).\n\t"
							+ "For example, \'date=9/7/17\' or \'date=9/7/2017\'.");
					descriptionMap.put("game", 
						"A game number which determines the point at which Elo ratings are taken.");
					descriptionMap.put("initial", 
						"Determines an initial rating for Elo ratings. 0 by default.");
					switch (league)
					{
						case NBA:
							break;
						case CFL:
							descriptionMap.put("week",
									"Determines the week at the end of which Elo ratings are taken.");
							break;
						case OWL:
							descriptionMap.put("week",
									"Determines the week at the end of which Elo ratings are taken. "
									+ "Week values can combine with the value.\n\t"
									+ "For instance, \'stage=2 week=1\' and \'week=2-1\' are "
									+ "equivalent settings. But \'week=2\' without a stage specification "
									+ "is invalid.");
							descriptionMap.put("stage", 
									"If combined with a week parameter, determines the stage during which "
									+ "Elo ratings are taken. Else, Elo ratings are taken at the end "
									+ "of the stage.");
							break;
					}
					break;
				case RECORDS:
					descriptionMap.put("type=<team/home/away/road/opponent>", 
							"Determines the type of records returned.");
					break;
				case OPPONENT_RECORDS:
					descriptionMap.put("same-list=<y/n>", 
							"Determines whether the game list includes playoff games. By default, "
							+ "this value is set to y (for yes). This parameter takes precedence "
							+ "over all other parameters.");
					break;
				default:
					System.out.println("Reached default");
			}
			System.out.println("Options:");
			for (Map.Entry<String, String> entry: descriptionMap.entrySet())
			{
				System.out.printf("%2s%-10s\n\t%s\n", "", entry.getKey(), entry.getValue());
			}
		}
		System.out.println("---");
	}
	
	private GameReader<?, ?, ?> getCurrentReader()
	{
		switch (league)
		{
			case CFL:
				return (CFLReader) cflReader;
			case NBA:
				return (NBAReader) nbaReader;
			case OWL:
				return (OWLReader) owlReader;
		}
		return null;
	}

	/* TODO: Allow for inputs like 'sort = division' to be considered a key-value pair.
	 * Currently, only sort=division is allowed.
	 */
	private static Map<String, String> getParameters(String[] params)
	{
		Map<String, String> paramMap = new HashMap<>();
		String first, second;
		int index;
		for (String s: params)
		{
			index = s.indexOf('=');
			if (index < 0) { continue; }
			
			first = s.substring(0,index);	// trim(), once space is allowed
			second = s.substring(index+1);	// trim(), once space is allowed
			paramMap.put(first, second);
		}
				
		return paramMap;
	}
	
	/* 
	 * Returns a date object if the passed-in string can be parsed. Otherwise,
	 * returns the passed-in string. 
	 */
	private static Object parseDateOrString(String str)
	{
		if (str == null) return null;
		
		Date date;
		DateFormat df = new SimpleDateFormat("M/d/yy");
		try {
			date = df.parse(str);
			return date;
		} catch (ParseException ex1)
		{
			date = null;
		}
		
		return str;
	}
	
	/* Meant only for Overwatch League, function returns a stage and a week number 
	 * parsed from a String. Is capable of passing only a stage number, along
	 * with a dummy (that is, negative) value for week number.
	 */
	private int[] parseStageAndWeek(String str)
	{
		Pattern pattern = Pattern.compile("(?<stage>\\d+)(-(?<week>\\d+))?");
		Matcher matcher = pattern.matcher(str);
		int startStage = -1, startWeek = -1;
		if (matcher.matches())
		{
			startStage = Integer.parseInt(matcher.group("stage"));
			if (matcher.group("week") != null)
			{
				startWeek = Integer.parseInt(matcher.group("week"));
			}
		}
		return new int[] {startStage, startWeek};
	}
	
	// Filters the list of known NBA games and produces a new list.
	private GameList<NBAGame> nbaGameList(Date startDate, Date endDate, boolean includePlayoffs, 
			boolean includeRegularSeason, boolean includeComplete, boolean includeIncomplete,
			String[] team1Strs, String[] team2Strs, String[] generalTeamStrs)
	{
		GameList<NBAGame> list = new GameList<>(nbaReader.gameList());
		if (startDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(startDate) < 0);
		}
		if (endDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(endDate) > 0);
		}
		if (!includePlayoffs)
		{
			list.removeIf(game -> game.isPlayoffs());
		}
		if (!includeRegularSeason)
		{
			list.removeIf(game -> !game.isPlayoffs());
		}
		if (!includeComplete)
		{
			list.removeIf(game -> game.isComplete());
		}
		if (!includeIncomplete)
		{
			list.removeIf(game -> !game.isComplete());
		}
		Graph<String> teamGraph = nbaReader.teamGraph();
		narrowListByCompetitors(teamGraph, generalTeamStrs, list, true, true);
		narrowListByCompetitors(teamGraph, team1Strs, list, true, false);
		narrowListByCompetitors(teamGraph, team2Strs, list, false, true);
		
		list.sort(Comparator.naturalOrder());
		return list;
		
	}
	
	// Filters the list of known OWL games and produces a new list.
	private GameList<OWLGame> owlGameList(Date startDate, Date endDate, int startStage, int startWeek,
			int endStage, int endWeek, boolean includePlayoffs, boolean includeRegularSeason,
			boolean includeComplete, boolean includeIncomplete,	String[] team1Strs, String[] team2Strs, 
			String[] generalTeamStrs)
	{
		GameList<OWLGame> list = new GameList<>(owlReader.gameList());
		
		if (startDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(startDate) < 0);
		}
		if (endDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(endDate) > 0);
		}
		if (startStage >= 0)
		{
			list.removeIf(game -> game.getStageNumber() < startStage);
		}
		if (endStage >= 0)
		{
			list.removeIf(game -> game.getStageNumber() > endStage);
		}
		if (startWeek >= 0)
		{
			list.removeIf(game -> game.getWeekNumber() < startWeek);
		}
		if (endWeek >= 0)
		{
			list.removeIf(game -> game.getWeekNumber() > endWeek);
		}
		if (!includePlayoffs)
		{
			list.removeIf(game -> game.isPlayoffGame());
		}
		if (!includeRegularSeason)
		{
			list.removeIf(game -> !game.isPlayoffGame());
		}
		if (!includeComplete)
		{
			list.removeIf(game -> game.isComplete());
		}
		if (!includeIncomplete)
		{
			list.removeIf(game -> !game.isComplete());
		}
		Graph<String> teamGraph = owlReader.teamGraph();
		narrowListByCompetitors(teamGraph, generalTeamStrs, list, true, true);
		narrowListByCompetitors(teamGraph, team1Strs, list, true, false);
		narrowListByCompetitors(teamGraph, team2Strs, list, false, true);
		
		list.sort(Comparator.naturalOrder());
		return list;
		}
	
	// Filters the list of known CFL games and produces a new list.
	private GameList<CFLGame> cflGameList(Date startDate, Date endDate, int startWeek, int endWeek, 
			boolean includePlayoffs, boolean includeRegularSeason, boolean includeComplete, 
			boolean includeIncomplete, String[] team1Strs, String[] team2Strs, String[] generalTeamStrs)
	{
		GameList<CFLGame> list = new GameList<>(cflReader.gameList());
		if (startDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(startDate) < 0);
		}
		if (endDate != null)
		{
			list.removeIf(game -> game.getDate().compareTo(endDate) > 0);
		}
		if (startWeek >= 0)
		{
			list.removeIf(game -> game.getWeekNumber() < startWeek);
		}
		if (endWeek >= 0)
		{
			list.removeIf(game -> game.getWeekNumber() > endWeek);
		}
		if (!includePlayoffs)
		{
			list.removeIf(game -> game.isPlayoffs());
		}
		if (!includeRegularSeason)
		{
			list.removeIf(game -> game.isRegularSeason());
		}
		if (!includeComplete)
		{
			list.removeIf(game -> game.isComplete());
		}
		if (!includeIncomplete)
		{
			list.removeIf(game -> !game.isComplete());
		}
		
		Graph<String> teamGraph = cflReader.teamGraph();
		narrowListByCompetitors(teamGraph, generalTeamStrs, list, true, true);
		narrowListByCompetitors(teamGraph, team1Strs, list, true, false);
		narrowListByCompetitors(teamGraph, team2Strs, list, false, true);
		
		list.sort(Comparator.naturalOrder());
		return list;
	}
	
	/* Filters the list of games according to whether:
	 * 		a) the away team has its key (or an ancestor's key) in the list of nodeKeys;
	 *  	b) the home team has its key (etc.) in the list of nodeKeys; or
	 *  	c) either team has its key (etc.) in the list of nodeKeys.
	 * The competitorGraph contains the keys for search.
	 */
	private<T extends Comparable<T>> void narrowListByCompetitors(Graph<T> competitorGraph,
			T[] nodeKeys, GameList<?> list, boolean checksAwayTeam, boolean checksHomeTeam)
	{
		if (!(checksAwayTeam || checksHomeTeam)) return;
		
		
		Set<Team> competitors = new HashSet<>();
		if (nodeKeys != null)
		{
			competitors.addAll(getTeams(competitorGraph, nodeKeys));
		}
		
		// TODO: Make this control statement more efficient, perhaps with a pair of initial predicates
		if (!competitors.isEmpty())
		{
			if (checksAwayTeam && checksHomeTeam)
				list.removeIf(game -> !(competitors.contains(game.getCompetitor1()) ||
					competitors.contains(game.getCompetitor2())));
			else if (checksAwayTeam)
			{
				list.removeIf(game -> !competitors.contains(game.getCompetitor1()));
			}
			else if (checksHomeTeam)
			{
				list.removeIf(game -> !competitors.contains(game.getCompetitor2()));
			}
		}
			
	}
	
	/* Searches competitorGraph for each key in nodeKeys and returns all teams which have that key
	 * and all teams which are successors to nodes with that key. */
	private<T extends Comparable<T>> Set<Team> getTeams(Graph<T> competitorGraph, 
			T[] nodeKeys)
	{
		Set<Team> teams = new HashSet<>();
		for (T key: nodeKeys)
		{
			Node<T> node = competitorGraph.findNode(key);
			if (node == null)
			{
				System.out.printf("Could not find a team with key %s\n", key);
				continue;
			}
			
			List<Node<T>> leaves = node.getLeaves();
			for (Node<T> leaf: leaves)
			{
				teams.add((Team)leaf);
			}
		}
		return teams;
	}
	
	public static void main(String[] args) throws ParseException {
		new MetricsScript().run();
	}
}

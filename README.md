# power-rankings
This project evaluates the performance of sports teams over a season in three leagues (Canadian Football League, National Basketball Association, and Overwatch League). The primary evaluative measure is an adapted Elo rating, though other measures are available: teams' overall, home, and away records, and their opponents records.

The primary entry point for this application is src/metrics/MetricsScript.java, run from the root folder. The other entry points include which update the raw and the clean data for each league (CFLUpdater, NBAUpdater, and OWLUpdater, in the 'update' package.)

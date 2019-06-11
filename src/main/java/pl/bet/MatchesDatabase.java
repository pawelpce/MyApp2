package pl.bet;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MatchesDatabase {

    private static MatchesDatabase matchesDatabase = null;

    private MongoConnector mongoConnector = new MongoConnector();
    private MongoDatabase database = mongoConnector.connect();
    private MongoCollection<Document> matchesToBet = database.getCollection("matchesToBet");
    private MongoCollection<Document> matchesHistory = database.getCollection("matchesHistory");
    private MongoCollection<Document> usersBets = database.getCollection("usersBets");

    static Logger mongoLogger = Logger.getLogger("org.mongodb.driver");

    private MatchesDatabase() {

    }

    public static MatchesDatabase getInstance() {

        if (matchesDatabase == null) {
            matchesDatabase = new MatchesDatabase();
        }
        return matchesDatabase;
    }


    public int linkedListPrinting(String userName) {

        mongoLogger.setLevel(Level.SEVERE);
        Bson sort = orderBy(ascending("date", "time"));
        List<Document> matchesFuture = matchesToBet.find().sort(sort).into(new ArrayList<>());
        Bson filter = eq("userName", userName);
        List<Document> playerMatches = usersBets.find(filter).into(new ArrayList<>());

        for (Document matchToBet : matchesFuture) {

            if (isBet(matchToBet, playerMatches)) {
                if (matchesFuture.indexOf(matchToBet) < 9) {
                    System.out.println(" " + (matchesFuture.indexOf(matchToBet) + 1) + ". " + matchesToBetString(matchToBet) +
                            playerBetString(playerMatches.get(indexOfBetMatch(matchToBet, playerMatches)), true));
                } else {
                    System.out.println((matchesFuture.indexOf(matchToBet) + 1) + ". " + matchesToBetString(matchToBet) +
                            playerBetString(playerMatches.get(indexOfBetMatch(matchToBet, playerMatches)), true));
                }
            } else {
                if (matchesFuture.indexOf(matchToBet) < 9) {
                    System.out.println(" " + (matchesFuture.indexOf(matchToBet) + 1) + ". " + matchesToBetString(matchToBet));
                } else {
                    System.out.println((matchesFuture.indexOf(matchToBet) + 1) + ". " + matchesToBetString(matchToBet));
                }
            }
        }
        return matchesFuture.size();
    }

    public void scoreboardPrinting(String userName, boolean isItAppUser) {

        mongoLogger.setLevel(Level.SEVERE);
        Bson sort = orderBy(ascending("date"));
        List<Document> matchesEnded = matchesHistory.find().sort(sort).into(new ArrayList<>());
        Bson filter = eq("userName", userName);
        List<Document> playerMatches = usersBets.find(filter).into(new ArrayList<>());

        checkPoints(userName);

        if (matchesEnded.isEmpty()) {
            System.out.println("No matches have been played yet.\n");
        } else {
            for (Document matchHistory : matchesEnded) {

                if (isBet(matchHistory, playerMatches)) {
                    System.out.println(matchesHistoryString(matchHistory) +
                            playerBetString(playerMatches.get(indexOfBetMatch(matchHistory, playerMatches)), isItAppUser) +
                            " || Points: " + playerMatches.get(indexOfBetMatch(matchHistory, playerMatches)).get("userPoints"));
                } else {
                    System.out.println(matchesHistoryString(matchHistory) + " || No bet");
                }
            }
        }
    }

    private boolean isBet(Document matchCheckedToBeBet, List<Document> matchesBet) {
        mongoLogger.setLevel(Level.SEVERE);
        return matchesBet.stream().anyMatch(matchBet -> matchBet.get("matchId").equals(matchCheckedToBeBet.get("_id")));
    }

    private int indexOfBetMatch(Document matchCheckedToBeBet, List<Document> matchesBet) {
        mongoLogger.setLevel(Level.SEVERE);
        Document playerBet = matchesBet.stream()
                .filter(matchBet1 -> matchBet1.get("matchId")
                        .equals(matchCheckedToBeBet.get("_id")))
                .findAny().get();

        return matchesBet.indexOf(playerBet);
    }

    public Document getMatchByListPosition(int userInput) {
        mongoLogger.setLevel(Level.SEVERE);
        Document chosenMatch = null;
        Bson sort = orderBy(ascending("date", "time"));
        List<Document> matchesFuture = matchesToBet.find().sort(sort).into(new ArrayList<>());

        for (Document match : matchesFuture) {
            if (matchesFuture.indexOf(match) + 1 == userInput) {
                chosenMatch = match;
            }
        }
        return chosenMatch;
    }

    public void betMatch(Document matchToBet, String userName) {

        mongoLogger.setLevel(Level.SEVERE);
        Bson filter = eq("userName", userName);
        List<Document> playerMatches = usersBets.find(filter).into(new ArrayList<>());

        if (checkDate(matchToBet.get("date").toString(), matchToBet.get("time").toString())) {
            System.out.println("Type score for below teams");
            System.out.println(matchToBet.get("hteam") + ":");
            int userHomeTeamScore = BetAppFunctions.getInstance().getPlayerInput(0, Integer.MAX_VALUE);
            System.out.println(matchToBet.get("ateam") + ":");
            int userAwayTeamScore = BetAppFunctions.getInstance().getPlayerInput(0, Integer.MAX_VALUE);

            Optional<Document> optionalPlayerBet = playerMatches.stream()
                    .filter(playerBet1 -> playerBet1.get("matchId").equals(matchToBet.get("_id"))).findAny();
            if (optionalPlayerBet.isPresent()) {

                usersBets.updateOne(eq("_id", optionalPlayerBet.get().get("_id")),
                        combine(set("userHScore", Integer.toString(userHomeTeamScore)),
                                set("userAScore", Integer.toString(userAwayTeamScore))));

                System.out.println("Bet edited successfully.\n");
            } else {

                Document newBet = new Document()
                        .append("userName", userName)
                        .append("matchId", matchToBet.get("_id"))
                        .append("userHScore", Integer.toString(userHomeTeamScore))
                        .append("userAScore", Integer.toString(userAwayTeamScore));

                usersBets.insertOne(newBet);

                System.out.println("Bet added successfully.\n");
            }
        } else {
            System.out.println("You can't bet this match. It has already started or ended.");
        }
    }

    public boolean checkDate(String matchDate, String matchTime) {

        mongoLogger.setLevel(Level.SEVERE);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String matchDay = String.join(" ", matchDate, matchTime);

        LocalDateTime betTime = LocalDateTime.parse(dateFormat.format(now), dateFormat);
        LocalDateTime playTime = LocalDateTime.parse(matchDay, dateFormat);

        return betTime.isBefore(playTime);
    }

    private void checkPoints(String userName) {

        mongoLogger.setLevel(Level.SEVERE);
        Bson sort = orderBy(ascending("date"));
        List<Document> matchesEnded = matchesHistory.find().sort(sort).into(new ArrayList<>());
        Bson filter = eq("userName", userName);
        List<Document> playerMatches = usersBets.find(filter).into(new ArrayList<>());

        for (Document matchHistory : matchesEnded) {

            if (isBet(matchHistory, playerMatches)) {

                int matchPoints = countPoints(matchHistory, playerMatches.get(indexOfBetMatch(matchHistory, playerMatches)));
                usersBets.updateOne(and(eq("matchId", matchHistory.get("_id")), eq("userName", userName)),
                        set("userPoints", matchPoints));

            } else {

                usersBets.updateOne(and(eq("matchId", matchHistory.get("_id")), eq("userName", userName)),
                        set("userPoints", 0));

            }
        }
    }

    private int countPoints(Document matchEnded, Document playerBet) {

        double doubleActualHomeScore = Double.parseDouble(matchEnded.get("hscore").toString());
        int actualHomeScore = (int) doubleActualHomeScore;

        double doubleActualAwayScore = Double.parseDouble(matchEnded.get("ascore").toString());
        int actualAwayScore = (int) doubleActualAwayScore;

        double doubleUserHomeScore = Double.parseDouble(playerBet.get("userHScore").toString());
        int userHomeScore = (int) doubleUserHomeScore;

        double doubleUserAwayScore = Double.parseDouble(playerBet.get("userAScore").toString());
        int userAwayScore = (int) doubleUserAwayScore;

        boolean fullScore = (actualHomeScore == userHomeScore) && (actualAwayScore == userAwayScore);
        boolean rightWinnerOrDraw = ((actualHomeScore > actualAwayScore) && (userHomeScore > userAwayScore)) ||
                ((actualHomeScore < actualAwayScore) && (userHomeScore < userAwayScore)) ||
                ((actualHomeScore == actualAwayScore) && (userHomeScore == userAwayScore));
        boolean rightGoalDifference = ((actualHomeScore - actualAwayScore) == (userHomeScore - userAwayScore)) ||
                ((actualHomeScore - actualAwayScore) == -(userHomeScore - userAwayScore));

        if (fullScore) {
            return 3;
        }

        if (rightWinnerOrDraw && rightGoalDifference) {
            return 2;
        }

        if (rightWinnerOrDraw) {
            return 1;
        }

        if (rightGoalDifference) {
            return 1;
        }

        return 0;
    }

    public void printRanking() {

        mongoLogger.setLevel(Level.SEVERE);

        Bson sort = orderBy(descending("totalPoints"));

        List<Document> ranking = usersBets.aggregate(Arrays.asList(group("$userName",
                sum("totalPoints", "$userPoints")), sort(sort))).into(new ArrayList<>());
        System.out.println("Bet ranking:");
        for (Document player : ranking) {
            System.out.println((ranking.indexOf(player) + 1) + ". " +
                    player.get("_id") + ": " + player.get("totalPoints"));
        }
    }

    private String matchesToBetString(Document matchToBet) {
        mongoLogger.setLevel(Level.SEVERE);
        return matchToBet.get("date") + " " + matchToBet.get("time") + " " +
                matchToBet.get("hteam") + " : " + matchToBet.get("ateam");
    }

    private String playerBetString(Document playerBet, boolean isItAppUser) {
        mongoLogger.setLevel(Level.SEVERE);
        if (isItAppUser) {
            return " || Your bet: " + playerBet.get("userHScore") + ":" + playerBet.get("userAScore");
        } else {
            return " || " + playerBet.get("userName") + " bet: " + playerBet.get("userHScore") + ":" + playerBet.get("userAScore");
        }
    }

    private String matchesHistoryString(Document matchHistory) {
        mongoLogger.setLevel(Level.SEVERE);
        return matchHistory.get("date") + " " + matchHistory.get("hteam") + " : " + matchHistory.get("ateam") + " " +
                matchHistory.get("hscore") + ":" + matchHistory.get("ascore");
    }

}
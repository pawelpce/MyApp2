package pl.bet;

import org.bson.Document;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BetApp {

    public static void main(String[] args) {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        Scanner scanner = new Scanner(System.in);
        mongoLogger.setLevel(Level.SEVERE);

        String userName = BetAppFunctions.getInstance().entryMenu();

        while (userName.equals("Error")) {
            System.out.println("Try again.");
            userName = BetAppFunctions.getInstance().entryMenu();
        }

        BetAppFunctions.getInstance().printMenu();
        int userInput = BetAppFunctions.getInstance().getPlayerInput(1, 4);

        while (userInput >= 1 && userInput <= 3) {

            switch (userInput) {

                case 1:
                    Document matchToBet;
                    System.out.println("*BET APP*\nInsert 0 to back to main menu.");
                    System.out.println("Insert the number of match you would like to bet or edit:\n");
                    int listSize = MatchesDatabase.getInstance().linkedListPrinting(userName);
                    int userMatchToBet = BetAppFunctions.getInstance().getPlayerInput(0, listSize);
                    if (userMatchToBet == 0) {
                        BetAppFunctions.getInstance().printMenu();
                        userInput = BetAppFunctions.getInstance().getPlayerInput(1, 4);
                        break;
                    } else {
                        matchToBet = MatchesDatabase.getInstance().getMatchByListPosition(userMatchToBet);
                        MatchesDatabase.getInstance().betMatch(matchToBet, userName);
                        BetAppFunctions.getInstance().printMenu();
                        userInput = BetAppFunctions.getInstance().getPlayerInput(1, 4);
                        break;
                    }
                case 2:
                    MatchesDatabase.getInstance().scoreboardPrinting(userName, true);
                    System.out.println("\nInsert 0 to back to main menu.");
                    int backToMenu = BetAppFunctions.getInstance().getPlayerInput(0, 0);
                    if (backToMenu == 0) {
                        BetAppFunctions.getInstance().printMenu();
                        userInput = BetAppFunctions.getInstance().getPlayerInput(1, 4);
                    }
                    break;
                case 3:
                    MatchesDatabase.getInstance().printRanking();
                    System.out.println("\nInsert 0 to back to main menu.");
                    System.out.println("Insert 1 to show other player's scoreboard.");
                    int backToMenu2 = BetAppFunctions.getInstance().getPlayerInput(0, 1);
                    if (backToMenu2 == 0) {

                    } else {
                        System.out.println("Enter the name of player:");
                        String playerName = scanner.nextLine();
                        if (UsersDatabase.getInstance().doesPlayerExists(playerName)) {
                            MatchesDatabase.getInstance().scoreboardPrinting(playerName, false);
                        } else {
                            System.out.println("User " + playerName + " not in database.");
                        }
                    }
                    BetAppFunctions.getInstance().printMenu();
                    userInput = BetAppFunctions.getInstance().getPlayerInput(1, 4);
                    break;
            }

        }
        System.out.println("Thank you for using our application.");
    }
}
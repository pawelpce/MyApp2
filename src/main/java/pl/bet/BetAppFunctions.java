package pl.bet;

import java.util.InputMismatchException;
import java.util.Scanner;

public class BetAppFunctions {

    private static BetAppFunctions betAppFunctions = null;
    private Scanner scannerLine = new Scanner(System.in);

    private BetAppFunctions() {

    }

    public static BetAppFunctions getInstance() {

        if (betAppFunctions == null) {
            betAppFunctions = new BetAppFunctions();
        }
        return betAppFunctions;
    }


    public void printMenu() {
        System.out.println("*BET APP*\nChoose one of below options:");
        System.out.println("1. Matches to bet");
        System.out.println("2. Scoreboard");
        System.out.println("3. Ranking");
        System.out.println("4. End app");
    }

    public int getPlayerInput(int minNumber, int maxNumber) {
        Scanner scanner = new Scanner(System.in);
        int number = 0;
        boolean correctInput;
        do {
            try {
                correctInput = true;
                number = scanner.nextInt();
                while (number < minNumber || number > maxNumber) {
                    System.out.println("Bad argument. Try again.");
                    number = scanner.nextInt();
                }
            } catch (InputMismatchException e) {
                scanner.next();
                correctInput = false;
                System.out.println("Bad argument. Try again.");
            }
        } while (!correctInput);

        return number;
    }

    public String entryMenu() {

        System.out.println("Welcome to bet app!");
        System.out.println("1.Log in\n2.Sign Up");
        int userFirstInput = getPlayerInput(1, 2);
        String finalUserName;
        if (userFirstInput == 1) {
            System.out.println("Enter your login:");
            String userLogin = scannerLine.nextLine();
            finalUserName = UsersDatabase.getInstance().validateLogin(userLogin);
        } else {
            System.out.println("Add login - you will be using it to enter the application:");
            String newLogin = scannerLine.nextLine();
            System.out.println("Add your user name(not the same as login) - you will see it in the ranking:");
            String newUserName = scannerLine.nextLine();
            while (newLogin.equals(newUserName)) {
                System.out.println("Login can't be the same as the user name. Try Again.");
                System.out.println("Add login - you will be using it to enter the application:");
                newLogin = scannerLine.nextLine();
                System.out.println("Add your user name(not the same as login) - you will see it in the ranking:");
                newUserName = scannerLine.nextLine();
            }
            finalUserName = UsersDatabase.getInstance().addUser(newUserName, newLogin);
        }
        if (!finalUserName.equals("Error")) {
            return finalUserName;
        } else return "Error";
    }
}
// importing necessary classes and packages for handling http connections, json processing, environment variables, and user input
import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String ACCOUNT_ID = dotenv.get("ACCOUNT_ID");
    private static final String ACCESS_TOKEN = dotenv.get("ACCESS_TOKEN");
    private static final String API_KEY = dotenv.get("API_KEY");
    private static final String DISCOVER_URL =
        "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc";
    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie?language=en-US&page=1&include_adult=false&query=";
    private static final String WATCHLIST_URL =
        "https://api.themoviedb.org/3/account/" + ACCOUNT_ID + "/watchlist/movies?api_key=" + API_KEY;
    private static final String FAVORITES_URL =
        ("https://api.themoviedb.org/3/account/" +
            ACCOUNT_ID +
            "/favorite/movies?api_key=" +
            API_KEY +
            "&language=en-US&page=1&sort_by=created_at.asc");
    private static final String RATED_MOVIES_URL =
        ("https://api.themoviedb.org/3/account/" +
            ACCOUNT_ID +
            "/rated/movies?language=en-US&page=1&sort_by=created_at.asc&api_key=" +
            API_KEY);
    private static final String ADD_WATCHLIST_URL =
        ("https://api.themoviedb.org/3/account/" + ACCOUNT_ID + "/watchlist?api_key=" + ACCESS_TOKEN);
    private static final String ADD_FAVORITES_URL =
        ("https://api.themoviedb.org/3/account/" + ACCOUNT_ID + "/favorite?api_key=" + ACCESS_TOKEN);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            ConsoleUtils.clearConsole(); // called at the beginning of each function to clear the screen and only show the relevant options or information
            System.out.println("\n󰎁 The Movie Database CLI 󰟞");
            System.out.println(
                "\nA simple command-line tool to explore movies and manage your favorites. Pick an option below to discover new films, search for specific titles, or check your watchlist. When you’re done, just hit exit!"
            );
            System.out.println("\n 1. Discover Movies - Explore new movies");
            System.out.println(" 2. Search Movies - Find specific movies by title or genre");
            System.out.println(" 3. View Watchlist - Your watch later list");
            System.out.println(" 4. View Favorites - Access your personal favorite movies");
            System.out.println(" 5. View Rated Movies - Acess the movies you've rated");
            System.out.println("󰈆 6. Exit - Close the application");
            System.out.print("\nChoice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    discoverMovies(scanner);
                    break;
                case "2":
                    searchMovies(scanner);
                    break;
                case "3":
                    viewWatchlist(scanner);
                    break;
                case "4":
                    viewFavorites(scanner);
                    break;
                case "5":
                    viewRatedMovies(scanner);
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting the program.");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static void discoverMovies(Scanner scanner) {
        try {
            URL url = new URL(DISCOVER_URL + "&api_key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // get movie count from the discover list
                int movieCount = results.length();

                ConsoleUtils.clearConsole();
                // display the count of movies in the output
                System.out.println("\nDiscover Movies (" + movieCount + " movies found)");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String title = movie.getString("title");
                    double rating = movie.getDouble("vote_average");
                    String releaseDate = movie.getString("release_date");
                    String overview = movie.getString("overview");
                    // truncate overview to a specified length (150 characters)
                    String truncatedOverview = truncateOverview(overview, 150);
                    System.out.println("\n" + (i + 1) + ". \u001B[32m" + title + "\u001B[0m ( " + rating + " |  " + releaseDate + ") ");
                    System.out.println(truncatedOverview);
                }

                System.out.println("\nSelect a movie number to view details\nEnter 0 to go back");
                System.out.print("\nOption: ");
                int selection = scanner.nextInt();
                scanner.nextLine();
                if (selection > 0 && selection <= results.length()) {
                    int movieId = results.getJSONObject(selection - 1).getInt("id");
                    fetchAndShowMovieDetails(movieId, scanner, "discover", true);
                }
            } else {
                System.out.println("Error: Unable to fetch data from TheMovieDB API. Response code: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void searchMovies(Scanner scanner) {
        ConsoleUtils.clearConsole(); // clear the menu at the top to only shows relevant data for search function
        System.out.print("\n Search: ");
        String query = scanner.nextLine();

        try {
            String searchQuery = SEARCH_URL + query.replace(" ", "%20") + "&api_key=" + API_KEY;
            URL url = new URL(searchQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // get movie count from the search results
                int movieCount = results.length();

                if (results.length() > 0) {
                    System.out.println("\nSearch Results (" + movieCount + " movies found)");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject movie = results.getJSONObject(i);
                        String title = movie.getString("title");
                        double rating = movie.getDouble("vote_average");
                        String releaseDate = movie.getString("release_date");
                        String overview = movie.getString("overview");
                        // truncate overview to a specified length (150 characters)
                        String truncatedOverview = truncateOverview(overview, 150);
                        System.out.println(
                            "\n" + (i + 1) + ". \u001B[32m" + title + "\u001B[0m ( " + rating + " |  " + releaseDate + ") "
                        );
                        System.out.println(truncatedOverview);
                    }

                    System.out.println("\nSelect a movie number to view details\nEnter 0 to go back");
                    System.out.print("\nOption: ");
                    int selection = scanner.nextInt();
                    scanner.nextLine();
                    if (selection > 0 && selection <= results.length()) {
                        int movieId = results.getJSONObject(selection - 1).getInt("id");
                        fetchAndShowMovieDetails(movieId, scanner, "search", true);
                    }
                } else {
                    System.out.println("No movies found with that title.");
                }
            } else {
                System.out.println("Error: Unable to fetch data from TheMovieDB API. Response code: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void viewWatchlist(Scanner scanner) {
        try {
            URL url = new URL(WATCHLIST_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }

                JSONObject jsonResponse = new JSONObject(content.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // get movie count from the watchlist
                int movieCount = results.length();

                ConsoleUtils.clearConsole(); // clear the menu at the top to only shows relevant data for watchlist function
                System.out.println("\nYour Watchlist (" + movieCount + " movies found)\n");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String title = movie.getString("title");
                    double rating = movie.getDouble("vote_average");
                    System.out.println((i + 1) + ". " + title + " ( " + rating + ")");
                }

                System.out.println(
                    "\nSelect a movie number to view details\nEnter 'x' to remove a movie from the watchlist\nEnter 0 to go back"
                );
                System.out.print("\nOption: ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("x")) {
                    System.out.print("\nEnter the number of the movie to remove: ");
                    int index = scanner.nextInt();
                    scanner.nextLine();

                    if (index > 0 && index <= results.length()) {
                        int movieId = results.getJSONObject(index - 1).getInt("id");
                        updateWatchlist(movieId, false);

                        // call viewWatchlist again to refresh after removal
                        viewWatchlist(scanner);
                    } else {
                        System.out.println("Invalid number.");
                        viewWatchlist(scanner); // stay on watchlist page for invalid input
                    }
                } else {
                    int selection = Integer.parseInt(input);
                    if (selection == 0) {
                        return; // go back to previous menu
                    } else if (selection > 0 && selection <= results.length()) {
                        int movieId = results.getJSONObject(selection - 1).getInt("id");
                        fetchAndShowMovieDetails(movieId, scanner, "watchlist", false);
                    }
                }
            } else {
                System.out.println("Failed to retrieve watchlist. Response code: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void viewFavorites(Scanner scanner) {
        try {
            URL url = new URL(FAVORITES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }

                JSONObject jsonResponse = new JSONObject(content.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // get movie count from the favorites list
                int movieCount = results.length();

                ConsoleUtils.clearConsole();
                System.out.println("\nYour Favorites List (" + movieCount + " movies found)\n");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String title = movie.getString("title");
                    double rating = movie.getDouble("vote_average");
                    System.out.println((i + 1) + ". " + title + " ( " + rating + ")");
                }

                System.out.println(
                    "\nSelect a movie number to view details\nEnter 'x' to remove a movie from favorites\nEnter 0 to go back"
                );
                System.out.print("\nOption: ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("x")) {
                    System.out.print("\nEnter the number of the movie to remove: ");
                    int index = scanner.nextInt();
                    scanner.nextLine();

                    if (index > 0 && index <= results.length()) {
                        int movieId = results.getJSONObject(index - 1).getInt("id");
                        updateFavorites(movieId, false);

                        // refresh the favorites view after removal
                        viewFavorites(scanner);
                    } else {
                        System.out.println("Invalid number.");
                        viewFavorites(scanner); // stay on the favorites page for invalid input
                    }
                } else {
                    int selection = Integer.parseInt(input);
                    if (selection == 0) {
                        return; // go back to the previous menu
                    } else if (selection > 0 && selection <= results.length()) {
                        int movieId = results.getJSONObject(selection - 1).getInt("id");
                        fetchAndShowMovieDetails(movieId, scanner, "favorites", false);
                    }
                }
            } else {
                System.out.println("Failed to retrieve favorites list. Response code: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void viewRatedMovies(Scanner scanner) {
        try {
            URL url = new URL(RATED_MOVIES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }

                JSONObject jsonResponse = new JSONObject(content.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // get movie count from rated movies
                int movieCount = results.length();

                ConsoleUtils.clearConsole();
                System.out.println("\nYour Rated Movies (" + movieCount + " movies found)\n");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String title = movie.getString("title");
                    double rating = movie.getDouble("rating"); // use "rating" instead of "vote_average" for rated movies
                    System.out.println((i + 1) + ". " + title + " ( " + rating + ")");
                }

                System.out.println(
                    "\nSelect a movie number to view details\nEnter 'x' to delete a rating\nEnter 'e' to edit a rating\nEnter 0 to go back"
                );
                System.out.print("\nOption: ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("x")) {
                    System.out.print("\nEnter the number of the movie to delete the rating: ");
                    int index = scanner.nextInt();
                    scanner.nextLine();

                    if (index > 0 && index <= results.length()) {
                        int movieId = results.getJSONObject(index - 1).getInt("id");
                        updateRating(movieId, null);

                        // refresh the rated movies view after deletion
                        viewRatedMovies(scanner);
                    } else {
                        System.out.println("Invalid index number.");
                        viewRatedMovies(scanner); // stay on the rated movies page for invalid input
                    }
                } else if (input.equalsIgnoreCase("e")) {
                    System.out.print("\nEnter the number of the movie to edit the rating: ");
                    int index = scanner.nextInt();
                    scanner.nextLine();

                    if (index > 0 && index <= results.length()) {
                        int movieId = results.getJSONObject(index - 1).getInt("id");
                        System.out.print("Enter the new rating (0.5 to 10): ");
                        double newRating = scanner.nextDouble();
                        scanner.nextLine();

                        if (newRating >= 0.5 && newRating <= 10) {
                            updateRating(movieId, newRating);
                        } else {
                            System.out.println("Invalid rating. Rating should be between 0.5 and 10.");
                        }

                        // refresh the rated movies view after editing
                        viewRatedMovies(scanner);
                    } else {
                        System.out.println("Invalid index number.");
                        viewRatedMovies(scanner); // stay on the rated movies page for invalid input
                    }
                } else {
                    int selection = Integer.parseInt(input);
                    if (selection == 0) {
                        return; // go back to the previous menu
                    } else if (selection > 0 && selection <= results.length()) {
                        int movieId = results.getJSONObject(selection - 1).getInt("id");
                        fetchAndShowMovieDetails(movieId, scanner, "rated", false);
                    }
                }
            } else {
                System.out.println("Failed to retrieve rated movies. Response code: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showMovieDetails(JSONObject movie, Scanner scanner, String previousMenu, boolean showActions) {
        ConsoleUtils.clearConsole(); // clear the menu at the top to only shows relevant data for movie details function
        System.out.println("\nMovie Details");
        String title = movie.getString("title");
        String tagline = movie.optString("tagline", ""); // use optString to avoid errors if tagline is missing
        String overview = movie.getString("overview");
        String releaseDate = movie.getString("release_date");
        double rating = movie.getDouble("vote_average");
        int movieId = movie.getInt("id");

        // dunno tf is this, a reddit user helped me with this :D
        JSONArray genresArray = movie.getJSONArray("genres");
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genresArray.length(); i++) {
            genres.append(genresArray.getJSONObject(i).getString("name"));
            if (i < genresArray.length() - 1) {
                genres.append(", ");
            }
        }

        System.out.println("\n" + title);
        // NOTE: this works in many command-line environments, but it may not work in some ide consoles
        // (like eclipse or intellij) which don’t support ansi escape codes by default
        if (!tagline.isEmpty()) {
            // ansi escape codes for italic text
            final String italic = "\u001B[3m"; // the ansi code for starting italic text
            final String reset = "\u001B[0m"; // resets the formatting, so the text following it will not be italic

            System.out.println(italic + tagline + reset);
        }

        System.out.println("\n" + overview);
        System.out.println("\n Release Date: " + releaseDate);
        System.out.println(" Rating: " + rating);
        System.out.println(" Genres: " + genres.toString());

        if (showActions) {
            System.out.println(
                "\nEnter 'r' to rate this movie\nEnter 'a' to add this movie to your watchlist\nEnter 'f' to add this movie to your favorites\nEnter 'b' to go back to the previous menu\nEnter 'e' to exit"
            );
        } else {
            System.out.println("\nEnter 'b' to go back to the previous menu\nEnter 'e' to exit");
        }

        System.out.print("\nOption: ");
        String input = scanner.nextLine();

        if (showActions && input.equalsIgnoreCase("a")) {
            updateWatchlist(movieId, true);
            showMovieDetails(movie, scanner, previousMenu, showActions);
        } else if (showActions && input.equalsIgnoreCase("f")) {
            updateFavorites(movieId, true);
            showMovieDetails(movie, scanner, previousMenu, showActions);
        } else if (showActions && input.equalsIgnoreCase("r")) {
            System.out.print("\nEnter your rating (0.5 to 10): ");
            double ratingValue = Double.parseDouble(scanner.nextLine());
            updateRating(movieId, ratingValue);
            showMovieDetails(movie, scanner, previousMenu, showActions);
        } else if (input.equalsIgnoreCase("b")) {
            // go back to the previous menu
            switch (previousMenu) {
                case "discover":
                    discoverMovies(scanner);
                    break;
                case "search":
                    searchMovies(scanner);
                    break;
                case "watchlist":
                    viewWatchlist(scanner);
                    break;
                case "favorites":
                    viewFavorites(scanner);
                    break;
                case "rated":
                    viewRatedMovies(scanner);
                    break;
            }
        } else if (input.equalsIgnoreCase("e")) {
            System.out.println("Exiting the program.");
            System.exit(0);
        }
    }

    private static void fetchAndShowMovieDetails(int movieId, Scanner scanner, String previousMenu, boolean showActions) {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject movieDetails = new JSONObject(response.toString());
                showMovieDetails(movieDetails, scanner, previousMenu, showActions);
            } else {
                System.out.println("Error: Unable to fetch movie details. Response code: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateWatchlist(int movieId, boolean add) {
        try {
            URL url = new URL(ADD_WATCHLIST_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"media_type\": \"movie\", \"media_id\": " + movieId + ", \"watchlist\": " + add + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String action = add ? "added to" : "removed from";
                System.out.println("Movie successfully " + action + " your watchlist.");

                // add a brief pause to let the user see the success message before the screen clears
                try {
                    Thread.sleep(2000); // delay for 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                String action = add ? "add" : "remove";
                System.out.println("Failed to " + action + " movie to watchlist. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateFavorites(int movieId, boolean isAdding) {
        try {
            URL url = new URL(ADD_FAVORITES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"media_type\": \"movie\", \"media_id\": " + movieId + ", \"favorite\": " + isAdding + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String action = isAdding ? "added to" : "removed from";
                System.out.println("Movie successfully " + action + " your favorites.");

                // add a brief pause to let the user see the success message before the screen clears
                try {
                    Thread.sleep(2000); // delay for 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                String action = isAdding ? "add" : "remove";
                System.out.println("Failed to " + action + " movie to favorites. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateRating(int movieId, Double ratingValue) {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/" + movieId + "/rating?api_key=" + ACCESS_TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request properties before opening the connection
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");

            if (ratingValue != null) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String jsonInputString = "{\"value\":" + ratingValue + "}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            } else {
                conn.setRequestMethod("DELETE");
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String action = ratingValue != null ? "submitted" : "removed";
                System.out.println("Rating successfully " + action + ".");

                // add a brief pause to let the user see the success message before the screen clears
                try {
                    Thread.sleep(2000); // delay for 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                String action = ratingValue != null ? "submit" : "remove";
                System.out.println("Failed to " + action + " rating. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to truncate the overview
    private static String truncateOverview(String overview, int maxLength) {
        if (overview.length() <= maxLength) {
            return overview; // return full overview if it's within the limit
        }
        return overview.substring(0, maxLength) + "..."; // truncate and append ellipsis
    }

    // utility class to perform console-related actions
    public class ConsoleUtils {

        public static void clearConsole() {
            // check if the os is windows
            try {
                if (System.getProperty("os.name").contains("Windows")) {
                    // TODO: test windows console clearing functionality
                    // use the 'cls' command on windows to clear the console
                    // creates a new process to run the command and waits for it to finish
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                    // for non-windows systems (linux, macos), use ansi escape codes
                    // \033[H moves the cursor to the top-left of the screen
                    // \033[2J clears the screen
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush(); // forcefully flush output to apply changes
                }
            } catch (Exception e) {
                // if an error occurs, print a message to indicate the console couldn't be cleared
                System.out.println("Could not clear console.");
            }
        }
    }
}

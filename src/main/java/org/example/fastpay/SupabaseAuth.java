package org.example.fastpay;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseAuth {
    private static final String SUPABASE_URL = "https://kkmcepagifexhdeodyog.supabase.co/rest/v1/users";
    private static final String API_KEY = "sb_publishable_Ebi_9rInKDfL_vP1i3IWvg_BnMUUWFY";
    private static final HttpClient client = HttpClient.newHttpClient();

    // REGISTER A NEW USER
    public static String registerUser(String username, String fullName, String cnic, String phone, String password) {
        try {
            // First, check if username exists
            if (getUserDetails(username) != null) {
                return "Error: Username already exists!";
            }

            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("full_name", fullName);
            newUser.put("cnic", cnic);
            newUser.put("phone", phone);
            newUser.put("password", password); // Note: In a real app, hash this!
            newUser.put("role", "user");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(newUser.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) return "Success";
            return "Error saving user to database.";
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    // LOGIN & GET ROLE
    public static String loginUser(String username, String password) {
        try {
            JSONObject user = getUserDetails(username);
            if (user == null) {
                return "Error: Username not found.";
            }
            if (!user.getString("password").equals(password)) {
                return "Error: Incorrect password.";
            }
            // Return the role so we know which dashboard to open
            return "Success:" + user.getString("role");
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    // HELPER: FETCH USER FROM DB
    private static JSONObject getUserDetails(String username) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "?username=eq." + username + "&select=*"))
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer " + API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray jsonArray = new JSONArray(response.body());

        if (jsonArray.isEmpty()) return null;
        return jsonArray.getJSONObject(0);
    }
}
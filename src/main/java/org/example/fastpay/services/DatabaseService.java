package org.example.fastpay.services;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DatabaseService {

    private static final String SUPABASE_URL = "https://kkmcepagifexhdeodyog.supabase.co/rest/v1/users";
    private static final String API_KEY = "sb_publishable_Ebi_9rInKDfL_vP1i3IWvg_BnMUUWFY";
    private static final HttpClient client = HttpClient.newHttpClient();

    // REGISTER USING SUPABASE GOTRUE AUTH
    public static String registerUser(String email, String password, String fullName, String cnic, String phone) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("password", password);

            // Store extra user details in the auth.users metadata
            JSONObject userMetaData = new JSONObject();
            userMetaData.put("full_name", fullName);
            userMetaData.put("cnic", cnic);
            userMetaData.put("phone", phone);
            userMetaData.put("role", "user");

            payload.put("data", userMetaData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/auth/v1/signup"))
                    .header("apikey", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return "Success";
            } else {
                JSONObject errorObj = new JSONObject(response.body());
                return "Registration Error: " + errorObj.optString("msg", "Unknown error occurred.");
            }
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    // LOGIN USING SUPABASE GOTRUE AUTH
    public static String loginUser(String email, String password) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/auth/v1/token?grant_type=password"))
                    .header("apikey", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                // Extract role from user metadata if you need it for routing
                JSONObject userObj = jsonResponse.getJSONObject("user");
                JSONObject metadata = userObj.getJSONObject("user_metadata");
                String role = metadata.optString("role", "user");

                return "Success:" + role;
            } else {
                JSONObject errorObj = new JSONObject(response.body());
                return "Login Error: " + errorObj.optString("error_description", "Invalid credentials.");
            }
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }
}
package org.example.fastpay.services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DatabaseService {

    // FIXED: The base URL should only be the domain name
    private static final String SUPABASE_URL = "https://kkmcepagifexhdeodyog.supabase.co";

    // NOTE: Make sure this is your 'anon public' key from Supabase Settings -> API.
    // Standard Supabase anon keys usually start with "eyJ...".
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
                return "Registration Error: " + errorObj.optString("msg", errorObj.optString("error_description", "Unknown error occurred."));
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

                // Extracting Session Data
                String accessToken = jsonResponse.getString("access_token");
                JSONObject userObj = jsonResponse.getJSONObject("user");
                String userId = userObj.getString("id");
                String userEmail = userObj.getString("email");

                JSONObject metadata = userObj.getJSONObject("user_metadata");
                String role = metadata.optString("role", "user");

                // Return a delimited string so AuthController can parse it
                return "Success|" + accessToken + "|" + userId + "|" + userEmail + "|" + role;
            } else {
                JSONObject errorObj = new JSONObject(response.body());
                return "Login Error: " + errorObj.optString("error_description", "Invalid credentials.");
            }
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    // FETCH USER PARTITIONS
    public static org.json.JSONArray getUserPartitions(String userId, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/rest/v1/wallet_partitions?user_id=eq." + userId + "&order=created_at.asc"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new org.json.JSONArray(response.body());
            }
        } catch (Exception e) {
            System.out.println("Error fetching partitions: " + e.getMessage());
        }
        return new org.json.JSONArray(); // Return empty array on failure
    }

    // CREATE NEW PARTITION (ATOMIC TRANSFER)
    public static boolean createPartition(String userId, String name, String sourceId, double amount, String token) {
        try {
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("p_user_id", userId);
            payload.put("p_name", name);
            payload.put("p_source_id", sourceId);
            payload.put("p_amount", amount);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/rpc/create_partition_with_transfer"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            // Supabase RPC returns 200 OK on success, and 400 Bad Request if our "Insufficient Funds" exception triggers
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.out.println("Error creating partition: " + e.getMessage());
            return false;
        }
    }

    // FETCH USER'S ISSUED CARD
    public static JSONObject getIssuedCard(String userId, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/rest/v1/issued_cards?user_id=eq." + userId + "&select=*"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray cards = new JSONArray(response.body());
                if (cards.length() > 0) {
                    return cards.getJSONObject(0); // Return the first (and only) card
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching card: " + e.getMessage());
        }
        return null; // Null means no card exists
    }

    // ORDER A NEW FASTPAY CARD
    public static boolean orderCard(String userId, String address, String cardTier, String token) {
        try {
            java.util.Random random = new java.util.Random();

            // Generate standard 16 digit card number based on tier
            String prefix = cardTier.equals("Visa") ? "4242" : (cardTier.equals("Mastercard") ? "5555" : "6011");
            String cardNumber = prefix + String.format("%04d", random.nextInt(10000)) +
                    String.format("%04d", random.nextInt(10000)) +
                    String.format("%04d", random.nextInt(10000));

            String cvv = String.format("%03d", random.nextInt(1000));
            java.time.LocalDate futureDate = java.time.LocalDate.now().plusYears(4);
            String expiry = String.format("%02d/%02d", futureDate.getMonthValue(), futureDate.getYear() % 100);

            JSONObject payload = new JSONObject();
            payload.put("user_id", userId);
            payload.put("card_number", cardNumber);
            payload.put("cvv", cvv);
            payload.put("expiry_date", expiry);
            payload.put("delivery_address", address);
            payload.put("card_tier", cardTier); // NEW COLUMN ADDED HERE
            payload.put("status", "ACTIVE");
            payload.put("daily_limit", 50000.00);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/issued_cards"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            System.out.println("Error ordering card: " + e.getMessage());
            return false;
        }
    }
    // PROCESS ATOMIC DEPOSIT
    public static boolean processDeposit(String userId, double amount, String token) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("p_user_id", userId);
            payload.put("p_amount", amount);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    // Calling the RPC (Remote Procedure Call) endpoint
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/rpc/process_deposit"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.out.println("Error processing deposit: " + e.getMessage());
            return false;
        }
    }
    // UNIFIED ADVANCED CARD UPDATE (Handles Status, Limits, PIN, and Toggles)
    public static boolean updateCardData(String userId, org.json.JSONObject payload, String token) {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/issued_cards?user_id=eq." + userId))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        } catch (Exception e) {
            System.out.println("Error updating card: " + e.getMessage());
            return false;
        }
    }
    // TRANSFER FUNDS BETWEEN PARTITIONS
    public static boolean transferFunds(String userId, String sourceId, String destId, double amount, String token) {
        try {
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("p_user_id", userId);
            payload.put("p_source_id", sourceId);
            payload.put("p_dest_id", destId);
            payload.put("p_amount", amount);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/rpc/transfer_between_partitions"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            System.out.println("Transfer Error: " + e.getMessage());
            return false;
        }
    }

    // DELETE PARTITION (WITH SAFE SWEEP)
    public static boolean deletePartition(String userId, String partitionId, String token) {
        try {
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("p_user_id", userId);
            payload.put("p_partition_id", partitionId);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/rpc/delete_partition_sweep"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            System.out.println("Delete Error: " + e.getMessage());
            return false;
        }
    }
    // FETCH TRANSACTION HISTORY
    public static org.json.JSONArray getTransactions(String userId, String token) {
        try {
            // Fetch ordered by created_at DESC (newest first)
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(SUPABASE_URL + "/rest/v1/transactions?user_id=eq." + userId + "&order=created_at.desc"))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new org.json.JSONArray(response.body());
            }
        } catch (Exception e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }
        return new org.json.JSONArray(); // Return empty array on failure
    }
}
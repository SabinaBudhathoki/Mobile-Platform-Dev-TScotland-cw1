

/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 __Sabina Budhathoki__
// Student ID           _S2366848_
// Programme of Study   _BSc (Hons) Computing_
//

package com.example.s2366848;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.Manifest;
import android.view.View.OnClickListener;
import android.location.Location;
import android.location.LocationManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private ListView incidentsList;
    private ListView roadworksList;
    private Button startButton;

    // Stores the search box and search button
    private EditText searchRoadInput;
    private Button searchButton;
    private ArrayList<Situation> incidentSituations = new ArrayList<>();
    private ArrayList<Situation> roadworkSituations = new ArrayList<>();

    //stores the user's current location
    private Location userLocation;
    private Button nearbyButton;

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    private String clientId;
    private String clientKey;
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    final String currentRoadworkURL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Check whether approximate location permission has been granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Ask the user for permission to access approximate location
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
        }
        //Read API credentials from AndroidManifest
        try {
            Bundle metaData = getPackageManager()
                    .getApplicationInfo(
                            getPackageName(),
                            PackageManager.GET_META_DATA
                    ).metaData;
            clientId = metaData.getString("com.example.s2366848.CLIENT_ID");
            clientKey = metaData.getString("com.example.s2366848.CLIENT_KEY");
        } catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException(e);
        }
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
        // Connect the layout views to the Java variables
        incidentsList = findViewById(R.id.incidentsList);
        roadworksList = findViewById(R.id.roadworksList);
        startButton = findViewById(R.id.startButton);

        // Connect the search box and button to the layout
        searchRoadInput = findViewById(R.id.searchRoadInput);
        searchButton = findViewById(R.id.searchButton);

        // Connect the nearby situations button to the layout
        nearbyButton = findViewById(R.id.nearbyButton);

        // Allow users to select an individual incident
        incidentsList.setOnItemClickListener((parent, view, position, id) -> {
            Situation selectedSituation = incidentSituations.get(position);
            showSituationDetails(selectedSituation);
        });

        // Allow users to select an individual roadwork
        roadworksList.setOnItemClickListener((parent, view, position, id) -> {
            Situation selectedSituation = roadworkSituations.get(position);
            showSituationDetails(selectedSituation);
        });

        startButton.setOnClickListener(this);

        // Run the road name search when the button is pressed
        searchButton.setOnClickListener(v -> searchSituations());

        // Find nearby traffic situations when the button is pressed
        nearbyButton.setOnClickListener(v -> findNearbySituations());
    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        // Run network access on a separate thread;
        new Thread(new Task(unplannedEventsURL, currentRoadworkURL)).start();
    } //

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        private String result="";
        private String myurl;
        private String currentRoadworkurl;
        BufferedReader in = null;
        String inputLine = "";
        public Task(String aurl, String currentRoadworkURL){
            myurl = aurl;
            this.currentRoadworkurl = currentRoadworkURL;
        }
        private String downloadFeed(String feedUrl) throws IOException {
            StringBuilder data = new StringBuilder();

            URL url = new URL(feedUrl);
            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection();

            String auth = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientKey).getBytes()
            );

            urlConnection.setRequestProperty("Authorization", auth);

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream())
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }

                reader.close();
            } finally {
                urlConnection.disconnect();
            }

            return data.toString();
        }
        @Override
        public void run(){
            Log.d("MyTask","in run");
            String unplannedXml = "";
            String currentRoadworkXml = "";

            try {
                unplannedXml = downloadFeed(myurl);
            } catch (IOException e) {
                Log.e("Download", "Could not download unplanned events", e);
            }

            try {
                currentRoadworkXml = downloadFeed(currentRoadworkurl);
            } catch (IOException e) {
                Log.e("Download", "Could not download current roadworks", e);
            }


            // Remove any leading characters before the XML declaration
            StringBuilder displayText = new StringBuilder();
            TrafficDataParser parser = new TrafficDataParser();

            // Parse unplanned events
            try {
                if (!unplannedXml.isEmpty()) {
                    int i = unplannedXml.indexOf("<?");
                    if (i >= 0) {
                        unplannedXml = unplannedXml.substring(i);
                    }
                    //parse unplanned events and store them in the incident list
                    incidentSituations = parser.parse(unplannedXml);

                    displayText.append("UNPLANNED EVENTS\n\n");

                    // Go through each parsed unplanned incident
                    for (Situation situation : incidentSituations) {
                        displayText.append("Type: ")
                                .append(situation.getType())
                                .append("\n");

                        displayText.append("Description: ")
                                .append(situation.getComment())
                                .append("\n");

                        displayText.append("Road: ")
                                .append(situation.getRoadName())
                                .append("\n");

                        displayText.append("Latitude: ")
                                .append(situation.getLatitude())
                                .append("\n");

                        displayText.append("Longitude: ")
                                .append(situation.getLongitude())
                                .append("\n");

                        displayText.append("Start Time: ")
                                .append(situation.getStartTime())
                                .append("\n");

                        displayText.append("Delay: ")
                                .append(situation.getDelay())
                                .append("\n\n");
                    }
                }
            } catch (XmlPullParserException | IOException e) {
                Log.e("Parsing", "Could not parse unplanned events", e);
            }

            // Parse current roadworks
            try {
                if (!currentRoadworkXml.isEmpty()) {
                    int i = currentRoadworkXml.indexOf("<?");
                    if (i >= 0) {
                        currentRoadworkXml = currentRoadworkXml.substring(i);
                    }

                    //parse current roadworks and store them in the roadworks list
                    roadworkSituations = parser.parse(currentRoadworkXml);

                    displayText.append("CURRENT ROADWORKS\n\n");

                    // Go through each parsed roadwork
                    for (Situation situation : roadworkSituations) {

                        displayText.append("Type: ")
                                .append(situation.getType())
                                .append("\n");

                        displayText.append("Description: ")
                                .append(situation.getComment())
                                .append("\n");

                        displayText.append("Road: ")
                                .append(situation.getRoadName())
                                .append("\n");

                        displayText.append("Latitude: ")
                                .append(situation.getLatitude())
                                .append("\n");

                        displayText.append("Longitude: ")
                                .append(situation.getLongitude())
                                .append("\n");

                        displayText.append("Start Time: ")
                                .append(situation.getStartTime())
                                .append("\n");

                        displayText.append("Delay: ")
                                .append(situation.getDelay())
                                .append("\n\n");
                    }
                }
            } catch (XmlPullParserException | IOException e) {
                Log.e("Parsing", "Could not parse current roadworks", e);
            }

            //update the incident and roadwork lists with the parsed situation descriptions
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ArrayList<String> incidentDescriptions = new ArrayList<>();
                    ArrayList<String> roadworkDescriptions = new ArrayList<>();

                    for (Situation situation : incidentSituations) {
                        incidentDescriptions.add(
                                situation.getType() + "\n" +
                                        situation.getComment()
                        );
                    }

                    for (Situation situation : roadworkSituations) {
                        roadworkDescriptions.add(
                                situation.getType() + "\n" +
                                        situation.getComment()
                        );
                    }

                    ArrayAdapter<String> incidentAdapter =
                            new ArrayAdapter<>(
                                    MainActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    incidentDescriptions
                            );

                    // Create a colour-coded adapter for current roadworks
                    ArrayAdapter<String> roadworkAdapter =
                            createRoadworkAdapter(roadworkSituations);

                    incidentsList.setAdapter(incidentAdapter);
                    roadworksList.setAdapter(roadworkAdapter);
                }
            });
        }

    }
    // Display the selected situation's details in a dialog
    private void showSituationDetails(Situation situation) {

        String details =
                "Type: " + situation.getType() +
                        "\n\nDescription: " + situation.getComment() +
                        "\n\nRoad: " + situation.getRoadName() +
                        "\n\nStart time: " + situation.getStartTime() +
                        "\n\nDelay: " + situation.getDelay() +
                        "\n\nLatitude: " + situation.getLatitude() +
                        "\nLongitude: " + situation.getLongitude();

        new AlertDialog.Builder(this)
                .setTitle("Situation Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }
    // Search incidents and roadworks using the entered road name
    private void searchSituations() {

        String searchText = searchRoadInput.getText().toString()
                .trim().toLowerCase();

        ArrayList<String> filteredIncidents = new ArrayList<>();
        ArrayList<String> filteredRoadworks = new ArrayList<>();

        // Find incidents matching the road name
        for (Situation situation : incidentSituations) {
            if (situation.getRoadName() != null &&
                    situation.getRoadName().toLowerCase().contains(searchText)) {

                filteredIncidents.add(
                        situation.getType() + "\n" + situation.getComment()
                );
            }
        }

        // Find roadworks matching the road name
        for (Situation situation : roadworkSituations) {
            if (situation.getRoadName() != null &&
                    situation.getRoadName().toLowerCase().contains(searchText)) {

                filteredRoadworks.add(
                        situation.getType() + "\n" + situation.getComment()
                );
            }
        }

        // Display the matching results in both lists
        incidentsList.setAdapter(new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                filteredIncidents
        ));

        roadworksList.setAdapter(new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                filteredRoadworks
        ));
    }
    // Find traffic situations close to the user's location
    private void findNearbySituations() {

        // Check that location permission is available
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
            return;
        }

        // Retrieve the last known location
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            userLocation = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
            );

            // Try other enabled providers if network location is unavailable
            if (userLocation == null) {
                for (String provider : locationManager.getProviders(true)) {
                    Location location =
                            locationManager.getLastKnownLocation(provider);

                    if (location != null &&
                            (userLocation == null ||
                                    location.getTime() > userLocation.getTime())) {
                        userLocation = location;
                    }
                }
            }

        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission is required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check whether the device has a location available
        if (userLocation == null) {
            Toast.makeText(this,
                    "Location unavailable. Please try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Collect situations within 50 kilometres
        ArrayList<Situation> nearbySituations = new ArrayList<>();

        for (Situation situation : incidentSituations) {
            if (getDistance(situation) <= 50000) {
                nearbySituations.add(situation);
            }
        }

        for (Situation situation : roadworkSituations) {
            if (getDistance(situation) <= 50000) {
                nearbySituations.add(situation);
            }
        }

        // Sort situations from nearest to furthest
        Collections.sort(nearbySituations,
                (first, second) ->
                        Float.compare(getDistance(first), getDistance(second)));

        if (nearbySituations.isEmpty()) {
            Toast.makeText(this, "No nearby situations found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the nearby situations for display
        String[] descriptions = new String[nearbySituations.size()];

        for (int i = 0; i < nearbySituations.size(); i++) {
            Situation situation = nearbySituations.get(i);
            float distance = getDistance(situation) / 1000;

            descriptions[i] = situation.getType() +
                    "\n" + situation.getRoadName() +
                    "\n" + String.format("%.1f km away", distance);
        }

        // Display nearby situations and allow users to select one
        new AlertDialog.Builder(this)
                .setTitle("Nearby Traffic Situations")
                .setItems(descriptions, (dialog, which) ->
                        showSituationDetails(nearbySituations.get(which)))
                .setPositiveButton("Close", null)
                .show();
    }

    // Calculate the distance between the user and a traffic situation
    private float getDistance(Situation situation) {

        try {
            double latitude = Double.parseDouble(situation.getLatitude());
            double longitude = Double.parseDouble(situation.getLongitude());

            float[] results = new float[1];

            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    latitude,
                    longitude,
                    results
            );

            return results[0];

        } catch (Exception e) {
            return Float.MAX_VALUE;
        }
    }
    // Display roadworks with colours based on their delay
    private ArrayAdapter<String> createRoadworkAdapter(
            ArrayList<Situation> situations) {

        ArrayList<String> descriptions = new ArrayList<>();

        for (Situation situation : situations) {
            descriptions.add(
                    situation.getType() + "\n" + situation.getComment()
            );
        }

        return new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                descriptions
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView textView = (TextView) super.getView(
                        position, convertView, parent
                );

                // Set the text colour according to the delay
                String delay = situations.get(position).getDelay();

                if ("longDelays".equalsIgnoreCase(delay)) {
                    textView.setTextColor(Color.RED);
                } else if ("delays".equalsIgnoreCase(delay)) {
                    textView.setTextColor(0xFFFF9800);
                } else if ("noDelays".equalsIgnoreCase(delay)) {
                    textView.setTextColor(Color.GREEN);
                } else {
                    textView.setTextColor(Color.BLACK);
                }

                return textView;
            }
        };
    }
}
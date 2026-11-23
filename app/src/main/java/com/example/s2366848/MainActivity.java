

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
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;

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

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
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

                    ArrayList<Situation> situations = parser.parse(unplannedXml);

                    displayText.append("UNPLANNED EVENTS\n\n");

                    for (Situation situation : situations) {
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

                    ArrayList<Situation> situations = parser.parse(currentRoadworkXml);

                    displayText.append("CURRENT ROADWORKS\n\n");

                    for (Situation situation : situations) {

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

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    rawDataDisplay.setText(displayText.toString());

                }
            });
        }

    }

}
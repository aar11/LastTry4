package com.example.conno08.lasttry4;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.conno08.lasttry4.R;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements LocationListener  {
    private static final String TAG = "OAuthDemo";
    private static final String OAUTH_KEY = "FXQdWceOloSZ9ilxCaJstJCuF";
    private static final String OAUTH_SECRET = "P3UmvM5P6JzsWFuyFZUbosKHCoOsKpRSKu4l6yieXWKmxpNTQc";
    private static final String OAUTH_CALLBACK_SCHEME = "x-marakana-oauth-twitter";
    private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME
            + "://callback";
    private static final String TWITTER_USER = "ISeeTraffic@gmail.com";
    //Variables
    private OAuthSignpostClient oauthClient;
    private OAuthConsumer mConsumer;
    private OAuthProvider mProvider;
    private Twitter twitter;
    SharedPreferences prefs;
    private TextView addressField;
    private LocationManager locationManager;
    private String provider;
    private TextView latituteField;
    private TextView longitudeField;
    private TextView text1;
    private TextView thePrint;
    private ImageView traffic;
    private ImageView accident;
    private TextView reports;
    private Button reportBtn;
    private ListView lastTry;
    private ArrayAdapter<String> listAdapter ;
    private static int id = 0;
    //private Button speakButton;
    protected static final int REQUEST_OK = 1;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Requesting Consumer Key, Customer Secret & Access Tokens
        // mConsumer = new DefaultOAuthConsumer(OAUTH_KEY, OAUTH_SECRET);
        mConsumer = new CommonsHttpOAuthConsumer(OAUTH_KEY, OAUTH_SECRET);
        mProvider = new DefaultOAuthProvider(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");

        // Reads the prefernces to see if we have tokens
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString("token", null);
        String tokenSecret = prefs.getString("tokenSecret", null);
        if (token != null && tokenSecret != null) {
            // Token is available
            mConsumer.setTokenWithSecret(token, tokenSecret);
            // Create Twitter Object
            oauthClient = new OAuthSignpostClient(OAUTH_KEY, OAUTH_SECRET, token,
                    tokenSecret);
            twitter = new Twitter(TWITTER_USER, oauthClient);
        }

        addressField = (TextView) findViewById(R.id.textView03);
        text1 = (TextView) findViewById(R.id.text1);
        startVoiceRecognitionActivity();
        //Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Define criteria how to select the location provider
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        //Initialise location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latituteField.setText("Location not available");
            longitudeField.setText("Location not available");
        }
        //startVoiceRecognitionActivity();

        //Database object - Reporting to log
        DatabaseHandler db = new DatabaseHandler(this);
        // Reading through all locations in the table
        Log.d("Reading: ", "Reading all Traffic..");
        List<TrafficInfo> traffics = db.getAllTraffic();
        for (TrafficInfo cn : traffics) {
            String log = "Id: " + cn.getID() + " ,Location: " +
                    cn.getLocation();
            // Writing Contacts to log
            Log.d("Location: ", log);
        }

        //Gets the number of times a traffic or accident is reported
        db.getTrafficCount();
        String theCount = "The number of traffic & accident reports made this week are " + db.getTrafficCount();
        Toast.makeText(this, " : " + theCount, Toast.LENGTH_LONG).show();
        reportBtn = (Button) findViewById(R.id.report);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,RecentReportsActivity.class);
                startActivity(i);
            }
        });

    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    //Initialise the latitude and lontitude fields
    @Override
    public void onLocationChanged(Location location) {
        double lat = (double) (location.getLatitude());
        double lng = (double) (location.getLongitude());
        //latituteField.setText(String.valueOf(lat));
        //longitudeField.setText(String.valueOf(lng));

        //Converts the lat and long coordinates into an address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                //Initialise the address field with the address
                addressField.setText(strReturnedAddress.toString().toUpperCase());
            } else {
                addressField.setText("No address returned");
            }
        } catch (IOException e) {
            e.printStackTrace();
            addressField.setText("Cannot get address");
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    //Is provider available
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    //Is provider not available
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    /* Callback once we are done with the authorization of this app with Twitter. */
    //Callback once the app is authorized with Twitter
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "intent: " + intent);

        // Check if this is a callback from OAuth
        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) {
            Log.d(TAG, "callback: " + uri.getPath());

            String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
            Log.d(TAG, "verifier: " + verifier);

            new RetrieveAccessTokenTask().execute(verifier);
        }

    }

    public void onClickAuthorize(View view) {
        new OAuthAuthorizeTask().execute();
    }

    //Twitter post
    public void onClickTweet(View view) {
        if (twitter == null) {
            Toast.makeText(this, "Authenticate first", Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseHandler db = new DatabaseHandler(this);
        //EditText status = (EditText) findViewById(R.id.status);
        //new PostStatusTask().execute(status.getText().toString());
        TextView addressField = (TextView) findViewById(R.id.textView03);
        String location = addressField.getText().toString();
        TextView text1 = (TextView) findViewById(R.id.text1);
        //ImageView condition = (ImageView) findViewById(R.id.condition);
        String dataProtect = text1.getText().toString().toLowerCase();
        if (dataProtect.equals("traffic")) {
            //Gives the post an incrementing id
            id++;
            new PostStatusTask().execute(id + ": " + text1.getText().toString().toUpperCase() + " ON: " + addressField.getText().toString().toUpperCase() + "#ISeeTraffic");
            //Adds the location to the database
            db.addTraffic(new TrafficInfo(location));
        } else if (dataProtect.equals("accident")) {
            id++;
            new PostStatusTask().execute(id + ": " + text1.getText().toString().toUpperCase() + " ON: " + addressField.getText().toString().toUpperCase() + "#ISeeTraffic");
            //Adds the location to the database
            db.addTraffic(new TrafficInfo(location));

        } else {
            //Validation - Users can only use to commands - Traffic & Accident
            Toast.makeText(this, "Please say traffic or accident", Toast.LENGTH_LONG).show();
            return;
        }
    }

    //Checks status to see if you are authenticated
    public void onClickGetStatus(View view) {
        if (twitter == null) {
            Toast.makeText(this, "Authenticate first", Toast.LENGTH_LONG).show();
            return;
        }
        new GetStatusTask().execute();
    }

    //Starts Twitter Authorization
    /* Responsible for starting the Twitter authorization */
    class OAuthAuthorizeTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String authUrl;
            String message = null;
            try {
                authUrl = mProvider.retrieveRequestToken(mConsumer, OAUTH_CALLBACK_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                startActivity(intent);
            } catch (OAuthMessageSignerException e) {
                message = "OAuthMessageSignerException";
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                message = "OAuthNotAuthorizedException";
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                message = "OAuthExpectationFailedException";
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                message = "OAuthCommunicationException";
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Responsible for retrieving access tokens from twitter */
    class RetrieveAccessTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String message = null;
            String verifier = params[0];
            try {
                // Get the token
                Log.d(TAG, "mConsumer: " + mConsumer);
                Log.d(TAG, "mProvider: " + mProvider);
                mProvider.retrieveAccessToken(mConsumer, verifier);
                String token = mConsumer.getToken();
                String tokenSecret = mConsumer.getTokenSecret();
                mConsumer.setTokenWithSecret(token, tokenSecret);

                Log.d(TAG, String.format("verifier: %s, token: %s, tokenSecret: %s",
                        verifier, token, tokenSecret));

                // Store token in prefs
                prefs.edit().putString("token", token).putString("tokenSecret",
                        tokenSecret).commit();

                // Make a Twitter object
                oauthClient = new OAuthSignpostClient(OAUTH_KEY, OAUTH_SECRET, token,
                        tokenSecret);
                twitter = new Twitter("ISeeTraffic", oauthClient);

                Log.d(TAG, "token: " + token);
            } catch (OAuthMessageSignerException e) {
                message = "OAuthMessageSignerException";
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                message = "OAuthNotAuthorizedException";
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                message = "OAuthExpectationFailedException";
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                message = "OAuthCommunicationException";
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Responsible for getting Twitter status */
    class GetStatusTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return twitter.getStatus().text;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    /* Responsible for posting new status to Twitter */
    class PostStatusTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                twitter.setStatus(params[0]);
                return "Successfully posted: " + params[0];
            } catch (TwitterException e) {
                return "Error connecting to server.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
        }

    }

    //Voice Recognition
    public void speakButtonClicked(View v) {
        startVoiceRecognitionActivity();
    }

    private void startVoiceRecognitionActivity() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    //Handling the results of the voice recognition activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((TextView) findViewById(R.id.text1)).setText(thingsYouSaid.get(0).toUpperCase());
        }
    }

 }






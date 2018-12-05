package qualityoverquantity.hexagrama.util;

import android.content.Context;
import android.support.constraint.solver.Cache;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RESTRequestSender {
    private RequestQueue requestQueue;
    private String baseUrl;
    private Context context;

    public RESTRequestSender(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.baseUrl = "https://jsonplaceholder.typicode.com/todos/";
    }

    //InputStream is for testing purpose.
    public ArrayList<String> sendRequest(InputStream is) {
        // We create a new JsonArrayRequest. This will use Volley to make a HTTP request
        // that expects a JSON Array Response.
        // To fully understand this, I'd recommend readng the office docs: https://developer.android.com/training/volley/index.html
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, baseUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Check the length of our response (to see if the user has any repos)
                        if (response.length() > 0) {
                            // The user does have repos, so let's loop through them all.
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    // For each repo, add a new line to our repo list.
                                    JSONObject jsonObj = response.getJSONObject(i);
                                    String title = jsonObj.get("title").toString();
                                    Log.i("JSON", title);
                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }

                            }
                        } else {
                            // The user didn't have any repos.
                            //Log.e("Request", "Response is not correct or empty.");
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        //setRepoListText("Error while calling REST API");
                        Log.e("Volley", error.toString());
                    }
                }
        );
        // Add the request we just defined to our request queue.
        // The request queue will automatically handle the request as soon as it can.
        requestQueue.add(arrReq);
        return obtainNotes(is);
    }

    private ArrayList<String> obtainNotes(InputStream is) {
        ArrayList<String> notes = new ArrayList<String>();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] lineSplit = line.split(",");
                for (String note: lineSplit) notes.add(note);
            }
            reader.close();
            for(String note : notes) Log.i("Notes", note);
            return notes;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read.");
            e.printStackTrace();
            return null;
        }
    }
}

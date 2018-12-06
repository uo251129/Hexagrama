package qualityoverquantity.hexagrama.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.solver.Cache;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RESTRequestSender {
    private RequestQueue requestQueue;
    private String baseUrl;
    private Context context;
    private ArrayList<String> notes;

    public RESTRequestSender(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.baseUrl = "";
        notes = new ArrayList<String>();
    }

    //InputStream is for testing purpose.
    public ArrayList<String> sendRequest(Bitmap image) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Pentagrama", bitMapToString(image));
            final String requestBody = jsonBody.toString();

            StringRequest arrReq = new StringRequest(Request.Method.POST, baseUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Check the length of our response (to see if the user has any repos)
                            if (response.length() > 0) {
                                notes = obtainNotes(response);
                            } else {
                                // The user didn't have any repos.
                                Log.e("Request", "Response is not correct or empty.");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley", error.toString());
                        }
                    }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        return null;
                    }
                }
            };
            // Add the request we just defined to our request queue.
            // The request queue will automatically handle the request as soon as it can.
            requestQueue.add(arrReq);

            return notes;
        } catch (Exception e) {
            return null;
        }
    }

    private ArrayList<String> obtainNotes(String s) {
        Log.i("Notes",s);
        ArrayList<String> notes = new ArrayList<String>();
        String[] lineSplit = s.split(",");
        for (String note: lineSplit) notes.add(note);
        for(String note : notes) Log.i("Notes", note);
        return notes;
    }

    private String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}

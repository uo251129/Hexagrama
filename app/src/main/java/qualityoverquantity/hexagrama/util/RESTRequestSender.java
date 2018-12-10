package qualityoverquantity.hexagrama.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.solver.Cache;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RESTRequestSender {
    private RequestQueue requestQueue;
    private String baseUrl = "http://18.218.59.101:80/audiveris/json";
    private Context context;
    public ArrayList<String> notes = new ArrayList<String>();

    private static RESTRequestSender instance;

    private RESTRequestSender() {}

    public static RESTRequestSender getInstance() {
        if(instance == null) {
            instance = new RESTRequestSender();
        }
        return instance;
    }

    //InputStream is for testing purpose.
    public void sendRequest(Context context, Bitmap image, final VolleyCallBack callBack) {
        try {
            this.context = context;
            this.requestQueue = Volley.newRequestQueue(context);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("image", bitMapToString(image));
            Log.i("body",jsonBody.toString());
            final String requestBody = jsonBody.toString();

            StringRequest arrReq = new StringRequest(Request.Method.POST, baseUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Check the length of our response (to see if the user has any repos)
                            if (response.length() > 0) {
                                Log.i("Request",response);
                                notes = obtainNotes(response);
                                Log.i("Request",notes.toString());

                                callBack.onSuccess();
                            } else {
                                // The user didn't have any repos.
                                Log.e("Request", "Response is not correct or empty.");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Request", error.toString());
                            callBack.onError();
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

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

            };

            arrReq.setRetryPolicy(new DefaultRetryPolicy(
                    40000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request we just defined to our request queue.
            // The request queue will automatically handle the request as soon as it can.
            requestQueue.add(arrReq);
        } catch (Exception e) {
            Log.e("Request",e.getMessage());
        }
    }

    private ArrayList<String> obtainNotes(String s) {
        Log.i("Notes",s);
        ArrayList<String> notes = new ArrayList<String>();
        String[] lineSplit = s.split(", ");
        for (String note: lineSplit) notes.add(note);
        for(String note : notes) Log.i("Notes", note);
        return notes;
    }

    private String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.NO_WRAP);
        return temp;
    }
}

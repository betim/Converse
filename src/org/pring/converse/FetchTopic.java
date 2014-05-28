package org.pring.converse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class FetchTopic extends AsyncTask<String, Void, JSONObject> {
  Context ctx = null;

  public FetchTopic(Context c) {
    ctx = c;
  }

  @Override
  protected JSONObject doInBackground(String... params) {
    try {
      if (isNetworkAvailable()) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();

        if (params[0].equals("All=0"))
          request.setURI(new URI(
              "http://chatoms.com/chatom.json?Normal=1&Fun=2"
                  + "&Philosophy=3&Out+There=4&Love=5&Naughty=6&Personal=7"));
        else
          request
              .setURI(new URI("http://chatoms.com/chatom.json?" + params[0]));

        HttpResponse response = client.execute(request);
        BufferedReader in = new BufferedReader(new InputStreamReader(response
            .getEntity().getContent()));

        StringBuffer sb = new StringBuffer("");
        String line = "";

        while ((line = in.readLine()) != null)
          sb.append(line);

        in.close();

        String result = sb.toString();

        JSONObject json = new JSONObject(result);
        json.put("last_used", System.currentTimeMillis());

        return json;
      } else {
        String[] _id = params[0].split("=");

        Cursor c = _id[1].equals("0") ? 
          MainActivity.database.rawQuery(
            "SELECT * FROM " + DataBaseHelper.TABLE_TOPICS
                + " ORDER BY RANDOM() LIMIT 1", null) : 
          MainActivity.database
            .rawQuery("SELECT * FROM " + DataBaseHelper.TABLE_TOPICS
                + " WHERE category_id = ? ORDER BY RANDOM() LIMIT 1",
                new String[] { _id[1] });

        c.moveToFirst();

        JSONObject json = new JSONObject();
        if (c.getCount() == 0) {
          json.put("text", "No matches from cache, try another category :(");
          json.put("id", -1);
          json.put("last_used", System.currentTimeMillis());

          c.close();
          return json;
        } else {
          json.put("id", c.getInt(1));
          json.put("starters_category_id", c.getInt(2));
          json.put("text", c.getString(3));
          json.put("last_used", c.getLong(4));

          c.close();
          return json;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  protected void onPreExecute() {
    TextView lastUsed = (TextView) MainActivity.rootView
        .findViewById(R.id.last_used);
    lastUsed.setText("Fetching ...");

    TextView textView = (TextView) MainActivity.rootView
        .findViewById(R.id.topic);
    textView.setVisibility(View.GONE);

    ProgressBar progressBar = (ProgressBar) MainActivity.rootView
        .findViewById(R.id.progressBar);
    progressBar.setVisibility(View.VISIBLE);

    super.onPreExecute();
  }

  @Override
  protected void onPostExecute(JSONObject result) {
    try {
      TextView lastUsed = (TextView) MainActivity.rootView
          .findViewById(R.id.last_used);

      ProgressBar progressBar = (ProgressBar) MainActivity.rootView
          .findViewById(R.id.progressBar);

      TextView textView = (TextView) MainActivity.rootView
          .findViewById(R.id.topic);

      progressBar.setVisibility(View.GONE);
      textView.setVisibility(View.VISIBLE);
      textView.setText(result.getString("text"));

      lastUsed.setText("Last time you talked about this: \n"
          + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(result
              .getLong("last_used"))));

      if (result.getInt("id") == -1)
        lastUsed.setVisibility(View.GONE);
      else
        MainActivity.database.execSQL(
            "INSERT OR REPLACE INTO " + DataBaseHelper.TABLE_TOPICS
                + " VALUES (NULL, ?, ?, ?, ?);",
            new Object[] { result.getInt("id"),
                result.getInt("starters_category_id"),
                result.getString("text"), System.currentTimeMillis() });
    } catch (Exception e) {
      e.printStackTrace();
    }

    super.onPostExecute(result);
  }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) ctx
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
}
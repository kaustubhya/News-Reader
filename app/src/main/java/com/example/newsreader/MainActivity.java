package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");

        // Download Task
        DownloadTask task = new DownloadTask();
        try{
            // NO NEED TO RUN AS WE ALREADY HAVE SOME INFORMATION SAVED in the database......
           // task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }
        catch(Exception e){
        }

        ListView listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        // jump to article activity from the following line of code
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("content", content.get(i));
                startActivity(intent);
            }
        });

        // calling update list view at appropriate places
        updateListView();
    }

    // update the table when we get new values
     public void updateListView() {
        // now bring stuff out of the database, put it into arrays and display it to the user
         Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

         int contentIndex = c.getColumnIndex("content");
         int titleIndex = c.getColumnIndex("title");

         if(c.moveToFirst()){
             titles.clear();
             content.clear();

             do {
                 titles.add(c.getString(titleIndex));
                 content.add(c.getString(contentIndex));
             }while(c.moveToNext());

             arrayAdapter.notifyDataSetChanged();
             // notify us if any info is changed
         }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{
//                                               input  updates  output
        @Override
        protected String doInBackground(String... urls) {

            // Code to get URL
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            // set to null initially

            // try to get something from url and start an input stream

            try{

                url = new URL(urls[0]);
                // get new url at position 0 if there's any
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                // used to parse all the info with while loop

                while (data != -1) {
                    char currentInfo = (char) data;
                    result += currentInfo;
                    data = inputStreamReader.read();
                }

                // Using JSON for title and elements i.e. ids of top news stories which we'll grab from the website

                JSONArray jsonArray = new JSONArray(result);

                int numberOfItems = 20;

                if (jsonArray.length() < 20){
                    numberOfItems = jsonArray.length();
                }

                // Clear out the table before starting
                articlesDB.execSQL("DELETE FROM articles");

                for(int i = 0; i < numberOfItems; i++){
                    String articleId = jsonArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+ articleId + ".json?print=pretty");
                    // we'll grab the respective article id and show the news here


                    // re-run the url sequence with this one
                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);

                    data = inputStreamReader.read();
                    // used to parse all the info with while loop

                    String articleInfo = "";

                    while (data != -1) {
                        char currentInfo = (char) data;
                        articleInfo += currentInfo;
                        data = inputStreamReader.read();
                    }

                    // printing out the title and url only
                    JSONObject jsonObject = new JSONObject(articleInfo);

                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")){
                        // here we are proceeding when json object only have title and url both
                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");


                        // passing this url into a url connection
                        url = new URL(articleUrl);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        inputStream = urlConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);
                        data = inputStreamReader.read();
                        String articleContent = "";
                        // represents HTML from the articles

                        while(data != -1){
                            char current = (char) data;
                            articleContent += current;
                            data = inputStreamReader.read();
                        }

                        Log.i("HTML", articleContent);

                        //process:
//                        Hit API for all article ids
//                        get info for each of those ids which gives us a title and a url
//                        getting html for each of those individual websites


                        // Storing data in SQL now

                        String sql = "INSERT INTO articles (articleId, title, content) VALUES (? ,?, ?)";
                        // no need to write id above

                        // filling the question mark values now
                        SQLiteStatement statement = articlesDB.compileStatement(sql);
                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleContent);

                        statement.execute();
                    }
                }



                Log.i("URL Content", result);
                return result;
                // order should be like this

            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }
    }
}

package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    // basic idea - type on the article and go to its HTML

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        // enable javascript

        webView.setWebViewClient(new WebViewClient());
        // if normal browser is not launched, the user's browser is launched

        Intent intent = getIntent();
        webView.loadData(intent.getStringExtra("content"), "text/html","UTF-8");
    }
}
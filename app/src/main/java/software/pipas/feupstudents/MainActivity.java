package software.pipas.feupstudents;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    String rightCss, leftCss, contentCss;
    Boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rightCss = getEncodedString("rightBar.css");
        leftCss = getEncodedString("leftBar.css");
        contentCss = getEncodedString("content.css");

        webView = (WebView) findViewById(R.id.webview);
        // Enable Javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Add a WebViewClient
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url)
            {
                webView.setVisibility(View.VISIBLE);
                if(firstLoad)
                {
                    loginSigarra();
                    injectCSS(leftCss);
                    firstLoad = false;
                }
                else
                    injectCSS(contentCss);

                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                if(url.startsWith("https://sigarra.up.pt/feup/pt/"))
                {
                    view.loadUrl(url);
                    return false;
                }
                else
                {
                    startInBrowser(url);
                    return true;
                }
            }
        });

        webView.loadUrl("https://sigarra.up.pt/feup/pt/web_page.inicial");
    }

    private void loginSigarra()
    {
        webView.loadUrl("javascript:(function(){" +
                "if(document.getElementsByClassName('nomelogin').length != 0) {" +
                "var u = document.getElementById('user').value = '" + "';" +
                "var p = document.getElementById('pass').value = '" + "';" +
                "document.forms[0].submit()" +
                "}})()");
    }

    private void startInBrowser(String url)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private String getEncodedString(String cssFile)
    {
        try
        {
            InputStream inputStream = null;
            inputStream = getAssets().open(cssFile);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return Base64.encodeToString(buffer, Base64.NO_WRAP);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void injectCSS(String encoded)
    {
        try
        {
            webView.loadUrl(
                    "javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.setAttribute('id', 'customcss');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void removeCSS()
    {
        try
        {
            webView.loadUrl(
                    "javascript:(function() {" +
                    "var elem = document.getElementById('customcss');" +
                    "elem.parentNode.removeChild(elem);"+
                    "})()");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void leftBar(View v)
    {
        removeCSS();
        injectCSS(leftCss);
    }

    public void rightBar(View v)
    {
        removeCSS();
        injectCSS(rightCss);
    }

    public void content(View v)
    {
        removeCSS();
        injectCSS(contentCss);
    }
}

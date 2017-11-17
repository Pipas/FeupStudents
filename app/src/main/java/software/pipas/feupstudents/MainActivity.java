package software.pipas.feupstudents;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private String debugTag = "FEUPDEBUG";


    private AHBottomNavigation bottomNavigation;
    private ObservableWebView webView;
    private SwipeRefreshLayout swipeContainer;
    private Drawer navDrawer;

    private String optionsCss, homeCss, contentCss, username, password;
    private int previousT = 0;
    private Boolean isLoggedIn = false;
    private Boolean animating = false;
    private Boolean loginAttempt = false;
    private Boolean firstLoad = true;

    private DownloadRequest downloadRequest;
    private BookmarkDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLoginCredentials();

        initNavDrawer();

        initEncodedCss();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        populateBottomBar();

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        swipeContainer = findViewById(R.id.swipeContainer);

        setSwipeContainerListener();

        addWebViewClient();

        setWebViewScrollListener();

        setWebViewDownloadListener();

        Log.d(debugTag, "Started loading page");
        webView.loadUrl("https://sigarra.up.pt/feup/pt/web_page.inicial");
    }

    private void initNavDrawer()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IProfile profile;
        if(username != null)
             profile = new ProfileDrawerItem().withName(username).withEmail(username+ "@fe.up.pt");
        else
            profile = new ProfileDrawerItem().withName("Login Inválido").withEmail("Login Inválido");

        AccountHeader navDrawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.header)
                .withProfileImagesVisible(false)
                .addProfiles(profile)
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        navDrawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(navDrawerHeader)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.home).withIcon(FontAwesome.Icon.faw_home),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.add).withIcon(FontAwesome.Icon.faw_plus).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.app_name),
                        new SecondaryDrawerItem().withIdentifier(3).withName(R.string.github).withIcon(FontAwesome.Icon.faw_github).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(4).withName(R.string.feedback).withIcon(FontAwesome.Icon.faw_envelope).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(5).withName(R.string.about).withIcon(FontAwesome.Icon.faw_question_circle).withSelectable(false)
                )
                .withSelectedItem(1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        final int fPosition = position;
                        if(isLoggedIn)
                        {
                            if(drawerItem.getIdentifier() == 1)
                                webView.loadUrl("https://sigarra.up.pt/feup/pt/web_page.inicial");
                            else if(drawerItem.getIdentifier() == 2)
                            {
                                new LovelyTextInputDialog(MainActivity.this)
                                        .setTopColorRes(R.color.primary)
                                        .setTitle(R.string.add_favorite)
                                        .setMessage("Nome do favoito\n\n" + webView.getUrl())
                                        .setInputFilter(R.string.invalid_name, new LovelyTextInputDialog.TextFilter()
                                        {
                                            @Override
                                            public boolean check(String text)
                                            {
                                                return text.matches("\\w+");
                                            }
                                        })
                                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener()
                                        {
                                            @Override
                                            public void onTextInputConfirmed(String text)
                                            {
                                                Bookmark bookmark = new Bookmark(text, webView.getUrl());
                                                db.addBookmark(bookmark);
                                                navDrawer.addItemAtPosition(new PrimaryDrawerItem().withIdentifier(6).withName(text).withIcon(FontAwesome.Icon.faw_file), 2);
                                                navDrawer.setSelectionAtPosition(fPosition);
                                            }
                                        })
                                        .show();
                            }
                            else if(drawerItem.getIdentifier() == 3)
                                startInBrowser("https://github.com/pipas/feupstudents");
                            else if(drawerItem.getIdentifier() == 4)
                                startInBrowser("mailto:pipas.software@gmail.com");
                            else if(drawerItem.getIdentifier() == 5)
                                new LovelyStandardDialog(MainActivity.this)
                                        .setTopColorRes(R.color.primary)
                                        .setTitle(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME)
                                        .setMessage(getString(R.string.about_text))
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            else
                            {
                                Bookmark bookmark = db.getBookmark(fPosition - 1);
                                webView.loadUrl(bookmark.getUrl());
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.logging_in), Toast.LENGTH_SHORT).show();
                            navDrawer.setSelectionAtPosition(1);
                        }

                        return false;
                    }
                })
                .build();

        db = new BookmarkDatabase(this);
        ArrayList<Bookmark> bookmarks = db.getAllBookmarks();
        for(Bookmark bookmark : bookmarks)
        {
            navDrawer.addItemAtPosition(new PrimaryDrawerItem().withIdentifier(6).withName(bookmark.getTitle()).withIcon(FontAwesome.Icon.faw_file), 2);
        }
    }

    private void checkLoginCredentials()
    {
        SharedPreferences sharedPref = this.getSharedPreferences("gameSettings", Context.MODE_PRIVATE);
        username = sharedPref.getString(getString(R.string.saved_username), null);
        password = sharedPref.getString(getString(R.string.saved_password), null);

        if(password != null && username != null)
            Log.d(debugTag, "Valid credentials");
        else
            Log.d(debugTag, "Null credentials");


        if(username == null && password == null)
        {
            Intent myIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(myIntent, 1);
        }
    }

    private void setSwipeContainerListener()
    {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                Log.d(debugTag, "Refreshing");
                webView.reload();
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private void addWebViewClient()
    {
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                webView.setVisibility(View.GONE);
                swipeContainer.setRefreshing(true);
                bottomNavigation.disableItemAtPosition(0);
                bottomNavigation.disableItemAtPosition(1);
                bottomNavigation.disableItemAtPosition(2);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                Log.d(debugTag, "Finished loading url: " + url);

                if(!isLoggedIn && !loginAttempt)
                {
                    checkLogin();
                }
                else if(url.contains("vld_entidades_geral.entidade_pagina") || url.contains("vld_validacao"))
                {
                    super.onPageFinished(view, url);
                    return;
                }
                else if(firstLoad)
                {
                    injectCSS(homeCss);
                    checkCssLoad();
                }
                else
                {
                    injectCSS(contentCss);
                    checkCssLoad();
                }

                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.contains("//sigarra.up.pt/feup/pt/"))
                {
                    return false;
                }
                else
                {
                    startInBrowser(url);
                    return true;
                }
            }
        });
    }

    private void setWebViewScrollListener()
    {
        webView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback()
        {
            public void onScroll(int l, int t)
            {
                if (t > (previousT + 5) && !animating)
                {
                    bottomNavigation.hideBottomNavigation(true);
                    animating = true;
                    webView.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            animating = false;
                        }
                    }, 300);
                }
                else if (t < (previousT - 5) && !animating)
                {
                    bottomNavigation.restoreBottomNavigation(true);
                    animating = true;
                    webView.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            animating = false;
                        }
                    }, 300);
                }
                previousT = t;
            }
        });
    }

    private void initEncodedCss()
    {
        Log.d(debugTag, "Started reading css encoding");

        optionsCss = getEncodedString("rightBar.css");
        homeCss = getEncodedString("leftBar.css");
        contentCss = getEncodedString("content.css");

        Log.d(debugTag, "Finshed reading css encoding");
    }

    private void loginSigarra()
    {
        Log.d(debugTag, "Login attempt, has credentials: " + (password != null && username != null));
        if(password != null && username != null)
        {
            webView.loadUrl("javascript:(function(){" +
                    "var u = document.getElementById('user').value = '" + username + "';" +
                    "var p = document.getElementById('pass').value = '" + password + "';" +
                    "document.forms[0].submit();" +
                    "})()");
        }
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
        webView.loadUrl(
                "javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var style = document.createElement('style');" +
                "style.setAttribute('id', 'customcss');" +
                "style.type = 'text/css';" +
                // Tell the browser to BASE64-decode the string into your script !!!
                "style.innerHTML = window.atob('" + encoded + "');" +
                "parent.prepend(style)" +
                "})()");
    }

    private void checkCssLoad()
    {
        webView.loadUrl("javascript:(function() {" +
                "if(document.getElementById('customcss') != null)" +
                "{Android.cssLoaded();}" +
                "else" +
                "{Android.cssNotLoaded();}" +
                "})()");
    }

    private void checkLogin()
    {
        webView.loadUrl("javascript:(function(){" +
                "if(document.getElementsByClassName('nomelogin').length != 0)" +
                "{Android.isLoggedIn();}" +
                "else" +
                "{Android.isNotLoggedIn();}" +
                "})()");
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

    private void populateBottomBar()
    {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.home, R.drawable.ic_menu, R.color.colorPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.content, R.drawable.ic_web_asset, R.color.colorPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.options, R.drawable.ic_menu, R.color.colorPrimary);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.setAccentColor(Color.parseColor("#8c2d19"));

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener()
        {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected)
            {
                switch (position)
                {
                    case 0:
                        removeCSS();
                        injectCSS(homeCss);
                        break;
                    case 1:
                        removeCSS();
                        injectCSS(contentCss);
                        break;
                    case 2:
                        removeCSS();
                        injectCSS(optionsCss);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                username = data.getStringExtra("username");
                password = data.getStringExtra("password");
            }
            else if(resultCode == Activity.RESULT_CANCELED)
            {
                username = null;
                password = null;
            }
        }
    }

    public void stopLoading()
    {
        Log.d(debugTag, "Finished loading page");
        webView.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(firstLoad)
                {
                    bottomNavigation.setCurrentItem(0);
                    firstLoad = false;
                }
                else
                    bottomNavigation.setCurrentItem(1);

                webView.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                bottomNavigation.enableItemAtPosition(0);
                bottomNavigation.enableItemAtPosition(1);
                bottomNavigation.enableItemAtPosition(2);
            }
        }, 1000);
    }

    public void reload()
    {
        webView.reload();
    }

    public void isLoggedIn(Boolean lg)
    {
        if(lg)
        {
            Log.d(debugTag, "User is logged in");
            enableNavigation();
            isLoggedIn = true;
        }
        else
        {
            Log.d(debugTag, "User is not logged in");
            if(!loginAttempt && password != null && username != null)
            {
                loginSigarra();
                loginAttempt = true;
            }
        }
    }

    private void enableNavigation()
    {
        injectCSS(homeCss);
        checkCssLoad();
    }

    public class WebAppInterface
    {
        MainActivity activity;

        WebAppInterface(MainActivity a) {
            activity = a;
        }

        @JavascriptInterface
        public void cssLoaded()
        {
            activity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    activity.stopLoading();
                }
            });
        }

        @JavascriptInterface
        public void cssNotLoaded()
        {
            activity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    activity.reload();
                }
            });
        }

        @JavascriptInterface
        public void isLoggedIn()
        {
            activity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    activity.isLoggedIn(true);
                }
            });
        }

        @JavascriptInterface
        public void isNotLoggedIn()
        {
            activity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    activity.isLoggedIn(false);
                }
            });
        }
    }

    private void setWebViewDownloadListener()
    {
        webView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    downloadRequest = new DownloadRequest(url, userAgent, contentDisposition, mimeType);
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                else
                {
                    startDownload(new DownloadRequest(url, userAgent, contentDisposition, mimeType));
                }
            }
        });
    }

    private void startDownload(DownloadRequest downloadRequest)
    {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadRequest.getUrl()));

        request.setMimeType(downloadRequest.getMimeType());
        String cookies = CookieManager.getInstance().getCookie(downloadRequest.getUrl());
        request.addRequestHeader("cookie", cookies);
        request.addRequestHeader("User-Agent", downloadRequest.getUserAgent());
        request.setDescription(getString(R.string.downloading_description));
        request.setTitle(URLUtil.guessFileName(downloadRequest.getUrl(), downloadRequest.getContentDisposition(), downloadRequest.getMimeType()));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(downloadRequest.getUrl(), downloadRequest.getContentDisposition(), downloadRequest.getMimeType()));
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
        Toast.makeText(getApplicationContext(), getString(R.string.downloading_file), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        if(requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startDownload(downloadRequest);
            }
            else
            {
                startInBrowser(downloadRequest.getUrl());
            }
        }
    }
}

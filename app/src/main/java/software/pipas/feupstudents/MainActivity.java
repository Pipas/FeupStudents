package software.pipas.feupstudents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
{
    private String debugTag = "FEUPDEBUG";


    private AHBottomNavigation bottomNavigation;
    private ObservableWebView webView;
    private SwipeRefreshLayout swipeContainer;
    private Drawer navDrawer;
    private AccountHeader navDrawerHeader;

    private String optionsCss, homeCss, contentCss, username, password;
    private int previousT = 0;
    private Boolean isLoggedIn = false;
    private Boolean animating = false;
    private Boolean loginAttempt = false;

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

        addAWebViewClient();

        setWebViewScrollListener();

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

        navDrawerHeader = new AccountHeaderBuilder()
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
                        new PrimaryDrawerItem().withIdentifier(1).withName("Inicio").withIcon(FontAwesome.Icon.faw_home),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Adicionar").withIcon(FontAwesome.Icon.faw_plus).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.app_name),
                        new SecondaryDrawerItem().withIdentifier(3).withName("GitHub").withIcon(FontAwesome.Icon.faw_github).withSelectable(false),
                        new SecondaryDrawerItem().withIdentifier(4).withName("Contacto").withIcon(FontAwesome.Icon.faw_envelope).withSelectable(false)
                )
                .withSelectedItem(1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        if(drawerItem.getIdentifier() == 2)
                        {
                            navDrawer.addItemAtPosition(new PrimaryDrawerItem().withIdentifier(1).withName("Novo").withIcon(FontAwesome.Icon.faw_file), 2);
                        }
                        return false;
                    }
                })
                .build();

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
                webView.setVisibility(View.GONE);
                webView.reload();
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private void addAWebViewClient()
    {
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                webView.setVisibility(View.GONE);
                swipeContainer.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                Log.d(debugTag, "Finished loading url: " + url);

                if(!isLoggedIn)
                {
                    checkLogin();
                }
                else
                {
                    injectCSS(contentCss);
                    bottomNavigation.setCurrentItem(1);
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

        bottomNavigation.disableItemAtPosition(1);
        bottomNavigation.disableItemAtPosition(2);

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
                loginSigarra();
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
                webView.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
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
            injectCSS(homeCss);
            bottomNavigation.setCurrentItem(0);
            bottomNavigation.enableItemAtPosition(1);
            bottomNavigation.enableItemAtPosition(2);
            isLoggedIn = true;
            checkCssLoad();
        }
        else
        {
            Log.d(debugTag, "User is not logged in");
            if(!loginAttempt)
            {
                loginSigarra();
                loginAttempt = true;
            }
        }
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
}

package com.ntw_20.girlsfronttime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private WebView webview;
    private ShareActionProvider mShareActionProvider;
    private String JsonUrl = "https://www.ntw-20.com/common/apk/version.json";
    //get json task
    private GetVersion task = null;
    private AlertDialog.Builder dlgAlert = null;
    private String version = "";

    //update link
    private String link = "";


    public String parseBase64(String base64) {

        try {
            Pattern pattern = Pattern.compile("((?<=base64,).*\\s*)", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(base64);
            if (matcher.find()) {
                return matcher.group().toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }
    @Override
    protected void attachBaseContext(Context newBase) {

        Log.i("ntw-20.com","attachBaseContext");
        Language lang = new Language();

        super.attachBaseContext(lang.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        Notification notif = new Notification(this);
        notif.SetNotification();

        String lang = getResources().getConfiguration().locale.toString();

        switch (lang) {
            case "zh":
            case "zh_CN_#Hans":
                lang = "cn/";
                break;
            case "ja_JP":
            case "ja":
            case "ja_jp":
                lang = "ja/";
                break;
            default:
                lang = "";
        }


        Log.i("ntw-20.com",getResources().getConfiguration().locale.toString()   );

        GetVersion task = new GetVersion(this);
        task.execute(JsonUrl);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCachePath("/cache/");
        webSettings.setAppCacheEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("intent://")) {
                    try {
                        Context context = view.getContext();
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                        if (intent != null) {
                            view.stopLoading();

                            PackageManager packageManager = context.getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (info != null) {
                                context.startActivity(intent);
                            } else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                view.loadUrl(fallbackUrl);

                            }

                            return true;
                        }
                    } catch (URISyntaxException e) {
                    }
                } else if (!url.startsWith("https://www.ntw-20.com")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }

                return false;
            }
        });
        webview.clearCache(true);
        webview.setWebChromeClient(new WebChromeClient());

        webview.loadUrl("https://www.ntw-20.com/" + lang + "?fn=nbar");

        dlgAlert = new AlertDialog.Builder(this);
        PackageInfo pInfo = null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = pInfo.versionName;


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        webview.loadUrl("javascript:window.goBack()");
        return true;
    }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String call = "javascript:";
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_more_feedback) {
            call = "javascript:window.pushPush('/more/feedback')";
        } else if (id == R.id.action_more_about) {
            call = "javascript:window.pushPush('/more/about')";
        } else if (id == R.id.action_updateLog) {
            call = "javascript:window.pushPush('/log/update')";
        } else if (id == R.id.action_setting){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        webview.loadUrl(call);
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        String call = "javascript:";

        int id = item.getItemId();

        if (id == R.id.nav_time_girl) {
            call = "javascript:window.pushPush('/time/girl')";
        } else if (id == R.id.nav_time_device) {
            call = "javascript:window.pushPush('/time/device')";
        } else if (id == R.id.nav_time_fairy) {
            call = "javascript:window.pushPush('/time/fairy')";
        } else if (id == R.id.nav_time_make) {
            call = "javascript:window.pushPush('/make/girl')";
        } else if (id == R.id.nav_time_make_device) {
            call = "javascript:window.pushPush('/make/device')";
        } else if (id == R.id.nav_time_list) {
            call = "javascript:window.pushPush('/time/list')";
        } else if (id == R.id.nav_time_list_fairy) {
            call = "javascript:window.pushPush('/time/list_fairy')";
        } else if (id == R.id.nav_fb_list) {
            call = "javascript:window.pushPush('/fb/list')";
        } else if (id == R.id.nav_time_like_list) {
            call = "javascript:window.pushPush('/like/list')";
        } else if (id == R.id.nav_bot_line) {
            call = "javascript:window.pushPush('/bot/line')";
        } else if (id == R.id.nav_time_more_line) {
            call = "javascript:window.pushPush('/more/line')";
        } else if (id == R.id.nav_time_hMake) {
            call = "javascript:window.pushPush('/make/hGirl')";
        } else if (id == R.id.nav_time_make_HDevice) {
            call = "javascript:window.pushPush('/make/hDevice')";
        } else if (id == R.id.nav_index) {
            call = "javascript:window.pushPush('/?fn=nbar')";
        } else if (id == R.id.nav_doc_list) {
            call = "javascript:window.pushPush('/list/doc')";
        } else if (id == R.id.nav_event){
            call = "javascript:window.pushPush('/event/schedule')";
        } else if (id == R.id.nav_link){
            call = "javascript:window.pushPush('/more/link')";
        } else if (id == R.id.nav_list_wafer){
            call = "javascript:window.pushPush('/list/wafer')";
        } else if (id == R.id.nav_support_unit){
            call = "javascript:window.pushPush('/list/support_unit')";
        } else if (id == R.id.nav_shared) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            String shareBody = getResources().getString(R.string.app_name) + "APP: \n https://www.ntw-20.com/tool/android";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.schedule)));

        } else if (id == R.id.nav_version) {
            try {
                PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
                String version = pInfo.versionName;

                dlgAlert.setMessage(getResources().getString(R.string.version) + ":" + version + "\nGitHub: girls-frontline-toolset/android-app" );
                dlgAlert.setTitle(getResources().getString(R.string.information));
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setNegativeButton(null, null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        webview.loadUrl(call);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetVersion extends AsyncTask<String, Void, String> {
        private Context context;

        private GetVersion(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                //set http connection
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();


                // https  to save the data
                String line = "";
                String reply = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null)
                    reply += line;
                reader.close();

                return reply;

            } catch (Exception e) {
                return e.getMessage();
            }


        }

        protected void onPostExecute(String result) {
            try {
                //new json object
                JSONObject json = new JSONObject(result);

                //get the Question in json
                String newVersion = json.getString("version");
                String text = json.getString("text");
                link = "https://www.ntw-20.com/tool/android";

                if (!version.equals(newVersion)) {
                    dlgAlert.setMessage("版本:" + newVersion + "\n\n" + text);
                    dlgAlert.setTitle("已有新版本:");
                    dlgAlert.setNegativeButton("OK", null);
                    dlgAlert.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            webview.clearCache(true);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            startActivity(browserIntent);
                        }
                    });
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();

                }


            } catch (Exception e) {

            }
        }

    }

}





package com.ntw_20.girlsfronttime;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

class Language {

    Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SharedPreferences pref = context.getSharedPreferences("setting", context.MODE_PRIVATE);
            String lang = pref.getString("lang", "");

            if(lang.equals("")) return context;

            return updateResources(context, lang);
        } else {
            return context;
        }
    }



    private static Context updateResources(Context context, String language) {
        Resources resources = context.getResources();
        Locale locale = new Locale(language);
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

}

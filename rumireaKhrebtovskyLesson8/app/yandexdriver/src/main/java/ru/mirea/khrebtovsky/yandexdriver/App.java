package ru.mirea.khrebtovsky.yandexdriver;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class App extends Application {

    private static final String MAPKIT_API_KEY = "d253ae53-4358-44cd-b597-cbda0dc647a3";

    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
    }
}

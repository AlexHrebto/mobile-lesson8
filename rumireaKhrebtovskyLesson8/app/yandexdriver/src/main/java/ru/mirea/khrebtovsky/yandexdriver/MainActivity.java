package ru.mirea.khrebtovsky.yandexdriver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener {

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private FusedLocationProviderClient fusedLocationClient;
    private Point userLocation;

    private static final Point DESTINATION_POINT = new Point(55.845443, 37.362586);
    private static final int[] COLORS = {0xFFFF0000, 0xFF00FF00, 0x00FFBBBB, 0xFF0000FF};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeMapView();
        initializeDrivingRouter();
        getUserLocationAndSubmitRequest();
    }

    private void initializeMapView() {
        mapView = findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
    }

    private void initializeDrivingRouter() {
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
    }

    private void getUserLocationAndSubmitRequest() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = new Point(location.getLatitude(), location.getLongitude());
                            mapView.getMap().move(new CameraPosition(userLocation, 10, 0, 0));
                            submitRequest();
                        } else {
                            Toast.makeText(MainActivity.this, "Не удалось получить текущее местоположение", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        drivingOptions.setRoutesCount(1);
        VehicleOptions vehicleOptions = new VehicleOptions();

        List<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(userLocation, RequestPointType.WAYPOINT, null));
        requestPoints.add(new RequestPoint(DESTINATION_POINT, RequestPointType.WAYPOINT, null));

        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
        if (!routes.isEmpty()) {
            mapObjects.addPolyline(routes.get(0).getGeometry()).setStrokeColor(COLORS[0]);

            PlacemarkMapObject marker = mapObjects.addPlacemark(DESTINATION_POINT);
            marker.addTapListener((mapObject, point) -> {
                Toast.makeText(MainActivity.this, "Любимая точка спавна - Митино", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
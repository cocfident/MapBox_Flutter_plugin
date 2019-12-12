package com.mapbox.flutter_mapbox_plugin;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.looyo.flutter_mapbox_gray.R;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


/**
 * Created by jiazhen on 2019-12-10.
 * Desc:
 */
public class MapboxMapController implements PlatformView,
        OnMapReadyCallback,
        Application.ActivityLifecycleCallbacks,
        MapboxMap.OnMapClickListener,
        MethodChannel.MethodCallHandler, MapboxMapOptionsSink{
    private static final String TAG = MapboxMapController.class.getSimpleName();
    private Context context;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private boolean disposed = false;
    private final int registrarActivityHashCode;
    private final PluginRegistry.Registrar registrar;
    private SymbolManager symbolManager;
    private LineManager lineManager;

     static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
     static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
     static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
     static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
     static final String PROPERTY_SELECTED = "selected";
     static final String PROPERTY_NAME = "name";
     static final String PROPERTY_CAPITAL = "capital";

    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    private MethodChannel.Result mapReadyResult;
    private boolean myLocationEnabled;
    private int myLocationTrackingMode;
    private LocationComponent locationComponent;


    MapboxMapController(
            int id,
            Context context,
            MapboxMapOptions options,
            PluginRegistry.Registrar registrar) {
        this.context = context;
        Mapbox.getInstance(context, getAccessToken(context));
        mapView = new MapView(context,options);
        this.registrar = registrar;
        this.registrarActivityHashCode = registrar.activity().hashCode();
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugins.flutter.io/mapbox_maps_"+id);
        channel.setMethodCallHandler(this);
    }

    private static String getAccessToken(@NonNull Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString("com.mapbox.token");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        return null;
    }

    void init(){
        registrar.activity().getApplication().registerActivityLifecycleCallbacks(this);
        mapView.getMapAsync(this);
    }

    @Override
    public View getView() {
        return mapView;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        mapView.onDestroy();
        registrar.activity().getApplication().unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if (mapReadyResult != null) {
            mapReadyResult.success(null);
            mapReadyResult = null;
        }

        mapboxMap.setStyle(Style.MAPBOX_STREETS, onStyleLoaded);
    }

    private Style.OnStyleLoaded onStyleLoaded = style -> {
        enableLocationComponent(style);
        mapboxMap.addOnMapClickListener(MapboxMapController.this);
    };



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(Style style) {
        if (hasLocationPermission()){
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build());
            locationComponent.setLocationComponentEnabled(myLocationEnabled);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            updateMyLocationTrackingMode();
            setMyLocationTrackingMode(this.myLocationTrackingMode);
        }else {
            Log.e(TAG, "missing location permissions");
        }


    }

    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        /* show image with id title based on the value of the name feature property */
                        iconImage("{name}"),
                        /* set anchor of icon to bottom-left */
                        iconAnchor(ICON_ANCHOR_BOTTOM),
                        /* all info window and marker image to appear at the same time*/
                        iconAllowOverlap(true),
                        /* offset the info window to be above the marker */
                        iconOffset(new Float[] {-2f, -28f})
                )
                /* add a filter to show only when selected feature property is true */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
    }

    private void setUpData(final FeatureCollection collection) {
        featureCollection = collection;
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                setupSource(style);
                setUpImage(style);
                setUpMarkerLayer(style);
                setUpInfoWindowLayer(style);
            });
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private void setupSource(@NonNull Style loadedStyle) {
        source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
        loadedStyle.addSource(source);
    }

    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private void setUpImage(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                context.getResources(), R.mipmap.red_marker));
    }

    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        iconImage(MARKER_IMAGE_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[] {0f, -8f})
                ));
    }

    /**
     * Updates the display of data on the map after the FeatureCollectionBean has been modified
     */
    void refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                // calling addImages is faster as separate addImage calls for each bitmap.
                style.addImages(imageMap);
            });
        }
    }



    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }

    private boolean handleClickIcon(PointF pointF) {

        List<Feature> features = mapboxMap.queryRenderedFeatures(pointF, MARKER_LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        if (featureSelectStatus(i)) {
                            setFeatureSelectState(featureList.get(i), false);
                        } else {
                            setSelected(i);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private void setSelected(int index) {
        if (featureCollection.features() != null) {
            Feature feature = featureCollection.features().get(index);
            setFeatureSelectState(feature, true);
            refreshSource();
        }
    }

    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource();
        }
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollectionBean's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onStart();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onPause();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onStop();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (disposed || activity.hashCode() != registrarActivityHashCode) {
            return;
        }
        mapView.onDestroy();
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method){
            case "map#waitForMap":
                if (mapboxMap != null) {
                    result.success(null);
                    return;
                }
                mapReadyResult = result;
                break;
            case "map#update": {
                //Convert.interpretMapboxMapOptions(call.argument("options"), this);
                result.success(Convert.toJson(mapboxMap.getCameraPosition()));
                break;
            }
            case "symbol#addList": {
                Object o = call.argument("optionsList");
                List<?> objects = Convert.toList(o);
                for (Object object : objects) {
                    Log.e(TAG, "onMethodCall: "+object.toString());
                }
                //Convert.interpretSymbolOption(call.argument("optionsList"),null);
                //new LoadGeoJsonDataTask(MapboxMapController.this).execute();
                //Log.e(TAG, "onMethodCall: symbol#add");
                //final SymbolBuilder symbolBuilder = newSymbolBuilder();
//                Convert.interpretSymbolOptions(call.argument("options"), symbolBuilder);
//                final Symbol symbol = symbolBuilder.build();
//                final String symbolId = String.valueOf(symbol.getId());
//                symbols.put(symbolId, new SymbolController(symbol, true, this));
//                result.success(symbolId);
                break;
            }
            case "symbol#add": {
                Convert.interpretSymbolOption(call.argument("options"),null);
                //new LoadGeoJsonDataTask(MapboxMapController.this).execute();
                //Log.e(TAG, "onMethodCall: symbol#add");
                //final SymbolBuilder symbolBuilder = newSymbolBuilder();
//                Convert.interpretSymbolOptions(call.argument("options"), symbolBuilder);
//                final Symbol symbol = symbolBuilder.build();
//                final String symbolId = String.valueOf(symbol.getId());
//                symbols.put(symbolId, new SymbolController(symbol, true, this));
//                result.success(symbolId);
                break;
            }
            case "line#add": {
                Log.e(TAG, "onMethodCall: line#add");
                Object o = call.argument("options");
                Log.e(TAG, "onMethodCall: "+o.toString() );
                result.success(null);
                break;
            }
            default:
                result.notImplemented();
        }
    }

    @Override
    public void setStyleString(String styleString) {
        //check if json, url or plain string:
        if (styleString == null || styleString.isEmpty()) {
            Log.e(TAG, "setStyleString - string empty or null");
        } else if (styleString.startsWith("{") || styleString.startsWith("[")) {
            mapboxMap.setStyle(new Style.Builder().fromJson(styleString), onStyleLoaded);
        } else {
            mapboxMap.setStyle(new Style.Builder().fromUrl(styleString), onStyleLoaded);
        }
    }


    @Override
    public void setMyLocationEnabled(boolean myLocationEnabled) {
        if (this.myLocationEnabled == myLocationEnabled) {
            return;
        }
        this.myLocationEnabled = myLocationEnabled;
        if (mapboxMap != null) {
            updateMyLocationEnabled();
        }
    }

    @Override
    public void setMyLocationTrackingMode(int myLocationTrackingMode) {
        if (this.myLocationTrackingMode == myLocationTrackingMode) {
            return;
        }
        this.myLocationTrackingMode = myLocationTrackingMode;
        if (mapboxMap != null && locationComponent != null) {
            updateMyLocationTrackingMode();
        }
    }

    private void updateMyLocationEnabled() {
        //TODO: call location initialization if changed to true and not initialized yet.;
        //Show/Hide use location as needed
    }

    private void updateMyLocationTrackingMode() {
        int[] mapboxTrackingModes = new int[] {CameraMode.NONE, CameraMode.TRACKING, CameraMode.TRACKING_COMPASS, CameraMode.TRACKING_GPS};
        locationComponent.setCameraMode(mapboxTrackingModes[this.myLocationTrackingMode]);
    }

    /**
     * AsyncTask to load data from the assets folder.
     */
    private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<MapboxMapController> activityRef;

        LoadGeoJsonDataTask(MapboxMapController activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            MapboxMapController activity = activityRef.get();

            if (activity == null) {
                return null;
            }


            //String geoJson = loadGeoJsonFromAsset(activity.context, "us_west_coast.geojson");
            String geoJson = FeatureCollectionBean.symbolInfoToJson(Point.fromLngLat(39.899782,116.393386));
            return FeatureCollection.fromJson(geoJson);
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            MapboxMapController activity = activityRef.get();
            if (featureCollection == null || activity == null) {
                return;
            }

            // This example runs on the premise that each GeoJSON Feature has a "selected" property,
            // with a boolean value. If your data's Features don't have this boolean property,
            // add it to the FeatureCollectionBean 's features with the following code:
            for (Feature singleFeature : featureCollection.features()) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
            }

            activity.setUpData(featureCollection);
            new GenerateViewIconTask(activity).execute(featureCollection);
        }


    }

    Context getContext() {
        return context;
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private int checkSelfPermission(String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        return context.checkPermission(
                permission, android.os.Process.myPid(), android.os.Process.myUid());
    }
}

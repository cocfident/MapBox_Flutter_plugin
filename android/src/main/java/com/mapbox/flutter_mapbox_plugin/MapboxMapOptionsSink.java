package com.mapbox.flutter_mapbox_plugin;

import com.mapbox.mapboxsdk.geometry.LatLngBounds;

/**
 * Receiver of MapboxMap configuration options.
 */
interface MapboxMapOptionsSink {

  void setStyleString(String styleString);

  void setMyLocationEnabled(boolean myLocationEnabled);

  void setMyLocationTrackingMode(int myLocationTrackingMode);
}
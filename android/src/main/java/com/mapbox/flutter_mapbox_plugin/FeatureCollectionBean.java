package com.mapbox.flutter_mapbox_plugin;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiazhen on 2019-12-11.
 * Desc:
 */
public class FeatureCollectionBean {


    /**
     * type : FeatureCollectionBean
     * features : [{"type":"Feature","properties":{"marker-color":"#7e7e7e","marker-size":"medium","marker-symbol":"","name":"Washington","capital":"Olympia"},"geometry":{"type":"Point","coordinates":[-122.9048,47.03676]}},{"type":"Feature","properties":{"marker-color":"#7e7e7e","marker-size":"medium","marker-symbol":"","name":"Oregon","capital":"Salem"},"geometry":{"type":"Point","coordinates":[-123.03048,44.93847]}},{"type":"Feature","properties":{"marker-color":"#7e7e7e","marker-size":"medium","marker-symbol":"","name":"California","capital":"Sacramento"},"geometry":{"type":"Point","coordinates":[-121.493779,38.576641]}},{"type":"Feature","properties":{"marker-color":"#7e7e7e","marker-size":"medium","marker-symbol":"","name":"Idaho","capital":"Boise"},"geometry":{"type":"Point","coordinates":[-116.199,43.61777]}}]
     */

    private String type = "FeatureCollection";
    private List<FeaturesBean> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FeaturesBean> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeaturesBean> features) {
        this.features = features;
    }

    public static class FeaturesBean {
        /**
         * type : Feature
         * properties : {"marker-color":"#7e7e7e","marker-size":"medium","marker-symbol":"","name":"Washington","capital":"Olympia"}
         * geometry : {"type":"Point","coordinates":[-122.9048,47.03676]}
         */

        private String type = "Feature";
        private PropertiesBean properties;
        private GeometryBean geometry;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public PropertiesBean getProperties() {
            return properties;
        }

        public void setProperties(PropertiesBean properties) {
            this.properties = properties;
        }

        public GeometryBean getGeometry() {
            return geometry;
        }

        public void setGeometry(GeometryBean geometry) {
            this.geometry = geometry;
        }

        public static class PropertiesBean {
            /**
             * marker-color : #7e7e7e
             * marker-size : medium
             * marker-symbol :
             * name : Washington
             * capital : Olympia
             */

            @SerializedName("marker-color")
            private String markercolor;
            @SerializedName("marker-size")
            private String markersize;
            @SerializedName("marker-symbol")
            private String markersymbol;
            private String name;
            private String capital;

            public String getMarkercolor() {
                return markercolor;
            }

            public void setMarkercolor(String markercolor) {
                this.markercolor = markercolor;
            }

            public String getMarkersize() {
                return markersize;
            }

            public void setMarkersize(String markersize) {
                this.markersize = markersize;
            }

            public String getMarkersymbol() {
                return markersymbol;
            }

            public void setMarkersymbol(String markersymbol) {
                this.markersymbol = markersymbol;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getCapital() {
                return capital;
            }

            public void setCapital(String capital) {
                this.capital = capital;
            }
        }

        public static class GeometryBean {
            /**
             * type : Point
             * coordinates : [-122.9048,47.03676]
             */

            private String type = "Point";
            private List<Double> coordinates;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<Double> getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(List<Double> coordinates) {
                this.coordinates = coordinates;
            }
        }
    }


   static String symbolInfoToJson(Point point){
        FeatureCollectionBean featureCollectionBean = new FeatureCollectionBean();
        FeatureCollectionBean.FeaturesBean featuresBean = new FeatureCollectionBean.FeaturesBean();

        FeatureCollectionBean.FeaturesBean.GeometryBean geometryBean = new FeatureCollectionBean.FeaturesBean.GeometryBean();
        ArrayList<Double> coordinates = new ArrayList<>();
        //39.939299  116.395628  -116.199,
        //          43.61777
        coordinates.add(point.longitude());
        coordinates.add(point.latitude());
        geometryBean.setCoordinates(coordinates);

        FeatureCollectionBean.FeaturesBean.PropertiesBean propertiesBean = new FeatureCollectionBean.FeaturesBean.PropertiesBean();
        propertiesBean.setCapital("Olympia");
        propertiesBean.setMarkercolor("#7e7e7e");
        propertiesBean.setMarkersize("medium");
        propertiesBean.setMarkersymbol("");
        propertiesBean.setName("Washington");

        featuresBean.setGeometry(geometryBean);
        featuresBean.setProperties(propertiesBean);

        ArrayList<FeatureCollectionBean.FeaturesBean> featuresBeans = new ArrayList<>();
        featuresBeans.add(featuresBean);
        featureCollectionBean.setFeatures(featuresBeans);
        Gson gson = new Gson();
        return gson.toJson(featureCollectionBean);
    }

}

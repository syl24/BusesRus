package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {

        updateVisibleArea();
        busRouteOverlays.clear();
        busRouteLegendOverlay.clear();
        Stop routeStops = StopManager.getInstance().getSelected();

        if (routeStops != null){
            for (Route routes : routeStops.getRoutes()){
                busRouteLegendOverlay.add(routes.getNumber());
                for (RoutePattern patterns : routes.getPatterns()){
                    List<GeoPoint> geoPoints= new ArrayList<>();
                    for (int i = 0; i < (patterns.getPath().size()-1); i++){
                        GeoPoint geoStart = new GeoPoint(patterns.getPath().get(i).getLatitude(), patterns.getPath().get(i).getLongitude());
                        GeoPoint geoEnd = new GeoPoint(patterns.getPath().get(i + 1).getLatitude(), patterns.getPath().get(i + 1).getLongitude());
                        if (Geometry.rectangleIntersectsLine(northWest,southEast, patterns.getPath().get(i), patterns.getPath().get(i + 1))){
                            geoPoints.add(geoStart);
                            geoPoints.add(geoEnd);

                            Polyline poly = new Polyline(BusesAreUs.activity);
                            poly.setColor(busRouteLegendOverlay.getColor(routes.getNumber()));
                            poly.setWidth(getLineWidth(zoomLevel));
                            poly.setPoints(geoPoints);
                            busRouteOverlays.add(poly);
                        }

                        if (!(Geometry.rectangleContainsPoint(northWest, southEast, patterns.getPath().get(i +1)))){
                            geoPoints = new ArrayList<>();
                        }
                    }
                }

            }
        }


    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}

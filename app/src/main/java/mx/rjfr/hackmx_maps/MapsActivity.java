package mx.rjfr.hackmx_maps;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.rjfr.hackmx_maps.Remote.IGoogleApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private List<LatLng> polyLineList;
    private Marker marker;
    private float v;
    private int index, next;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, myLocation;
    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyLine, greyPolyLine;
    IGoogleApi mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

         polyLineList = new ArrayList<>();
         btnGo = (Button)findViewById(R.id.btnSearch);
         edtPlace= (EditText) findViewById(R.id.edtPlace);
         btnGo.setOnClickListener(new View.OnClickListener(){
            @Override
             public void onClick(View view) {
                destination = edtPlace.getText().toString();
                destination = destination.replace(" ","+");
                destination = destination.replace("\n","+");
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });

         mService = Common.getGoogleApi();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        // Add a marker in Sydney and move the camera
        final LatLng TecCem = new LatLng(19.593944, -99.227694);
        mMap.addMarker(new MarkerOptions().position(TecCem).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TecCem));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder().target(googleMap.getCameraPosition()
                        .target).zoom(17).bearing(30).tilt(45).build()));
        String requestUrl = null;
        try {
            System.out.println("destination");
            System.out.println(destination);
            System.out.println("fin destination");
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+ "origin="+TecCem.latitude+","+TecCem.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_directions_key);
            Log.d("URL", requestUrl);
            mService.getDataFromGoogleApi(requestUrl)
            .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i<jsonArray.length();i++){
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);

                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for(LatLng latLng:polyLineList) builder.include(latLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mcameraUpdate = CameraUpdateFactory.newLatLngBounds(
                                bounds, 2);
                        mMap.animateCamera(mcameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyLine = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.GRAY);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.endCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polyLineList);
                        blackPolyLine = mMap.addPolyline(blackPolylineOptions);

                        mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size()-1)));

                        //Animator
                        final ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                        polyLineAnimator.setDuration( 2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                List<LatLng> points = greyPolyLine.getPoints();
                                int percentValue =  (int)valueAnimator.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size*(percentValue/100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyLine.setPoints(p);
                            }
                        });

                        polyLineAnimator.start();
                        //Add car marker
                        marker = mMap.addMarker(new MarkerOptions().position(TecCem).flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                        //Car Moving
                        handler = new Handler();
                         index = -1;
                         next = 1;
                         handler.postDelayed(new Runnable() {
                             @Override
                             public void run() {
                                 if (index <polyLineList.size()-1){
                                     //aqui
                                     index ++;
                                     next = index+1;
                                 }if( index<polyLineList.size()-1) {

                                     startPosition = polyLineList.get(index);
                                     endPosition = polyLineList.get(next);
                                 }

                                 ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
                                 valueAnimator.setDuration( 3000);
                                 valueAnimator.setInterpolator(new LinearInterpolator());
                                 valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                     @Override
                                     public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                         v = valueAnimator.getAnimatedFraction();
                                         lng  = v*endPosition.longitude+(1-v)
                                                 *startPosition.longitude;
                                         lat = v*endPosition.latitude+(1-v)
                                                 *startPosition.latitude;

                                         if (index== polyLineList.size()-polyLineList.size()/2){
                                             System.out.println("Desea un nuevo lugar?");
                                             //si, pop up
                                         }


                                         LatLng newPos = new LatLng(lat, lng);
                                         marker.setPosition(newPos);
                                         marker.setAnchor(0.5f, 0.5f);
                                         marker.setRotation(getBearing(startPosition,newPos));
                                         mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                                 new CameraPosition.Builder().target(newPos)
                                                 .zoom(15.5f).build()));
                                     }
                                 });

                                 valueAnimator.start();
                                 handler.postDelayed(this, 3000);

                             }
                         }, 3000);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(MapsActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private float getBearing(LatLng startPosition, LatLng newPos) {
        double lat = Math.abs(startPosition.latitude - newPos.latitude);
        double lng = Math.abs(startPosition.longitude - newPos.longitude);

        if (startPosition.latitude < newPos.latitude && startPosition.longitude < newPos.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude >= newPos.latitude && startPosition.longitude < newPos.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.latitude >= newPos.latitude && startPosition.longitude >= newPos.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (startPosition.latitude < newPos.latitude && startPosition.longitude >= newPos.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

    public List<LatLng> decodePoly(String encoded) {
// encoded is overview_polyline.points;
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}

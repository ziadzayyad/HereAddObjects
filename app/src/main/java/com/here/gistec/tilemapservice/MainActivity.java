package com.here.gistec.tilemapservice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.Vector3f;
import com.here.android.mpa.mapping.GeoMesh;
import com.here.android.mpa.mapping.LocalMesh;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGeoModel;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapLocalModel;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapModelObject;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapOverlay;
import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapScreenMarker;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private Map map = null;
    private MapFragment mapFragment = null;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private LiveMapRasterTileSource mTileSource = null;

    private ImageView mImageView;
    private MapOverlay mapOverlay;
    private Bitmap bitmapA;
    private byte[] imgByteArray;
    private static final String TAG = "tilemapService RUN";
    private LiveMapRasterTileSource tileSource;

    private MapObject mapObject;

    private MapLocalModel mapLocalModel;

    private Image myImage ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    private void initialize() {
        setContentView(R.layout.activity_main);




        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {


                        map = mapFragment.getMap();

                    map.setCenter(new GeoCoordinate(24.933552, 55.242723, 0.0),
                            Map.Animation.NONE);
                    // Set the map zoom level to close to street level
                    map.setZoomLevel(11);

                    // Listen for gesture events. For example tapping on buildings
                    mapFragment.getMapGesture().addOnGestureListener(gestureListener);

                  /*  tileSource = new LiveMapRasterTileSource();

                    map.addRasterTileSource(tileSource);
*/


                    // retrieve a reference of the map from the map fragment
              //      map = mapFragment.getMap();
                   // setUpMap();
                    //loadImageFromURL();
                    loadLocalImage();
                    addRandomObject();

                  //  map.setCenter(new GeoCoordinate(24.839770, 55.174134, 0.0),
                       //     Map.Animation.BOW);

                    // Set the zoom level to the average between min and max
                  /*  map.setZoomLevel(
                            (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);


                    map.setCenter(new GeoCoordinate(24.839770, 55.174134, 0.0),
                            Map.Animation.BOW);*/

                   // Toast.makeText(MainActivity.this, "geo SET" + map.addRasterTileSource(tileSource), Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                    Toast.makeText(MainActivity.this, "err=" + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }


    private void setUpMap() {
        if (map != null) {
            // we do not have option on not having tiles as scheme with HERE
            //mMap.setMapType(GoogleMap.MAP_TYPE_NONE);

            mTileSource = new LiveMapRasterTileSource();

            map.addRasterTileSource(mTileSource);
           // Toast.makeText(MainActivity.this, "getBoundingArea = " + mTileSource.getBoundingArea().toString(), Toast.LENGTH_LONG).show();
        }
    }




    private void loadImageFromURL()
    {

        String url = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Map_de_berlin_1789_%28georeferenced%29.jpg/720px-Map_de_berlin_1789_%28georeferenced%29.jpg";
        //mImageView = (ImageView) findViewById(R.id.myImage);
        mImageView = new ImageView(MainActivity.this);
       // mImageView = (ImageView)findViewById(R.id.imgV);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
// Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                        bitmapA = bitmap;

                        mImageView.setImageResource(R.drawable.ags);
                        Toast.makeText(MainActivity.this, "image SET", Toast.LENGTH_LONG).show();
                      //  onImageLoaded();
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        mImageView.setImageResource(R.drawable.ags);
                        Toast.makeText(MainActivity.this, "image NOT SET", Toast.LENGTH_LONG).show();
                      //  onImageLoaded();
                    }
                });

        requestQueue.add(request);
    }


    private void loadLocalImage()
    {
        bitmapA = BitmapFactory.decodeResource(getResources(), R.drawable.ags);
    }

    private void onImageLoaded()
    {
   /*     mapOverlay = new MapOverlay(mImageView,new GeoCoordinate(25.155216, 55.338758));
        mapOverlay.getView().setAlpha(0.5f);
        map.addMapOverlay(mapOverlay);*/

     //   MapRasterTileSource.TileResult.Error error = new MapRasterTileSource.TileResult.Error();
      //  MapRasterTileSource.TileResult tileResult = new MapRasterTileSource.TileResult( MapRasterTileSource.TileResult.Error,imgByteArray);
    }

    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener.OnGestureListenerAdapter() {

        @Override
        public void onMultiFingerManipulationEnd() {

            double mapZoomLevel = map.getZoomLevel();

/*
            if(mapZoomLevel>10)
            {
                mapLocalModel.setVisible(true);
            }
            else
            {
                mapLocalModel.setVisible(false);
            }*/
           // Toast.makeText(MainActivity.this, "ZoomLevel = " + mapZoomLevel, Toast.LENGTH_LONG).show();

            Toast.makeText(MainActivity.this,mapZoomLevel + "      Zoom MAX=" + map.getMaxZoomLevel() + "   MIN=" + map.getMinZoomLevel(), Toast.LENGTH_LONG).show();

           // mImageView.setScaleType(ImageView.ScaleType.MATRIX);
         /*   mImageView.setScaleX((float)(mapZoomLevel * 0.1));
            mImageView.setScaleY((float)(mapZoomLevel * 0.1));*/

            super.onMultiFingerManipulationEnd();
        }

    };

    private void addRandomObject ()
    {
        myImage = new Image();
        myImage.setBitmap(bitmapA);
        //addMapCircle();
        //addMapPolygon();
       // addPolyline();
      //  addMapMarker();
        //addScreenMarker();
        addMapLocalModel();
       // addMapGeoModel();

    }

    private void addMapGeoModel()
    {
                List<GeoCoordinate> myLocations = Arrays.asList(
                new GeoCoordinate(37.783409, -122.439473),
              //  new GeoCoordinate(37.785444, -122.424667),
                new GeoCoordinate(37.774149, -122.429345),
              //  new GeoCoordinate(37.774140, -122.437649),
                new GeoCoordinate(37.771562, -122.432628));
                IntBuffer vertIndicieBuffer = IntBuffer.allocate(3);
                vertIndicieBuffer.put(0);
                vertIndicieBuffer.put(2);
                vertIndicieBuffer.put(1);
                FloatBuffer textCoordBuffer = FloatBuffer.allocate(6);
                textCoordBuffer.put(0.5f);
                textCoordBuffer.put(0.5f);
                textCoordBuffer.put(0.5f);
                textCoordBuffer.put(0.5f);
                textCoordBuffer.put(0.5f);
                textCoordBuffer.put(0.5f);
                GeoMesh meshy = new GeoMesh();
                meshy.setVertices(myLocations);
                meshy.setVertexIndices(vertIndicieBuffer);
                meshy.setTextureCoordinates(textCoordBuffer);
                MapGeoModel myGeoModel = new MapGeoModel();
                myGeoModel.setMesh(meshy);
                myGeoModel.setTexture(myImage);
                map.addMapObject(myGeoModel);
                map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }

    private void addMapCircle()
    {
        MapCircle mapCircle = new MapCircle(500.0, new GeoCoordinate(37.783409, -122.439473));
        mapCircle.setOverlayType(MapOverlayType.AREA_OVERLAY);
        map.addMapObject(mapCircle);
        map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }



    private void addMapPolygon()
    {
        List<GeoCoordinate> myGeos = Arrays.asList(
                new GeoCoordinate(37.783409, -122.439473),
                new GeoCoordinate(37.785444, -122.424667),
                new GeoCoordinate(37.774149, -122.429345),
               // new GeoCoordinate(37.774140, -122.437649),
                new GeoCoordinate(37.771562, -122.432628));

        GeoPolygon geoPolygon = new GeoPolygon();
        MapPolygon mapPolygon = new MapPolygon();
        geoPolygon.add(myGeos);
     /*   geoPolygon.insert(new GeoCoordinate(37.774140, -122.437649),0);
        geoPolygon.insert(new GeoCoordinate(37.774149, -122.429345),1);
        geoPolygon.insert(new GeoCoordinate(37.771562, -122.432628),2);
       // geoPolygon.insert(new GeoCoordinate(37.785444, -122.424667),3);
        geoPolygon.insert(new GeoCoordinate(37.783409, -122.439473),4);
*/
        mapPolygon.setGeoPolygon(geoPolygon);

        map.addMapObject(mapPolygon);
        map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }

    private void addPolyline()
    {
        List<GeoCoordinate> myGeos = Arrays.asList(
                new GeoCoordinate(37.783409, -122.439473),
                new GeoCoordinate(37.785444, -122.424667),
                new GeoCoordinate(37.774149, -122.429345),
                new GeoCoordinate(37.774140, -122.437649),
                new GeoCoordinate(37.771562, -122.432628));
        GeoPolyline geoPolyline = new GeoPolyline(myGeos);
        MapPolyline mapPolyline = new MapPolyline(geoPolyline);
        mapPolyline.setLineWidth(5);
        map.addMapObject(mapPolyline);
        map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }

    private void addMapMarker()
    {
        MapMarker mapMarker = new MapMarker(new GeoCoordinate(37.783409, -122.439473),myImage);
        map.addMapObject(mapMarker);
        map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }

    private void addScreenMarker()
    {
        PointF pointF = new PointF((float)(37.783409),(float)(-122.439473));
        MapScreenMarker mapScreenMarker = new MapScreenMarker(pointF,myImage);
        map.addMapObject(mapScreenMarker);
        map.setCenter( new GeoCoordinate(37.783409, -122.439473), Map.Animation.BOW);
    }


    private void addMapLocalModel()
    {
        int delta = 1;
        FloatBuffer buff = FloatBuffer.allocate(12); // Two triangles
        buff.put(0- delta);
        buff.put(0- delta);
        buff.put(1.f);
        buff.put(0 + delta);
        buff.put(0 - delta);
        buff.put(1.f);
        buff.put(0 - delta);
        buff.put(0 + delta);
        buff.put(1.f);
        buff.put(0 + delta);
        buff.put(0 + delta);
        buff.put(1.f);

// Two triangles to generate the rectangle. Both front and back face
        IntBuffer vertIndicieBuffer = IntBuffer.allocate(12);
        vertIndicieBuffer.put(0);
        vertIndicieBuffer.put(2);
        vertIndicieBuffer.put(1);
        vertIndicieBuffer.put(2);
        vertIndicieBuffer.put(3);
        vertIndicieBuffer.put(1);
        vertIndicieBuffer.put(0);
        vertIndicieBuffer.put(1);
        vertIndicieBuffer.put(2);
        vertIndicieBuffer.put(1);
        vertIndicieBuffer.put(3);
        vertIndicieBuffer.put(2);

// Texture coordinates
        FloatBuffer textCoordBuffer = FloatBuffer.allocate(8);
        textCoordBuffer.put(0.f);
        textCoordBuffer.put(0.f);
        textCoordBuffer.put(1.f);
        textCoordBuffer.put(0.f);
        textCoordBuffer.put(0.f);
        textCoordBuffer.put(1.f);
        textCoordBuffer.put(1.f);
        textCoordBuffer.put(1.f);

        LocalMesh myMesh = new LocalMesh();
        myMesh.setVertices(buff);
        myMesh.setVertexIndices(vertIndicieBuffer);
        myMesh.setTextureCoordinates(textCoordBuffer);

        MapLocalModel myObject = new MapLocalModel();
        myObject.setMesh(myMesh); //a LocalMesh object
        myObject.setTexture(myImage); //an Image object
        myObject.setAnchor(new GeoCoordinate(25.207118, 55.306331)); //a GeoCoordinate object

        myObject.setScale(5.0f);
        myObject.setDynamicScalingEnabled(true);
        myObject.setYaw(45.0f);
        map.addMapObject(myObject);
        map.setCenter( new GeoCoordinate(25.207118, 55.306331), Map.Animation.BOW);

        // This light shines from above in the Z axis
        MapModelObject.DirectionalLight light = new MapModelObject.DirectionalLight(new Vector3f(0, 0.5f, 0));
        myObject.addLight(light);

// Give this a default color
        MapModelObject.PhongMaterial mat = new MapModelObject.PhongMaterial();
        mat.setAmbientColor(0xffffffff);
        mat.setDiffuseColor(0x00000000);
        myObject.setMaterial(mat);
    }







}


package com.example.kessi.guiadosantuariocaninde;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GeoQueryEventListener{

    private static final GeoLocation INITIAL_CENTER = new GeoLocation(-4.35802919, -39.31373835);
    private static final int INITIAL_ZOOM_LEVEL = 15;
    private static final String GEO_FIRE_DB = "https://guiasantuariocaninde.firebaseio.com";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/geofire";
    public static String title;
    public static String content;
    public static int directory;
    public static int childImage;
    DataSnapshot dataSnapshotTeste;
    //Firebase database
    private FirebaseDatabase database;
    private DatabaseReference refMenu;
    private DatabaseReference refChild;
    //Storage
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference imageRef;
    private ImageView mImageView;
    //GeoFire
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private DatabaseReference refGeoFire;
    private Map<String,Marker> markers;
    //Interface map
    private android.support.v4.app.FragmentManager mFragmentManager;
    private SupportMapFragment mMapFragment;
    private GoogleMap map;
    private Circle searchCircle;

    //Expand List Adapter
    private ExpandListAdapter expandListAdapter;
    private ArrayList<Group> groupArrayList;
    private ExpandableListView expandableListView;

    //Menu Drawer
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        mMapFragment = SupportMapFragment.newInstance();

        //Firebase database
        database = FirebaseDatabase.getInstance();
        refMenu = database.getReference().child("menu");

        //FirebaseOptions options = new FirebaseOptions.Builder().setApplicationId("geofire").setDatabaseUrl(GEO_FIRE_DB).build();
        //FirebaseApp app = FirebaseApp.initializeApp(this, options);
        // setup GeoFire
        //this.geoFire = new GeoFire(FirebaseDatabase.getInstance(app).getReferenceFromUrl(GEO_FIRE_REF));

        DatabaseReference refGeoFire = FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF);
        geoFire = new GeoFire(refGeoFire);

        // raio em 1 km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);
        // Comfigurando marcadores
        this.markers = new HashMap<String, Marker>();


        //Barra de ferramenta
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Next Page", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Menu Drawer Navigation
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        mMapFragment.getMapAsync(this);
        mFragmentManager.beginTransaction().add(R.id.map, mMapFragment).commit();

        //Adiciona um ouvinte para atualizar os locais novamente
        this.geoQuery.addGeoQueryEventListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        String [] data2 ={"Kirit", "Miral", "Bhushan", "Jiten", "Ajay", "Kamlesh"};

        final ListView mListView1 = (ListView)findViewById(R.id.listView1);
        final ListView mListView2 = (ListView)findViewById(R.id.listView2);
        final ListView mListView3 = (ListView)findViewById(R.id.listView3);
        final ListView mListView4 = (ListView)findViewById(R.id.listView4);
        final ListView mListView5 = (ListView)findViewById(R.id.listView5);

        final ArrayList<String> arrayListIgrejas = new ArrayList<>();
        final ArrayList<String> arrayListPontoReligioso = new ArrayList<>();

         ListUtils.setDynamicHeight(mListView1);
        ListUtils.setDynamicHeight(mListView2);
        ListUtils.setDynamicHeight(mListView3);
        ListUtils.setDynamicHeight(mListView4);
        ListUtils.setDynamicHeight(mListView5);


        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data2);
        final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data2);
        final ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data1);
        final ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data2);
        final ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data1);

        mListView1.setAdapter(adapter1);
        mListView2.setAdapter(adapter2);
        mListView3.setAdapter(adapter3);
        mListView4.setAdapter(adapter4);
        mListView5.setAdapter(adapter5);


        mListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

           */

        expandableListView = (ExpandableListView) findViewById(R.id.exp_list);
        groupArrayList = new ArrayList<Group>();
        expandListAdapter = new ExpandListAdapter(MainActivity.this, groupArrayList);
        expandableListView.setAdapter(expandListAdapter);

        refMenu.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    final Group group = new Group();

                    group.setName(item.child("name").getValue().toString());

                    String groupFather = (String) item.child("group").getValue();

                    DatabaseReference listItems = database.getReference().child("groups").child(groupFather); //Nome do grupo

                    listItems.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<Child> childList = new ArrayList<Child>(); //Nomes dos filhos para a lista

                            for (DataSnapshot subItem : dataSnapshot.getChildren()){ //Obtendo nomes dos filhos para a lista


                                DatabaseReference itemsInfo = database.getReference().child("markers").child(subItem.getKey());

                                Child child = new Child();

                                child.setName(itemsInfo.child("name").toString());

                                childList.add(child);

                                group.setItems(childList);
                            }
                            groupArrayList.add(group);
                            expandListAdapter.notifyDataSetChanged(); //Atualizar lista
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






        /*
        //Obtendo listview
        final ListView mListView = (ListView) findViewById(R.id.navigation_drawer_list);

        // Criando um novo adpter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        //Atribuindo adaptador para listview
        mListView.setAdapter(adapter);

        //Itent teste para alternar de tela
        final Intent next = new Intent(this, DescriptionActivity.class);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.add((String)dataSnapshot.child("name").getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Intent i = new Intent(getApplicationContext(), ListChild.class);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int posicao = i + 1;
                refChild = database.getReference().child("menu").child(""+posicao);
                refChild.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        HashMap<String, Boolean> child;

                        child  = (HashMap<String, Boolean>) dataSnapshot.child("markers").getValue();

                        for (String key : child.keySet() ){
                            //Log.v("key", key);
                            Intent i = new Intent(getApplicationContext(), ListChild.class);
                            i.putExtra("key",key);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        */
    }

    public ArrayList<Group> setStandardGroups(String item){
        ArrayList<Group> groupList =  new ArrayList<Group>();
        ArrayList<Child> childList;

        //group
        childList = new ArrayList<Child>();
        Group group1 = new Group();
        group1.setName("Igrejas");

        Child child1 = new Child();
        child1.setName("Basilica");
        childList.add(child1);

        group1.setItems(childList);

        //Adicionando grupos
        groupList.add(group1);

        return groupList;
    }

    @Override
    public void onBackPressed() {
       drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
        }

        return super.onOptionsItemSelected(item);
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        android.support.v4.app.FragmentManager mFm = getSupportFragmentManager();
        int id = item.getItemId();

        if(mMapFragment.isAdded())
            mFm.beginTransaction().hide(mMapFragment).commit();

        if (id == R.id.nav_camera) {

            fm.beginTransaction().replace(R.id.content_frame, new ImportFragment()).commit();

        } else if (id == R.id.nav_gallery) {

            if(!mMapFragment.isAdded())
                mFm.beginTransaction().add(R.id.map, mMapFragment).commit();
            else
                mFm.beginTransaction().show(mMapFragment).commit();

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    */

    @Override
    public void onMapReady(GoogleMap map){
        this.map = map;
        //LatLng caninde = new LatLng(-4.358, -39.3137);
        //LatLng estatua = new LatLng(-4.367486, -39.30511236);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);

        this.searchCircle = this.map.addCircle(new CircleOptions().center(latLngCenter).radius(300));
        this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
        this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, INITIAL_ZOOM_LEVEL));
        map.setOnCameraMoveListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void showAlertDialog(Context ctx, String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx).setMessage(body).setCancelable(false);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

    /*
    private void downloadFile(){
        storageRef.child("locais/1/1.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'locais/1/1.jpg'
                //Picasso.with().load(""+uri).into(mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }
    */
    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }


    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = this.map.getCameraPosition();
        LatLng center = cameraPosition.target;
        double radius = zoomLevelToRadius(cameraPosition.zoom);
        this.searchCircle.setCenter(center);
        this.searchCircle.setRadius(1000);
        this.geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
        Log.v("geoQuery ", "Radius: " + this.geoQuery.getRadius() + " center.latitude: "+ this.geoQuery.getCenter().latitude + " center.longitude:" +  this.geoQuery.getCenter().longitude);
        // radius in km
        this.geoQuery.setRadius(1000);

    }

    //Geo Query
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Log.v("onKeyEntered", key);
        Marker marker = this.map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        this.markers.put(key, marker);
    }

    @Override
    public void onKeyExited(String key) {
        // Remove any old marker
        Log.v("onKeyExited", key);
        Marker marker = this.markers.get(key);
        if (marker != null) {
            marker.remove();
            this.markers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // Move the marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            this.animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed/DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}

/*
class ListUtils {
    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }
}

*/
/*ListView mListView;
        TextView textViewtitle = (TextView) findViewById(R.id.textViewtitle);
        TextView textViewcontent = (TextView) findViewById(R.id.textViewcontent);

        mListView = (ListView) findViewById(R.id.navigation_drawer_list);
        final Intent vai = new Intent(this, TeladeTeste.class);

        FirebaseListAdapter<String> adapter = new FirebaseListAdapter<String>(
                this,
                String.class,
                android.R.layout.simple_list_item_1,
                ref
        ) {
            @Override
            protected void populateView(final View v, final String s, final int position) {
                TextView textView = (TextView)v.findViewById(android.R.id.text1);
                textView.setText(s);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startService(vai);
                    }
                });

            }
        };



        mListView.setAdapter(adapter);*/
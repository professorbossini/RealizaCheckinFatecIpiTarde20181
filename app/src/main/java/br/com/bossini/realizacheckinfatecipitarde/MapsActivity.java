package br.com.bossini.realizacheckinfatecipitarde;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private static final int REQ_PERM_GPS = 1001;
    private static final int REQ_PERM_CAMERA = 1002;
    private GoogleMap mMap;
    private static final int REQ_ABRIR_CAMERA = 1;
    private Location locationAtual = null;
    private LatLng latLngAtual;
    private LocationManager locationManager;
    private LocationListener locationListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location novoLocation) {
                    locationAtual = novoLocation;
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager =
                (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
        FragmentManager fm =
                (FragmentManager) getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)
                fm.findFragmentById(R.id.map);


        mapFragment.getMapAsync(meuObserverDeNuvem);

    }
    private OnMapReadyCallback meuObserverDeNuvem =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    /*
                    // Add a marker in Sydney and move the camera
                    LatLng sydney = new LatLng(-34, 151);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    */
                }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.
                checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.
                    shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this,
                        getString(R.string.explicacao_gps),
                        Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.
                    requestPermissions(this,
                            new String[]{
                              Manifest.permission.ACCESS_FINE_LOCATION
                            }, REQ_PERM_GPS);
        }
        else{
            locationManager.
                    requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    public void checkin (View view){
        if (mMap == null){
            Toast.makeText(this,
                    getString(R.string.mapa_indisponivel),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            if (locationAtual == null){
                Toast.makeText(this,
                        getString(R.string.gps_indisponivel),
                        Toast.LENGTH_SHORT).show();
                latLngAtual = new LatLng(-23.5631338 , -46.6543286);
            }
            else{
                double latitude =
                        locationAtual.getLatitude();
                double longitude =
                        locationAtual.getLongitude();
                latLngAtual =
                        new LatLng(latitude, longitude);
            }
        }
        tirarFoto();
    }
    public void tirarFoto (){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA)
            ){
                Toast.makeText(this,
                        getString(R.string.explicacao_camera),
                        Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String []{Manifest.permission.CAMERA},
                    REQ_PERM_CAMERA);
        }
        else{
            Intent intent = new Intent (
                    MediaStore.ACTION_IMAGE_CAPTURE
            );
            if (intent.resolveActivity(getPackageManager())
                    != null){
                startActivityForResult(intent, REQ_ABRIR_CAMERA);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == REQ_ABRIR_CAMERA){
            if (Activity.RESULT_OK == resultCode){
                Bundle dados =
                        data.getExtras();
                Bitmap foto =
                        (Bitmap) dados.get("data");
                BitmapDescriptor fotoParaOMapa =
                        BitmapDescriptorFactory.
                                fromBitmap(foto);
                MarkerOptions pontoNoMapa =
                        new MarkerOptions().
                                position(latLngAtual).
                                icon(fotoParaOMapa).
                                title(getString(
                                        R.string.estou_aqui
                                ));
                mMap.addMarker(pontoNoMapa);
                CameraUpdate update =
                        CameraUpdateFactory.
                                newLatLng(latLngAtual);
                mMap.moveCamera(update);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM_GPS){
            if (grantResults.length > 0
                    && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                ){
                    locationManager.
                            requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    0,
                                    0,
                                    locationListener
                            );
                }

            }
        }
    else if (requestCode == REQ_PERM_CAMERA){
            Intent intent = new Intent (
                    MediaStore.ACTION_IMAGE_CAPTURE
            );
            if (intent.resolveActivity(getPackageManager())
                    != null){
                startActivityForResult(intent, REQ_ABRIR_CAMERA);
            }
        }
    }
}

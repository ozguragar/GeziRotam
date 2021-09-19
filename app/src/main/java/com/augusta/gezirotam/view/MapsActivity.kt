package com.augusta.gezirotam.view
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.augusta.gezirotam.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.google.android.gms.maps.model.PolylineOptions

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


import com.google.maps.android.PolyUtil


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

import com.augusta.gezirotam.Model.DirectionResponses
import com.augusta.gezirotam.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.win_layout.*
import kotlinx.android.synthetic.main.win_layout.view.*
import java.util.*
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private var currentMarker : Marker? = null
    private var dialog: Dialog? = null
    var editText: EditText? = null

//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.983013,28.810269&radius=1500&type=restaurant&key=AIzaSyDxsPg7-OCYmLXd4nL5usNJLFOFRgXP7ZE"

    lateinit var newUserLocation: LatLng
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val drawerLayout : DrawerLayout =findViewById(R.id.drawerlayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle= ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        editText = findViewById(R.id.edit_text)

        Places.initialize(applicationContext, "AIzaSyDxsPg7-OCYmLXd4nL5usNJLFOFRgXP7ZE")
        editText!!.setFocusable(false)
        editText!!.setOnClickListener(View.OnClickListener {
            val fieldList = Arrays.asList(
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.NAME
            )
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fieldList
            ).build(this@MapsActivity)
            startActivityForResult(intent, 100)
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_harita ->  mapbut()
                R.id.nav_logout -> logoutbut()
                R.id.nav_hakkimizda -> hakkimizdabut()
                R.id.nav_rateus -> Toast.makeText(applicationContext,"Uygulamamız Çeşitli Platformlara Yüklendiğinde Değerlendirebilirsiniz.",Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext,"Uygulamamız Çeşitli Platformlara Yüklendiğinde Paylaşabilirsiniz",Toast.LENGTH_SHORT).show()


            }
            true
        }


        val animatedDrawable = linearlayoutt.background as AnimationDrawable
        animatedDrawable.apply {
            setEnterFadeDuration(1000)
            setExitFadeDuration(2000)
            start()
        }

        auth = Firebase.auth

        dialog = Dialog(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            editText!!.setText(place.address)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,15f))
        }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(applicationContext, status.statusMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun logoutbut(){
        auth.signOut()

        GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
            .signOut()

        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }
    fun mapbut(){

        val intent = Intent(this@MapsActivity, MapsActivity::class.java)
        startActivity(intent)


    }
    private fun hakkimizdabut() {
        val intent = Intent(this@MapsActivity, AboutUsActivity::class.java)
        startActivity(intent)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //mMap.isMyLocationEnabled = true
        mMap.setOnMapClickListener(selectingPlace)
        mMap.setOnMarkerClickListener(selectingMarker)

        val hahaa =LatLng(40.983013,28.810269)

        val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
            .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
            .snippet(auth.currentUser?.email)

        currentMarker?.remove()
        currentMarker=mMap.addMarker(markerOption)
        currentMarker?.tag=703

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hahaa,2f))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,15f))

        binding.gastronomi.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            gastronomiMekan()

        }
        binding.kultur.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            kulturMekan()

        }
        binding.konak.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            konaklama()

        }
        binding.acill.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            acildurum()

        }
        binding.eglence.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            eglenceMekan()

        }
        binding.mutlaka.setOnClickListener {
            mMap.clear()

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            gezilmesigerekenler()

        }

        //"https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=<AIzaSyDxsPg7-OCYmLXd4nL5usNJLFOFRgXP7ZE>"

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
             override fun onLocationChanged(location: Location) {

                if (location != null){
                    //mMap.clear()
                    newUserLocation = LatLng(40.983013,28.810269)

                    println(newUserLocation)

                    val markerOption = MarkerOptions().position(LatLng(newUserLocation.latitude,newUserLocation.longitude))
                        .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                        .snippet(auth.currentUser?.email)

                    currentMarker?.remove()
                    currentMarker=mMap.addMarker(markerOption)
                    currentMarker?.tag=703

                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation,15f))
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation,10f))
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)

            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation!=null){
                val lastKnownLatLng = LatLng(40.983013,28.810269)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng,15f))
            }
        }
    }

    val selectingMarker = object  : GoogleMap.OnMarkerClickListener{
        override fun onMarkerClick(p0: Marker): Boolean {

            val hahaa =LatLng(40.983013,28.810269)
            val markerlat = p0.position.latitude
            val markerlong = p0.position.longitude

            dialog!!.setContentView(R.layout.win_layout)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val imageViewClose = dialog!!.findViewById<ImageView>(R.id.imageView_close)
            dialog!!.adress.text = "Gideceğiniz Yer: ${p0.title}"

            val btnOK = dialog!!.findViewById<Button>(R.id.rotaolusturbtn)

            imageViewClose.setOnClickListener {
                dialog!!.dismiss()
                Toast.makeText(this@MapsActivity,"Rota Oluşum İşlemi İptal Edildi!",Toast.LENGTH_LONG).show()
            }

            btnOK.setOnClickListener {
                titreme()
                dialog!!.dismiss()
                mMap.clear()

                val rotamarkeri = MarkerOptions().position(LatLng(markerlat,markerlong))
                    .title("Mekan Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.finishh))
                mMap.addMarker(rotamarkeri)

                val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                    .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                    .snippet(auth.currentUser?.email)

                currentMarker?.remove()
                currentMarker=mMap.addMarker(markerOption)
                currentMarker?.tag=703

                val baslangic = hahaa.latitude.toString() + "," + hahaa.longitude.toString()
                val hedef = markerlat.toString() + "," + markerlong.toString()

                val apiServices = RetrofitClient.apiServices(this@MapsActivity)
                apiServices.getDirection(baslangic, hedef, getString(R.string.api_key))
                    .enqueue(object : Callback<DirectionResponses> {
                        override fun onResponse(call: Call<DirectionResponses>, response: Response<DirectionResponses>) {
                            println(response)
                            drawPolyline(response)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,12f))
                            Log.d("AAAAAAAAAAAAAA", response.message())
                        }

                        override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                            Log.e("BBBBBBBBBBBB", t.localizedMessage)
                        }
                    })}

            dialog!!.show()

            return true
        }
    }

    val selectingPlace = object : GoogleMap.OnMapClickListener{
        override fun onMapClick(p0: LatLng)  {

            val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())

            var address = ""
            if (p0 != null){
                val addressList = geoCoder.getFromLocation(p0.latitude,p0.longitude,1)
                if (addressList != null && addressList.size > 0){
                    if (addressList[0].thoroughfare != null){
                        address += addressList[0].thoroughfare

                    }
                }
            }else{
                address="No Address"
            }
                    mMap.clear()

            val hahaa =LatLng(40.983013,28.810269)
            val hohoo = LatLng(-6.1890511,106.8251573)

            val mark = mMap.addMarker(MarkerOptions().position(p0).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.finishh))
                .title("Seçilen Konum"))

            val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                .snippet(auth.currentUser?.email)

            currentMarker?.remove()
            currentMarker=mMap.addMarker(markerOption)
            currentMarker?.tag=703

            val baslangic = hahaa.latitude.toString() + "," + hahaa.longitude.toString()
            val hedef = p0.latitude.toString() + "," + p0.longitude.toString()

            dialog!!.setContentView(R.layout.win_layout)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val imageViewClose = dialog!!.findViewById<ImageView>(R.id.imageView_close)
            dialog!!.adress.text = "Gideceğiniz Yer: ${address}"

            val btnOK = dialog!!.findViewById<Button>(R.id.rotaolusturbtn)

            imageViewClose.setOnClickListener {
                dialog!!.dismiss()
                mark.remove()
                Toast.makeText(this@MapsActivity,"Rota Oluşum İşlemi İptal Edildi!",Toast.LENGTH_LONG).show()
            }

            btnOK.setOnClickListener {
                titreme()
                dialog!!.dismiss()
                mMap.clear()

                val rotamarkeri = MarkerOptions().position(LatLng(p0.latitude,p0.longitude))
                    .title("Mekan Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.finishh))
                mMap.addMarker(rotamarkeri)

                val markerOption = MarkerOptions().position(LatLng(hahaa.latitude,hahaa.longitude))
                    .title("Güncel Konum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.man))
                    .snippet(auth.currentUser?.email)

                currentMarker?.remove()
                currentMarker=mMap.addMarker(markerOption)
                currentMarker?.tag=703

                val baslangic = hahaa.latitude.toString() + "," + hahaa.longitude.toString()
                val hedef = p0.latitude.toString() + "," + p0.longitude.toString()

                val apiServices = RetrofitClient.apiServices(this@MapsActivity)
                apiServices.getDirection(baslangic, hedef, getString(R.string.api_key))
                    .enqueue(object : Callback<DirectionResponses> {
                        override fun onResponse(call: Call<DirectionResponses>, response: Response<DirectionResponses>) {
                            println(response)
                            drawPolyline(response)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,12f))
                            Log.d("AAAAAAAAAAAAAA", response.message())
                        }

                        override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                            Log.e("BBBBBBBBBBBB", t.localizedMessage)
                        }
                    })}

            dialog!!.show()

        }

    }

    private fun gastronomiMekan(){
        val hahaa =LatLng(40.983013,28.810269)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,13f))

        val canababa= MarkerOptions().position(LatLng(41.000637,28.792988))
            .title("Canbaba Restaurant").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(canababa)
        val Walking=MarkerOptions().position(LatLng(40.9651538,28.79809879999999))
            .title("Walkin Brasserie").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(Walking)
        val Cookah=MarkerOptions().position(LatLng(40.959323,28.822556))
            .title("Cookah Cafe & Bistro").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(Cookah)
        val Lacasa=MarkerOptions().position(LatLng(40.9586712,28.8234899))
            .title("LaCasa Yeşilköy").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(Lacasa)
        val Publig=MarkerOptions().position(LatLng(41.010963,28.81707399999999))
            .title("Publig Bar").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(Publig)
        val shield=MarkerOptions().position(LatLng(40.9591667,28.8361111))
            .title("The North Shield Pub").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(shield)
        val pippo=MarkerOptions().position(LatLng(40.9728171,28.8046708))
            .title("Pippo Shisha Café at DHMI").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(pippo)

        val winebar=MarkerOptions().position(LatLng(40.9812562,28.7931342))
            .title("M&G Wine Bar").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(winebar)

        val zuzu=MarkerOptions().position(LatLng(40.9802427,28.7933144))
            .title("Zuzu Shisha Lounge").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(zuzu)

        val TheChef =MarkerOptions().position(LatLng(40.99122209999999,28.7959438))
            .title("The Chef").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(TheChef)

        val Roof=MarkerOptions().position(LatLng(40.974241,28.796319))
            .title("Roof 9").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(Roof)

        val KahveYeşilköy=MarkerOptions().position(LatLng(40.958262,28.820905))
            .title("Kahve Dünyası - Yeşilköy").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(KahveYeşilköy)

        val CafeRestaurant=MarkerOptions().position(LatLng(40.985575,28.830657))
            .title("57 Cafe & Restaurant").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(CafeRestaurant)

        val CafeCrown=MarkerOptions().position(LatLng(40.9731362,28.8044548))
            .title("Cafe Crown").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(CafeCrown)

        val Lavazza=MarkerOptions().position(LatLng(40.9731315,28.8045988))
            .title("Lavazza Best Coffee Shop").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(Lavazza)

        val Mondo=MarkerOptions().position(LatLng(40.9875639,28.7971652))
            .title("Mondo Lounge").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(Mondo)

        val Shozy=MarkerOptions().position(LatLng(40.9888114,28.7965261))
            .title("Shozy Cafe").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(Shozy)

        val FloryaKahvesi=MarkerOptions().position(LatLng(40.98166499999999,28.7940884))
            .title("Florya Kahvesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.coffee))
        mMap.addMarker(FloryaKahvesi)

        val GAJA=MarkerOptions().position(LatLng(40.9815207,28.7937145))
            .title("GAJA GARDEN HOOKAH LOUNGE").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(GAJA)

        val Dhabı=MarkerOptions().position(LatLng(40.9909829,28.7956458))
            .title("Abu Dhabı Cafe & Restaurant").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(Dhabı)

        val Golden=MarkerOptions().position(LatLng(41.0018744,28.81714089999999))
            .title("Golden Izgara").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(Golden)

        val Munzur=MarkerOptions().position(LatLng(40.9929089,28.8356621))
            .title("Munzur Ocakbaşı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(Munzur)

        val KIRKPINAR=MarkerOptions().position(LatLng(40.9937659,28.8046708))
            .title("KIRKPINAR ET BALIK RESTAURANT").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dinnerr))
        mMap.addMarker(KIRKPINAR)

    }
    private fun kulturMekan() {
        val hahaa =LatLng(40.983013,28.810269)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,13f))

        val İKÜSAG= MarkerOptions().position(LatLng(40.9913737,28.832098))
            .title("İKÜSAG İstanbul Kültür Üniversitesi Sanat Galerisi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(İKÜSAG)

        val Sefaköy= MarkerOptions().position(LatLng(40.9922769,28.7891048))
            .title("T.C. Kültür ve Turizm Bakanlığı Sefaköy Halk Kütüphanesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.book))
        mMap.addMarker(Sefaköy)

        val Hava= MarkerOptions().position(LatLng(40.964241,28.8261242))
            .title("İstanbul Hava Kuvvetleri Müzesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Hava)

        val Avukatlık= MarkerOptions().position(LatLng(41.0050005,28.8119103))
            .title("Avukatlık Müzesi ( Museum of Lawyers)").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Avukatlık)

        val CERABLUS= MarkerOptions().position(LatLng(41.0036457,28.8325555))
            .title("CERABLUS CEPHESİ").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(CERABLUS)

        val Merkez= MarkerOptions().position(LatLng(41.00288969999999,28.8344105))
            .title("Merkez Camii").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Merkez)

        val Belediyesi= MarkerOptions().position(LatLng(40.9559636,28.8213597))
            .title("T.C Bakırköy Belediyesi Sanat Evi ve Kent Müzesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Belediyesi)

        val atatürk= MarkerOptions().position(LatLng(40.9559108,28.8212939))
            .title("Yeşilköy atatürk Müzesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(atatürk)

        val Fatih= MarkerOptions().position(LatLng(40.9908586,28.770798))
            .title("Fatih Çeşmesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Fatih)

        val Mehmet= MarkerOptions().position(LatLng(40.9909853,28.7707245))
            .title("Vezir Mehmet Paşa Çeşmesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Mehmet)

        val Akvaryum= MarkerOptions().position(LatLng(40.9651131,28.7989621))
            .title("İstanbul Akvaryum").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.circus))
        mMap.addMarker(Akvaryum)

        val Ormanı= MarkerOptions().position(LatLng(40.9768634,28.7870159))
            .title("Florya Atatürk Ormanı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tree))
        mMap.addMarker(Ormanı)

        val lunaparkf= MarkerOptions().position(LatLng(40.96249479999999,28.8053779))
            .title("Florya lunapark").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.circus))
        mMap.addMarker(lunaparkf)

        val Zafer= MarkerOptions().position(LatLng(41.0009393,28.83688580000001))
            .title("Zafer Meydanı Parkı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tree))
        mMap.addMarker(Zafer)

        val Hamamı= MarkerOptions().position(LatLng(40.9954829,28.8384013))
            .title("Saray Hamamı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.monument))
        mMap.addMarker(Hamamı)

    }
    private fun eglenceMekan(){
        val Cinefly= MarkerOptions().position(LatLng(40.973277834791816,28.80457472256813))
            .title("Cinefly").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.film))
        mMap.addMarker(Cinefly)

        val Airport= MarkerOptions().position(LatLng(40.9916667,28.8308333))
            .title("Airport Sinema").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.film))
        mMap.addMarker(Airport)

        val Aqua= MarkerOptions().position(LatLng(40.9648182,28.7985945))
            .title("Cinemaximum Aqua Florya").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.film))
        mMap.addMarker(Aqua)

        val Medya= MarkerOptions().position(LatLng(40.9868253,28.833382))
            .title("DMP Medya Açık Havada Sinema Tic. Ltd. Şti.").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.film))
        mMap.addMarker(Medya)

        val Acikhava= MarkerOptions().position(LatLng(40.96533160000001,28.7978234))
            .title("Acikhava Sinemasi Ucretsiz Carsamba Gunleri").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.film))
        mMap.addMarker(Acikhava)

        val Aproon= MarkerOptions().position(LatLng(40.983457,28.794725))
            .title("Aproon Club Florya").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(Aproon)

        val Dejavu= MarkerOptions().position(LatLng(40.9948323,28.7779686))
            .title("Dejavu Club").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(Dejavu)

        val Kirpi= MarkerOptions().position(LatLng( 41.0105086, 28.8172574))
            .title("Kirpi Istanbul").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(Kirpi)

        val Playzone= MarkerOptions().position(LatLng(40.9661236,28.7971834))
            .title("Playzone").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.cocktail))
        mMap.addMarker(Playzone)

        val FenerStad= MarkerOptions().position(LatLng(40.9879195496037, 29.036791111289812))
            .title("Fenerbahçe Şükrü Saracoğlu Stadı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.stadium))
        mMap.addMarker(FenerStad)

        val VodStad= MarkerOptions().position(LatLng(41.03953142543389, 28.994533899650317))
            .title("Vodafone Park").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.stadium))
        mMap.addMarker(VodStad)

        val Veliefendi= MarkerOptions().position(LatLng(40.98744066964474, 28.886570789803326))
            .title("Veliefendi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.stadium))
        mMap.addMarker(Veliefendi)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Veliefendi.position,11f))

        val Jungleis= MarkerOptions().position(LatLng(41.07618881066642, 28.924225609369312))
            .title("Jungle İstanbul").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.dog))
        mMap.addMarker(Jungleis)
    }
    private fun konaklama(){
        val hahaa =LatLng(40.983013,28.810269)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,13f))

        val qqHotel= MarkerOptions().position(LatLng(40.9887145,28.83009759999999))
            .title("WOW İstanbul Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker((qqHotel))

        val FLORYAHOTEL= MarkerOptions().position(LatLng(40.9856055, 28.7877529))
            .title("FLORYA PARK HOTEL").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(FLORYAHOTEL)

        val MidmarHotel= MarkerOptions().position(LatLng( 41.0028804802915, 28.8044901802915))
        .title("Midmar Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
                mMap.addMarker(MidmarHotel)

        val GORRIONHOTEL= MarkerOptions().position(LatLng( 41.0027094, 28.8215953))
            .title("GORRION HOTEL").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
                    mMap.addMarker(GORRIONHOTEL)

        val AdelaHotel= MarkerOptions().position(LatLng( 40.9924722, 28.83892350000001))
            .title("Adela Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(AdelaHotel)

        val GrandHotel= MarkerOptions().position(LatLng(40.994128, 28.8396926))
            .title("Grand Palace Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(GrandHotel)

        val ShahHotel= MarkerOptions().position(LatLng(40.9582037, 28.8224682))
            .title("Shah İnn Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(ShahHotel)

        val AirportHotel= MarkerOptions().position(LatLng(40.955618, 28.821491))
            .title("Airport Inn Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(AirportHotel)

        val TempoAirport= MarkerOptions().position(LatLng(40.99378340000001, 28.8412688))
            .title("Tempo Suites Airport").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(TempoAirport)

        val KocasinanHotel= MarkerOptions().position(LatLng(40.99217300000001, 28.846639))
            .title("Kocasinan Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(KocasinanHotel)

        val EliteHotel= MarkerOptions().position(LatLng(40.988953, 28.791375))
            .title("Elite World Business Hotel").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(EliteHotel)

        val HotelAirport= MarkerOptions().position(LatLng(41.01067429999999, 28.8173024))
            .title("Hotel Osaka Airport").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_hotel_black_24dp))
        mMap.addMarker(HotelAirport)
    }
    private fun gezilmesigerekenler(){
        val Dolmabahçe= MarkerOptions().position(LatLng(41.03938277217389,29.000577414432108))
            .title("Dolmabahçe Sarayı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.temple))
        mMap.addMarker(Dolmabahçe)

        val Ayasofya= MarkerOptions().position(LatLng(41.00863965198598, 28.980164268406504))
            .title("Ayasofya Camii").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mosque))
        mMap.addMarker(Ayasofya)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Ayasofya.position,12f))


        val Eyüp= MarkerOptions().position(LatLng(41.0480601743515,28.933890229773844))
            .title("Eyüp Sultan Camii").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mosque))
        mMap.addMarker(Eyüp)

        val Sultanahmet= MarkerOptions().position(LatLng(41.00663879084365,28.97615125059403))
            .title("Sultanahmet Meydanı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mosque))
        mMap.addMarker(Sultanahmet)

        val Yerebatan= MarkerOptions().position(LatLng(41.00834903809817,28.977839988125464))
            .title("Yerebatan Sarnıcı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.column))
        mMap.addMarker(Yerebatan)

        val beylerbeyi= MarkerOptions().position(LatLng(41.04253940859686,29.040016183749263))
            .title("Beylerbeyi Sarayı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.temple))
        mMap.addMarker(beylerbeyi)

        val serefiye= MarkerOptions().position(LatLng(41.00735769430867,28.972555858607848))
            .title("Şerefiye Sarnıcı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.column))
        mMap.addMarker(serefiye)

        val fati= MarkerOptions().position(LatLng(41.01942799464877,28.950135973949752))
            .title("Fatih Camii").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.mosque))
        mMap.addMarker(fati)

        val Galata= MarkerOptions().position(LatLng(41.02568634681176,28.97418179598868))
            .title("Galata Kulesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.galata))
        mMap.addMarker(Galata)

        val kızkule= MarkerOptions().position(LatLng(41.02112256742744,29.00410761508998))
            .title("Kız Kulesi").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.irishtower))
        mMap.addMarker(kızkule)

        val rumeli= MarkerOptions().position(LatLng(41.08480620584398,29.056676439573877))
            .title("Rumeli Hisari").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tower))
        mMap.addMarker(rumeli)

        val miniatürk= MarkerOptions().position(LatLng(41.059036986402155,28.949393962064853))
            .title("Miniatürk").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ship))
        mMap.addMarker(miniatürk)

        val hidiv= MarkerOptions().position(LatLng(41.10310348616948,29.075165824233))
            .title("Hidiv Kasrı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tower))
        mMap.addMarker(hidiv)

        val bozdoğan= MarkerOptions().position(LatLng(41.015928793715126,28.955505708888346))
            .title("Bozdoğan Kemeri").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.column))
        mMap.addMarker(bozdoğan)

        val boga= MarkerOptions().position(LatLng(40.990414592528055,29.02914000807772))
            .title("Boğa Heykeli").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.strong))
        mMap.addMarker(boga)

        val topkapı= MarkerOptions().position(LatLng(41.022120454436894,28.9268586230535))
            .title("Topkapı Sarayı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.temple))
        mMap.addMarker(topkapı)

        val belgrad= MarkerOptions().position(LatLng(41.184020053795905,28.988486003751692))
            .title("Belgrad Ormanı").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.tree))
        mMap.addMarker(belgrad)

    }
    private fun acildurum(){
        val hahaa =LatLng(40.983013,28.810269)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hahaa,13f))

        val Küçükçekmece= MarkerOptions().position(LatLng(41.0003338,28.7999268))
            .title("Küçükçekmece İlçe Emniyet Müdürlüğü").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.siren))//bar
        mMap.addMarker(Küçükçekmece)

        val Safa= MarkerOptions().position(LatLng(40.99914729999999,28.83829619999999))
            .title("Safa Hastanesi Balkan özel sağlık hizmetleri").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_local_hospital_24))
        mMap.addMarker(Safa)
    }

    private fun titreme(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(500) // Vibrate method for below API Level 26
            }
        }
    }

        //rotaya random renk
    val rnd = Random.Default //kotlin.random
    val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

    private fun drawPolyline(response: Response<DirectionResponses>) {
        val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(shape))
            .width(15f)
            .color(color)
        mMap.addPolyline(polyline)


    }
    private interface ApiServices {
        @GET("maps/api/directions/json")
        fun getDirection(@Query("origin") origin: String,
                         @Query("destination") destination: String,
                         @Query("key") apiKey: String): Call<DirectionResponses>
    }
    private object RetrofitClient {
        fun apiServices(context: Context): ApiServices {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(context.resources.getString(R.string.base_url))
                .build()

            return retrofit.create<ApiServices>(ApiServices::class.java)
        }
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0){
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
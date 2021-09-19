package com.augusta.gezirotam.view

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.augusta.gezirotam.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.activity_maps.*

class AboutUsActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        auth = Firebase.auth

        val animatedDrawable = hakk.background as AnimationDrawable
        animatedDrawable.apply {
            setEnterFadeDuration(1000)
            setExitFadeDuration(2000)
            start()
        }

        val drawerLayout : DrawerLayout =findViewById(R.id.drawerrlayout)
        val navView: NavigationView = findViewById(R.id.nav_vieww)

        toggle= ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_harita ->  mapbut()
                R.id.nav_logout -> logoutbut()
                R.id.nav_hakkimizda -> hakkimizdabut()
                R.id.nav_rateus -> Toast.makeText(applicationContext,"Uygulamamız Çeşitli Platformlara Yüklendiğinde Değerlendirebilirsiniz.",
                    Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext,"Uygulamamız Çeşitli Platformlara Yüklendiğinde Paylaşabilirsiniz",
                    Toast.LENGTH_SHORT).show()


            }
            true
        }
    }

    private fun hakkimizdabut() {
        val intent = Intent(this@AboutUsActivity, AboutUsActivity::class.java)
        startActivity(intent)

    }

    fun logoutbut(){
        auth.signOut()

        GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
            .signOut()

        val intent = Intent(this@AboutUsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }
    fun mapbut(){

        val intent = Intent(this@AboutUsActivity, MapsActivity::class.java)
        startActivity(intent)


    }

}
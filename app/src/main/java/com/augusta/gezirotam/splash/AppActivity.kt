package com.augusta.gezirotam.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.augusta.gezirotam.databinding.ActivityAppBinding
import com.augusta.gezirotam.view.MainActivity
import com.augusta.gezirotam.view.MapsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_splash.*

class AppActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAppBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        iv_note.alpha = 0f
        iv_note.animate().setDuration(1500).alpha(1f).withEndAction{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}





package com.example.finalproject.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationActivity : AppCompatActivity() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private lateinit var tvLocation: TextView

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

            if (granted) fetchLocation()
            else {
                tvLocation.text = "אין הרשאת מיקום"
                Toast.makeText(this, "אין הרשאת מיקום", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        tvLocation = findViewById(R.id.tvLocation)
        val btnGet = findViewById<Button>(R.id.btnGetLocation)
        val btnMaps = findViewById<Button>(R.id.btnOpenMaps)

        // הצגה נקייה: אם יש כתובת שמורה – נראה אותה, אחרת טקסט כללי
        val prefs = getSharedPreferences("gps_prefs", Context.MODE_PRIVATE)
        val savedAddress = prefs.getString("address", null)
        tvLocation.text = savedAddress?.let { "הכתובת האחרונה:\n$it" }
            ?: "לחצי על 'קבל מיקום' כדי לקבל כתובת"

        btnGet.setOnClickListener { ensurePermissionAndFetch() }

        btnMaps.setOnClickListener {
            // פתיחה במפות על בסיס lat/lng שמור (גם אם לא מציגים אותם)
            val latS = prefs.getString("lat", null)
            val lngS = prefs.getString("lng", null)

            if (latS == null || lngS == null) {
                Toast.makeText(this, "אין מיקום שמור עדיין", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uri = Uri.parse("geo:$latS,$lngS?q=$latS,$lngS")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun ensurePermissionAndFetch() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        tvLocation.text = "מחפש כתובת..."
        val prefs = getSharedPreferences("gps_prefs", Context.MODE_PRIVATE)

        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc == null) {
                        tvLocation.text = "לא נמצא מיקום. ודאי שמיקום דולק"
                        return@addOnSuccessListener
                    }

                    val lat = loc.latitude
                    val lng = loc.longitude

                    // Reverse geocoding (כתובת)
                    val address = try {
                        val geocoder = Geocoder(this, Locale("he"))
                        val list = geocoder.getFromLocation(lat, lng, 1)
                        list?.firstOrNull()?.getAddressLine(0)
                    } catch (_: Exception) {
                        null
                    }

                    val display = address ?: "לא נמצאה כתובת מדויקת (אבל המיקום נקלט)"

                    tvLocation.text = "הכתובת הנוכחית:\n$display"

                    // שמירה (עומד בדרישת GPS)
                    prefs.edit()
                        .putString("lat", lat.toString())
                        .putString("lng", lng.toString())
                        .putString("address", display)
                        .apply()
                }
                .addOnFailureListener { e ->
                    tvLocation.text = "שגיאה בקבלת מיקום: ${e.message}"
                }
        } catch (e: SecurityException) {
            tvLocation.text = "אין הרשאת מיקום"
        }
    }
}

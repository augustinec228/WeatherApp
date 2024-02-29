package com.example.weatherapp

import android.app.DownloadManager.Request
import android.health.connect.datatypes.units.Temperature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.fragments.MainFragment
import org.json.JSONObject

const val API_KEY = "fb31c8ac25e4446399a74955242202"

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.placeHolder, MainFragment.newInstance()).commit()




        fun getResult(name: String) {


            val url = "https://api.weatherapi.com/v1/current.json" +
                    "?key=$API_KEY&q=$name&aqi=no"
            val queue = Volley.newRequestQueue(this)
            val stringRequest = StringRequest(com.android.volley.Request.Method.GET,
                url, { reponse ->
                    val obj = JSONObject(reponse)
                    val temp = obj.getJSONObject("current")
                    val country_obj = obj.getJSONObject("location")
                    Log.d("MyLog", "Volley success  ${temp.getString("temp_c")}")
                },
                { Log.d("MyLog", "Volley error $it") })
            queue.add(stringRequest)
        }
    }
}

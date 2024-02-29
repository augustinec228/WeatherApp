package com.example.weatherapp.fragments

import android.Manifest
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.DialogManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.adapters.vpAdapter
import com.example.weatherapp.adapters.weatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.isPesmissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "fb31c8ac25e4446399a74955242202&q="

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val model: MainViewModel by activityViewModels()


    private lateinit var binding: FragmentMainBinding
    private val flist = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tlist = listOf(
        "Часы",
        "Дни"
    )
    private lateinit var vp: ViewPager
    private lateinit var pLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissonListener()
        init()
        requestWeatherData("London}")
        updateCurrentCard()
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = vpAdapter(activity as FragmentActivity, flist)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tlist[pos]
        }.attach()
        ibSync.setOnClickListener{
            getLocation()
        }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun getLocation(){
        if (!isLocationEnabled()) {
            Toast.makeText(requireContext(), "У вас выключен GPS", Toast.LENGTH_SHORT).show()
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                }

            })
                return
        }
            val ct = CancellationTokenSource()
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
                .addOnCompleteListener {
                    requestWeatherData("${it.result.latitude},${it.result.longitude}")
                }
    }

    private fun updateCurrentCard() = with(binding){
        model.liveDataCurrent.observe(viewLifecycleOwner){
            val maxmin = "${it.maxTemp}C°/${it.minTemp}C°"
            tvData.text = it.time
            Picasso.get().load("https:"+it.imageUrl).into(imWeather)
            tvCity.text = it.city
            tvCondition.text = it.condition
            tvMaxMin.text = maxmin
            tvCurrentTemp.text = it.currentTemp

        }
    }

    private fun permissonListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "permission is $it", Toast.LENGTH_SHORT).show()
        }

    }

    private fun check_permission() {
        if (!isPesmissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissonListener()
            pLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "$city&days=3&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                ParseWeatherData(result)
            },
            { error ->
                Log.d("MyLog", "Error - $error")
            })

        queue.add(request)
    }

    private fun ParseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject,list[0])


    }

    private fun parseDays(mainObject: JSONObject): List<weatherModel>{
        val list = ArrayList<weatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").
        getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = weatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()

            )

            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: weatherModel) {
        val item = weatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )
        model.liveDataCurrent.value = item

        Log.d("MyLog", "Город - ${item.city}")
        Log.d("MyLog", "Дата - ${item.time}")
        Log.d("MyLog", "Погодные условия - ${item.condition}")
        Log.d("MyLog", "Температура - ${item.currentTemp}")
        Log.d("MyLog", "Картинка - ${item.imageUrl}")
        Log.d("MyLog", "Макс. температура - ${item.maxTemp}")
        Log.d("MyLog", "Минимальная температура - ${item.minTemp}")
        Log.d("MyLog", "Часы - ${item.hours}")
    }


    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()
    }
}

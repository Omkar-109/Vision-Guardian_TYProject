package eu.learning.voiceassistance

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private val BLUETOOTH_PERMISSION_CODE = 102
    private val CAMERA_PERMISSION_CODE = 103
    private lateinit var button2: ImageButton
    private lateinit var button3: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private var isListeningForUserQuestion = false
    var string = ""
    private var recognitionJob: Job? = null // Job to handle the coroutine
    private val stringURLEndPoint = "https://api.openai.com/v1/chat/completions"
    private val stringAPIKey = "PUT_KEY_HERE"
    private var stringOutput = ""
    private var textToSpeech: TextToSpeech? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var intent: Intent
    private var isListeningForUserResponse = false
    private var isSecondActivityRunning=false
    private var isThirdActivityRunning=false
    private var secondActivityReference: MainActivity2? = null
    private var thirdActivityReference: MainActivity2? = null
    private lateinit var secondActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var thirdActivityLauncher: ActivityResultLauncher<Intent>
    private var x=0
    private var y=0
    private var z=0

    private var isLocationRequested = false
    private var isWeatherRequested=false
    private val url = "https://api.openweathermap.org/data/2.5/weather"
    private val appid = "2cde457f2e013ef14182e5688809ebee"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)


        if (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }


        textToSpeech = TextToSpeech(applicationContext) {
            textToSpeech!!.setSpeechRate(1.2.toFloat())
//            textToSpeech!!.setLanguage(Locale.getDefault())
            textToSpeech!!.setLanguage(Locale.US)

        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        initializeSecondActivityLauncher()

        initializeThirdActivityLauncher()


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Implementation for onReadyForSpeech
            }

            override fun onBeginningOfSpeech() {
                // Implementation for onBeginningOfSpeech
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Implementation for onRmsChanged
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Implementation for onBufferReceived
            }

            override fun onEndOfSpeech() {
                // Implementation for onEndOfSpeech
            }

            override fun onError(error: Int) {
                // Implementation for onError
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var string = ""



                if (matches != null && matches.isNotEmpty()) {
                    val recognizedText = matches[0].lowercase()

                    if (recognizedText.contains(Regex("\\bnova\\b"))) {

                        promptUserForQuestion()
                    }
                    else if (isListeningForUserQuestion) {
                        // Assume the first match is the user's question
                        if(recognizedText.contains(Regex("\\btime\\b")))
                        {
                            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            val now= Calendar.getInstance().time
                            val formattedTime=sdf.format(now)
                            textToSpeech!!.speak("Its $formattedTime right now", TextToSpeech.QUEUE_FLUSH, null, null)
                            isListeningForUserQuestion = false

                        }
                        else if(recognizedText.contains(Regex("\\bdate\\b")))
                        {   val currentDate=Date()
                            val dateFormate=SimpleDateFormat("MM/dd/yyyy")
                            val formattedDate = dateFormate.format(currentDate)
                            textToSpeech!!.speak("Today's Date is $formattedDate",TextToSpeech.QUEUE_FLUSH, null, null)
                            isListeningForUserQuestion = false

                        }
                        else if(recognizedText.contains("distance")){
                            isListeningForUserQuestion = false
                            if(!isSecondActivityRunning) {
                                isSecondActivityRunning=true
                                button2.performClick()
                            }

                        }
                        else if(recognizedText.contains("object")){
                            isListeningForUserQuestion = false
                            if(!isThirdActivityRunning) {
                                isThirdActivityRunning=true
                                button3.performClick()
                            }
                        }
                        else if (recognizedText.contains("exit") && (isSecondActivityRunning || isThirdActivityRunning)) {
                            textToSpeech!!.speak("stopping...",TextToSpeech.QUEUE_FLUSH, null, null)
                            while(textToSpeech!!.isSpeaking()){

                            }
                            if(isSecondActivityRunning) {

                                stopSecondActivityAsync()
                            }
                            if(isThirdActivityRunning) {
                                stopThirdActivityAsync()
                            }
                            isListeningForUserQuestion = false
                        }

                        else if(recognizedText.contains(Regex("\\blocation\\b"))){
                            isListeningForUserQuestion = false
                            isLocationRequested=false
                            requestLocationPermission()
                            startLocationUpdates()
                            if(x==0) {
                                getUserLocation()
                            }
                        }
                        else if(recognizedText.contains(Regex("\\bweather\\b"))){
                            isListeningForUserQuestion = false
                            isWeatherRequested=true
                            requestLocationPermission()
                            startLocationUpdates()
                            if(y==0) {
                                getUserLocation()
                            }
                        }

                        else {
                            stringOutput = ""
                            string = matches[0]

                            handleUserQuestion(string)
                        }
                    }

                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Implementation for onPartialResults
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Implementation for onEvent
            }


        })
        button2.setOnClickListener{
            z=1
            isSecondActivityRunning=true
            textToSpeech
            val Intent=Intent(this,MainActivity2::class.java)
            secondActivityLauncher.launch(Intent)
        }
        button3.setOnClickListener{

            isThirdActivityRunning=true

            val Intent=Intent(this,MainActivity3::class.java)
            thirdActivityLauncher.launch(Intent)

        }
    }


    private fun promptUserForQuestion() {
        textToSpeech!!.setSpeechRate(1.3f)
        textToSpeech!!.speak(
            "Hello, how may I assist you Today?",
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )

        // Set the flag to indicate that the chatbot is now waiting for the user's question
        isListeningForUserQuestion = true
    }

    private fun handleUserQuestion(string: String) {
        // Process the user's question using chatGPTModel
        chatGPTModel(string)

        // Reset the flag as the chatbot is no longer waiting for the user's question
        isListeningForUserQuestion = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show()
                    // Now, request location permission
                    requestLocationPermission()
                } else {
                    Toast.makeText(this, "Audio permission denied", Toast.LENGTH_SHORT).show()
                    // Permission denied by the user.
                }
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission()
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, "Location access not granted", Toast.LENGTH_SHORT).show()
                    // Permission denied by the user.
                }
            }

            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted.
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                    requestBluetoothPermission()

                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    // Permission denied by the user.
                }
            }
            BLUETOOTH_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted.
                    Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
                    // Permission denied by the user.
                }
            }
        }
    }

    private fun initializeSecondActivityLauncher() {
        secondActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Retrieve the boolean value sent from SecondActivity
//                textToSpeech!!.speak("second",TextToSpeech.QUEUE_FLUSH, null, null)
                    isSecondActivityRunning = result.data?.getBooleanExtra("booleanKey2", true) ?: true

                }
            }
    }


    private fun initializeThirdActivityLauncher() {
        thirdActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // ThirdActivity was closed successfully
                    // Retrieve the boolean value sent from ThirdActivity
                    isThirdActivityRunning = result.data?.getBooleanExtra("booleanKey", true) ?: true

                }
            }
    }


                private fun stopActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
//        finish()
    }

    private fun stopSecondActivityAsync() {
        if (isSecondActivityRunning) {
            CoroutineScope(Dispatchers.Main).launch {
                stopActivity()
                isSecondActivityRunning = false
            }
        }
    }

    private fun stopThirdActivityAsync() {
        if (isThirdActivityRunning) {
            CoroutineScope(Dispatchers.Main).launch {
                stopActivity()
                isThirdActivityRunning = false
            }
        }
    }
/*
    private fun stopSecondActivity() {
        if (isSecondActivityRunning) {
            // Create an intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            textToSpeech!!.speak("Exit", TextToSpeech.QUEUE_FLUSH, null, null)
            while(textToSpeech!!.isSpeaking){

            }
            // Finish MainActivity2
            finish()

            // Clear the flag to indicate that SecondActivity is no longer running
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            isSecondActivityRunning = false
        }
    }
    private fun stopThirdActivity() {
        if (isThirdActivityRunning) {
            // Create an intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            textToSpeech!!.speak("Exit", TextToSpeech.QUEUE_FLUSH, null, null)
            while(textToSpeech!!.isSpeaking){

            }
            // Finish MainActivity2
            finish()

            // Clear the flag to indicate that SecondActivity is no longer running
            Toast.makeText(this, "Exit", Toast.LENGTH_SHORT).show()
            isThirdActivityRunning = false
        }
    }

 */
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun requestBluetoothPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                BLUETOOTH_PERMISSION_CODE
            )
        }
    }


    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Check if location services are enabled before requesting updates
            if (isLocationEnabled()) {
                Toast.makeText(this,"Enabled",Toast.LENGTH_SHORT).show()

//                mindistance: 1f,
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // Minimum time interval between updates (in milliseconds)
                    1f,   // Minimum distance between updates (in meters)
                    locationListener
                )
            } else {
                // Location services are disabled; you can prompt the user to enable them
                Toast.makeText(this,"Disabled",Toast.LENGTH_SHORT).show()
                // Add your logic to prompt the user to enable location services or take other actions
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /*     LocationManager.GPS_PROVIDER,
                    1000, // Minimum time interval between updates (in milliseconds)
                    1f,   // Minimum distance between updates (in meters)
                    locationListener*/

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle the updated location

            if(!isLocationRequested) {
                isLocationRequested=true
                getAddressFromLocation(location.latitude, location.longitude)
                x=1
            }
            if(isWeatherRequested){
                getAddressFromLocation(location.latitude, location.longitude)
                y=1

            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
        }
    }

    private fun getUserLocation() {

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                // Use the location to get the address or perform other actions
                Toast.makeText(this,"Available",Toast.LENGTH_SHORT).show()

                getAddressFromLocation(location.latitude, location.longitude)
            } else {
                Toast.makeText(this,"Not available",Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {

        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare // Street name
                val city = address.locality // City
                val state = address.adminArea // State
                val postalCode = address.postalCode // Postal code
                val country = address.countryName // Country

               val fullAddress = "$street, $city, $state, $country"

                val latString = address.latitude.toString()
                val lonString = address.longitude.toString()

                if(isWeatherRequested) {
                    y=1
                    Log.d("Geocoding", "Latitude: $latString, Longitude: $lonString")
                    Toast.makeText(
                        this,
                        "Latitude: $latString, Longitude: $lonString",
                        Toast.LENGTH_SHORT
                    ).show()
                    getWeatherData(latString, lonString)
                }
               else{
                    x=1
                    textToSpeech?.speak(
                        "The current address is $fullAddress",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }

                // Respond to the user with the individual address components or perform other actions
        /*        textToSpeech!!.speak(
                    "Street: $street, City: $city, State: $state, Postal Code: $postalCode, Country: $country",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )*/
                // Respond to the user with the full address or perform other actions
            } else {
                Toast.makeText(this,"Location Not Available",Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getWeatherData(latitude:String,longitude:String) {
//            val latitude = "51.509865" // Replace with the actual latitude of London
//            val longitude = "-0.118092" // Replace with the actual longitude of London
        val tempUrl = "$url?lat=$latitude&lon=$longitude&appid=$appid"

        val stringRequest = StringRequest(
            Request.Method.GET,
            tempUrl,
            { response ->
                Log.d("response", response)

                try {
                    val jsonResponse = JSONObject(response)

                    // Extract temperature and weather details
                    val mainObject = jsonResponse.getJSONObject("main")
                    val temperatureKelvin = mainObject.getDouble("temp")
                    val temperatureCelsius = temperatureKelvin - 273.15
                    val weatherArray = jsonResponse.getJSONArray("weather")
                    val weatherObject = weatherArray.getJSONObject(0)
                    val weatherDescription = weatherObject.getString("description")

                    // Display the extracted information in tvResult
                    textToSpeech?.speak( "Temperature: ${String.format("%.1f", temperatureCelsius)}°C\nWeather: $weatherDescription", TextToSpeech.QUEUE_FLUSH, null, null)



//                    tvResult.text = "Weather Response:\n$response"

                } catch (e: JSONException) {
                    e.printStackTrace()
                    textToSpeech?.speak( "Error parsing response", TextToSpeech.QUEUE_FLUSH, null, null)
                    }
            },
            { error ->
                Toast.makeText(
                    applicationContext,
                    error.toString().trim(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        val requestQueue: RequestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(stringRequest)
        isWeatherRequested = false

    }

    private fun chatGPTModel(stringInput: String) {
  //      textView.setText("In Progress...")
        textToSpeech!!.speak("In Progress...", TextToSpeech.QUEUE_FLUSH, null, null)

        val jsonObject = JSONObject()
        try {
            jsonObject.put("model", "gpt-3.5-turbo")
            val jsonArrayMessage = JSONArray()
            val jsonObjectMessage = JSONObject()
            jsonObjectMessage.put("role", "user")
            jsonObjectMessage.put("content", stringInput)
            jsonArrayMessage.put(jsonObjectMessage)
            jsonObject.put("messages", jsonArrayMessage)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            stringURLEndPoint, jsonObject,
            Response.Listener<JSONObject> { response ->
                var stringText: String? = null
                stringText = try {
                    response.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
                stringOutput = stringOutput + stringText
         //       textView.text = stringOutput
                textToSpeech!!.speak(stringOutput, TextToSpeech.QUEUE_FLUSH, null, null)

            }, Response.ErrorListener { }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val mapHeader: MutableMap<String, String> = HashMap()
                mapHeader["Authorization"] = "Bearer $stringAPIKey"
                mapHeader["Content-Type"] = "application/json"
                return mapHeader
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                return super.parseNetworkResponse(response)
            }
        }
        val intTimeoutPeriod = 60000 // 60 seconds timeout duration defined
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(
            intTimeoutPeriod,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        jsonObjectRequest.setRetryPolicy(retryPolicy)
        Volley.newRequestQueue(applicationContext).add(jsonObjectRequest)
    }

    override fun onResume() {
        super.onResume()
        // Start a coroutine for continuous listening
        recognitionJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) { // Continue while the coroutine is active
                speechRecognizer.startListening(intent)
                delay(1000) // Adjust delay as needed
            }
        }
    }

    // Stop listening for voice input when the app is paused
    override fun onPause() {
        super.onPause()
        // Do not stop listening in onPause() to allow continued recognition in the background

    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the recognition job when the activity is destroyed
        recognitionJob?.cancel()
        speechRecognizer.destroy()
        locationManager.removeUpdates(locationListener)
        textToSpeech!!.stop()
        textToSpeech!!.shutdown()

    }

    override fun onBackPressed() {

        finishAffinity() // This will finish all activities in the task
    }

}

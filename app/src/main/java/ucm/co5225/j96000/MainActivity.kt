package ucm.co5225.j96000

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import ucm.co5225.j96000.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    //val API_URL = "https://ron-swanson-quotes.herokuapp.com/v2/quotes"
    val API_URL = "https://v6.exchangerate-api.com/v6/325b56c003ec0e19ce02de94/latest/GBP"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //setContentView(R.layout.activity_main)

        val currencies = resources.getStringArray(R.array.Currencies)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, currencies)
        val autoCompleteTV = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteTV.setAdapter(arrayAdapter)
    }

    fun getQuote(view: View) {
        val thread = Thread {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            //if (connection is HttpURLConnection) {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val scanner = Scanner(connection.inputStream)
                scanner.useDelimiter("\\A")
//                    var input = ""
//                    while (scanner.hasNext()) {
//                        input += scanner.next()
//                    }
                val jsonData = if (scanner.hasNext()) scanner.next() else ""
                val jsonArray = JSONObject(jsonData)
                val currencyConversions = JSONObject(jsonArray.getString("conversion_rates"))
                val currency = "USD"
                val quote = currencyConversions.getString("$currency")
                runOnUiThread {
                    binding.textView.text = quote
                }
            }
        }
        thread.start()
        //handleRetrieveQuote()
        //handleRetrieveQuoteWithVolley()
    }

    private fun handleRetrieveQuoteWithVolley() {
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, API_URL, null,
            { response -> binding.textView.text = parseJson(response.toString()) },
            { binding.textView.text = "That didn't work!" + ": $it"}
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleRetrieveQuote() {
        val thread = Thread {
            val url = URL(API_URL)

            try {
                val connection = url.openConnection()
                if (connection is HttpURLConnection) {
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.requestMethod = "GET"
                    connection.connect()

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val scanner = Scanner(connection.inputStream)
                        scanner.useDelimiter("\\A")
                        //                var input = ""
                        //                while (scanner.hasNext()) {
                        //                    input += scanner.next()
                        //                }
                        val jsonData = if (scanner.hasNext()) scanner.next() else ""

                        var quote = parseJson(jsonData)
                        updateTextBoxFromThread(quote)

                        //Log.e("MAIN_ACTIVITY", quote)
                    } else {
                        updateTextBoxFromThread("Sorry, there's a problem with the server")
                    }

                } else {
                    Log.wtf("MAIN_ACTIVITY", "Someone changed the API protocol")
                }
            } catch (e: IOException) {
                updateTextBoxFromThread("Sorry, there was an error processing the data")
            }
        }
        thread.start()
    }

    private fun parseJson(jsonData: String?): String {
        val jsonArray = JSONObject(jsonData)
        val item = "USD"
        
        //val conversionRates = jsonArray.getJSONObject().getString(item)
        //imports all the data into an array
        //now we need to get the data by a key value instead of just printing out the first one.
        // conversionRates = Response(list)
        //val quote = jsonArray("conversion_rates")
        //val quote = jsonArray.firstNotNullOf {item -> item.toString()}
        val quote = jsonArray.toString()
        return quote
    }

    private fun updateTextBoxFromThread(text: String) {
        runOnUiThread {
            binding.textView.text = text
        }
    }
}
package com.example.newrelictest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import kotlinx.coroutines.delay
import com.newrelic.agent.android.FeatureFlag
import com.newrelic.agent.android.NewRelic
import com.newrelic.agent.android.logging.LogLevel
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.example.newrelictest.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var testLoggingActive = true
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "NewRelicTestPrefs"
        private const val KEY_LOG_COUNTER = "log_counter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Trigger NPE - Null Pointer Exception", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
            lifecycleScope.launch {
                delay(3000)
                simulateNPE()
            }
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initNewrelic()

        lifecycleScope.launch {
            var counter = loadCounter()
            while (true) {
                val ms = Random.nextInt(10, 100)
                delay(ms.milliseconds)
                if (!testLoggingActive) {
                    continue
                }
                val charCount = Random.nextInt(50, 500)
                val message = "$counter: foobar ${"_".repeat(charCount)}"
                android.util.Log.i("nr_test", message)
                logToNR(message)
                counter++
                saveCounter(counter)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun initNewrelic() {
        NewRelic
            .withApplicationToken(BuildConfig.NEW_RELIC_TOKEN)
            .withDeviceID("test-device-id")
            .withCrashReportingEnabled(true)
            .withLoggingEnabled(true)
            .start(this)
        NewRelic.disableFeature(FeatureFlag.InteractionTracing)
        NewRelic.disableFeature(FeatureFlag.Jetpack)
        NewRelic.disableFeature(FeatureFlag.DistributedTracing)
        NewRelic.disableFeature(FeatureFlag.HttpResponseBodyCapture)
        NewRelic.disableFeature(FeatureFlag.NetworkRequests)
        NewRelic.disableFeature(FeatureFlag.NetworkErrorRequests)
        NewRelic.enableFeature(FeatureFlag.OfflineStorage)
        NewRelic.enableFeature(FeatureFlag.BackgroundReporting)
    }

    private fun logToNR(message: String) {
        val attributes = mapOf(
            "level" to "INFO",
            "message" to message,
        )
        NewRelic.logAttributes(attributes as Map<String, Any>?)
    }

    private fun simulateNPE() {
        android.util.Log.i("NewRelicTest", "simulateNPE 123")
        android.util.Log.i("NewRelicTest", "simulateNPE 456")
        val str: String? = null
        str!!.length // Force NPE
    }

    private fun simulateOOM() {
        val list = mutableListOf<ByteArray>()
        while (true) {
            list.add(ByteArray(10 * 1024 * 1024)) // Allocate 10MB each time
        }
    }

    fun toggleLogging() {
        testLoggingActive = !testLoggingActive
    }

    fun isLoggingActive(): Boolean {
        return testLoggingActive
    }

    private fun loadCounter(): Int {
        return sharedPreferences.getInt(KEY_LOG_COUNTER, 1)
    }

    private fun saveCounter(counter: Int) {
        sharedPreferences.edit()
            .putInt(KEY_LOG_COUNTER, counter)
            .apply()
    }
}
package de.benehmb.ev3bluetoothcontroller

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val typeOfConnection = prefs.getInt(getString(R.string.connection_file_key), 0)

        val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayOf("Android", "Mikrocontroller")
        )
        connectionType.adapter = adapter
        connectionType.setSelection(typeOfConnection)

        connectionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                prefs.edit().putInt(getString(R.string.connection_file_key), position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val typeOfCenterDisplay = prefs.getInt(getString(R.string.center_display_file_key), 0)

        val displays = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayOf("Incoming text", "Proximity sensors")
        )
        displayCenter.adapter = displays
        displayCenter.setSelection(typeOfCenterDisplay)

        displayCenter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                prefs.edit().putInt(getString(R.string.center_display_file_key), position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnApply.setOnClickListener {
            finish()
        }



    }
}


package de.benehmb.ev3bluetoothcontroller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val connectionTypes = arrayOf("Android", "Mikrocontroller")

        val adapter = ArrayAdapter(
                this, // Context
                android.R.layout.simple_spinner_item, // Layout
                connectionTypes // Array
        )
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        connectionType.adapter = adapter

        connectionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Display the selected item text on text view
                print("Spinner selected : ${parent.getItemAtPosition(position)}")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        btnApply.setOnClickListener {
            super.finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //save Settings
    }
}


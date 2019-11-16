package de.benehmb.ev3bluetoothcontroller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import kotlinx.android.synthetic.main.activity_main.*


private const val REQUEST_ENABLE_BT = 9274

class MainActivity : Activity(), OnSeekBarChangeListener {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val hasBluetoothAdapter = bluetoothAdapter != null
    private lateinit var bluetooth: BluetoothSPP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetooth = BluetoothSPP(this)
        enableBluetooth()

        incomeing.movementMethod = ScrollingMovementMethod()

        reconnect.setOnClickListener {
            bluetooth.disconnect()
            bluetooth.stopService()
            chooseDevice()
        }

        reset.setOnClickListener {
            resetControls()
        }

        bluetooth.setBluetoothStateListener {
            state.text = getString(when (it) {
                BluetoothState.STATE_CONNECTING -> R.string.state_connecting
                BluetoothState.STATE_CONNECTED -> R.string.state_connected
                BluetoothState.STATE_LISTEN -> R.string.state_listening
                BluetoothState.STATE_NONE -> R.string.state_none
                else -> R.string.state_unknown
            })
            if (it == BluetoothState.STATE_CONNECTED) {
                state.append(" to " + bluetooth.connectedDeviceName)
            }

        }

        controlLeft.setOnSeekBarChangeListener(this)
        controlLeft.minSeekBarProgress = resources.getInteger(R.integer.max_range)
        progressLeft.text = controlLeft.seekBarProgress.toString()
        controlRight.setOnSeekBarChangeListener(this)
        controlRight.minSeekBarProgress = resources.getInteger(R.integer.max_range)
        progressRight.text = controlLeft.seekBarProgress.toString()

        bluetooth.setOnDataReceivedListener { _, message ->
            incomeing.append(message + "\n")
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this.bluetooth.isBluetoothEnabled) {
            resetControls()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == RESULT_OK) {
                bluetooth.connect(data)
            }
        } else if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is up and running, we're good to go
                chooseDevice()
            } else {
                // Bluetooth is not enabled. We'll just send another request
                // Please don't do this in actual production releases
                enableBluetooth()
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (seekBar is VerticalSeekBar && fromUser) { // smart cast
            if (sync.isChecked) {
                controlLeft.seekBarProgress = progress
                controlRight.seekBarProgress = progress
            }

            progressLeft.text = controlLeft.seekBarProgress.toString()
            progressRight.text = controlRight.seekBarProgress.toString()



            sendData("${controlLeft.seekBarProgress}.${controlRight.seekBarProgress}")
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    private fun sendData(data: String) {
        bluetooth.send(data, true)
    }

    private fun resetControls() {
        controlLeft.seekBarProgress = 0
        controlRight.seekBarProgress = 0
        onProgressChanged(controlLeft, 0, true)
    }

    private fun chooseDevice() {
        bluetooth.setupService()
        //bluetooth.startService(BluetoothState.DEVICE_OTHER)
        bluetooth.startService(BluetoothState.DEVICE_ANDROID)

        val intent = Intent(applicationContext, DeviceList::class.java)
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
    }

    private fun enableBluetooth() {
        if (hasBluetoothAdapter) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                chooseDevice()
            }
        } else {
            // Device doesn't support bluetooth
            Toast.makeText(this, "No bluetooth adapter found!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

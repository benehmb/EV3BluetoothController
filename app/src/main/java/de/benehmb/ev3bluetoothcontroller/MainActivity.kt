package de.benehmb.ev3bluetoothcontroller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.car_proximaty.*

private const val REQUEST_ENABLE_BT = 9274
private const val REQUEST_CODE_SETTINGS = 5234

class MainActivity : Activity(), OnSeekBarChangeListener {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val hasBluetoothAdapter = bluetoothAdapter != null
    private lateinit var bluetooth: BluetoothSPP
    private var compareTypeOfConnection = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        compareTypeOfConnection = prefs.getInt(this.getString(R.string.connection_file_key), 0)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetooth = BluetoothSPP(this)
        enableBluetooth()

        incoming.movementMethod = ScrollingMovementMethod()

        if (prefs.getInt(this.getString(R.string.center_display_file_key), 0) == 0){
            incoming.visibility = View.VISIBLE
            carProximatyDisplay.visibility = View.INVISIBLE
        }else if(prefs.getInt(this.getString(R.string.center_display_file_key), 0) == 1){
            incoming.visibility = View.INVISIBLE
            carProximatyDisplay.visibility = View.VISIBLE
        }

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
            incoming.append(message + "\n")
            if(prefs.getInt(this.getString(R.string.center_display_file_key), 0) == 1){
                try {
                    val position = message.indexOf('.')
                    proximityBack.progress = Integer.parseInt(message.substring(0, position))
                    proximityFront.progress = Integer.parseInt(message.substring(position + 1))
                }catch (ex:Exception){
                    when(ex){
                        is NumberFormatException,
                        is StringIndexOutOfBoundsException -> {
                            Toast.makeText(this, "Error while receiving sensor data", Toast.LENGTH_SHORT).show()
                        } else -> throw ex
                    }

                }
            }
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
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

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        compareTypeOfConnection = prefs.getInt(this.getString(R.string.connection_file_key), 0)

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
        } else if (requestCode == REQUEST_CODE_SETTINGS) {
            if(compareTypeOfConnection == prefs.getInt(this.getString(R.string.connection_file_key), 0)) {
                compareTypeOfConnection = prefs.getInt(this.getString(R.string.connection_file_key), 0)
                bluetooth.disconnect()
                bluetooth.stopService()
                chooseDevice()
            }
            if (prefs.getInt(this.getString(R.string.center_display_file_key), 0) == 0){
                incoming.visibility = View.VISIBLE
                carProximatyDisplay.visibility = View.INVISIBLE
            }else if(prefs.getInt(this.getString(R.string.center_display_file_key), 0) == 1){
                incoming.visibility = View.INVISIBLE
                carProximatyDisplay.visibility = View.VISIBLE
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val typeOfConnection = prefs.getInt(getString(R.string.connection_file_key), 0)
        if (typeOfConnection == 0) {
            bluetooth.startService(BluetoothState.DEVICE_ANDROID)
        } else if (typeOfConnection == 1) {
            bluetooth.startService(BluetoothState.DEVICE_OTHER)
        }

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

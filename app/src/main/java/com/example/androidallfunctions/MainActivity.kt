package com.example.androidallfunctions
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.database.ContentObserver
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.location.LocationManager
import android.media.AudioManager
import android.media.AudioManager.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.Global.AIRPLANE_MODE_ON
import android.text.format.DateFormat
import android.util.Rational
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.androidallfunctions.databinding.ActivityMainBinding
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    val vm: MainActivityVM by viewModels()

    /** Ringer Mode **/
    val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            initRingerModeButton()
        }
    }
    val table = mapOf(
        RINGER_MODE_SILENT to R.drawable.silent,
        RINGER_MODE_VIBRATE to R.drawable.vibration,
        RINGER_MODE_NORMAL to R.drawable.sound,
    )
    val indexes = table.keys.toList()

    override fun onResume() {
        super.onResume()
        if (permissionManager.checkSystemWriteSettings()) {
            initLightnessSeekBar()
        }
        /** Ringer Mode **/
        if (permissionManager.checkNotificationAccess()) {
            registerReceiver(ringerModeReceiver, IntentFilter(RINGER_MODE_CHANGED_ACTION))
            initRingerModeButton()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        /** Normal Permission Checking **/
        permissionManager.checkNormalPermissions()
        /** Brightness/Autorotate **/
        if (permissionManager.checkSystemWriteSettings()) {
            initLightnessSeekBar()
        }
        /** Ringer Mode **/
        if (permissionManager.checkNotificationAccess()) {
            registerReceiver(ringerModeReceiver, IntentFilter(RINGER_MODE_CHANGED_ACTION))
            initRingerModeButton()
        }
        /** Volume **/
        initVolumeSeekBar()
        /** Wifi **/
        binding.wifiBtn.setOnClickListener {
            val wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            wifi.isWifiEnabled = !wifi.isWifiEnabled
        }
        /** Bluetooth **/
        binding.bluetoothBtn.setOnClickListener {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.testbg))
//            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//            mBluetoothAdapter.run {
//                if (isEnabled) disable() else enable()
//            }
        }
        /** Flashlight **/
        binding.flashlightBtn.setOnClickListener {
            val torchCallback = object : TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    vm.isTorchON = enabled
                }
            }
            val camManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = camManager.cameraIdList[0]
            try {
                camManager.setTorchMode(cameraId, !vm.isTorchON) //Turn ON
                camManager.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
        /** Autorotate **/
        vm.isAutoRotateON =
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        binding.autorotateBtn.setOnClickListener {
            Settings.System.putInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                (++vm.isAutoRotateON) % 2
            )
        }
        /** Flight Mode (Only allow read value since android 4.2)**/
        binding.flightModeBtn.setOnClickListener {
            val result = Settings.Global.getInt(contentResolver, AIRPLANE_MODE_ON) != 0
            Snackbar.make(
                binding.root,
                "Flight mode is ${if (result) "ON" else "OFF"}",
                Snackbar.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            startActivity(intent)
        }
        /** Mobile Data/Data Roaming/4G **/
        binding.mobileDataBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            startActivity(intent)
        }
        /** Screenshot **/
        binding.screenshotBtn.setOnClickListener {
            takeScreenshot()
        }
        /** Location **/
        binding.locationBtn.setOnClickListener {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (lm.isLocationEnabled)
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            else
                displayLocationSettingsRequest()
        }
        /** NFC **/
        binding.nfcBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
        /** Do Not Disturb **/
        binding.doNotDisturbBtn.setOnClickListener {
            if (permissionManager.checkNotificationAccess()){
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_NONE)
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                else {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        }
        binding.pipBtn.setOnClickListener {
            pipMode(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterContentObservers()
        unregisterBroadcastReceivers()
    }

    fun unregisterBroadcastReceivers() {
        unregisterReceiver(ringerModeReceiver)
    }

    private fun unregisterContentObservers() {
        /** Brightness/Autorotate **/
        contentResolver.unregisterContentObserver(mBrightnessObserver)
        contentResolver.unregisterContentObserver(mVolumeObserver)
    }

    /** Ringer Mode **/
    private fun initRingerModeButton() {
        var currentMode = indexes.indexOf(audioManager.ringerMode)
        binding.ringerModeBtn.run {
            visibility = View.VISIBLE
            setImageDrawable(ContextCompat.getDrawable(this@MainActivity, table[currentMode]!!))
            setOnClickListener {
                currentMode = (currentMode + 1) % 3
                audioManager.ringerMode = indexes.indexOf(currentMode)
                setImageDrawable(ContextCompat.getDrawable(this@MainActivity, table[currentMode]!!))
            }
        }
    }



    /** Brightness **/
    val brightnessSetting: Uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
    val brightnessSeekbarProgressListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            Settings.System.putInt(
                applicationContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (progress / 100f * 255f).toInt()
            )
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            contentResolver.unregisterContentObserver(mBrightnessObserver)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            contentResolver.registerContentObserver(brightnessSetting, false, mBrightnessObserver)
        }
    }

    val mBrightnessObserver: ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val brightness =
                    Settings.System.getInt(
                        applicationContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0
                    )
                binding.brightnessSeekbar.setOnSeekBarChangeListener(null)
                binding.brightnessSeekbar.progress = (brightness / 255f * 100f).toInt()
                binding.brightnessSeekbar.setOnSeekBarChangeListener(
                    brightnessSeekbarProgressListener
                )
            }
        }

    private fun initLightnessSeekBar() {
        contentResolver.registerContentObserver(brightnessSetting, false, mBrightnessObserver)
        binding.brightnessSeekbar.run {
            progress = (Settings.System.getInt(
                applicationContext.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            ) / 255f * 100f).toInt()
            setOnSeekBarChangeListener(brightnessSeekbarProgressListener)
        }
    }
    /** Brightness **/

    /** Volume **/
    val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val volumeSeekbarProgressListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            audioManager.setStreamVolume(STREAM_MUSIC, progress, 0)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    val mVolumeObserver: ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                binding.volumeSeekbar.progress =
                    audioManager.getStreamVolume(STREAM_MUSIC)
            }
        }

    private fun initVolumeSeekBar() {
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mVolumeObserver)
        volumeControlStream = AudioManager.STREAM_MUSIC
        binding.volumeSeekbar.run {
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            setOnSeekBarChangeListener(volumeSeekbarProgressListener)
        }
    }
    /** Volume **/

    /** Screenshot **/
    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        // image naming and path to include sd card appending name you choose for file
        val mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
        // create bitmap screen capture
        val v1: View = window.decorView.rootView
        v1.isDrawingCacheEnabled = true
        val bitmap: Bitmap = Bitmap.createBitmap(v1.drawingCache)
        v1.isDrawingCacheEnabled = false
        val imageFile = File(mPath)
        val outputStream = FileOutputStream(imageFile)
        val quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.flush()
        outputStream.close()
        // Save Bitmap to gallery
        MediaStore.Images.Media.insertImage(contentResolver, bitmap, "$now.jpg", "$now.jpg");
        openScreenshot(imageFile)
    }

    private fun openScreenshot(imageFile: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri = FileProvider.getUriForFile(
            applicationContext,
            applicationContext.packageName + ".provider",
            imageFile
        );
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
    /** Screenshot **/

    /** Location **/
    private fun displayLocationSettingsRequest() {
        val googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback(object : ResultCallback<LocationSettingsResult> {
            override fun onResult(result: LocationSettingsResult) {
                val status: Status = result.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {}
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(this@MainActivity, 1)
                    } catch (e: SendIntentException) {
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }
    /** Location **/

    /** PIP Mode **/
    private fun pipMode(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            val aspectRatio = Rational(4, 3)
            val pipBuilder = PictureInPictureParams.Builder()
            pipBuilder.setAspectRatio(aspectRatio).build()
            enterPictureInPictureMode(pipBuilder.build())
        }
    }
    /** PIP Mode **/
}

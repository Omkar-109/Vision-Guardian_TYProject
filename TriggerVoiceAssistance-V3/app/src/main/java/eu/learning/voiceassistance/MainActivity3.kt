package eu.learning.voiceassistance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import eu.learning.voiceassistance.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import java.util.*

class MainActivity3 : AppCompatActivity(), TextToSpeech.OnInitListener {


    lateinit var labels:List<String>
    var colors = listOf<Int>(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
        Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED)
    val paint = Paint()
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap:Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var model:SsdMobilenetV11Metadata1
    private lateinit var textToSpeech: TextToSpeech
    private var lastSpeechTime: Long = 0 // Declaration added here


    override fun onCreate(savedInstanceState: Bundle?) {

        getWindow(). addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        get_permission()
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = SsdMobilenetV11Metadata1.newInstance(this)
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)
        //imageView.visibility = View.INVISIBLE

        textureView = findViewById(R.id.textureView)


        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            // ...


            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                bitmap = textureView.bitmap!!
                var image = TensorImage.fromBitmap(bitmap)
                image = imageProcessor.process(image)

                val outputs = model.process(image)
                val locations = outputs.locationsAsTensorBuffer.floatArray
                val classes = outputs.classesAsTensorBuffer.floatArray
                val scores = outputs.scoresAsTensorBuffer.floatArray

                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)

                val h = mutable.height
                val w = mutable.width
                paint.textSize = h / 15f
                paint.strokeWidth = h / 85f

                var maxScore = 0.0
                var maxIndex = -1

                scores.forEachIndexed { index, fl ->
                    if (fl > maxScore) {
                        maxScore = fl.toDouble()
                        maxIndex = index
                    }
                }

                if (maxIndex != -1 && maxScore > 0.65) {
                    val detectedObject = labels[classes[maxIndex].toInt()]
                    val speechText = "$detectedObject detected "

                    val currentTime = System.currentTimeMillis()
                    val timeDifference = currentTime - lastSpeechTime

                    if (timeDifference > 3000) { // Check if 3 seconds have passed since the last speech
                        textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                        lastSpeechTime = currentTime
                    }
                    val x = maxIndex * 4
                    paint.setColor(colors.get(maxIndex))
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        RectF(
                            locations.get(x + 1) * w,
                            locations.get(x) * h,
                            locations.get(x + 3) * w,
                            locations.get(x + 2) * h
                        ),
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    canvas.drawText(
                        labels.get(classes.get(maxIndex).toInt()) + " " + maxScore.toString(),
                        locations.get(x + 1) * w,
                        locations.get(x) * h,
                        paint
                    )
                }

                imageView.setImageBitmap(mutable)
            }

        }

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        textToSpeech = TextToSpeech(this, this)



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                // Handle back button press
                val resultIntent = Intent()
                resultIntent.putExtra("booleanKey", false) // Put your boolean value here
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        })

    }



    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language initialization error
            }
        } else {
            // Handle TTS initialization error
        }
    }


    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                var surfaceTexture = textureView.surfaceTexture
                var surface = Surface(surfaceTexture)

                var captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {

            }

            override fun onError(p0: CameraDevice, p1: Int) {

            }
        }, handler)
    }

    fun get_permission(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
            get_permission()
        }
    }


}
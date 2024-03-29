package com.example.studypal
import android.app.Application
import android.content.Context
import org.webrtc.*

class RTCClient(
    context: Application,
    private val localVideoOutput: SurfaceViewRenderer?) {

    private val rootEglBase: EglBase = EglBase.create()

    init {
        initPeerConnectionFactory(context)
        initSurfaceView()
    }

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapturer(context) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")   // H264 video format
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true    // maybe not?
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    // fetch the front facing camera
    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun initSurfaceView() {
        localVideoOutput?.setMirror(true)
        localVideoOutput?.setEnableHardwareScaler(true)

        localVideoOutput?.init(rootEglBase.eglBaseContext, null)
        localVideoOutput?.setZOrderMediaOverlay(true)
    }

    fun startLocalVideoCapture() {
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(surfaceTextureHelper, localVideoOutput!!.context, localVideoSource.capturerObserver)

        // width, height, frame per second
        videoCapturer.startCapture(320, 240, 60)

        // isScreencast=false in localVideoSource
        val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack.addSink(localVideoOutput)
    }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
    }
}
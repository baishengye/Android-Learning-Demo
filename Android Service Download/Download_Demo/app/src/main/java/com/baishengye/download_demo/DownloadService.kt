package com.baishengye.download_demo

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


/**
 * 用于下载的Service
 */
class DownloadService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private val mBinder: DownloadBinder = DownloadBinder()
    private var downLoadJob: Job? = null //要通过服务来下载，当然要在服务中创建下载任务并执行。
    private var downloadUrl: String? = null
    private var downloadFilePath:String? = null

    //创建一个下载的监听
    private val listener: DownloadListener = object : DownloadListener {
        //通知进度
        override fun onProgress(progress: Int) {
            //下载过程中不停更新进度
            notificationManager!!.notify(1, getNotification("正在下载...", progress))
        }

        //下载成功
        override fun onSuccess() {
            downLoadJob = null
            notificationManager!!.notify(1, getNotification("下载成功！", 0))
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true)
        }

        //下载失败
        override fun onFailed() {
            downLoadJob = null
            notificationManager!!.notify(1, getNotification("下载失败！", 0))
            //下载失败时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true)
        }

        //取消下载
        override fun onCanceled() {
            downLoadJob = null
            stopForeground(true)
        }
    }

    /**
     * 代理对象：在这里面添加三个方法：
     * 开始下载，暂停下载，取消下载
     * 就可以在Activity中绑定Service，并控制Service来实现下载功能
     */
    internal inner class DownloadBinder : Binder() {
        //开始下载，在Activity中提供下载的地址
        @OptIn(DelicateCoroutinesApi::class)
        fun startDownload(url: String, filePath:String) {
            DownloadUtils.instance.setDownloadListener(listener)
            if (downLoadJob == null) {
                downLoadJob = GlobalScope.launch {
                    DownloadUtils.instance.startDownload(url, filePath)
                }
                downLoadJob!!.start()
                downloadUrl = url
                downloadFilePath = filePath
                startForeground(1, getNotification("正在下载...", 0)) //开启前台通知
            }
        }

        //取消下载
        fun canceledDownload() {
            if (downLoadJob != null&&downLoadJob!!.isActive) {
                downLoadJob!!.cancel()
                listener.onCanceled()
            }
            if (downloadUrl != null&&downloadFilePath != null) {
                //取消下载时需要将下载的文件删除  并将通知关闭
                val file = File(downloadFilePath!!)
                if (file.exists()) {
                    file.delete()
                }
                notificationManager!!.cancel(1)
                stopForeground(true)
            }
        }
    }

    private fun getNotification(title: String, progress: Int): Notification {
        val channelName = BuildConfig.APPLICATION_ID
        val channelId = BuildConfig.APPLICATION_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager!!.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this,channelId)
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, android.R.drawable.ic_lock_idle_alarm))
        builder.setContentIntent(pendingIntent)
        builder.setContentTitle(title)
        if (progress >= 0) {
            builder.setContentText("$progress%")
            builder.setProgress(100, progress, false) //最大进度。当前进度。是否使用模糊进度
        }
        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DownloadTest","Service#onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DownloadTest","Service#onStartCommand")
        notificationManager = NotificationManagerCompat.from(this@DownloadService.applicationContext)
        startForeground(1, getNotification("等待中", 0)) //开启前台通知
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("DownloadTest","Service#onUnbind")
        mBinder.canceledDownload()

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d("DownloadTest","Service#onDestroy")
        super.onDestroy()
    }

    //获取通知管理器
    private var notificationManager: NotificationManagerCompat ?= null
}

package com.baishengye.download_demo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import com.baishengye.download_demo.databinding.ActivityMainBinding
import com.baishengye.libbase.base.BaseViewBindingActivity
import com.baishengye.libutil.utils.FolderUtils

class MainActivity : BaseViewBindingActivity<ActivityMainBinding>(){
    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    private var downloadBinder:DownloadService.DownloadBinder ?= null
    private var downloadInfo:DownloadInfo? = null
    private val connect = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
           downloadBinder = service as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, DownloadService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent,connect, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(this, DownloadService::class.java)
        unbindService(connect)
        stopService(intent)
    }

    override fun initViews() {
    }

    private fun startDownload() {
        downloadBinder?.startDownload(downloadInfo!!.url,downloadInfo!!.filePath)
    }

    private fun cancelDownload(){
        downloadBinder?.canceledDownload()
    }

    override fun initData() {
        downloadInfo = DownloadInfo("凄美地", "http://www.kumeiwp.com/sub/filestores/2022/02/23/413e4049846d25d4f9813ad6913e2822.mp3", IDLE, 0,
            "${FolderUtils.getAudioFolderPath(this)}凄美地.mp3")

        binding.tvTitle.text = downloadInfo!!.title
        binding.tvUrl.text = downloadInfo!!.url
    }

    override fun initListeners() {
        binding.btnStart.setOnClickListener {
            startDownload()
        }
        binding.btnCancel.setOnClickListener {
            cancelDownload()
        }
    }
}
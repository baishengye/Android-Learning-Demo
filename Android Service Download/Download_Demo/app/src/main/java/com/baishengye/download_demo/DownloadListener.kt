package com.baishengye.download_demo

/**
 * 回调接口，对下载状态进行监听
 */
interface DownloadListener {
    fun onProgress(progress: Int) //通知当前下载进度
    fun onSuccess() //下载成功
    fun onFailed() //失败
    fun onCanceled() //取消下载
}

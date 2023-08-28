package com.baishengye.download_demo

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class DownloadUtils private constructor() {
    companion object{
        var instance = DownloadUtils()
    }
    private var mOkHttpClient:OkHttpClient? = null

    init {
        //设置连接超时等属性,不设置可能会报异常
        mOkHttpClient = OkHttpClient.Builder()
            //设置连接超时等属性,不设置可能会报异常
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private var mDownloadListener:DownloadListener? = null
    fun setDownloadListener(listener:DownloadListener){
        mDownloadListener = listener
    }

    private var mCall:Call? = null
    private var mUrl:String? = null
    private var mFilePath:String? = null

    /**
     * 开始下载*/
    fun startDownload(url:String,filePath:String){
        mUrl = url
        mFilePath = filePath
        val request: Request = Request.Builder().url(url).build()
        //可以在这里自定义路径
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
        mCall = mOkHttpClient!!.newBuilder().addInterceptor(Interceptor { chain ->
            val originalResponse: Response = chain.proceed(chain.request())
            originalResponse.newBuilder().body(
                ProgressResponseBody(originalResponse.body!!, object : ProgressResponseBody.ProgressListener{
                    override fun onProgressUpdate(progress: Int) {
                        mDownloadListener?.onProgress(progress)
                    }
                })
            ).build()
        }).build().newCall(request)
        mCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("DownloadTest",e.toString())
                mDownloadListener?.onFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DownloadTest","start")
                try {
                    //将返回结果转化为流，并写入文件
                    var len: Int
                    val buf = ByteArray(2048)
                    val inputStream: InputStream = response.body!!.byteStream()
                    val fileOutputStream = FileOutputStream(file)
                    while (inputStream.read(buf).also { len = it } != -1) {
                        fileOutputStream.write(buf, 0, len)
                    }
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    inputStream.close()
                    mDownloadListener?.onSuccess()
                }catch (e:IOException){
                    mDownloadListener?.onFailed()
                }catch (e:Exception){
                    mDownloadListener?.onFailed()
                }
            }
        })
    }

    fun cancelDownload(){
        if(mCall!=null&&!mCall!!.isCanceled()){
            mCall!!.cancel()
        }

        if(mFilePath!=null){
            val file = File(mFilePath!!)
            if(file.exists()){
                file.delete()
            }
        }

        mCall = null
        mUrl = null
        mFilePath = null
    }

}
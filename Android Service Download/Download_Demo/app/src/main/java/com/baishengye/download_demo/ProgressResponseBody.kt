package com.baishengye.download_demo

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener?
) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    private val mProgressPoster: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            progressListener?.onProgressUpdate(msg.what)
        }
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                //                progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                Log.d("DownloadTest","progress:${(100 * totalBytesRead / responseBody.contentLength()).toInt()}")
                mProgressPoster.sendEmptyMessage((100 * totalBytesRead / responseBody.contentLength()).toInt())
                return bytesRead
            }
        }
    }

    interface ProgressListener {
        //        void update(long bytesRead, long contentLength, boolean done);
        fun onProgressUpdate(progress: Int)
    }
}
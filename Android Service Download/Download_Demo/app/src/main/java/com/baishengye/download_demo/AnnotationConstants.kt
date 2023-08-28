package com.baishengye.download_demo

import androidx.annotation.IntDef


const val IDLE = 0
const val DOWNLOADING = 1
const val SUCCESS = 2
const val FAIL = 3

@IntDef(IDLE, DOWNLOADING, SUCCESS, FAIL)
@Retention(AnnotationRetention.SOURCE)
annotation class DownloadState
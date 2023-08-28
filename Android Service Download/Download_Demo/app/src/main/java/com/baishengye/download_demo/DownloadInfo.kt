package com.baishengye.download_demo

data class DownloadInfo(
    var title: String = "",
    var url: String = "",
    @DownloadState var downloadState: Int = IDLE,
    var progress:Int = 0,
    var filePath:String = ""
)
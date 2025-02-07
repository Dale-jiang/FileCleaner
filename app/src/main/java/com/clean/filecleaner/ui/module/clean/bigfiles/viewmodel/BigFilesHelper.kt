package com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel

import com.clean.filecleaner.ui.module.clean.bigfiles.FileTypes

object BigFilesHelper {

    private val fileTypesList: Map<String, FileTypes> = mapOf(
        "application/vnd.android.package-archive" to FileTypes.TYPE_APK,
        "application/ogg" to FileTypes.TYPE_AUDIO,
        "application/x-flac" to FileTypes.TYPE_AUDIO,
        "application/pgp-keys" to FileTypes.TYPE_CERTIFICATE,
        "application/pgp-signature" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs12" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs7-certreqresp" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs7-crl" to FileTypes.TYPE_CERTIFICATE,
        "application/x-x509-ca-cert" to FileTypes.TYPE_CERTIFICATE,
        "application/x-x509-user-cert" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs7-certificates" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs7-mime" to FileTypes.TYPE_CERTIFICATE,
        "application/x-pkcs7-signature" to FileTypes.TYPE_CERTIFICATE,
        "application/rdf+xml" to FileTypes.TYPE_SOURCE_CODE,
        "application/rss+xml" to FileTypes.TYPE_SOURCE_CODE,
        "application/x-object" to FileTypes.TYPE_SOURCE_CODE,
        "application/xhtml+xml" to FileTypes.TYPE_SOURCE_CODE,
        "text/css" to FileTypes.TYPE_SOURCE_CODE,
        "text/html" to FileTypes.TYPE_SOURCE_CODE,
        "text/xml" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-c++hdr" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-c++src" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-chdr" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-csrc" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-dsrc" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-csh" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-haskell" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-java" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-literate-haskell" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-pascal" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-tcl" to FileTypes.TYPE_SOURCE_CODE,
        "text/x-tex" to FileTypes.TYPE_SOURCE_CODE,
        "application/x-latex" to FileTypes.TYPE_SOURCE_CODE,
        "application/x-texinfo" to FileTypes.TYPE_SOURCE_CODE,
        "application/atom+xml" to FileTypes.TYPE_SOURCE_CODE,
        "application/ecmascript" to FileTypes.TYPE_SOURCE_CODE,
        "application/json" to FileTypes.TYPE_SOURCE_CODE,
        "application/javascript" to FileTypes.TYPE_SOURCE_CODE,
        "application/xml" to FileTypes.TYPE_SOURCE_CODE,
        "text/javascript" to FileTypes.TYPE_SOURCE_CODE,
        "application/x-javascript" to FileTypes.TYPE_SOURCE_CODE,
        "application/mac-binhex40" to FileTypes.TYPE_ARCHIVES,
        "application/rar" to FileTypes.TYPE_ARCHIVES,
        "application/zip" to FileTypes.TYPE_ARCHIVES,
        "application/x-apple-diskimage" to FileTypes.TYPE_ARCHIVES,
        "application/x-debian-package" to FileTypes.TYPE_ARCHIVES,
        "application/x-gtar" to FileTypes.TYPE_ARCHIVES,
        "application/x-iso9660-image" to FileTypes.TYPE_ARCHIVES,
        "application/x-lha" to FileTypes.TYPE_ARCHIVES,
        "application/x-lzh" to FileTypes.TYPE_ARCHIVES,
        "application/x-lzx" to FileTypes.TYPE_ARCHIVES,
        "application/x-stuffit" to FileTypes.TYPE_ARCHIVES,
        "application/x-tar" to FileTypes.TYPE_ARCHIVES,
        "application/x-webarchive" to FileTypes.TYPE_ARCHIVES,
        "application/x-webarchive-xml" to FileTypes.TYPE_ARCHIVES,
        "application/gzip" to FileTypes.TYPE_ARCHIVES,
        "application/x-7z-compressed" to FileTypes.TYPE_ARCHIVES,
        "application/x-deb" to FileTypes.TYPE_ARCHIVES,
        "application/x-rar-compressed" to FileTypes.TYPE_ARCHIVES,
        "text/x-vcard" to FileTypes.TYPE_CONTACT,
        "text/vcard" to FileTypes.TYPE_CONTACT,
        "text/calendar" to FileTypes.TYPE_EVENT,
        "text/x-vcalendar" to FileTypes.TYPE_EVENT,
        "application/x-font" to FileTypes.TYPE_FONT,
        "application/font-woff" to FileTypes.TYPE_FONT,
        "application/x-font-woff" to FileTypes.TYPE_FONT,
        "application/x-font-ttf" to FileTypes.TYPE_FONT,
        "application/vnd.oasis.opendocument.graphics" to FileTypes.TYPE_IMAGE,
        "application/vnd.oasis.opendocument.graphics-template" to FileTypes.TYPE_IMAGE,
        "application/vnd.oasis.opendocument.image" to FileTypes.TYPE_IMAGE,
        "application/vnd.stardivision.draw" to FileTypes.TYPE_IMAGE,
        "application/vnd.sun.xml.draw" to FileTypes.TYPE_IMAGE,
        "application/vnd.sun.xml.draw.template" to FileTypes.TYPE_IMAGE,
        "application/pdf" to FileTypes.TYPE_PDF,
        "application/vnd.oasis.opendocument.text" to FileTypes.TYPE_TEXT,
        "application/vnd.oasis.opendocument.text-master" to FileTypes.TYPE_TEXT,
        "application/vnd.oasis.opendocument.text-template" to FileTypes.TYPE_TEXT,
        "application/vnd.oasis.opendocument.text-web" to FileTypes.TYPE_TEXT,
        "application/vnd.stardivision.writer" to FileTypes.TYPE_TEXT,
        "application/vnd.stardivision.writer-global" to FileTypes.TYPE_TEXT,
        "application/vnd.sun.xml.writer" to FileTypes.TYPE_TEXT,
        "application/vnd.sun.xml.writer.global" to FileTypes.TYPE_TEXT,
        "application/vnd.sun.xml.writer.template" to FileTypes.TYPE_TEXT,
        "application/x-abiword" to FileTypes.TYPE_TEXT,
        "application/x-kword" to FileTypes.TYPE_TEXT,
        "application/x-quicktimeplayer" to FileTypes.TYPE_VIDEO,
        "application/x-shockwave-flash" to FileTypes.TYPE_VIDEO,
        "application/msword" to FileTypes.TYPE_WORD,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to FileTypes.TYPE_WORD,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template" to FileTypes.TYPE_WORD,
        "application/vnd.ms-excel" to FileTypes.TYPE_EXCEL,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to FileTypes.TYPE_EXCEL,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template" to FileTypes.TYPE_EXCEL,
        "application/vnd.ms-powerpoint" to FileTypes.TYPE_PPT,
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" to FileTypes.TYPE_PPT,
        "application/vnd.openxmlformats-officedocument.presentationml.template" to FileTypes.TYPE_PPT,
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow" to FileTypes.TYPE_PPT
    )


    fun getFileTypeByMimeType(mimeType: String): FileTypes {
        return fileTypesList[mimeType] ?: when (mimeType.substringBefore("/")) {
            "image" -> FileTypes.TYPE_IMAGE
            "video" -> FileTypes.TYPE_VIDEO
            "audio" -> FileTypes.TYPE_AUDIO
            "text" -> FileTypes.TYPE_TEXT
            else -> FileTypes.TYPE_OTHER
        }
    }


    private val documentTypes = listOf(
        FileTypes.TYPE_PDF,
        FileTypes.TYPE_WORD,
        FileTypes.TYPE_EXCEL,
        FileTypes.TYPE_PPT,
        FileTypes.TYPE_TEXT,
        FileTypes.TYPE_CERTIFICATE,
        FileTypes.TYPE_SOURCE_CODE,
        FileTypes.TYPE_CONTACT,
        FileTypes.TYPE_EVENT,
        FileTypes.TYPE_FONT
    )

    fun isDocument(type: FileTypes): Boolean {
        return documentTypes.any { it == type }
    }

}
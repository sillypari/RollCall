package com.pesky.app.data.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles GZIP compression and decompression for database payloads.
 * Compression is applied before encryption to reduce file size.
 */
@Singleton
class CompressionManager @Inject constructor() {
    
    companion object {
        private const val BUFFER_SIZE = 8192
    }
    
    /**
     * Compresses data using GZIP.
     * 
     * @param data The data to compress
     * @return The compressed data
     */
    fun compress(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { gzipStream ->
            gzipStream.write(data)
        }
        return outputStream.toByteArray()
    }
    
    /**
     * Decompresses GZIP data.
     * 
     * @param compressedData The compressed data
     * @return The decompressed data
     */
    fun decompress(compressedData: ByteArray): ByteArray {
        val inputStream = ByteArrayInputStream(compressedData)
        val outputStream = ByteArrayOutputStream()
        
        GZIPInputStream(inputStream).use { gzipStream ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (gzipStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }
        
        return outputStream.toByteArray()
    }
}

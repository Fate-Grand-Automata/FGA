package com.mathewsachin.fategrandautomata

import java.io.InputStream
import java.io.OutputStream

interface SupportStore {
    sealed class SupportImage(val name: String) {
        abstract class File(name: String, val kind: String) : SupportImage(name) {
            abstract fun read(): InputStream

            abstract val nameWithoutExtension: String
        }

        abstract class Dir(name: String, val kind: String) : SupportImage(name) {
            abstract val list: List<File>
        }
    }

    val servants: List<SupportImage>
    val ces: List<SupportImage.File>
    val friends: List<SupportImage.File>

    fun addServant(name: String): OutputStream
    fun addCE(name: String): OutputStream
    fun addFriend(name: String): OutputStream

    fun getServants(name: String): List<SupportImage.File>
    fun getCE(name: String): SupportImage.File
    fun getFriend(name: String): SupportImage.File
}
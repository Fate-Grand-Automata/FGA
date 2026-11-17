package io.github.fate_grand_automata.util

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import java.io.File
import kotlin.test.Test

/**
 * A key in [SupportNameResources] that no longer matches an asset degrades silently to the
 * English name, which looks exactly like the intended fallback for user-added supports. Nothing
 * at runtime can tell the two apart, so the drift has to be caught here.
 */
class SupportNameResourcesTest {
    private val supportAssets = File(
        System.getProperty("fga.supportAssets")
            ?: error("fga.supportAssets is set by app/build.gradle.kts")
    )

    private val servantAssets = supportAssets.resolve("servant")
        .listFiles().orEmpty()
        .filter { it.isDirectory }
        .map { it.name }
        .toSet()

    private val ceAssets = supportAssets.resolve("ce")
        .listFiles().orEmpty()
        .filter { it.isFile && it.extension == "png" }
        .map { it.nameWithoutExtension }
        .toSet()

    /*
     * Every comparison below passes trivially against an empty asset list, so an asset layout
     * that moved out from under this test would look like a pass.
     */
    @Test
    fun `the support assets are where the test expects them`() {
        assertThat(servantAssets, "servant folders in $supportAssets").isNotEmpty()
        assertThat(ceAssets, "CE images in $supportAssets").isNotEmpty()
    }

    @Test
    fun `every servant asset has a localized name`() {
        assertThat(
            servantAssets - SupportNameResources.servantNameResIds.keys,
            "servant folders missing from SupportNameResources, which would display untranslated"
        ).isEmpty()
    }

    @Test
    fun `every localized servant name has an asset`() {
        assertThat(
            SupportNameResources.servantNameResIds.keys - servantAssets,
            "servants mapped in SupportNameResources with no matching asset folder"
        ).isEmpty()
    }

    @Test
    fun `every CE asset has a localized name`() {
        assertThat(
            ceAssets - SupportNameResources.ceNameResIds.keys,
            "CE images missing from SupportNameResources, which would display untranslated"
        ).isEmpty()
    }

    @Test
    fun `every localized CE name has an asset`() {
        assertThat(
            SupportNameResources.ceNameResIds.keys - ceAssets,
            "CEs mapped in SupportNameResources with no matching asset image"
        ).isEmpty()
    }
}

package io.github.fate_grand_automata.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R

/**
 * Provides localized display names for default servants and craft essences.
 *
 * The keys stored in SharedPreferences remain as the original English names
 * (used for image file matching). Only the display layer is localized.
 * Custom/user-added servants/CEs automatically fall back to their original names.
 */
object SupportNameResources {
    /**
     * Maps English servant folder names to their localized string resource IDs.
     * Only contains the 23 default servants shipped with the app.
     */
    val servantNameResIds: Map<String, Int> = mapOf(
        "Arcueid" to R.string.servant_name_arcueid,
        "Artoria (Caster)" to R.string.servant_name_artoria_caster,
        "BB Dubai" to R.string.servant_name_bb_dubai,
        "Chloe (Avenger)" to R.string.servant_name_chloe_avenger,
        "Ciel" to R.string.servant_name_ciel,
        "Douman" to R.string.servant_name_douman,
        "Flora" to R.string.servant_name_flora,
        "Jalter" to R.string.servant_name_jalter,
        "Koyanskaya" to R.string.servant_name_koyanskaya,
        "Koyanskaya of the Darkness (Foreigner)" to R.string.servant_name_koyanskaya_of_the_darkness_foreigner,
        "Lady Avalon" to R.string.servant_name_lady_avalon,
        "Mash" to R.string.servant_name_mash,
        "Merlin" to R.string.servant_name_merlin,
        "Nero (Bride)" to R.string.servant_name_nero_bride,
        "Oberon" to R.string.servant_name_oberon,
        "Omii-san" to R.string.servant_name_omii_san,
        "Sima Yi (Reines)" to R.string.servant_name_sima_yi_reines,
        "Skadi" to R.string.servant_name_skadi,
        "Skadi (Ruler)" to R.string.servant_name_skadi_ruler,
        "Tamamo" to R.string.servant_name_tamamo,
        "Tiamat Summer" to R.string.servant_name_tiamat_summer,
        "U-Olga Marie" to R.string.servant_name_u_olga_marie,
        "Waver" to R.string.servant_name_waver
    )

    /**
     * Maps English CE file names to their localized string resource IDs.
     * Only contains the 22 default CEs shipped with the app.
     */
    val ceNameResIds: Map<String, Int> = mapOf(
        "Aerial Drive" to R.string.ce_name_aerial_drive,
        "Bella Lisa" to R.string.ce_name_bella_lisa,
        "Black Grail" to R.string.ce_name_black_grail,
        "Blessed Bride" to R.string.ce_name_blessed_bride,
        "Chaldea Lunchtime" to R.string.ce_name_chaldea_lunchtime,
        "Chaldea Morning" to R.string.ce_name_chaldea_morning,
        "Chaldea Teatime" to R.string.ce_name_chaldea_teatime,
        "Foreign God" to R.string.ce_name_foreign_god,
        "From NFF with Love" to R.string.ce_name_from_nff_with_love,
        "Golden Sumo" to R.string.ce_name_golden_sumo,
        "Great Library of Memories" to R.string.ce_name_great_library_of_memories,
        "GudaGuda Poster Girl" to R.string.ce_name_gudaguda_poster_girl,
        "Holy Night Supper" to R.string.ce_name_holy_night_supper,
        "Kaleidoscope" to R.string.ce_name_kaleidoscope,
        "Marshal of the Sorcerors" to R.string.ce_name_marshal_of_the_sorcerors,
        "Mona Lisa" to R.string.ce_name_mona_lisa,
        "Needle of Sincerity" to R.string.ce_name_needle_of_sincerity,
        "Painting Summer" to R.string.ce_name_painting_summer,
        "Report Check" to R.string.ce_name_report_check,
        "Secret Mission" to R.string.ce_name_secret_mission,
        "The Chaldean" to R.string.ce_name_the_chaldean,
        "Wings of Manuscript" to R.string.ce_name_wings_of_manuscript
    )

    /**
     * Returns the localized display name for a given English servant name.
     * Falls back to the English name if no translation resource exists (covers custom servants).
     */
    fun getLocalizedServantName(context: Context, englishName: String): String {
        val resId = servantNameResIds[englishName]
        return if (resId != null) context.getString(resId) else englishName
    }

    /**
     * Composable variant using [stringResource].
     */
    @Composable
    fun getLocalizedServantName(englishName: String): String {
        val resId = servantNameResIds[englishName]
        return if (resId != null) stringResource(resId) else englishName
    }

    /**
     * Returns the localized display name for a given English CE name.
     * Falls back to the English name if no translation resource exists (covers custom CEs).
     */
    fun getLocalizedCEName(context: Context, englishName: String): String {
        val resId = ceNameResIds[englishName]
        return if (resId != null) context.getString(resId) else englishName
    }

    /**
     * Composable variant using [stringResource].
     */
    @Composable
    fun getLocalizedCEName(englishName: String): String {
        val resId = ceNameResIds[englishName]
        return if (resId != null) stringResource(resId) else englishName
    }
}

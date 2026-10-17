package io.github.fate_grand_automata.atlas

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test

/**
 * The decoder is what stands between a payload the API changed and a list of servants to add, so
 * it is exercised against trimmed copies of real responses.
 */
class AtlasAcademyApiTest {
    private val searchResponse = """
        [
          {"id":103500,"collectionNo":223,"name":"Diarmuid Ua Duibhne","originalName":"Diarmuid Ua Duibhne","type":"normal","flag":"normal","flags":[],"classId":1,"className":"saber","attribute":"earth","traits":[{"id":1,"name":"genderMale"}],"rarity":4,"atkMax":10048,"hpMax":11362,"face":"https://static.atlasacademy.io/NA/Faces/f_1035000.png","costume":{"103500":{"id":0,"costumeCollectionNo":0,"battleCharaId":103500,"shortName":""}}},
          {"id":800100,"collectionNo":1,"name":"Mash Kyrielight","type":"heroine","flag":"normal","className":"shielder","rarity":3,"face":"https://static.atlasacademy.io/NA/Faces/f_8001000.png"},
          {"id":9935530,"collectionNo":0,"name":"Solomon","type":"enemyCollectionDetail","flag":"normal","className":"caster","rarity":5,"face":"https://static.atlasacademy.io/NA/Faces/f_99355300.png"},
          {"id":1700100,"collectionNo":0,"name":"Solomon","type":"enemyCollectionDetail","flag":"normal","className":"loreGrandCaster","rarity":5,"face":"https://static.atlasacademy.io/NA/Faces/f_17001000.png"}
        ]
    """.trimIndent()

    /** Trimmed `/nice/JP/servant/2300500`: ascensions under `ascension`, costumes under `costume`. */
    private val servantResponse = """
        {
          "id": 2300500,
          "name": "Archetype: EARTH",
          "type": "normal",
          "extraAssets": {
            "charaGraph": {"ascension": {"1": "https://static.atlasacademy.io/JP/CharaGraph/2300500/2300500a@1.png"}},
            "faces": {
              "ascension": {
                "1": "https://static.atlasacademy.io/JP/Faces/f_23005000.png",
                "2": "https://static.atlasacademy.io/JP/Faces/f_23005001.png",
                "10": "https://static.atlasacademy.io/JP/Faces/f_23005009.png",
                "3": " "
              },
              "costume": {
                "300": "https://static.atlasacademy.io/JP/Faces/f_23009300.png"
              }
            }
          }
        }
    """.trimIndent()

    @Test
    fun `search hits keep the servants a support list can offer`() {
        val servants = decodeServants(searchResponse)

        assertThat(servants.map { it.name })
            .isEqualTo(listOf("Diarmuid Ua Duibhne", "Mash Kyrielight"))
    }

    @Test
    fun `search hits carry the class and rarity that tell servants apart`() {
        val servant = decodeServants(searchResponse).first()

        assertThat(servant.id).isEqualTo(103500)
        assertThat(servant.className).isEqualTo("saber")
        assertThat(servant.rarity).isEqualTo(4)
        assertThat(servant.face).isEqualTo("https://static.atlasacademy.io/NA/Faces/f_1035000.png")
    }

    @Test
    fun `an enemy is not offered as a support`() {
        assertThat(decodeServants(searchResponse).none { it.name == "Solomon" }).isTrue()
    }

    @Test
    fun `ascensions come first and costumes after, ordered by their index`() {
        assertThat(decodeFaceUrls(servantResponse)).isEqualTo(
            listOf(
                "https://static.atlasacademy.io/JP/Faces/f_23005000.png",
                "https://static.atlasacademy.io/JP/Faces/f_23005001.png",
                "https://static.atlasacademy.io/JP/Faces/f_23005009.png",
                "https://static.atlasacademy.io/JP/Faces/f_23009300.png"
            )
        )
    }

    @Test
    fun `a face listed for an ascension and a costume is only downloaded once`() {
        val body = """
            {"extraAssets":{"faces":{
              "ascension":{"1":"https://static.atlasacademy.io/JP/Faces/f_23005000.png"},
              "costume":{"300":"https://static.atlasacademy.io/JP/Faces/f_23005000.png"}
            }}}
        """.trimIndent()

        assertThat(decodeFaceUrls(body))
            .isEqualTo(listOf("https://static.atlasacademy.io/JP/Faces/f_23005000.png"))
    }

    @Test
    fun `a servant without faces has no images to add`() {
        assertThat(decodeFaceUrls("""{"id":1,"name":"Nobody","extraAssets":{}}""")).isEmpty()
    }

    @Test
    fun `face files are named after the url they came from`() {
        assertThat(faceFileName("https://static.atlasacademy.io/JP/Faces/f_23005000.png"))
            .isEqualTo("f_23005000.png")

        assertThat(faceFileName("https://static.atlasacademy.io/JP/Faces/f_23005000.png?v=2"))
            .isEqualTo("f_23005000.png")
    }

    @Test
    fun `the face shown in the dialog is the final ascension`() {
        assertThat(finalAscensionFace("https://static.atlasacademy.io/CN/Faces/f_1001000.png"))
            .isEqualTo("https://static.atlasacademy.io/CN/Faces/f_1001003.png")

        // Servants whose faces don't start at index 0, like Mash (`f_8001002`)
        assertThat(finalAscensionFace("https://static.atlasacademy.io/NA/Faces/f_8001002.png"))
            .isEqualTo("https://static.atlasacademy.io/NA/Faces/f_8001003.png")

        // A face that is already the final ascension stays as it is
        assertThat(finalAscensionFace("https://static.atlasacademy.io/NA/Faces/f_8001003.png"))
            .isEqualTo("https://static.atlasacademy.io/NA/Faces/f_8001003.png")
    }

    @Test
    fun `a face that is not named after an ascension index is left alone`() {
        assertThat(finalAscensionFace("https://static.atlasacademy.io/NA/Faces/face.png"))
            .isEqualTo("https://static.atlasacademy.io/NA/Faces/face.png")

        assertThat(finalAscensionFace("")).isEqualTo("")
    }
}

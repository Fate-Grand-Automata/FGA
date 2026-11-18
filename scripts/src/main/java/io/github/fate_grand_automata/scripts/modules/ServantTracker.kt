package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.OrderChangeMember
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.models.skills
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.Pattern
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.mapValues
import kotlin.time.Duration.Companion.milliseconds

@ScriptScope
class ServantTracker @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api, AutoCloseable {

    private val servantQueue = mutableListOf<TeamSlot>()
    private val _deployed = mutableMapOf<FieldSlot, TeamSlot>()
    val deployed: Map<FieldSlot, TeamSlot> = _deployed

    /**
     * Servants like Mélusine and Ptolemaios change their NP type when they transform with their skill,
     * so the NP type is tracked per form: [npCardTypes] for the form the Servant entered the field in,
     * [transformedNpCardTypes] for the other form, and [transformedServants] for who is currently in the other form.
     */
    private val npCardTypes = mutableMapOf<TeamSlot, CardTypeEnum>()
    private val transformedNpCardTypes = mutableMapOf<TeamSlot, CardTypeEnum>()
    private val transformedServants = mutableSetOf<TeamSlot>()

    fun nextRun() {
        servantQueue.clear()
        servantQueue.addAll(
            listOf(TeamSlot.D, TeamSlot.E, TeamSlot.F)
        )

        _deployed.clear()
        _deployed.putAll(
            mapOf(
                FieldSlot.A to TeamSlot.A,
                FieldSlot.B to TeamSlot.B,
                FieldSlot.C to TeamSlot.C
            )
        )

        // Every run starts with all Servants in the form they entered the field in
        transformedServants.clear()
    }

    init {
        nextRun()
    }

    data class TeamSlotData(
        val checkImage: MutableList<Pattern>,
        val skills: List<Pattern>
    ) : AutoCloseable {
        override fun close() {
            checkImage.forEach { it.close() }
            skills.forEach { it.close() }
        }
    }

    val checkImages = mutableMapOf<TeamSlot, TeamSlotData>()
    private var supportSlot: TeamSlot? = null

    private val faceCardImages = mutableMapOf<TeamSlot, MutableList<Pattern>>()

    private val npSplashImages = mapOf(
        CardTypeEnum.Buster to images[Images.NpBuster],
        CardTypeEnum.Arts to images[Images.NpArts],
        CardTypeEnum.Quick to images[Images.NpQuick],
    )

    fun getNpCardType(teamSlot: TeamSlot): CardTypeEnum {
        val npCardType =
            if (teamSlot in transformedServants) transformedNpCardTypes[teamSlot]
            else npCardTypes[teamSlot]

        return npCardType ?: CardTypeEnum.Unknown
    }


    override fun close() {
        checkImages.values.forEach { it.close() }
        faceCardImages.values.flatten().forEach { it.close() }
        checkImages.clear()
    }


    private fun init(teamSlot: TeamSlot, slot: FieldSlot) {
        messages.log(
            ScriptLog.ServantEnteredSlot(
                servant = teamSlot,
                slot = slot
            )
        )

        var isSupport = false
        // use same screenshot for support + face detection
        useSameSnapIn {
            isSupport = isSupport(slot)

            if (teamSlot !in checkImages || isSupport) {
                checkImages[teamSlot] = TeamSlotData(
                    checkImage = mutableListOf(
                        locations.battle.servantChangeCheckRegion(slot)
                            .getPattern("Servant $teamSlot")
                    ),
                    skills = slot.skills().mapIndexed { index, it ->
                        locations.battle.imageRegion(it)
                            .getPattern("Servant $teamSlot S${index + 1}")
                    }
                )
            }
        }

        if (supportSlot == null && isSupport) {
            supportSlot = teamSlot
        }

        // Supports also get a face card, so that their NP type is known
        // Don't useSameSnapIn here, since we open a dialog
        initFaceCard(teamSlot, slot)?.let { npCardTypes[teamSlot] = it }
    }

    /**
     * Compares the NP area of the Servant details dialog with the Buster/Arts/Quick splash images.
     * Logs a warning with all scores if no type matches.
     *
     * @return The best matching type, or null if none matches.
     */
    private fun detectNpCardType(teamSlot: TeamSlot, npImage: Pattern): CardTypeEnum? {
        // Similarity 0 returns the best score even when it is too low, so the warning can show it
        val scores = npSplashImages
            .mapValues { (_, splashImage) -> npImage.find(splashImage, 0.0)?.score ?: 0.0 }

        val npCardType = scores
            .filterValues { it >= prefs.npCardTypeSimilarity }
            .maxByOrNull { it.value }
            ?.key

        if (npCardType == null) {
            messages.log(ScriptLog.NpCardTypeNotDetected(teamSlot, scores, prefs.npCardTypeSimilarity))
        }

        return npCardType
    }

    /**
     * Drops the cached face card and NP types of [teamSlot], so that the next [initFaceCard] captures them again.
     */
    private fun forgetFaceCard(teamSlot: TeamSlot) {
        faceCardImages.remove(teamSlot)?.forEach { it.close() }
        npCardTypes.remove(teamSlot)
        transformedNpCardTypes.remove(teamSlot)
        transformedServants.remove(teamSlot)
    }

    /**
     * Captures the face card of the Servant in [slot] from their details dialog.
     *
     * @param addAnotherImage Capture even if [teamSlot] already has a face card, for a second form.
     * @return The NP type shown in the details dialog, or null if nothing was captured or no type matched.
     */
    private fun initFaceCard(teamSlot: TeamSlot, slot: FieldSlot, addAnotherImage: Boolean = false): CardTypeEnum? {
        if (prefs.skipServantFaceCardCheck || (!addAnotherImage && teamSlot in faceCardImages))
            return null

        // Open details dialog and click on INFO
        locations.battle.servantOpenDetailsClick(slot).click()
        locations.battle.servantDetailsInfoClick.click()

        250.milliseconds.wait()

        val image = locations.battle.servantDetailsFaceCardRegion.getPattern("Face $teamSlot")
        val npImage = locations.battle.servantNpCardTypeRegion.getPattern("NP type $teamSlot")

        // Close dialog
        locations.battle.extraInfoWindowCloseClick.click()

        faceCardImages.getOrPut(teamSlot) { mutableListOf() }
            .add(image)

        val npCardType = npImage.use { detectNpCardType(teamSlot, it) }

        250.milliseconds.wait()

        return npCardType
    }

    private fun check(slot: FieldSlot) {
        // If a servant is not present, that means none are left in the backline
        if (!locations.battle.servantPresentRegion(slot)
                .exists(images[Images.ServantExist], similarity = 0.70)
        ) {
            _deployed.remove(slot)
            servantQueue.clear()
            return
        }

        val teamSlot = deployed[slot] ?: return
        if (teamSlot is TeamSlot.Unknown) return

        val checkImage = checkImages[teamSlot]?.checkImage

        if (checkImage == null) {
            init(teamSlot, slot)
            return
        }

        val isDifferentServant = checkImage.none { it in locations.battle.servantChangeCheckRegion(slot) }
        val isSupport = isSupport(slot)
        val wasSupport = supportSlot == teamSlot

        // New run with different support
        if (wasSupport && isSupport && isDifferentServant) {
            forgetFaceCard(teamSlot)
            init(teamSlot, slot)
        } else if (isDifferentServant || (wasSupport != isSupport)) {
            val newTeamSlot = servantQueue.removeFirstOrNull()

            if (newTeamSlot != null) {
                _deployed[slot] = newTeamSlot
                init(newTeamSlot, slot)
            } else {
                // Something has gone wrong with matching servants, a servant is present but we don't know which one
                _deployed[slot] = TeamSlot.Unknown

                messages.log(
                    ScriptLog.ServantEnteredSlot(
                        servant = TeamSlot.Unknown,
                        slot = slot
                    )
                )
            }
        }
    }

    fun beginTurn() =
        FieldSlot.list.forEach {
            check(it)
        }

    fun orderChanged(starting: OrderChangeMember.Starting, sub: OrderChangeMember.Sub) {
        val startingSlot = when (starting) {
            OrderChangeMember.Starting.A -> FieldSlot.A
            OrderChangeMember.Starting.B -> FieldSlot.B
            OrderChangeMember.Starting.C -> FieldSlot.C
        }
        val subIndex = sub.autoSkillCode - OrderChangeMember.Sub.A.autoSkillCode

        if (subIndex in servantQueue.indices) {
            deployed[startingSlot]?.let { swapOut ->
                _deployed[startingSlot] = servantQueue[subIndex]
                servantQueue[subIndex] = swapOut

                check(startingSlot)
            }
        }
    }

    fun faceCardsGroupedByServant(): Map<TeamSlot, Collection<CommandCard.Face>> {
        if (prefs.skipServantFaceCardCheck) {
            return emptyMap()
        }

        val cardsRemaining = CommandCard.Face.list.toMutableSet()
        val result = mutableMapOf<TeamSlot, Set<CommandCard.Face>>()

        supportSlot?.let { supportSlot ->
            if (supportSlot in deployed.values) {
                val matched = cardsRemaining.filter { card ->
                    images[Images.Support] in locations.attack.supportCheckRegion(card)
                }.toSet()

                cardsRemaining -= matched
                result[supportSlot] = matched
            }
        }

        // If no card has the support label (e.g. hidden by too many buffs), match the support by its face instead
        val isSupportFound = result[supportSlot].orEmpty().isNotEmpty()

        val ownedServants = faceCardImages
            .filterKeys { (!isSupportFound || it != supportSlot) && it in deployed.values }
        cardsRemaining
            .groupBy { card ->
                // find the best matching Servant which isn't the support
                ownedServants
                    .mapValues { (_, images) ->
                        images.maxOf { image ->
                            locations.attack.servantMatchRegion(card)
                                .find(image, 0.5)?.score ?: 0.0
                        }
                    }
                    .filterValues { it > 0.0 }
                    .maxByOrNull { it.value }
                    ?.key
            }
            .filterKeys { it != null }
            .entries
            .associateTo(result) { (key, value) -> key!! to value.toSet() }

        result.forEach { (servant, cards) ->
            messages.log(
                ScriptLog.CardsBelongToServant(
                    cards,
                    servant,
                    isSupport = servant == supportSlot
                )
            )
        }

        return result
    }

    fun npCardsDetectedUsingServantFaces(): Set<CommandCard.NP> {
        if (prefs.skipServantFaceCardCheck) {
            return emptySet()
        }

        val npCards = CommandCard.NP.list.toSet()
        val deployedServants = faceCardImages
            .filterKeys { it in deployed.values }

        val scores = npCards
            .map { npCard ->
                npCard to deployedServants
                    .filterKeys { it.toFieldSlot() == npCard.toFieldSlot() }
                    .mapValues { (_, images) ->
                        images.maxOf { image ->
                            locations.attack.servantNPMatchRegion(npCard)
                                .find(image, 0.4)?.score ?: 0.0
                        }
                    }
                    .values
            }
        val result = scores
            .filter { (_, values) ->
                values.filter { it > prefs.npSpamCardDetectionSimilarity }.maxByOrNull { it } != null
            }
            .map { it.first }
            .toSet()

        return result
    }

    /**
     * Updates the form of the Servant in [fieldSlot] after a skill like Mélusine's or Ptolemaios's 3rd skill.
     * The form is read from the portrait, so a skill that was rejected (e.g. on cooldown) changes nothing.
     *
     * The first time the portrait changes, the other form's image is added to the existing one so both are
     * detected as the same Servant, and that form's face card and NP type are captured.
     */
    fun servantTransformed(fieldSlot: FieldSlot) {
        val teamSlot = _deployed[fieldSlot] ?: return
        val checkImage = checkImages[teamSlot]?.checkImage ?: return
        val portraitRegion = locations.battle.servantChangeCheckRegion(fieldSlot)

        // checkImage[0] is the form the Servant entered the field in, checkImage[1] the other form once known
        val originalForm = checkImage.first()
        val otherForm = checkImage.getOrNull(1)

        if (otherForm == null) {
            if (originalForm in portraitRegion) return

            checkImage.add(portraitRegion.getPattern("Servant $teamSlot transformed"))
            transformedServants += teamSlot

            initFaceCard(teamSlot, fieldSlot, addAnotherImage = true)
                ?.let { transformedNpCardTypes[teamSlot] = it }
            return
        }

        useSameSnapIn {
            when {
                otherForm in portraitRegion -> transformedServants += teamSlot
                originalForm in portraitRegion -> transformedServants -= teamSlot
            }
        }
    }

    /**
     * Checks if the given [slot] is a Support Servant. Will always return `false` if "Treat Support like own Servant" is enabled.
     */
    private fun isSupport(slot: FieldSlot) = !prefs.treatSupportLikeOwnServant &&
            images[Images.ServantCheckSupport] in locations.battle.servantChangeSupportCheckRegion(slot)

    fun TeamSlot.toFieldSlot(): FieldSlot? {
        return deployed
            .entries
            .firstOrNull { (_, teamSlot) -> teamSlot == this }
            ?.key
    }
}

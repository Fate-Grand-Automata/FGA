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
import io.github.lib_automata.Pattern
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.mapValues
import kotlin.time.Duration.Companion.milliseconds

@ScriptScope
class ServantTracker @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api, AutoCloseable {

    private val servantQueue = mutableListOf<TeamSlot>()
    private val _deployed = mutableMapOf<FieldSlot, TeamSlot>()
    val deployed: Map<FieldSlot, TeamSlot> = _deployed

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

    /**
     * NP card type handling
     */
    private val npCardImages = mutableMapOf<TeamSlot, MutableList<Pattern>>()
    private val npSplashImages = mapOf(
        CardTypeEnum.Buster to images[Images.SplashBuster],
        CardTypeEnum.Arts to images[Images.SplashArts],
        CardTypeEnum.Quick to images[Images.SplashQuick],
    )
    val npCardTypes = mutableMapOf<TeamSlot, CardTypeEnum>()

    fun getNpCardType(teamSlot: TeamSlot): CardTypeEnum {
        return npCardTypes.getOrElse(teamSlot) { CardTypeEnum.Unknown }
    }


    override fun close() {
        checkImages.values.forEach { it.close() }
        faceCardImages.values.flatten().forEach { it.close() }
        npCardImages.values.flatten().forEach { it.close() }
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

        // We now always want to init the face card, so that we can check the npType
        // Don't useSameSnapIn here, since we open a dialog
        initFaceCard(teamSlot, slot)

        // After initFaceCard, we need to check npType for servant here
        if (npCardImages.contains(teamSlot)) {
            val patternList = npCardImages[teamSlot]
            // Get type by comparing the patterns we have with the splash arts
            val type = npSplashImages
                .mapValues {
                    (_, image) -> patternList?.maxOf { pattern ->
                        pattern.find(image)?.score ?: 0.0
                    } ?: 0.0
                }
                .filterValues { it > 0.0 }
                .maxByOrNull { it.value }
                ?.key
            if (type != null) {
                npCardTypes.put(teamSlot, type)
            }
        }
    }

    private fun initFaceCard(teamSlot: TeamSlot, slot: FieldSlot, addAnotherImage: Boolean = false) {
        if (prefs.skipServantFaceCardCheck || (!addAnotherImage && teamSlot in faceCardImages))
            return

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
        npCardImages.getOrPut(teamSlot) { mutableListOf() }
            .add(npImage)

        250.milliseconds.wait()
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

        val ownedServants = faceCardImages
            .filterKeys { it != supportSlot && it in deployed.values }
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

    /**
     * Adds the 3rd Ascension Melusine image to the existing 1st/2nd Ascension
     * image so both are detected as the same Servant.
     */
    fun melusineChangedAscension(fieldSlot: FieldSlot) {
        val teamSlot = _deployed[fieldSlot]!!
        val teamSlotData = checkImages[teamSlot]
        if (teamSlotData != null && teamSlotData.checkImage.size == 1) {
            teamSlotData.checkImage.add(
                locations.battle.servantChangeCheckRegion(fieldSlot)
                    .getPattern("Melusine Asc3")
            )

            initFaceCard(teamSlot, fieldSlot, addAnotherImage = true)
        }
    }

    /**
     * Checks if the given [slot] is a Support Servant. Will always return `false` if "Treat Support like own Servant" is enabled.
     */
    private fun isSupport(slot: FieldSlot) = !prefs.treatSupportLikeOwnServant &&
            images[Images.ServantCheckSupport] in locations.battle.servantChangeSupportCheckRegion(slot)
}

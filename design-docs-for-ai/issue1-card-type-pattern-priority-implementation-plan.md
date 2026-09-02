# Issue #1: カード選択への型パターン優先度の追加 — 実装計画

対象 Issue: https://github.com/5cd8/FGA/issues/1

このファイルは、本セッションの文脈を持たない別の AI コーディングエージェントがそのまま実装に着手できることを目的とした、それ単体で完結する実装計画である。追加の調査や質問を必要としない。

> Issue本文はこの実装計画の内容(`cardsBeforeNP`を読まない簡略化設計、`BattleExit.kt`への1行追加)に一致するよう更新済み(2026-09-02)。差分の経緯・導出はセクション2.5・2.6に記載。

---

## 0. 前提知識(実装前に必ず把握すること)

### 0.1 用語

リポジトリルートの `CONTEXT.md` に記録済み。読むこと。要点:

- **型パターン(Type Pattern)**: 今回実装する概念。3枚の最終選択カードについて、ポジションごとに要求する種別(B/A/Q)を順序付きで指定する。
- **カードの優先順位(Card Priority)**: 既存の`CardPriority`。型パターンとは別軸(手札全体をランキングする)。
- **ブレイブチェイン(Brave Chain)**: 既存の`BraveChainEnum`。型パターンとは無関係。

コード中のクラス名・コメントで「コンボ」という語は使わない。

### 0.2 対象モジュールとバージョン

- 対象は `scripts` モジュール(`scripts/src/main/java/io/github/fate_grand_automata/scripts/`)のみ。Android非依存の純粋JVM Kotlin。
- Kotlin: `2.4.10`(`gradle/libs.versions.toml:15`)
- テスト: `assertk 0.28.1`、`junit-bom 6.1.3`、`kotlin-test` / `kotlin-test-junit5`(`gradle/libs.versions.toml`)
- 依存性注入なし。すべて`kotlin.test.Test`のみで書かれた素のJVMユニットテスト(モック不要)。

### 0.3 このIssueのスコープ

実装するのは以下のみ:

- 新規モデル3つ(`CardTypePattern`, `CardTypePatternPriority`, `CardTypePatternPriorityPerWave`)
- 新規マッチングロジック1つ(`CardTypePatternSelector`)
- `AutoBattle.ExitReason`への1ケース追加
- 上記すべてのユニットテスト

**変更しないもの**(次のIssueに切り出し済み):

- `Card.kt`(`pickCards()`への配線)
- `IBattleConfig`インターフェース
- `prefs`モジュール(永続化)
- `app`モジュールのUI・機能(画面・設定編集は一切追加しない)

`CardTypePatternSelector`は今回、どこからも呼び出されない独立したコンポーネントとして追加する。これは意図的な状態であり、未完成ではない。

**唯一の例外**: `app`モジュールに1箇所だけ機械的な追加が必要になる。詳細はセクション2.6を参照。これは新機能をUIに露出させるものではなく、Kotlinのsealed classの網羅性チェックを満たすためだけの追加である(この判断はセッション内で確認済み)。

---

## 1. 変更対象ファイル一覧

| # | ファイル | 種別 |
|---|---|---|
| 1 | `scripts/src/main/java/io/github/fate_grand_automata/scripts/models/CardTypePattern.kt` | 新規 |
| 2 | `scripts/src/main/java/io/github/fate_grand_automata/scripts/models/CardTypePatternPriority.kt` | 新規 |
| 3 | `scripts/src/main/java/io/github/fate_grand_automata/scripts/models/CardTypePatternPriorityPerWave.kt` | 新規 |
| 4 | `scripts/src/main/java/io/github/fate_grand_automata/scripts/entrypoints/AutoBattle.kt` | 変更(1行追加) |
| 5 | `scripts/src/main/java/io/github/fate_grand_automata/scripts/modules/CardTypePatternSelector.kt` | 新規 |
| 6 | `app/src/main/java/io/github/fate_grand_automata/ui/exit/BattleExit.kt` | 変更(1行追加、セクション2.6) |
| 7 | `scripts/src/test/java/io/github/fate_grand_automata/scripts/CardTypePatternTest.kt` | 新規 |
| 8 | `scripts/src/test/java/io/github/fate_grand_automata/scripts/CardTypePatternPriorityTest.kt` | 新規 |
| 9 | `scripts/src/test/java/io/github/fate_grand_automata/scripts/CardTypePatternPriorityPerWaveTest.kt` | 新規 |
| 10 | `scripts/src/test/java/io/github/fate_grand_automata/scripts/CardTypePatternSelectorTest.kt` | 新規 |

---

## 2. 実装詳細

### 2.1 `models/CardTypePattern.kt`(新規)

参照した既存コード: `scripts/src/main/java/io/github/fate_grand_automata/scripts/models/CardPriority.kt`(全文、`.trim().uppercase()`のトークン処理・`raiseParseError`のパターンを踏襲)。

```kotlin
package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

/**
 * An ordered request for the type (Buster/Arts/Quick) of each of the 3 cards a turn
 * ultimately taps, independent of [CardPriority]'s whole-hand ranking. See CONTEXT.md
 * ("型パターン / Type Pattern") for the concept this models.
 */
class CardTypePattern private constructor(
    private val types: List<CardTypeEnum>
) : List<CardTypeEnum> by types {
    override fun toString() = types.joinToString("") {
        when (it) {
            CardTypeEnum.Buster -> "B"
            CardTypeEnum.Arts -> "A"
            CardTypeEnum.Quick -> "Q"
            // of() never produces Unknown; this branch only exists to keep the `when` exhaustive.
            CardTypeEnum.Unknown -> "?"
        }
    }

    companion object {
        private const val requiredLength = 3
        private const val errorPrefix = "Battle_CardTypePattern Error at '"

        fun from(types: List<CardTypeEnum>) = CardTypePattern(types)

        private fun raiseParseError(msg: String): Nothing {
            throw AutoBattle.BattleExitException(
                AutoBattle.ExitReason.CardTypePatternParseError(msg)
            )
        }

        fun of(pattern: String): CardTypePattern {
            val normalized = pattern.trim().uppercase()

            if (normalized.length != requiredLength) {
                raiseParseError("$errorPrefix$pattern': Expected exactly $requiredLength characters, but ${normalized.length} found.")
            }

            val types = normalized.map {
                when (it) {
                    'B' -> CardTypeEnum.Buster
                    'A' -> CardTypeEnum.Arts
                    'Q' -> CardTypeEnum.Quick
                    else -> raiseParseError("$errorPrefix$pattern': Only 'B', 'A' and 'Q' are valid card types.")
                }
            }

            return CardTypePattern(types)
        }
    }
}
```

**命名根拠**(`code-naming` skill準拠): `of`/`from`は既存の`CardPriority`と対になる同一の役割(パース/直接構築)を持つため、プロジェクト内の既存命名を踏襲した(スキル本文「適用範囲」: 既存プロジェクトが定めた命名との一貫性を優先)。

### 2.2 `models/CardTypePatternPriority.kt`(新規)

```kotlin
package io.github.fate_grand_automata.scripts.models

/**
 * A priority-ordered list of [CardTypePattern]s. The first pattern the current hand can
 * satisfy wins; see [io.github.fate_grand_automata.scripts.modules.CardTypePatternSelector].
 */
class CardTypePatternPriority private constructor(
    private val patterns: List<CardTypePattern>
) : List<CardTypePattern> by patterns {
    override fun toString() = patterns.joinToString(", ")

    companion object {
        fun from(patterns: List<CardTypePattern>) = CardTypePatternPriority(patterns)

        // A blank string means "no type patterns for this wave", which is a valid,
        // meaningful state (nothing to try) rather than an error. Unlike
        // CardPriority.of(), there is no sensible non-empty default to fall back to,
        // so this doesn't mirror CardPriorityPerWave.of()'s isBlank()-to-default branch.
        fun of(priority: String): CardTypePatternPriority =
            if (priority.isBlank()) {
                CardTypePatternPriority(emptyList())
            } else {
                CardTypePatternPriority(priority.split(",").map { CardTypePattern.of(it) })
            }
    }
}
```

### 2.3 `models/CardTypePatternPriorityPerWave.kt`(新規)

参照した既存コード: `scripts/src/main/java/io/github/fate_grand_automata/scripts/models/CardPriorityPerWave.kt`(`atWave()`の`coerceIn`によるクランプ、`"\n"`区切りの規約を踏襲)。

```kotlin
package io.github.fate_grand_automata.scripts.models

/**
 * Per-wave wrapper around [CardTypePatternPriority], mirroring [CardPriorityPerWave]'s shape.
 */
class CardTypePatternPriorityPerWave private constructor(
    private val patternsPerWave: List<CardTypePatternPriority>
) : List<CardTypePatternPriority> by patternsPerWave {
    fun atWave(wave: Int) =
        patternsPerWave[wave.coerceIn(patternsPerWave.indices)]

    override fun toString() =
        patternsPerWave.joinToString(waveSeparator)

    companion object {
        private const val waveSeparator = "\n"

        fun from(patternsPerWave: List<CardTypePatternPriority>) = CardTypePatternPriorityPerWave(patternsPerWave)

        // Deliberately no isBlank() special case, unlike CardPriorityPerWave.of():
        // "".split(waveSeparator) already yields a single blank element, and
        // CardTypePatternPriority.of() already treats a blank element as "no patterns"
        // rather than a parse error (see above). So a fully blank config naturally
        // becomes one wave entry with an empty pattern list, and atWave() never
        // indexes an empty list.
        fun of(priority: String): CardTypePatternPriorityPerWave =
            CardTypePatternPriorityPerWave(
                priority.split(waveSeparator).map { CardTypePatternPriority.of(it) }
            )
    }
}
```

### 2.4 `entrypoints/AutoBattle.kt`(変更)

現状(`scripts/src/main/java/io/github/fate_grand_automata/scripts/entrypoints/AutoBattle.kt:61-79`):

```kotlin
    sealed class ExitReason(val cause: Exception? = null) {
        data object Abort : ExitReason()
        class Unexpected(cause: Exception) : ExitReason(cause)
        data object CEGet : ExitReason()
        class LimitCEs(val count: Int) : ExitReason()
        data object FirstClearRewards : ExitReason()
        class LimitMaterials(val count: Int) : ExitReason()
        data object WithdrawDisabled : ExitReason()
        data object APRanOut : ExitReason()
        data object InventoryFull : ExitReason()
        class LimitRuns(val count: Int) : ExitReason()
        data object SupportSelectionManual : ExitReason()
        data object SupportSelectionPreferredNotSet : ExitReason()
        class SkillCommandParseError(cause: Exception) : ExitReason(cause)
        class CardPriorityParseError(val msg: String) : ExitReason()
        data object Paused : ExitReason()
        data object StopAfterThisRun : ExitReason()
        data object OutOfCommandSpells: ExitReason()
    }
```

`class CardPriorityParseError(val msg: String) : ExitReason()` の行の直後に1行追加する:

```kotlin
        class CardPriorityParseError(val msg: String) : ExitReason()
        class CardTypePatternParseError(val msg: String) : ExitReason()
```

これ以外の変更はしない。フィールド名は既存の`CardPriorityParseError`と揃えて`msg`とする(`message`ではない。既存コードの実物で確認済み)。

### 2.5 `modules/CardTypePatternSelector.kt`(新規)

参照した既存コード: `scripts/src/main/java/io/github/fate_grand_automata/scripts/modules/ApplyBraveChains.kt`(全文)。`@ScriptScope` + `@Inject constructor()`(引数なし)というDIコンテナ不要のインスタンス化パターン、`picked + notPicked`で常に全件を返す規約、`remainingCards -= chosenCard`という`MutableList`の差分演算子(`legacyAvoidBraveChains`と同じ書き方)を踏襲する。

```kotlin
package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CardTypePatternSelector @Inject constructor() {

    /**
     * Tries each pattern in priority order and returns the first one the hand can satisfy,
     * as [picked cards in pattern order] + [every other hand card, in hand order] —
     * the same "always return the whole hand" contract as [ApplyBraveChains.pick]. The
     * caller reads only the leading `3 - npUsage.nps.size` entries as the actual selection.
     *
     * Returns null when no pattern is satisfiable, so the caller falls back to the existing
     * CardPriority + ApplyBraveChains pipeline unchanged.
     */
    fun select(
        cards: List<ParsedCard>,
        patterns: List<CardTypePattern>,
        cardPriority: CardPriorityPerWave,
        stage: Int,
        npUsage: NPUsage = NPUsage.none
    ): List<ParsedCard>? {
        for (pattern in patterns) {
            val picked = matchPattern(cards, pattern, cardPriority, stage, npUsage) ?: continue
            val notPicked = cards - picked

            return picked + notPicked
        }

        return null
    }

    private fun matchPattern(
        cards: List<ParsedCard>,
        pattern: CardTypePattern,
        cardPriority: CardPriorityPerWave,
        stage: Int,
        npUsage: NPUsage
    ): List<ParsedCard>? {
        val requiredTypes = requiredFaceCardTypes(pattern, npUsage)
        val priorityOrder = cardPriority.atWave(stage)

        val remainingCards = cards.toMutableList()
        val picked = mutableListOf<ParsedCard>()

        for (requiredType in requiredTypes) {
            val candidates = remainingCards.filter { it.type == requiredType }
            if (candidates.isEmpty()) return null

            // minByOrNull cannot return null here since candidates is non-empty (checked
            // above); it also keeps the first element on ties, so candidates' hand order
            // becomes the tiebreaker once CardPriority itself is tied.
            val chosenCard = candidates.minByOrNull { priorityRank(it, priorityOrder) }!!

            picked += chosenCard
            remainingCards -= chosenCard
        }

        return picked
    }

    // CardPriority.of() only validates that a custom priority string names exactly 9 cards; it
    // does not validate that all 9 (type, affinity) combinations are distinct. If a card's score
    // is absent from priorityOrder, List.indexOf would return -1, which minByOrNull would then
    // treat as the *highest* priority. That contradicts this codebase's existing convention for
    // "not in the list" (CardPriorityPerWave.atWave() appends CardTypeEnum.Unknown at the end
    // specifically to give it minimum priority), so an absent score is ranked last here too.
    private fun priorityRank(card: ParsedCard, priorityOrder: List<CardScore>): Int =
        priorityOrder.indexOf(CardScore(card.type, card.affinity)).let {
            if (it == -1) priorityOrder.size else it
        }

    // A turn has exactly 3 attack slots. When npUsage.nps is non-empty, that many slots
    // are spent tapping NPs instead of face cards. npUsage.cardsBeforeNP only decides how
    // many of the *remaining* face cards are tapped before vs after those NP taps — it
    // doesn't change which face cards are needed, so it's intentionally not read here.
    // (Derivation: inserting npCount NP slots at index cardsBeforeNP and then keeping only
    // the first 3 always keeps pattern[0 until 3 - npCount) and drops pattern's tail,
    // regardless of cardsBeforeNP's value, because before-slice ++ after-slice telescopes
    // to that same contiguous range for any valid cardsBeforeNP.) Splitting the result into
    // before/after-NP groups is the responsibility of whoever wires this into Card.kt.
    private fun requiredFaceCardTypes(pattern: CardTypePattern, npUsage: NPUsage): List<CardTypeEnum> {
        val npCount = npUsage.nps.size

        return pattern.take(3 - npCount)
    }
}
```

**命名根拠**: `select`は同モジュール内の既存の役割動詞(`Support.selectSupport()`, `PartySelection.selectParty()`)に揃えた。`matchPattern`/`requiredFaceCardTypes`は「何をして値を得るか」を動詞で明示している(`code-naming`規則1)。`k`・`n`のような1文字変数は使わず、`npCount`・`requiredType`・`priorityOrder`・`chosenCard`と対象を明示する語にした(規則7)。`picked`/`notPicked`は既存の`ApplyBraveChains.pick()`と同じ対語(規則6・既存踏襲)。

### 2.6 `app/src/main/java/io/github/fate_grand_automata/ui/exit/BattleExit.kt`(変更、機械的な1行)

**なぜこの変更が必要か**: このファイルの`AutoBattle.ExitReason.text()`(`BattleExit.kt:57-81`)は、`sealed class ExitReason`を`when(this) { ... }`で**網羅的に**(`else`節なしで)分岐している。Kotlinはsealed classの`when`を網羅性チェックの対象にするため、`ExitReason`に新しいサブクラス(`CardTypePatternParseError`)を追加すると、この`when`式に対応する分岐が無い限り**`app`モジュールがコンパイルエラーになる**。

当初のIssue原案は「`app`モジュールには一切触れない」としていたが、これは実際のコードと矛盾していた。この矛盾はセッション内でユーザーに確認済みで、**既存の`CardPriorityParseError`と全く同じ形の1行を追加することを機械的な例外として許容する**、という結論になっている(Issue #1本文もこの結論に沿って更新済み)。UIの見た目・挙動は一切変わらない(この新しい`ExitReason`はまだどこからも投げられないため、実行時にこの分岐が使われることもない)。

現状(`BattleExit.kt:75-77`):

```kotlin
    is AutoBattle.ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${cause?.message}"
    is AutoBattle.ExitReason.CardPriorityParseError -> msg
    AutoBattle.ExitReason.FirstClearRewards -> stringResource(R.string.first_clear_rewards)
```

`is AutoBattle.ExitReason.CardPriorityParseError -> msg` の行の直後に1行追加する:

```kotlin
    is AutoBattle.ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${cause?.message}"
    is AutoBattle.ExitReason.CardPriorityParseError -> msg
    is AutoBattle.ExitReason.CardTypePatternParseError -> msg
    AutoBattle.ExitReason.FirstClearRewards -> stringResource(R.string.first_clear_rewards)
```

これ以外の変更はしない。文字列リソース(`strings.xml`)の追加も不要(`CardPriorityParseError`と同様、`msg`フィールドをそのまま表示するため)。

---

## 3. テスト方針

### 3.1 テスト対象・配置場所

`scripts/src/test/java/io/github/fate_grand_automata/scripts/` 配下に、既存の`FaceCardPriorityTest.kt`・`BraveChainsTest.kt`と同じ作法(`kotlin.test.Test`、`assertk`、DIコンテナなしで対象クラスを直接`new`する、`ParsedCard`のフィクスチャを直接組み立てる)で配置する。

`CardTypePatternSelectorTest.kt`は、既存の`FaceCardPriorityTest.lineup1`(同一パッケージのため`import`不要、`FaceCardPriorityTest.lineup1`で直接参照可能)を基本の手札として再利用する。新規に専用フィクスチャを組み立てるのは3ケースのみ: スタン中カードのケース(`lineup1`にスタン中カードが存在しないため必須)、同じ型を2枚必要とするケース(`lineup1`でも「QAQ」パターンなら表現できるが、タイブレークの計算が絡まないよう3枚だけの最小手札にして、検証したい「同一カードを2回選ばない」という1点だけに読者の注意を絞る)、そして`CardPriority`に存在しないスコアを持つカードのケース(2.5節の`priorityRank`が対象。`lineup1`は`CardPriorityPerWave.default`の9種類すべてを前提にしており、意図的に不完全な`CardPriority`を渡すシナリオを表現できないため専用フィクスチャが必要)。

### 3.2 利用するライブラリAPI(現在インストールされているバージョンでの正しい書き方)

すべて `assertk 0.28.1`(`gradle/libs.versions.toml:3` `assertk_version = "0.28.1"`)で確認済み。

| API | パッケージ | 一次情報 |
|---|---|---|
| `assertThat(actual)` | `assertk.assertThat` | https://github.com/assertk-org/assertk |
| `containsExactly(vararg elements)` (`Assert<List<*>>`拡張、順序込みで完全一致を検証) | `assertk.assertions.containsExactly` | https://github.com/assertk-org/assertk/blob/v0.28.1/assertk/src/commonMain/kotlin/assertk/assertions/list.kt |
| `isEmpty()` (`Assert<Iterable<*>>`拡張) | `assertk.assertions.isEmpty` | https://github.com/assertk-org/assertk/blob/v0.28.1/assertk/src/commonMain/kotlin/assertk/assertions/iterable.kt |
| `isNull()` (`Assert<Any?>`拡張) | `assertk.assertions.isNull` | https://github.com/assertk-org/assertk/blob/v0.28.1/assertk/src/commonMain/kotlin/assertk/assertions/any.kt |
| `isEqualTo(expected)` | `assertk.assertions.isEqualTo` | 既存テスト(`BraveChainsTest.kt:5`)で使用実績あり |
| `Test` アノテーション | `kotlin.test.Test` | https://kotlinlang.org/api/latest/kotlin.test/kotlin.test/-test/ |
| `assertFailsWith<T>(block)` | `kotlin.test.assertFailsWith` | https://kotlinlang.org/api/latest/kotlin.test/kotlin.test/assert-fails-with.html |

`containsExactly`は`Assert<List<*>>`向けの拡張関数であり、`CardTypePattern`/`CardTypePatternPriority`は`List<T> by delegate`で実装されているが**構造的等価性(`equals`/`hashCode`)を持たない**(既存の`CardPriority`と同じ設計、意図的にオーバーライドしていない)。そのため、`CardTypePattern`/`CardTypePatternPriority`の**インスタンス同士を直接比較するアサーションは書かない**。必ず次のいずれかで比較する:

- `.toList()`で具体的な`List<CardTypeEnum>`に変換してから`containsExactly`(内容の型は`enum class`であり参照等価=構造的等価なので安全)
- `.toString()`(`"BAB"`等の正規化された文字列)や`.map { it.toString() }`で文字列比較する
- `ParsedCard`は`equals`をカード枠(`card`)基準でオーバーライド済み(`models/ParsedCard.kt`)なので、`CardTypePatternSelector`の戻り値(`List<ParsedCard>`)は直接`containsExactly`/`isEqualTo`で比較してよい

**`internal`クラスへのテストアクセスについて**: `AutoBattle.BattleExitException`は`internal class`(`AutoBattle.kt:81`)。Kotlin Gradle plugin(`org.jetbrains.kotlin.jvm`、`scripts/build.gradle.kts:6`)は標準で`test`ソースセットのコンパイルに`main`への`friendPaths`を自動設定するため、通常は`src/test`から`internal`宣言を参照できる。ただし本リポジトリにこの前提を裏付ける既存の使用例が無い(既存テストは`internal`宣言を一切参照していない)。もし`assertFailsWith<AutoBattle.BattleExitException>`がアクセス不可でコンパイルエラーになった場合は、`assertFailsWith<Exception>`に緩めるか、`kotlin.test.assertFails { ... }`(型を問わない)に置き換えること。

### 3.3 テストコード全文

#### `CardTypePatternTest.kt`(新規)

```kotlin
package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CardTypePatternTest {
    @Test
    fun parsesThreeLetterPattern() {
        val pattern = CardTypePattern.of("BAB")

        assertThat(pattern.toList()).containsExactly(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Buster)
    }

    @Test
    fun parsesLowercasePatternAsUppercase() {
        val pattern = CardTypePattern.of("bab")

        assertThat(pattern.toList()).containsExactly(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Buster)
    }

    @Test
    fun throwsWhenPatternIsShorterThanThreeCharacters() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BA") }
    }

    @Test
    fun throwsWhenPatternIsLongerThanThreeCharacters() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BABQ") }
    }

    @Test
    fun throwsWhenPatternContainsAnInvalidCharacter() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePattern.of("BAX") }
    }
}
```

#### `CardTypePatternPriorityTest.kt`(新規)

```kotlin
package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.models.CardTypePatternPriority
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CardTypePatternPriorityTest {
    @Test
    fun parsesMultiplePatternsInPriorityOrder() {
        val priority = CardTypePatternPriority.of("BAB, ABB")

        assertThat(priority.map { it.toString() }).containsExactly("BAB", "ABB")
    }

    @Test
    fun throwsWhenAnyPatternInTheListIsInvalid() {
        assertFailsWith<AutoBattle.BattleExitException> { CardTypePatternPriority.of("BAB, XYZ") }
    }

    @Test
    fun parsesBlankStringAsNoPatterns() {
        val priority = CardTypePatternPriority.of("")

        assertThat(priority.toList()).isEmpty()
    }

    @Test
    fun parsesWhitespaceOnlyStringAsNoPatterns() {
        val priority = CardTypePatternPriority.of("   ")

        assertThat(priority.toList()).isEmpty()
    }
}
```

#### `CardTypePatternPriorityPerWaveTest.kt`(新規)

```kotlin
package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.fate_grand_automata.scripts.models.CardTypePatternPriorityPerWave
import kotlin.test.Test

class CardTypePatternPriorityPerWaveTest {
    @Test
    fun returnsThePatternsConfiguredForEachWave() {
        val perWave = CardTypePatternPriorityPerWave.of("BAB\nABB")

        assertThat(perWave.atWave(0).map { it.toString() }).containsExactly("BAB")
        assertThat(perWave.atWave(1).map { it.toString() }).containsExactly("ABB")
    }

    @Test
    fun clampsAWaveNumberPastTheLastConfiguredWaveToTheLastWave() {
        val perWave = CardTypePatternPriorityPerWave.of("BAB\nABB")

        assertThat(perWave.atWave(99).map { it.toString() }).containsExactly("ABB")
    }

    @Test
    fun blankConfigurationLeavesEveryWaveWithNoPatterns() {
        val perWave = CardTypePatternPriorityPerWave.of("")

        assertThat(perWave.atWave(0).toList()).isEmpty()
        assertThat(perWave.atWave(99).toList()).isEmpty()
    }
}
```

#### `CardTypePatternSelectorTest.kt`(新規)

```kotlin
package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.CardTypePatternSelector
import kotlin.test.Test

class CardTypePatternSelectorTest {
    private val selector = CardTypePatternSelector()

    @Test
    fun matchesEachPositionUsingCardPriorityAsTiebreak() {
        // lineup1 = [Scathach WB, Kama Q, Nero RA, Nero RA, Scathach WQ] (FaceCardPriorityTest.kt)
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BQA")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        // B: only Scathach WB. Q: Scathach WQ beats Kama's plain Q under the default
        // priority ("WB,WA,WQ,B,A,Q,RB,RA,RQ"). A: both Nero cards tie (RA=RA), so the
        // one dealt first (Face.C) wins. The two cards never picked trail unordered.
        assertThat(result?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
        )
    }

    @Test
    fun fallsBackToTheNextPatternWhenTheFirstCannotBeSatisfied() {
        // lineup1 only has 1 Buster card, so a pattern needing 3 is unsatisfiable.
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BBB"), CardTypePattern.of("BQA")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result?.take(3)?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C)
        )
    }

    @Test
    fun returnsNullWhenNoPatternCanBeSatisfied() {
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BBB"), CardTypePattern.of("BBQ")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun returnsNullImmediatelyWhenNoPatternsAreConfigured() {
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = emptyList(),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun doesNotReuseTheSamePhysicalCardForARepeatedTypeInThePattern() {
        val firstBuster = ParsedCard(CommandCard.Face.A, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)
        val arts = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Arts)
        val secondBuster = ParsedCard(CommandCard.Face.C, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)

        val result = selector.select(
            cards = listOf(firstBuster, arts, secondBuster),
            patterns = listOf(CardTypePattern.of("BAB")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C)
        )
    }

    @Test
    fun excludesAStunnedCardFromMatchingAnyRequiredType() {
        // CardTypePatternSelector never reads ParsedCard.isStunned. This works because
        // CardParser.kt:74-76 always reports a stunned card's type as CardTypeEnum.Unknown,
        // and CardTypePattern.of() can never require Unknown (only B/A/Q), so a stunned
        // card structurally cannot satisfy any requiredType. isStunned = true is set below
        // only to mirror how CardParser actually produces this ParsedCard.
        val stunned = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Unknown,
            isStunned = true
        )
        val onlyArts = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Arts)

        val result = selector.select(
            cards = listOf(stunned, onlyArts),
            patterns = listOf(CardTypePattern.of("BAB")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun requiresFewerCardsAsMoreNpsAreUsedThisTurn() {
        val pattern = CardTypePattern.of("BQA")

        val noNp = selector.select(FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0)
        val oneNp = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A), 0)
        )
        val twoNps = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        )
        val threeNps = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0)
        )

        // 0 NPs: all 3 positions (B, Q, A) need a matching card.
        assertThat(noNp?.take(3)?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C)
        )
        // 1 NP: only the pattern's leading 2 positions (B, Q) need a matching card.
        assertThat(oneNp?.take(2)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.A, CommandCard.Face.E))
        // 2 NPs: only the pattern's leading 1 position (B) needs a matching card.
        assertThat(twoNps?.take(1)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.A))
        // 3 NPs: no face card is required at all, so every hand trivially satisfies it
        // and the hand comes back exactly as dealt.
        assertThat(threeNps).isEqualTo(FaceCardPriorityTest.lineup1)
    }

    @Test
    fun ranksACardWhoseScoreIsAbsentFromCardPriorityLastNotFirst() {
        val weakBuster = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val normalBuster = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)

        // "B" repeated 9 times only ever names CardScore(Buster, Normal); CardScore(Buster,
        // Weak) never appears in this priority list even though it has the required length.
        val incompletePriority = CardPriorityPerWave.of("B,B,B,B,B,B,B,B,B")

        val result = selector.select(
            cards = listOf(weakBuster, normalBuster),
            patterns = listOf(CardTypePattern.of("BBB")),
            cardPriority = incompletePriority,
            stage = 0,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        )

        // Only 1 required type (pattern.take(3 - 2 nps) = 1 Buster). normalBuster's score is
        // listed (rank 0); weakBuster's score is absent and must lose the tiebreak, not win it.
        assertThat(result?.take(1)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.B))
    }
}
```

**テスト方針の根拠**(`code-comments` skill準拠): テスト名だけを並べて読んで仕様が分かる名前にした(例: `fallsBackToTheNextPatternWhenTheFirstCannotBeSatisfied`)。テスト本体には検証したいことだけを残し、期待値の根拠(タイブレークがどちらに転ぶか等)が自明でない箇所にだけコメントを添えた。

---

## 4. 品質管理の実行手順

このリポジトリのルート `CLAUDE.md` は `@AGENTS.md` を読み込む構成で、「品質管理の実行手順」という見出しそのものは存在しない。最も近い正式な手順は `AGENTS.md` の "Build & test"節であり、本実装計画ではそれを品質管理手順として扱う。

実装後、以下を順に実行する:

```bash
./gradlew :app:compileDebugKotlin
```
→ `ExitReason`への新規ケース追加が`app`モジュールの網羅的な`when`(`BattleExit.kt`、セクション2.6)を壊していないことを確認する。セクション2.6の1行を追加し忘れると、このコマンドが失敗する。

```bash
./gradlew :scripts:test --tests '*CardTypePattern*'
```
→ 今回追加した4つのテストクラス(`CardTypePatternTest`, `CardTypePatternPriorityTest`, `CardTypePatternPriorityPerWaveTest`, `CardTypePatternSelectorTest`)のみを実行する。

```bash
./gradlew :scripts:test
```
→ `scripts`モジュールの既存テスト(`FaceCardPriorityTest`, `BraveChainsTest`等)を含めて全体を実行し、既存のカード選択ロジックに影響がないことを確認する。

```bash
./gradlew lint
```
→ `abortOnError = false`のためビルドは失敗しないが、出力される警告に今回の変更に起因するものがないか目視確認する。

**確認すべき合格基準**: 上記4コマンドすべてが正常終了し、`:scripts:test`のテストレポート(`scripts/build/test-results/`)に失敗が0件であること。

---

## 5. ブラウザでの動作確認について(非該当)

この変更はAndroidアプリの`scripts`モジュール(Android非依存の純粋JVM Kotlin)に閉じたロジック追加であり、開発サーバー・Webページ・Storybook等のブラウザで確認できる対象が存在しない。`Card.kt`への配線・UIへの反映は次のIssueのスコープであり、本Issueの時点ではエンドユーザーが観測できる挙動の変化は一切ない(既存の動作は完全に不変)。

代替として、上記「品質管理の実行手順」の`./gradlew :scripts:test --tests '*CardTypePattern*'`が全テスト成功することを、実装が正しく動作していることの確認手段とする(既存の`FaceCardPriorityTest`/`BraveChainsTest`が担っている役割と同じ)。戻り値を目視確認するための簡易スクリプトなどは別途書く必要はない。

---

## 6. 実装順序の推奨

依存関係の順に実装するとビルドが常に通る状態を保てる:

1. `models/CardTypePattern.kt` + `AutoBattle.kt`の`ExitReason`追加 + `BattleExit.kt`の1行追加(セクション2.6)。この3つは`CardTypePattern`が`ExitReason.CardTypePatternParseError`に依存し、それが`app`の網羅的`when`に波及するため、まとめて1つの変更として扱う
2. `CardTypePatternTest.kt`
3. `models/CardTypePatternPriority.kt` → `CardTypePatternPriorityTest.kt`
4. `models/CardTypePatternPriorityPerWave.kt` → `CardTypePatternPriorityPerWaveTest.kt`
5. `modules/CardTypePatternSelector.kt` → `CardTypePatternSelectorTest.kt`
6. 品質管理の実行手順(セクション4)を実行

---

## 7. 提出前のセルフチェック

- [x] 実装計画に登場するファイルパスはすべて実在するか確認済み(`CardPriority.kt`, `CardPriorityPerWave.kt`, `FaceCardPriority.kt`, `ApplyBraveChains.kt`, `AutoBattle.kt`, `ParsedCard.kt`, `CommandCard.kt`, `FaceCardPriorityTest.kt`, `BraveChainsTest.kt`, `BattleExit.kt`, `scripts/build.gradle.kts`をすべて実物で確認)。新規作成するファイルはすべて「新規」と明記した。
- [x] `ExitReason`への新規ケース追加が`app`モジュールの網羅的な`when`(`BattleExit.kt:57-81`)を壊す、という依存関係を実際に検索して発見し、対応を計画に含めた(当初のIssue本文にはこの依存関係の記載が無かった)。
- [x] `CardTypePatternSelectorTest.kt`の全アサーションを手計算で1件ずつ検算し、期待値が実装のロジックと一致することを確認した(タイブレーク・NP枚数ごとの必要種別数・重複型の非再利用・スタン除外・`CardPriority`に無いスコアの最低優先度化を含む)。
- [x] 参照する関数・フィールド名(`CardPriorityParseError(val msg: String)`の`msg`、`ApplyBraveChains`の`@ScriptScope @Inject constructor()`、`ParsedCard`の`equals`実装等)はすべて実物のソースコードから確認したものであり、推測を含まない。
- [x] `assertk`の全アサーション(`containsExactly`, `isEmpty`, `isNull`, `isEqualTo`)は`v0.28.1`タグのソースで存在を確認済み、一次情報URLを記載した。
- [x] `CardTypePattern`/`CardTypePatternPriority`が構造的等価性を持たない(既存の`CardPriority`と同じ設計)という、テストコードを書く際に踏みやすい罠を明記した。
- [x] Issue #1本文を、当初の`cardsBeforeNP`挿入方式から本計画の簡略化設計(`npUsage.nps.size`のみ使用)・`BattleExit.kt`への1行追加の2点について更新済み(2026-09-02)。Issue本文と本計画に矛盾は残っていない。
- [x] 本セッションでしか分からない前提は残っていない(CONTEXT.mdへの参照、Issueへの参照はいずれも実在するリソースへのポインタ)。

using System;
using System.Collections.Generic;

namespace FateGrandAutomata
{
    public class Card
    {
        public AutoSkill AutoSkill { get; private set; }
        public Battle Battle { get; private set; }

        readonly List<CardScore> _cardPriorityArray = new List<CardScore>();

        public void Init(AutoSkill AutoSkillModule, Battle BattleModule)
        {
            AutoSkill = AutoSkillModule;
            Battle = BattleModule;

            InitCardPriorityArray();
        }

        void InitCardPriorityArray()
        {
            var errorString = "Battle_CardPriority Error at '";

            if (Preferences.BattleCardPriority.Length == 3)
            {
                InitCardPriorityArraySimple(errorString);
            }
            else InitCardPriorityArrayDetailed(errorString);
        }

        void InitCardPriorityArraySimple(string ErrorString)
        {
            foreach (var card in Preferences.BattleCardPriority)
            {
                var score = CardScore.Normal;

                switch (card)
                {
                    case 'B':
                        score = CardScore.Buster;
                        break;

                    case 'A':
                        score = CardScore.Arts;
                        break;

                    case 'Q':
                        score = CardScore.Quick;
                        break;

                    default:
                        throw new FormatException($"{ErrorString}{card}': Only 'B', 'A' and 'Q' are allowed in simple mode.");
                }

                _cardPriorityArray.Add(score | CardScore.Weak);
                _cardPriorityArray.Add(score);
                _cardPriorityArray.Add(score | CardScore.Resist);
            }
        }

        void InitCardPriorityArrayDetailed(string ErrorString)
        {
            var cardCounter = 0;

            foreach (var i in Preferences.BattleCardPriority.Split(','))
            {
                var card = i.ToUpper().Trim();

                if (card.Length < 1 || card.Length > 2)
                {
                    throw new FormatException($"{ErrorString}{card}': Invalid card length.");
                }

                var score = CardScore.Normal;

                switch (card.Length == 1 ? card[0] : card[1])
                {
                    case 'B':
                        score |= CardScore.Buster;
                        break;

                    case 'A':
                        score |= CardScore.Arts;
                        break;

                    case 'Q':
                        score |= CardScore.Quick;
                        break;

                    default:
                        throw new FormatException($"{ErrorString}{card}': Only 'B', 'A' and 'Q' are valid card types.");
                }

                if (card.Length == 2)
                {
                    switch (card[0])
                    {
                        case 'W':
                            score |= CardScore.Weak;
                            break;

                        case 'R':
                            score |= CardScore.Resist;
                            break;

                        default:
                            throw new FormatException($"{ErrorString}{card}': Only 'W', and 'R' are valid card affinities.");
                    }
                }

                _cardPriorityArray.Add(score);
                ++cardCounter;
            }

            if (cardCounter != 9)
            {
                throw new FormatException($"{ErrorString}{Preferences.BattleCardPriority}': Expected 9 cards, but {cardCounter} found.");
            }
        }

        CardScore GetCardAffinity(Region Region)
        {
            if (Region.Exists(ImageLocator.Weak))
            {
                return CardScore.Weak;
            }

            if (Region.Exists(ImageLocator.Resist))
            {
                return CardScore.Resist;
            }

            return CardScore.Normal;
        }

        CardScore GetCardType(Region Region)
        {
            if (Region.Exists(ImageLocator.Buster))
            {
                return CardScore.Buster;
            }

            if (Region.Exists(ImageLocator.Art))
            {
                return CardScore.Arts;
            }

            if (Region.Exists(ImageLocator.Quick))
            {
                return CardScore.Quick;
            }

            Game.Impl.Toast($"Failed to determine Card type (X: {Region.X}, Y: {Region.Y}, W: {Region.W}, H: {Region.H})");

            return CardScore.Buster;
        }

        Dictionary<CardScore, List<int>> GetCommandCards()
        {
            var storagePerPriority = new Dictionary<CardScore, List<int>>();

            Game.Impl.UseSameSnapIn(() =>
            {
                for (var cardSlot = 0; cardSlot < 5; ++cardSlot)
                {
                    var score = GetCardAffinity(Game.BattleCardAffinityRegionArray[cardSlot])
                                | GetCardType(Game.BattleCardTypeRegionArray[cardSlot]);

                    if (!storagePerPriority.ContainsKey(score))
                    {
                        storagePerPriority.Add(score, new List<int>());
                    }

                    storagePerPriority[score].Add(cardSlot);
                }
            });

            return storagePerPriority;
        }

        public void ClickCommandCards()
        {
            var commandCards = GetCommandCards();

            foreach (var cardPriority in _cardPriorityArray)
            {
                if (!commandCards.ContainsKey(cardPriority))
                    continue;

                var currentCardTypeStorage = commandCards[cardPriority];

                foreach (var cardSlot in currentCardTypeStorage)
                {
                    Game.BattleCommandCardClickArray[cardSlot].Click();
                }
            }
        }

        public bool CanClickNpCards
        {
            get
            {
                var weCanSpam = Preferences.BattleNoblePhantasm == BattleNoblePhantasmType.Spam;
                var weAreInDanger = Preferences.BattleNoblePhantasm == BattleNoblePhantasmType.Danger
                                    && Battle.HasChoosenTarget;

                return (weCanSpam || weAreInDanger) && AutoSkill.IsFinished;
            }
        }

        public void ClickNpCards()
        {
            foreach (var npCard in Game.BattleNpCardClickArray)
            {
                npCard.Click();
            }
        }
    }
}
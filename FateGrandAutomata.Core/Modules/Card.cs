using System.Collections.Generic;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class Card
    {
        public AutoSkill AutoSkill { get; private set; }
        public Battle Battle { get; private set; }

        readonly List<CardScore> _cardPriorityArray = new List<CardScore>();

        Dictionary<CardScore, List<int>> _commandCards = new Dictionary<CardScore, List<int>>();
        int _cardsClickedSoFar;

        bool chains;

        public void Init(AutoSkill AutoSkillModule, Battle BattleModule)
        {
            AutoSkill = AutoSkillModule;
            Battle = BattleModule;

            InitCardPriorityArray();
        }

        void InitCardPriorityArray()
        {
            var errorString = "Battle_CardPriority Error at '";

            if (Preferences.Instance.BattleCardPriority.Length == 3)
            {
                InitCardPriorityArraySimple(errorString);
            }
            else InitCardPriorityArrayDetailed(errorString);
        }

        void InitCardPriorityArraySimple(string ErrorString)
        {
            foreach (var card in Preferences.Instance.BattleCardPriority)
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
                        throw new ScriptExitException($"{ErrorString}{card}': Only 'B', 'A' and 'Q' are allowed in simple mode.");
                }

                _cardPriorityArray.Add(score | CardScore.Weak);
                _cardPriorityArray.Add(score);
                _cardPriorityArray.Add(score | CardScore.Resist);
            }
        }

        void InitCardPriorityArrayDetailed(string ErrorString)
        {
            var cardCounter = 0;

            foreach (var i in Preferences.Instance.BattleCardPriority.Split(','))
            {
                var card = i.ToUpper().Trim();

                if (card.Length < 1 || card.Length > 2)
                {
                    throw new ScriptExitException($"{ErrorString}{card}': Invalid card length.");
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
                        throw new ScriptExitException($"{ErrorString}{card}': Only 'B', 'A' and 'Q' are valid card types.");
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
                            throw new ScriptExitException($"{ErrorString}{card}': Only 'W', and 'R' are valid card affinities.");
                    }
                }

                _cardPriorityArray.Add(score);
                ++cardCounter;
            }

            if (cardCounter != 9)
            {
                throw new ScriptExitException($"{ErrorString}{Preferences.Instance.BattleCardPriority}': Expected 9 cards, but {cardCounter} found.");
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

            AutomataApi.Toast($"Failed to determine Card type (X: {Region.X}, Y: {Region.Y}, W: {Region.W}, H: {Region.H})");

            return CardScore.Buster;
        }

        Dictionary<CardScore, List<int>> GetCommandCards()
        {
            var storagePerPriority = new Dictionary<CardScore, List<int>>();

            AutomataApi.UseSameSnapIn(() =>
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

        public void ClickCommandCards(int Clicks)
        {
            if (chains)
            {
                return;
            }

            var i = 1;

            foreach (var cardPriority in _cardPriorityArray)
            {
                if (!_commandCards.ContainsKey(cardPriority))
                    continue;

                var currentCardTypeStorage = _commandCards[cardPriority];

                foreach (var cardSlot in currentCardTypeStorage)
                {
                    if (Clicks < i)
                    {
                        _cardsClickedSoFar = i - 1;
                        return;
                    }

                    if (i > _cardsClickedSoFar)
                    {
                        Game.BattleCommandCardClickArray[cardSlot].Click();
                    }

                    ++i;
                }
            }
        }

        public bool CanClickNpCards
        {
            get
            {
                var weCanSpam = Preferences.Instance.BattleNoblePhantasm == BattleNoblePhantasmType.Spam;
                var weAreInDanger = Preferences.Instance.BattleNoblePhantasm == BattleNoblePhantasmType.Danger
                                    && Battle.HasChoosenTarget;

                return (weCanSpam || weAreInDanger) && AutoSkill.IsFinished;
            }
        }

        public bool ClickNpCards()
        {
            var npsClicked = false;

            foreach (var npCard in Game.BattleNpCardClickArray)
            {
                npCard.Click();

                npsClicked = true;
            }

            return npsClicked;
        }

        public void ReadCommandCards()
        {
            _commandCards = GetCommandCards();
        }

        public void ResetCommandCards()
        {
            _commandCards = new Dictionary<CardScore, List<int>>();
            _cardsClickedSoFar = 0;
        }
    }
}
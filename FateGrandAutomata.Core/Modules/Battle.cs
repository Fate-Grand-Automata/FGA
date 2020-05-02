using System.Linq;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class Battle
    {
        bool _hasTakenFirstStageSnapshot;

        public bool HasClickedAttack { get; private set; }

        bool _chaining,
            chaining;

        bool? chains;

        int _npsClicked,
            _servantChain;

        public bool HasChoosenTarget { get; private set; }

        public int CurrentStage { get; private set; } = -1;
        public int CurrentTurn { get; private set; } = -1;

        public AutoSkill AutoSkill { get; private set; }

        public Card Card { get; private set; }

        public void Init(AutoSkill AutoSkillModule, Card CardModule)
        {
            AutoSkill = AutoSkillModule;
            Card = CardModule;

            chaining = chains ?? false;

            ResetState();
        }

        public void ResetState()
        {
            AutoSkill.ResetState();

            CurrentStage = CurrentTurn = -1;

            _hasTakenFirstStageSnapshot = HasChoosenTarget = HasClickedAttack = false;
            _chaining = chaining;
            _servantChain = 0;
            _npsClicked = 0;
        }

        public bool IsIdle()
        {
            return Game.BattleScreenRegion.Exists(ImageLocator.Battle);
        }

        public void ClickAttack()
        {
            Game.BattleAttackClick.Click();

            // Although it seems slow, make it no shorter than 1 sec to protect user with less processing power devices.
            AutomataApi.Wait(1.5);

            HasClickedAttack = true;

            Card.ReadCommandCards();
        }

        bool IsPriorityTarget(Region Target)
        {
            var isDanger = Target.Exists(ImageLocator.TargetDanger);
            var isServant = Target.Exists(ImageLocator.TargetServant);

            return isDanger || isServant;
        }

        void ChooseTarget(int Index)
        {
            Game.BattleTargetClickArray[Index].Click();

            AutomataApi.Wait(0.5);

            Game.BattleExtrainfoWindowCloseClick.Click();

            HasChoosenTarget = true;
        }

        void OnStageChanged()
        {
            ++CurrentStage;
            CurrentTurn = -1;
            HasChoosenTarget = false;
        }

        void AutoChooseTarget()
        {
            // from my experience, most boss stages are ordered like(Servant 1)(Servant 2)(Servant 3),
            // where(Servant 3) is the most powerful one. see docs/ boss_stage.png
            // that's why the table is iterated backwards.

            var i = 2;

            foreach (var target in Game.BattleTargetRegionArray.Reverse())
            {
                if (IsPriorityTarget(target))
                {
                    ChooseTarget(i);
                    return;
                }

                --i;
            }
        }

        public void PerformBattle()
        {
            AutomataApi.UseSameSnapIn(OnTurnStarted);
            AutomataApi.Wait(2);

            var wereNpsClicked = false;

            if (Preferences.Instance.EnableAutoSkill)
            {
                wereNpsClicked = AutoSkill.Execute();

                AutoSkill.ResetNpTimer();
            }

            if (!HasClickedAttack)
            {
                ClickAttack();
            }

            if (Card.CanClickNpCards)
            {
                // We shouldn't do the long wait due to NP spam/danger modes
                // They click on NPs even when not charged
                // So, don't assign wereNpsClicked here
                Card.ClickNpCards();
            }

            Card.ClickCommandCards(5);

            Card.ResetCommandCards();

            AutomataApi.Wait(wereNpsClicked ? 25 : 5);
        }

        void OnTurnStarted()
        {
            CheckCurrentStage();

            ++CurrentTurn;

            HasClickedAttack = false;

            if (!HasChoosenTarget && Preferences.Instance.BattleAutoChooseTarget)
            {
                AutoChooseTarget();
            }
        }

        void CheckCurrentStage()
        {
            if (!_hasTakenFirstStageSnapshot || DidStageChange())
            {
                OnStageChanged();

                TakeStageSnapshot();
            }
        }

        IPattern _generatedStageCounterSnapshot;

        bool DidStageChange()
        {
            // Alternative fix for different font of stage count number among different regions, worked pretty damn well tho.
            // This will compare last screenshot with current screen, effectively get to know if stage changed or not.

            return !Game.BattleStageCountRegion.Exists(_generatedStageCounterSnapshot, Similarity: 0.85);
        }

        void TakeStageSnapshot()
        {
            _generatedStageCounterSnapshot?.Dispose();

            // It is important that the image gets cloned here.
            _generatedStageCounterSnapshot = Game.BattleStageCountRegion.GetPattern();

            _hasTakenFirstStageSnapshot = true;
        }

        void BraveChainNp(int NoOfNp)
        {
            _servantChain = NoOfNp - 3;
            ++_npsClicked;
            _chaining = chaining;

            // TODO: Check if this is the variable to use
            if (_npsClicked > 1)
            {
                _chaining = false;
            }
        }
    }
}

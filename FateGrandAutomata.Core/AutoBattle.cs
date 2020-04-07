using System;
using System.Linq;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class AutoBattle : EntryPoint
    {
        readonly Support _support = new Support();
        readonly Card _card = new Card();
        readonly Battle _battle = new Battle();
        readonly AutoSkill _autoSkill = new AutoSkill();

        int _stonesUsed;
        bool _isContinuing;

        void RefillStamina()
        {
            if (Preferences.Instance.Refill.Enabled && _stonesUsed < Preferences.Instance.Refill.Repetitions)
            {
                switch (Preferences.Instance.Refill.Resource)
                {
                    case RefillResource.SQ:
                        Game.StaminaSqClick.Click();
                        break;

                    case RefillResource.AllApples:
                        Game.StaminaBronzeClick.Click();
                        Game.StaminaSilverClick.Click();
                        Game.StaminaGoldClick.Click();
                        break;

                    case RefillResource.Gold:
                        Game.StaminaGoldClick.Click();
                        break;

                    case RefillResource.Silver:
                        Game.StaminaSilverClick.Click();
                        break;

                    case RefillResource.Bronze:
                        Game.StaminaBronzeClick.Click();
                        break;
                }

                AutomataApi.Wait(1);
                Game.StaminaOkClick.Click();
                ++_stonesUsed;

                AutomataApi.Wait(3);
            }
            else throw new ScriptExitException("AP ran out!");
        }

        bool NeedsToWithdraw()
        {
            return Game.WithdrawRegion.Exists(ImageLocator.Withdraw);
        }

        void Withdraw()
        {
            if (!Preferences.Instance.WithdrawEnabled)
            {
                throw new ScriptExitException("All servants have been defeated and auto-withdrawing is disabled.");
            }

            Game.WithdrawRegion.Click();

            AutomataApi.Wait(0.5);

            // Click the "Accept" button after choosing to withdraw
            Game.WithdrawAcceptClick.Click();

            AutomataApi.Wait(1);

            // Click the "Close" button after accepting the withdrawal
            Game.StaminaBronzeClick.Click();
        }

        // Click begin quest in Formation selection, then select boost item, if applicable, then confirm selection.
        void StartQuest()
        {
            Game.MenuStartQuestClick.Click();

            AutomataApi.Wait(2);

            Game.MenuBoostItemClickArray[Preferences.Instance.BoostItemSelectionMode].Click();

            // in case you run out of items
            Game.MenuBoostItemSkipClick.Click();

            if (Preferences.Instance.StorySkip)
            {
                AutomataApi.Wait(10);

                if (Game.MenuStorySkipRegion.Exists(ImageLocator.StorySkip))
                {
                    Game.MenuStorySkipClick.Click();
                    AutomataApi.Wait(0.5);
                    Game.MenuStorySkipYesClick.Click();
                }
            }
        }

        // Checking if in menu.png is on screen, indicating you are in the screen to choose your quest
        bool IsInMenu()
        {
            return Game.MenuScreenRegion.Exists(ImageLocator.Menu);
        }

        // Reset battle state, then click quest and refill stamina if needed.
        void Menu()
        {
            _battle.ResetState();

            AutomataApi.Toast($"{_stonesUsed} refills used out of {Preferences.Instance.Refill.Repetitions}");

            // Click uppermost quest
            Game.MenuSelectQuestClick.Click();
            AutomataApi.Wait(1.5);

            // Auto refill
            while (Game.StaminaScreenRegion.Exists(ImageLocator.Stamina))
            {
                RefillStamina();
            }
        }

        // Checking if Quest Completed screen is up, specifically if Bond point/reward is up.
        bool IsInResult()
        {
            return Game.ResultScreenRegion.Exists(ImageLocator.Result)
                || Game.ResultBondRegion.Exists(ImageLocator.Bond);
        }

        // Click through reward screen, continue if option presents itself, otherwise continue clicking through
        void Result()
        {
            // Validator document https://github.com/29988122/Fate-Grand-Order_Lua/wiki/In-Game-Result-Screen-Flow for detail.
            AutomataApi.ContinueClick(Game.ResultNextClick, 55);

            // Checking if there was a Bond CE reward
            if (Game.ResultCeRewardRegion.Exists(ImageLocator.Bond10Reward))
            {
                if (Preferences.Instance.StopAfterBond10)
                {
                    throw new ScriptExitException("Bond 10 CE GET!");
                }

                Game.ResultCeRewardCloseClick.Click();

                // Still need to proceed through reward screen.
                AutomataApi.ContinueClick(Game.ResultNextClick, 35);
            }

            AutomataApi.Wait(5);

            // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
            if (Game.ResultFriendRequestRegion.Exists(ImageLocator.FriendRequest))
            {
                Game.ResultFriendRequestRejectClick.Click();
            }

            AutomataApi.Wait(1);

            // Only for JP currently. Searches for the Continue option after select Free Quests
            if (Preferences.Instance.GameServer == GameServer.Jp && Game.ContinueRegion.Exists(ImageLocator.Confirm))
            {
                // Needed to show we don't need to enter the "StartQuest" function
                _isContinuing = true;

                // Pressing Continue option after completing a quest, reseting the state as would occur in "Menu" function
                Game.ContinueClick.Click();
                _battle.ResetState();

                AutomataApi.Wait(1.5);

                // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
                while (Game.StaminaScreenRegion.Exists(ImageLocator.Stamina))
                {
                    RefillStamina();
                }

                return;
            }

            // Post-battle story is sometimes there.
            if (Preferences.Instance.StorySkip)
            {
                if (Game.MenuStorySkipRegion.Exists(ImageLocator.StorySkip))
                {
                    Game.MenuStorySkipClick.Click();
                    AutomataApi.Wait(0.5);
                    Game.MenuStorySkipYesClick.Click();
                }
            }

            AutomataApi.Wait(10);

            // Quest Completion reward. Exits the screen when it is presented.
            if (Game.ResultCeRewardRegion.Exists(ImageLocator.Bond10Reward))
            {
                Game.ResultCeRewardCloseClick.Click();
                AutomataApi.Wait(1);
                Game.ResultCeRewardCloseClick.Click();
            }

            AutomataApi.Wait(5);

            // 1st time quest reward screen, eg. Mana Prisms, Event CE, Materials, etc.
            if (Game.ResultQuestRewardRegion.Exists(ImageLocator.QuestReward))
            {
                AutomataApi.Wait(1);
                Game.ResultNextClick.Click();
            }
        }

        // Checks if Support Selection menu is up
        bool IsInSupport()
        {
            return Game.SupportScreenRegion.Exists(ImageLocator.SupportScreen, Similarity: 0.85);
        }

        // Selections Support option
        void Support()
        {
            // Friend selection
            var hasSelectedSupport = _support.SelectSupport(Preferences.Instance.Support.SelectionMode);

            if (hasSelectedSupport && !_isContinuing)
            {
                AutomataApi.Wait(4);
                StartQuest();

                // Wait timer till battle starts.
                // Uses less battery to wait than to search for images for a few seconds.
                // Adjust according to device.
                AutomataApi.Wait(10);
            }
        }

        // Initialize Aspect Ratio adjustment for different sized screens,ask for input from user for Autoskill plus confirming Apple/Stone usage
        // Then initialize the Autoskill, Battle, and Card modules in modules.
        void Init()
        {
            Scaling.Init();

            _autoSkill.Init(_battle, _card);
            _battle.Init(_autoSkill, _card);
            _card.Init(_autoSkill, _battle);

            _support.Init();

            AutomataApi.Toast("Will only select servant/danger enemy as noble phantasm target, unless specified using Skill Command. Please check github for further detail.");
        }

        protected override void Script()
        {
            Init();

            while (Preferences.Instance.DebugMode)
            {
                Game.MenuScreenRegion.Highlight(5);
            }

            // SCREENS represents list of Validators and Actors
            // When Validator returns true/1, perform the Actor
            var screens = new (Func<bool> Validator, Action Actor)[]
            {
                (Game.NeedsToRetry, Game.Retry),
                (_battle.IsIdle, _battle.PerformBattle),
                (IsInMenu, Menu),
                (IsInResult, Result),
                (IsInSupport, Support),
                (NeedsToWithdraw, Withdraw)
            };

            // Loop through SCREENS until a Validator returns true/1
            while (true)
            {
                var actor = AutomataApi.UseSameSnapIn(() =>
                {
                    return screens.Where(M => M.Validator())
                        .Select(M => M.Actor)
                        .FirstOrDefault();
                });

                actor?.Invoke();

                AutomataApi.Wait(1);
            }
        }
    }
}
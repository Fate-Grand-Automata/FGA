using System;
using System.Linq;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class Regular : EntryPoint
    {
        readonly Scaling _scaling = new Scaling();
        readonly Support _support = new Support();
        readonly Card _card = new Card();
        readonly Battle _battle = new Battle();
        readonly AutoSkill _autoSkill = new AutoSkill();

        int _stonesUsed;
        int? _isContinuing = 0;

        void RefillStamina()
        {
            if (Preferences.Refill.Enabled && _stonesUsed < Preferences.Refill.Repetitions)
            {
                switch (Preferences.Refill.Resource)
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

        // Click begin quest in Formation selection, then select boost item, if applicable, then confirm selection.
        void StartQuest()
        {
            Game.MenuStartQuestClick.Click();

            AutomataApi.Wait(2);

            Game.MenuBoostItemClickArray[Preferences.BoostItemSelectionMode].Click();

            // in case you run out of items
            Game.MenuBoostItemSkipClick.Click();

            if (Preferences.StorySkip)
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
                if (Preferences.StopAfterBond10)
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
            if (Preferences.GameServer == GameServer.Jp && Game.ContinueRegion.Exists(ImageLocator.Confirm))
            {
                // Needed to show we don't need to enter the "StartQuest" function
                _isContinuing = 1;

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
            if (Preferences.StorySkip)
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
            var hasSelectedSupport = _support.SelectSupport(Preferences.Support.SelectionMode);

            if (hasSelectedSupport)
            {
                if (_isContinuing == null)
                {
                    StartQuest();
                }
                else if (_isContinuing == 0)
                {
                    AutomataApi.Wait(2.5);
                    StartQuest();
                }
            }
        }

        void PsaDialogue()
        {
            AutomataApi.Toast("PsaDialogue");
        }

        // Initialize Aspect Ratio adjustment for different sized screens,ask for input from user for Autoskill plus confirming Apple/Stone usage
        // Then initialize the Autoskill, Battle, and Card modules in modules.
        void Init()
        {
            // Set only ONCE for every separated script run.
            _scaling.ApplyAspectRatioFix(Game.ScriptWidth,
                Game.ScriptHeight,
                Game.ImageWidth,
                Game.ImageHeight);

            PsaDialogue();

            _autoSkill.Init(_battle, _card);
            _battle.Init(_autoSkill, _card);
            _card.Init(_autoSkill, _battle);

            _support.Init();

            AutomataApi.Toast("Will only select servant/danger enemy as noble phantasm target, unless specified using Skill Command. Please check github for further detail.");
        }

        protected override void Script()
        {
            Init();

            // SCREENS represents list of Validators and Actors
            // When Validator returns true/1, perform the Actor
            var screens = new (Func<bool> Validator, Action Actor)[]
            {
                (_battle.IsIdle, _battle.PerformBattle),
                (IsInMenu, Menu),
                (IsInResult, Result),
                (IsInSupport, Support)
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
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class AutoSkill
    {
        Dictionary<char, Action> _currentArray;
        readonly Dictionary<char, Action> _defaultFunctionArray,
            _startingMemberFunctionArray,
            _subMemberFunctionArray,
            _enemyTargetArray,
            _cardsPressed;

        public bool IsFinished { get; private set; }

        public bool NpsClicked { get; private set; }

        const int Normal = 1,
            Chaining = 2;

        public Battle Battle { get; private set; }
        public Card Card { get; private set; }

        void WaitForAnimationToFinish(TimeSpan? Timeout = null)
        {
            var img = ImageLocator.Battle;

            // slow devices need this. do not remove.
            Game.BattleScreenRegion.WaitVanish(img, TimeSpan.FromSeconds(2));

            Game.BattleScreenRegion.Exists(img, Timeout ?? TimeSpan.FromSeconds(5));
        }

        void CastSkill(Location Location)
        {
            Location.Click();

            if (Preferences.Instance.SkillConfirmation)
            {
                Game.BattleSkillOkClick.Click();
            }

            WaitForAnimationToFinish();
        }

        void SelectSkillTarget(Location Location)
        {
            Location.Click();

            AutomataApi.Wait(0.5);

            // Exit any extra menu
            Game.BattleExtrainfoWindowCloseClick.Click();

            WaitForAnimationToFinish();
        }

        void CastNoblePhantasm(Location Location)
        {
            if (!Battle.HasClickedAttack)
            {
                Battle.ClickAttack();

                // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
                AutomataApi.Wait(2);
            }

            /*
                Embed the PreloadNP Feature in this function to as a prerequisite for chaining to be viable.
               
                Problem with Chaining (as of right now). PreloadNP feature clicks cards prior to knowing what NPs are being clicked.
                
                If the NP was known (in this function for instance) then the script can register the face of the NP user first,
                then use that face to determine what cards to select in PreloadNP.
             */

            Location.Click();

            NpsClicked = true;
        }

        void OpenMasterSkillMenu()
        {
            Game.BattleMasterSkillOpenClick.Click();

            AutomataApi.Wait(0.5);
        }

        void CastMasterSkill(Location Location)
        {
            OpenMasterSkillMenu();

            CastSkill(Location);
        }

        void ChangeArray(Dictionary<char, Action> NewArray)
        {
            _currentArray = NewArray;
        }

        void BeginOrderChange()
        {
            OpenMasterSkillMenu();

            Game.BattleMasterSkill3Click.Click();

            if (Preferences.Instance.SkillConfirmation)
            {
                Game.BattleSkillOkClick.Click();
            }

            AutomataApi.Wait(0.3);

            ChangeArray(_startingMemberFunctionArray);
        }

        void SelectStartingMember(Location Location)
        {
            Location.Click();

            ChangeArray(_subMemberFunctionArray);
        }

        void SelectSubMemeber(Location Location)
        {
            Location.Click();

            AutomataApi.Wait(0.3);

            Game.BattleOrderChangeOkClick.Click();

            // Extra wait to allow order change dialog to close
            AutomataApi.Wait(1);

            WaitForAnimationToFinish(TimeSpan.FromSeconds(15));

            // Extra wait for the lag introduced by Order change
            AutomataApi.Wait(1);

            ChangeArray(_defaultFunctionArray);
        }

        void SelectTarget()
        {
            ChangeArray(_enemyTargetArray);
        }

        void SelectEnemyTarget(Location Location)
        {
            Location.Click();

            AutomataApi.Wait(0.5);

            // Exit any extra menu
            Game.BattleExtrainfoWindowCloseClick.Click();

            ChangeArray(_defaultFunctionArray);
        }

        void PreloadNp()
        {
            if (!Battle.HasClickedAttack)
            {
                Battle.ClickAttack();

                // There is a delay after clicking attack before NP Cards come up. DON'T DELETE!
                AutomataApi.Wait(2);
            }

            ChangeArray(_cardsPressed);
        }

        void PressCards(int NoOfCards)
        {
            Card.ClickCommandCards(NoOfCards);

            ChangeArray(_defaultFunctionArray);
        }

        public AutoSkill()
        {
            _defaultFunctionArray = new Dictionary<char, Action>
            {
                ['a'] = () => CastSkill(Game.BattleSkill1Click),
                ['b'] = () => CastSkill(Game.BattleSkill2Click),
                ['c'] = () => CastSkill(Game.BattleSkill3Click),
                ['d'] = () => CastSkill(Game.BattleSkill4Click),
                ['e'] = () => CastSkill(Game.BattleSkill5Click),
                ['f'] = () => CastSkill(Game.BattleSkill6Click),
                ['g'] = () => CastSkill(Game.BattleSkill7Click),
                ['h'] = () => CastSkill(Game.BattleSkill8Click),
                ['i'] = () => CastSkill(Game.BattleSkill9Click),

                ['j'] = () => CastMasterSkill(Game.BattleMasterSkill1Click),
                ['k'] = () => CastMasterSkill(Game.BattleMasterSkill2Click),
                ['l'] = () => CastMasterSkill(Game.BattleMasterSkill3Click),

                ['x'] = BeginOrderChange,
                ['t'] = SelectTarget,
                ['n'] = PreloadNp,

                ['0'] = () => { },

                ['1'] = () => SelectSkillTarget(Game.BattleServant1Click),
                ['2'] = () => SelectSkillTarget(Game.BattleServant2Click),
                ['3'] = () => SelectSkillTarget(Game.BattleServant3Click),

                ['4'] = () => CastNoblePhantasm(Game.BattleNpCardClickArray[0]),
                ['5'] = () => CastNoblePhantasm(Game.BattleNpCardClickArray[1]),
                ['6'] = () => CastNoblePhantasm(Game.BattleNpCardClickArray[2])
            };

            _startingMemberFunctionArray = new Dictionary<char, Action>
            {
                ['1'] = () => SelectStartingMember(Game.BattleStartingMember1Click),
                ['2'] = () => SelectStartingMember(Game.BattleStartingMember2Click),
                ['3'] = () => SelectStartingMember(Game.BattleStartingMember3Click)
            };

            _subMemberFunctionArray = new Dictionary<char, Action>
            {
                ['1'] = () => SelectSubMemeber(Game.BattleSubMember1Click),
                ['2'] = () => SelectSubMemeber(Game.BattleSubMember2Click),
                ['3'] = () => SelectSubMemeber(Game.BattleSubMember3Click)
            };

            _enemyTargetArray = new Dictionary<char, Action>
            {
                ['1'] = () => SelectEnemyTarget(Game.BattleTargetClickArray[0]),
                ['2'] = () => SelectEnemyTarget(Game.BattleTargetClickArray[1]),
                ['3'] = () => SelectEnemyTarget(Game.BattleTargetClickArray[2]),
            };

            _cardsPressed = new Dictionary<char, Action>
            {
                ['1'] = () => PressCards(1),
                ['2'] = () => PressCards(2)
            };
        }

        readonly List<List<string>> _commandTable = new List<List<string>>();

        void InitCommands()
        {
            var stageCount = 0;

            foreach (var commandList in Preferences.Instance.SkillCommand.Split(','))
            {
                if (Regex.IsMatch(commandList, @"[^0]"))
                {
                    if (Regex.IsMatch(commandList, @"^[1-3]"))
                    {
                        throw new ScriptExitException($"Error at '{commandList}': Skill Command cannot start with number '1', '2' and '3'!");
                    }

                    if (Regex.IsMatch(commandList, @"[^,]#") || Regex.IsMatch(commandList, @"#[^,]"))
                    {
                        throw new ScriptExitException($"Error at '{commandList}': '#' must be preceded and followed by ','! Correct: ',#,'");
                    }

                    if (Regex.IsMatch(commandList, @"[^a-l1-6#ntx]"))
                    {
                        throw new ScriptExitException($"Error at '{commandList}': Skill Command exceeded alphanumeric range! Expected 'x', 'n', 't' or range 'a' to 'l' for alphabets and '0' to '6' for numbers.");
                    }
                }

                if (stageCount >= _commandTable.Count)
                {
                    _commandTable.Add(new List<string>());
                }

                if (commandList == "#")
                {
                    ++stageCount;
                }
                else _commandTable[stageCount].Add(commandList);
            }
        }

        public void ResetState()
        {
            IsFinished = !Preferences.Instance.EnableAutoSkill;

            ChangeArray(_defaultFunctionArray);
        }

        public void Init(Battle BattleModule, Card CardModule)
        {
            Battle = BattleModule;
            Card = CardModule;

            if (Preferences.Instance.EnableAutoSkill)
            {
                InitCommands();
            }

            ResetState();
        }

        string GetCommandListFor(int Stage, int Turn)
        {
            if (Stage < _commandTable.Count)
            {
                var commandList = _commandTable[Stage];

                if (Turn < commandList.Count)
                {
                    return commandList[Turn];
                }
            }

            return null;
        }

        void ExecuteCommandList(string CommandList)
        {
            foreach (var command in CommandList)
            {
                _currentArray[command]();
            }
        }

        public void ResetNpTimer()
        {
            NpsClicked = false;
        }

        public bool Execute()
        {
            var commandList = GetCommandListFor(Battle.CurrentStage, Battle.CurrentTurn);

            if (commandList != null)
            {
                ExecuteCommandList(commandList);
            }
            else if (Battle.CurrentStage + 1 >= _commandTable.Count)
            {
                // this will allow NP spam after all commands have been executed
                IsFinished = true;
            }

            return NpsClicked;
        }
    }
}

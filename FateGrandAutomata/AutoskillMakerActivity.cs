using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.Content.Res;
using Android.Graphics;
using Android.OS;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.ConstraintLayout.Widget;
using AndroidX.Core.View;
using Java.Lang;
using AlertDialog = AndroidX.AppCompat.App.AlertDialog;
using StringBuilder = System.Text.StringBuilder;
using Toolbar = AndroidX.AppCompat.Widget.Toolbar;

namespace FateGrandAutomata
{
    [Activity(Label = "Autoskill Maker", Theme = "@style/AppTheme.NoActionBar", ScreenOrientation = ScreenOrientation.Landscape)]
    public class AutoskillMakerActivity : AppCompatActivity
    {
        enum AutoskillMakerState
        {
            Main,
            Atk,
            Target,
            OrderChange
        }

        StringBuilder _skillCmd = new StringBuilder();
        string _npSequence = "";
        AutoskillMakerState _state;
        int _stage = 1, _turn = 1, _xSelectedParty = 1, _xSelectedSub = 1;

        ConstraintLayout _viewMain, _viewAtk, _viewTarget, _viewOrderChange;
        TextView _stageText, _turnText;
        Button[] _xParty, _xSub;

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.autoskill_maker);

            var toolbar = FindViewById<Toolbar>(Resource.Id.autoskill_maker_toolbar);
            SetSupportActionBar(toolbar);

            _viewMain = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_main);
            _viewAtk = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_atk);
            _viewTarget = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_target);
            _viewOrderChange = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_order_change);

            _stageText = FindViewById<TextView>(Resource.Id.battle_stage_txt);
            _turnText = FindViewById<TextView>(Resource.Id.battle_turn_txt);

            var np4Btn = FindViewById<ToggleButton>(Resource.Id.np_4);
            var np5Btn = FindViewById<ToggleButton>(Resource.Id.np_5);
            var np6Btn = FindViewById<ToggleButton>(Resource.Id.np_6);

            void OnNpClick(string NpCommand)
            {
                if (_npSequence.Contains(NpCommand))
                {
                    _npSequence = _npSequence.Replace(NpCommand, "") + NpCommand;
                }
                else
                {
                    _npSequence += NpCommand;
                }
            }

            np4Btn.Click += (S, E) => OnNpClick("4");
            np5Btn.Click += (S, E) => OnNpClick("5");
            np6Btn.Click += (S, E) => OnNpClick("6");

            var enemyTargetGroup = FindViewById<RadioGroup>(Resource.Id.enemy_target_radio);

            var btnAtk = FindViewById<Button>(Resource.Id.atk_btn);
            btnAtk.Click += (S, E) =>
            {
                // Uncheck NP buttons
                np4Btn.Checked = np5Btn.Checked = np6Btn.Checked = false;

                // Uncheck selected targets
                enemyTargetGroup.ClearCheck();

                ChangeState(AutoskillMakerState.Atk);
            };

            void OnSkill(char SkillCode)
            {
                _skillCmd.Append(SkillCode);

                ChangeState(AutoskillMakerState.Target);
            }

            var btnA = FindViewById<Button>(Resource.Id.skill_a_btn);
            var btnB = FindViewById<Button>(Resource.Id.skill_b_btn);
            var btnC = FindViewById<Button>(Resource.Id.skill_c_btn);
            var btnD = FindViewById<Button>(Resource.Id.skill_d_btn);
            var btnE = FindViewById<Button>(Resource.Id.skill_e_btn);
            var btnF = FindViewById<Button>(Resource.Id.skill_f_btn);
            var btnG = FindViewById<Button>(Resource.Id.skill_g_btn);
            var btnH = FindViewById<Button>(Resource.Id.skill_h_btn);
            var btnI = FindViewById<Button>(Resource.Id.skill_i_btn);
            var btnJ = FindViewById<Button>(Resource.Id.master_j_btn);
            var btnK = FindViewById<Button>(Resource.Id.master_k_btn);
            var btnL = FindViewById<Button>(Resource.Id.master_l_btn);

            btnA.Click += (S, E) => OnSkill('a');
            btnB.Click += (S, E) => OnSkill('b');
            btnC.Click += (S, E) => OnSkill('c');
            btnD.Click += (S, E) => OnSkill('d');
            btnE.Click += (S, E) => OnSkill('e');
            btnF.Click += (S, E) => OnSkill('f');
            btnG.Click += (S, E) => OnSkill('g');
            btnH.Click += (S, E) => OnSkill('h');
            btnI.Click += (S, E) => OnSkill('i');
            btnJ.Click += (S, E) => OnSkill('j');
            btnK.Click += (S, E) => OnSkill('k');
            btnL.Click += (S, E) => OnSkill('l');

            var targetNoneBtn = FindViewById<Button>(Resource.Id.no_target_btn);
            var target1Btn = FindViewById<Button>(Resource.Id.target_1);
            var target2Btn = FindViewById<Button>(Resource.Id.target_2);
            var target3Btn = FindViewById<Button>(Resource.Id.target_3);

            void OnTarget(char? TargetCommand)
            {
                if (TargetCommand != null)
                {
                    _skillCmd.Append(TargetCommand.Value);
                }

                ChangeState(AutoskillMakerState.Main);
            }

            targetNoneBtn.Click += (S, E) => OnTarget(null);
            target1Btn.Click += (S, E) => OnTarget('1');
            target2Btn.Click += (S, E) => OnTarget('2');
            target3Btn.Click += (S, E) => OnTarget('3');

            var n1Radio = FindViewById<RadioButton>(Resource.Id.cards_before_np_1);
            var n2Radio = FindViewById<RadioButton>(Resource.Id.cards_before_np_2);

            void AddNpsToSkillCmd()
            {
                if (_npSequence.Length > 0)
                {
                    if (n1Radio.Checked)
                    {
                        _skillCmd.Append("n1");
                    }
                    else if (n2Radio.Checked)
                    {
                        _skillCmd.Append("n2");
                    }
                }

                _skillCmd.Append(_npSequence);

                if (_skillCmd.Length >= 1 && _skillCmd[_skillCmd.Length - 1] == ',')
                {
                    _skillCmd.Append('0');
                }

                _npSequence = "";
            }

            void OnGoToNext(string Separator)
            {
                AddNpsToSkillCmd();

                _skillCmd.Append(Separator);

                ++_turn;
                UpdateStageAndTurn();

                ChangeState(AutoskillMakerState.Main);
            }

            var nextBattleBtn = FindViewById<Button>(Resource.Id.autoskill_next_battle_btn);
            var nextTurnBtn = FindViewById<Button>(Resource.Id.autoskill_next_turn_btn);

            nextBattleBtn.Click += (S, E) =>
            {
                ++_stage;
                OnGoToNext(",#,");
            };

            nextTurnBtn.Click += (S, E) => OnGoToNext(",");

            var doneBtn = FindViewById<Button>(Resource.Id.autoskill_done_btn);
            doneBtn.Click += (S, E) =>
            {
                AddNpsToSkillCmd();

                Toast.MakeText(ApplicationContext, "Autoskill command copied to clipboard", ToastLength.Short).Show();

                ClipboardManager.FromContext(ApplicationContext).Text = _skillCmd.ToString();

                Finish();
            };

            void SetEnemyTarget(int Target)
            {
                // Merge consecutive target changes
                if (_skillCmd.Length >= 2 && _skillCmd[_skillCmd.Length - 2] == 't')
                {
                    _skillCmd.Remove(_skillCmd.Length - 1, 1)
                        .Append(Target);
                }
                else
                {
                    _skillCmd.Append($"t{Target}");
                }
            }

            enemyTargetGroup.CheckedChange += (S, E) =>
            {
                var radioBtn = enemyTargetGroup.FindViewById<RadioButton>(E.CheckedId);

                if (radioBtn == null || !radioBtn.Checked)
                    return;

                switch (E.CheckedId)
                {
                    case Resource.Id.enemy_target_1:
                        SetEnemyTarget(1);
                        break;

                    case Resource.Id.enemy_target_2:
                        SetEnemyTarget(2);
                        break;

                    case Resource.Id.enemy_target_3:
                        SetEnemyTarget(3);
                        break;
                }
            };

            var orderChangeSkillBtn = FindViewById<Button>(Resource.Id.master_x_btn);
            var orderChangeCancelBtn = FindViewById<Button>(Resource.Id.order_change_cancel);
            var orderChangeOkBtn = FindViewById<Button>(Resource.Id.order_change_ok);

            _xParty = new[]
            {
                FindViewById<Button>(Resource.Id.x_party_1),
                FindViewById<Button>(Resource.Id.x_party_2),
                FindViewById<Button>(Resource.Id.x_party_3)
            };

            for (var i = 0; i < 3; ++i)
            {
                // Making a copy is important
                var member = i + 1;

                _xParty[i].Click += (S, E) => SetOrderChangePartyMember(member);
            }

            _xSub = new[]
            {
                FindViewById<Button>(Resource.Id.x_sub_1),
                FindViewById<Button>(Resource.Id.x_sub_2),
                FindViewById<Button>(Resource.Id.x_sub_3)
            };

            for (var i = 0; i < 3; ++i)
            {
                // Making a copy is important
                var member = i + 1;

                _xSub[i].Click += (S, E) => SetOrderChangeSubMember(member);
            }

            orderChangeSkillBtn.Click += (S, E) =>
            {
                ChangeState(AutoskillMakerState.OrderChange);

                SetOrderChangePartyMember(1);
                SetOrderChangeSubMember(1);
            };
            orderChangeCancelBtn.Click += (S, E) => ChangeState(AutoskillMakerState.Main);
            orderChangeOkBtn.Click += (S, E) =>
            {
                _skillCmd.Append($"x{_xSelectedParty}{_xSelectedSub}");

                ChangeState(AutoskillMakerState.Main);
            };
        }

        View GetStateView(AutoskillMakerState State) => State switch
        {
            AutoskillMakerState.Atk => _viewAtk,
            AutoskillMakerState.Target => _viewTarget,
            AutoskillMakerState.OrderChange => _viewOrderChange,
            _ => _viewMain
        };

        void ChangeState(AutoskillMakerState NewState)
        {
            // Hide current
            GetStateView(_state).Visibility = ViewStates.Gone;

            // Hide the default view just in case
            GetStateView(0).Visibility = ViewStates.Gone;

            // Show new state
            _state = NewState;
            GetStateView(NewState).Visibility = ViewStates.Visible;
        }

        void SetOrderChangeMember(Button[] Members, int Member)
        {
            var i = 0;

            foreach (var button in Members)
            {
                ++i;

                ViewCompat.SetBackgroundTintList(
                    button,
                    i == Member
                        ? ColorStateList.ValueOf(new Color(GetColor(Resource.Color.accent_material_dark)))
                        : null);
            }
        }

        void SetOrderChangePartyMember(int Member)
        {
            _xSelectedParty = Member;

            SetOrderChangeMember(_xParty, Member);
        }

        void SetOrderChangeSubMember(int Member)
        {
            _xSelectedSub = Member;

            SetOrderChangeMember(_xSub, Member);
        }

        protected override void OnSaveInstanceState(Bundle OutState)
        {
            base.OnSaveInstanceState(OutState);

            OutState.PutString(nameof(_skillCmd), _skillCmd.ToString());
            OutState.PutString(nameof(_npSequence), _npSequence);
            OutState.PutInt(nameof(_state), (int)_state);
            OutState.PutInt(nameof(_stage), _stage);
            OutState.PutInt(nameof(_turn), _turn);
            OutState.PutInt(nameof(_xSelectedParty), _xSelectedParty);
            OutState.PutInt(nameof(_xSelectedSub), _xSelectedSub);
        }

        protected override void OnRestoreInstanceState(Bundle SavedInstanceState)
        {
            base.OnRestoreInstanceState(SavedInstanceState);

            _skillCmd = new StringBuilder(SavedInstanceState.GetString(nameof(_skillCmd), ""));
            _npSequence = SavedInstanceState.GetString(nameof(_npSequence), "");
            ChangeState((AutoskillMakerState)SavedInstanceState.GetInt(nameof(_state), 0));

            _stage = SavedInstanceState.GetInt(nameof(_stage), 1);
            _turn = SavedInstanceState.GetInt(nameof(_turn), 1);

            SetOrderChangePartyMember(SavedInstanceState.GetInt(nameof(_xSelectedParty), 1));
            SetOrderChangeSubMember(SavedInstanceState.GetInt(nameof(_xSelectedSub), 1));
        }

        void UpdateStageAndTurn()
        {
            _stageText.Text = _stage.ToString();
            _turnText.Text = _turn.ToString();
        }

        public override void OnBackPressed()
        {
            new AlertDialog.Builder(this)
                .SetMessage("Are you sure you want to exit? Autoskill command will be lost.")
                .SetTitle("Confirm Exit")
                .SetPositiveButton(Android.Resource.String.Yes, (S, E) =>
                {
                    base.OnBackPressed();
                })
                .SetNegativeButton(Android.Resource.String.No, (S, E) => { })
                .Show();
        }
    }
}
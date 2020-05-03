using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.OS;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.ConstraintLayout.Widget;
using AlertDialog = AndroidX.AppCompat.App.AlertDialog;
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
            Target
        }

        string _skillCmd = "";
        string _npSequence = "";
        AutoskillMakerState _state;

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.autoskill_maker);

            var toolbar = FindViewById<Toolbar>(Resource.Id.autoskill_maker_toolbar);
            SetSupportActionBar(toolbar);

            var viewMain = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_main);
            var viewAtk = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_atk);
            var viewTarget = FindViewById<ConstraintLayout>(Resource.Id.autoskill_view_target);

            var np4Btn = FindViewById<ToggleButton>(Resource.Id.np_4);
            var np5Btn = FindViewById<ToggleButton>(Resource.Id.np_5);
            var np6Btn = FindViewById<ToggleButton>(Resource.Id.np_6);

            View GetStateView(AutoskillMakerState State) => State switch 
            {
                AutoskillMakerState.Atk => viewAtk,
                AutoskillMakerState.Target => viewTarget,
                _ => viewMain
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

            var btnAtk = FindViewById<Button>(Resource.Id.atk_btn);
            btnAtk.Click += (S, E) =>
            {
                np4Btn.Checked = np5Btn.Checked = np6Btn.Checked = false;

                ChangeState(AutoskillMakerState.Atk);
            };

            void OnSkill(char SkillCode)
            {
                _skillCmd += SkillCode;

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
                    _skillCmd += TargetCommand.Value;
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
                        _skillCmd += "n1";
                    }
                    else if (n2Radio.Checked)
                    {
                        _skillCmd += "n2";
                    }
                }

                _skillCmd += _npSequence;

                if (_skillCmd.EndsWith(','))
                {
                    _skillCmd += '0';
                }

                _npSequence = "";
            }

            void OnGoToNext(string Separator)
            {
                AddNpsToSkillCmd();

                _skillCmd += Separator;

                ChangeState(AutoskillMakerState.Main);
            }

            var nextBattleBtn = FindViewById<Button>(Resource.Id.autoskill_next_battle_btn);
            var nextTurnBtn = FindViewById<Button>(Resource.Id.autoskill_next_turn_btn);

            nextBattleBtn.Click += (S, E) => OnGoToNext(",#,");
            nextTurnBtn.Click += (S, E) => OnGoToNext(",");

            var doneBtn = FindViewById<Button>(Resource.Id.autoskill_done_btn);
            doneBtn.Click += (S, E) =>
            {
                AddNpsToSkillCmd();

                Toast.MakeText(ApplicationContext, "Autoskill command copied to clipboard", ToastLength.Short).Show();

                ClipboardManager.FromContext(ApplicationContext).Text = _skillCmd;

                Finish();
            };
        }

        protected override void OnSaveInstanceState(Bundle OutState)
        {
            base.OnSaveInstanceState(OutState);

            OutState.PutString(nameof(_skillCmd), _skillCmd);
            OutState.PutString(nameof(_npSequence), _npSequence);
            OutState.PutInt(nameof(_state), (int)_state);
        }

        protected override void OnRestoreInstanceState(Bundle SavedInstanceState)
        {
            base.OnRestoreInstanceState(SavedInstanceState);

            _skillCmd = SavedInstanceState.GetString(nameof(_skillCmd), "");
            _npSequence = SavedInstanceState.GetString(nameof(_npSequence), "");
            _state = (AutoskillMakerState)SavedInstanceState.GetInt(nameof(_state), 0);
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
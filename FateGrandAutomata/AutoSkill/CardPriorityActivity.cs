using System.Collections.Generic;
using System.Linq;
using System.Text;
using Android.App;
using Android.OS;
using AndroidX.AppCompat.App;
using AndroidX.Preference;
using DraggableListView;

namespace FateGrandAutomata
{
    [Activity(Label = "Card Priority")]
    public class CardPriorityActivity : AppCompatActivity
    {
        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.card_priority);
        }

        static string FilterCapitals<T>(T Obj)
        {
            var s = Obj.ToString();

            var sb = new StringBuilder();

            foreach (var t in s)
            {
                if (char.IsUpper(t))
                    sb.Append(t);
            }

            return sb.ToString();
        }

        protected override void OnPause()
        {
            base.OnPause();

            if (_cardScores == null)
                return;

            var val = _cardScores
                .Select(FilterCapitals)
                .Aggregate((Result, Current) => $"{Result}, {Current}");

            var preferences = PreferenceManager.GetDefaultSharedPreferences(this);
            var key = GetString(Resource.String.pref_card_priority);
            preferences.Edit()
                .PutString(key, val)
                .Commit();
        }

        List<CardScore> _cardScores;

        const string DefaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ";

        protected override void OnResume()
        {
            base.OnResume();

            var list = FindViewById<DraggableListView.DraggableListView>(Resource.Id.card_priority_lv);

            var preferences = PreferenceManager.GetDefaultSharedPreferences(this);
            var key = GetString(Resource.String.pref_card_priority);
            var cardPriority = preferences.GetString(key, DefaultCardPriority);

            if (cardPriority.Length == 3)
            {
                cardPriority = DefaultCardPriority;
            }

            _cardScores = Card.GetCardScores(cardPriority).ToList();

            list.Adapter = new DraggableListAdapter(this, _cardScores);
        }
    }
}
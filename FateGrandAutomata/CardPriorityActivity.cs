using System.Collections.Generic;
using System.Linq;
using System.Text;
using Android.App;
using Android.OS;
using AndroidX.AppCompat.App;
using AndroidX.Preference;
using AndroidX.RecyclerView.Widget;

namespace FateGrandAutomata
{
    [Activity(Label = "Card Priority")]
    public class CardPriorityActivity : AppCompatActivity, IOnStartDragListener
    {
        ItemTouchHelper _itemTouchHelper;

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            SetContentView(Resource.Layout.card_priority);

            var preferences = PreferenceManager.GetDefaultSharedPreferences(this);
            var key = GetString(Resource.String.pref_card_priority);
            var cardPriority = preferences.GetString(key, DefaultCardPriority);

            // Handle simple mode and empty string
            if (cardPriority.Length == 3 || string.IsNullOrWhiteSpace(cardPriority))
            {
                cardPriority = DefaultCardPriority;
            }

            _cardScores = Card.GetCardScores(cardPriority).ToList();

            var adapter = new RecyclerListAdapter(_cardScores, this);

            var recyclerView = FindViewById<RecyclerView>(Resource.Id.card_priority_lv);
            recyclerView.HasFixedSize = true;
            recyclerView.SetAdapter(adapter);
            recyclerView.SetLayoutManager(new LinearLayoutManager(this));

            var callback = new ItemTouchHelperCallback(adapter);
            _itemTouchHelper = new ItemTouchHelper(callback);
            _itemTouchHelper.AttachToRecyclerView(recyclerView);
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
        public void OnStartDrag(RecyclerView.ViewHolder ViewHolder)
        {
            _itemTouchHelper?.StartDrag(ViewHolder);
        }
    }
}
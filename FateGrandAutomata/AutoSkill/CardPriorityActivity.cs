using System.Collections.Generic;
using Android.App;
using Android.OS;
using AndroidX.AppCompat.App;
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

            var list = FindViewById<DraggableListView.DraggableListView>(Resource.Id.card_priority_lv);

            var items = new List<CardScore>
            {
                CardScore.WeakBuster,
                CardScore.WeakArts,
                CardScore.WeakQuick,
                CardScore.Buster,
                CardScore.Arts,
                CardScore.Quick,
                CardScore.ResistBuster,
                CardScore.ResistArts,
                CardScore.ResistQuick
            };

            list.Adapter = new DraggableListAdapter(this, items);
        }
    }
}
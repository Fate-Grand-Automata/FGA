using System;
using Android.App;
using Android.Views;
using Android.Widget;
using System.Collections.Generic;
using Android.Graphics;
using AndroidX.Core.Content.Resources;
using FateGrandAutomata;

namespace DraggableListView
{
	public class DraggableListAdapter : BaseAdapter, IDraggableListAdapter
	{
		public List<CardScore> Items { get; set; }

		public int MMobileCellPosition { get; set; }

        readonly Activity _context;

		public DraggableListAdapter(Activity Context, List<CardScore> Items)
        {
			this.Items = Items;
			_context = Context;
			MMobileCellPosition = int.MinValue;
		}

		public override Java.Lang.Object GetItem(int Position)
		{
			return (int)Items[Position];
		}

		public override long GetItemId(int Position)
		{
			return Position;
		}

        static int GetCardColor(CardScore Card)
        {
            if (Card.HasFlag(CardScore.Buster))
            {
                return Resource.Color.colorBuster;
            }

            if (Card.HasFlag(CardScore.Arts))
            {
                return Resource.Color.colorArts;
            }

            return Resource.Color.colorQuick;
        }

		public override View GetView(int Position, View ConvertView, ViewGroup Parent)
        {
            var cell = ConvertView ?? _context.LayoutInflater.Inflate(Resource.Layout.autoskill_item, Parent, false);
            cell.SetBackgroundColor(Color.White);

            if (cell is TextView text)
            {
                var colorInt = ResourcesCompat.GetColor(_context.Resources, GetCardColor(Items[Position]), null);

                text.SetTextColor(new Color(colorInt));
                text.Text = Enum.GetName(typeof(CardScore), Items[Position]);
            }

            cell.Visibility = MMobileCellPosition == Position ? ViewStates.Invisible : ViewStates.Visible;
            cell.TranslationY = 0;

            return cell;
        }

        public override int Count => Items.Count;

        public void SwapItems(int IndexOne, int IndexTwo)
		{
			var oldValue = Items[IndexOne];
			Items[IndexOne] = Items[IndexTwo];
			Items[IndexTwo] = oldValue;
			MMobileCellPosition = IndexTwo;
			NotifyDataSetChanged();
		}
	}
}


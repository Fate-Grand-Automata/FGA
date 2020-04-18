using System.Collections.Generic;
using System.Text;
using Android.Graphics;
using Android.Views;
using AndroidX.RecyclerView.Widget;

namespace FateGrandAutomata
{
    public class RecyclerListAdapter : RecyclerView.Adapter, IItemTouchHelperAdapter
    {
        readonly List<CardScore> _items;
        readonly IOnStartDragListener _dragStartListener;

        public RecyclerListAdapter(List<CardScore> Items, IOnStartDragListener DragStartListener)
        {
            _items = Items;
            _dragStartListener = DragStartListener;
        }

        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup Parent, int ViewType)
        {
            var view = LayoutInflater.From(Parent.Context).Inflate(Resource.Layout.card_priority_item, Parent, false);
            var itemViewHolder = new ItemViewHolder(view);
            return itemViewHolder;
        }

        static int GetCardColor(CardScore Card) => Card switch
        {
            CardScore.WeakBuster => Resource.Color.colorBusterWeak,
            CardScore.Buster => Resource.Color.colorBuster,
            CardScore.ResistBuster => Resource.Color.colorBusterResist,

            CardScore.WeakArts => Resource.Color.colorArtsWeak,
            CardScore.Arts => Resource.Color.colorArts,
            CardScore.ResistArts => Resource.Color.colorArtsResist,

            CardScore.WeakQuick => Resource.Color.colorQuickWeak,
            CardScore.Quick => Resource.Color.colorQuick,
            CardScore.ResistQuick => Resource.Color.colorQuickResist,
            _ => 0
        };

        static string SpaceAtCapitals<T>(T Obj)
        {
            var s = Obj.ToString();

            var sb = new StringBuilder();

            for (var i = 0; i < s.Length; ++i)
            {
                if (i != 0 && char.IsUpper(s[i]))
                    sb.Append(" ");

                sb.Append(s[i]);
            }

            return sb.ToString();
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder Holder, int Position)
        {
            if (Holder is ItemViewHolder itemViewHolder)
            {
                itemViewHolder.TextView.Text = SpaceAtCapitals(_items[Position]);

                var context = itemViewHolder.TextView.Context;
                var colorRes = GetCardColor(_items[Position]);
                var colorInt = context.GetColor(colorRes);
                itemViewHolder.TextView.SetTextColor(new Color(colorInt));

                itemViewHolder.ImageView.Touch += (S, E) =>
                {
                    if (E.Event.ActionMasked == MotionEventActions.Down)
                    {
                        _dragStartListener?.OnStartDrag(Holder);
                    }
                };
            }
        }

        public override int ItemCount => _items.Count;

        public void OnItemMove(int From, int To)
        {
            var temp = _items[From];
            _items[From] = _items[To];
            _items[To] = temp;

            NotifyItemMoved(From, To);
        }
    }
}
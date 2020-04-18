using Android.Graphics;
using Android.Views;
using Android.Widget;
using AndroidX.RecyclerView.Widget;

namespace FateGrandAutomata
{
    public class ItemViewHolder : RecyclerView.ViewHolder, IItemTouchHelperViewHolder
    {
        public TextView TextView { get; }

        public ImageView ImageView { get; }

        public ItemViewHolder(View ItemView) : base(ItemView)
        {
            TextView = ItemView.FindViewById<TextView>(Resource.Id.card_priority_textview);
            ImageView = ItemView.FindViewById<ImageView>(Resource.Id.card_priority_dragger);
        }

        public void OnItemSelected()
        {
            ItemView.SetBackgroundColor(Color.LightGray);
        }

        public void OnItemClear()
        {
            ItemView.SetBackgroundColor(Color.Transparent);
        }
    }
}
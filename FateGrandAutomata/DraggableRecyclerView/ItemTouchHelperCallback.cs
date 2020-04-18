using AndroidX.RecyclerView.Widget;

namespace FateGrandAutomata
{
    public class ItemTouchHelperCallback : ItemTouchHelper.Callback
    {
        readonly IItemTouchHelperAdapter _adapter;

        public ItemTouchHelperCallback(IItemTouchHelperAdapter Adapter)
        {
            _adapter = Adapter;
        }

        public override int GetMovementFlags(RecyclerView RecyclerView, RecyclerView.ViewHolder ViewHolder)
        {
            const int dragFlags = ItemTouchHelper.Up | ItemTouchHelper.Down;

            return MakeMovementFlags(dragFlags, 0);
        }

        public override bool IsLongPressDragEnabled => true;

        public override bool IsItemViewSwipeEnabled => false;

        public override bool OnMove(RecyclerView RecyclerView, RecyclerView.ViewHolder ViewHolder, RecyclerView.ViewHolder Target)
        {
            _adapter.OnItemMove(ViewHolder.AdapterPosition, Target.AdapterPosition);
            return true;
        }

        public override void OnSwiped(RecyclerView.ViewHolder ViewHolder, int Direction)
        {
        }

        public override void OnSelectedChanged(RecyclerView.ViewHolder ViewHolder, int ActionState)
        {
            // We only want the active item to change
            if (ActionState != ItemTouchHelper.ActionStateIdle)
            {
                if (ViewHolder is IItemTouchHelperViewHolder itemViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    itemViewHolder.OnItemSelected();
                }
            }

            base.OnSelectedChanged(ViewHolder, ActionState);
        }

        public override void ClearView(RecyclerView RecyclerView, RecyclerView.ViewHolder ViewHolder)
        {
            base.ClearView(RecyclerView, ViewHolder);

            if (ViewHolder is IItemTouchHelperViewHolder itemViewHolder) {
                // Tell the view holder it's time to restore the idle state
                itemViewHolder.OnItemClear();
            }
        }
    }
}
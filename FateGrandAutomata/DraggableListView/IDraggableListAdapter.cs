namespace DraggableListView
{
    public interface IDraggableListAdapter
    {
        /// <summary>
        /// Responsbile for ensuring the correct visiblity of the cells in the ListView. Suggested Usage:
        /// Set its value to int.MinValue in the adapter's constructor. In the GetView(...) method, determine if mMobileCellPosition == position
        /// and if this is true, set the cell.Visibility to ViewState.Invisibile. Otherwise, set the visible to ViewState.Visible.
        /// </summary>
        /// <value>The m mobile cell position.</value>
        int MMobileCellPosition { get; set; }

        /// <summary>
        /// Responsible for updating the mMobileCellPosition variable, updaing the dataset, and calling NotifyDataSetChange on the Adapter
        /// Example: 
        /// var oldValue = Items [indexOne];
        ///	Items [indexOne] = Items [indexTwo];
        ///	Items [indexTwo] = oldValue;
        ///	mMobileCellPosition = indexTwo;
        ///	NotifyDataSetChanged ();
        /// </summary>
        /// <param name="From">From.</param>
        /// <param name="To">To.</param>
        void SwapItems(int From, int To);
    }
}
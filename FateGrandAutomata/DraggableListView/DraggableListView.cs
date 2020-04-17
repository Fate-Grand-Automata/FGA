/*
 * Author:
 * Pranav PK Khandelwal (pranav@pranavkhandelwal.com)
 * Hello
 * DynamicListView.cs is a Xamarin.Android/C# port of DevBytes DynamicListView.java,
 * (http://developer.android.com/shareables/devbytes/ListViewDraggingAnimation.zip)
 * with some modifications.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


using Android.Widget;
using Android.Animation;
using Android.Views;
using Android.Graphics.Drawables;
using Android.Graphics;
using Android.Content;
using Android.Util;
using System;

namespace DraggableListView
{
	public class DraggableListView : ListView, ITypeEvaluator, GestureDetector.IOnGestureListener
	{
		bool _reorderingEnabled = true;

		public bool ReorderingEnabled
		{
			get
			{
				return _reorderingEnabled;
			}
			set
			{
				if (!value)
				{
					ItemLongClick -= HandleItemLongClick;
				}
				else
				{
					ItemLongClick += HandleItemLongClick;
				}
				_reorderingEnabled = value;
			}
		}

		const int LINE_THICKNESS = 15;
		const int INVALID_ID = -1;
		const int INVALID_POINTER_ID = -1;

		int mLastEventY = -1;
		int mDownY = -1;
		int mDownX = -1;
		int mTotalOffset = 0;
		int mActivePointerId = INVALID_POINTER_ID;

		bool mCellIsMobile = false;

		long mAboveItemId = INVALID_ID;
		long mMobileItemId = INVALID_ID;
		long mBelowItemId = INVALID_ID;

		View mobileView;
		Rect mHoverCellCurrentBounds;
		Rect mHoverCellOriginalBounds;
		BitmapDrawable mHoverCell;
		GestureDetector dectector;

		///
		/// Constructors
		///
		public DraggableListView(Context context) : base(context)
		{
			init(context);
		}

		public DraggableListView(Context context, IAttributeSet attrs, int defStyle) : base(context, attrs, defStyle)
		{
			init(context);
		}

		public DraggableListView(Context context, IAttributeSet attrs) : base(context, attrs)
		{
			init(context);
		}

		public void init(Context context)
		{
			//	the detector handles all the gestures
			dectector = new GestureDetector(this);
			ItemLongClick += HandleItemLongClick;
		}

		#region Handlers


		void HandleItemLongClick(object sender, ItemLongClickEventArgs e)
		{
			// Long press is handeled in the OnLongPress method of the IOnGestureListener Interface
			// for some reason we have to wire this up to detect the long press, otherwise, the gesture gets ignored
		}


		void HandleHoverAnimatorUpdate(object sender, ValueAnimator.AnimatorUpdateEventArgs e)
		{
			Invalidate();
		}

		void HandleHoverAnimationStart(object sender, EventArgs e)
		{
			Enabled = false;
		}

		/// <summary>
		///  Resets the global variables and visbility of the mobile view
		/// </summary>
		void HandleHoverAnimationEnd(object sender, EventArgs e)
		{
			mAboveItemId = INVALID_ID;
			mMobileItemId = INVALID_ID;
			mBelowItemId = INVALID_ID;
			mHoverCell = null;
			Enabled = true;
			Invalidate();

			mobileView.Visibility = ViewStates.Visible;
		}

		#endregion

		#region IOnGestureListener Implementation

		public bool OnDown(MotionEvent e)
		{
			return true;
		}

		/// <summary>
		/// Determines if the touch event was right or left swipe
		/// </summary>
		public bool OnFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			return false;
		}

		public bool OnSingleTapUp(MotionEvent e)
		{
			return false;
		}

		/// <summary>
		/// Handles the item long click by hiding the selected view, creating the dummy view drawable and adding it to the screen
		/// </summary>
		public void OnLongPress(MotionEvent e)
		{
			mTotalOffset = 0;

			int position = PointToPosition(mDownX, mDownY);

			if (position < 0 || !LongClickable)
				return;

			int itemNum = position - FirstVisiblePosition;

			View selectedView = GetChildAt(itemNum);
			mMobileItemId = Adapter.GetItemId(position); // use this varable to keep track of which view is currently moving
			mHoverCell = GetAndAddHoverView(selectedView);
			selectedView.Visibility = ViewStates.Invisible; // set the visibility of the selected view to invisible

			mCellIsMobile = true;

			UpdateNeighborViewsForID(mMobileItemId);
		}

		public bool OnScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			return false;
		}

		public void OnShowPress(MotionEvent e)
		{
		}

		#endregion

		#region Bitmap Drawable Creation

		/// <summary>
		/// Creates the hover cell with the appropriate bitmap and of appropriate
		/// size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
		/// single time an invalidate call is made.
		/// </summary>
		BitmapDrawable GetAndAddHoverView(View v)
		{

			int w = v.Width;
			int h = v.Height;
			int top = v.Top;
			int left = v.Left;

			Bitmap b = GetBitmapWithBorder(v);

			BitmapDrawable drawable = new BitmapDrawable(Resources, b);

			mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
			mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

			drawable.SetBounds(left, top, left + w, top + h);

			return drawable;
		}

		/// <summary>
		/// Draws a red border over the screenshot of the view passed in.
		/// </summary>
		static Bitmap GetBitmapWithBorder(View v)
		{
			Bitmap bitmap = GetBitmapFromView(v);
			Canvas can = new Canvas(bitmap);

			Rect rect = new Rect(0, 0, bitmap.Width, bitmap.Height);

			Paint paint = new Paint();
			paint.SetStyle(Paint.Style.Stroke);
			paint.StrokeWidth = LINE_THICKNESS;
			paint.Color = Color.Red;

			can.DrawBitmap(bitmap, 0, 0, null);
			can.DrawRect(rect, paint);

			return bitmap;
		}

		/// <summary>
		/// Returns a bitmap showing a screenshot of the view passed in
		/// </summary>
		static Bitmap GetBitmapFromView(View v)
		{
			try
			{
				Bitmap bitmap = Bitmap.CreateBitmap(v.Width, v.Height, Bitmap.Config.Argb8888);
				Canvas canvas = new Canvas(bitmap);
				v.Draw(canvas);
				return bitmap;
			}
			catch (Exception ex)
			{
				Console.WriteLine(ex.Message);
			}
			return default(Bitmap);

		}

		#endregion

		/// <summary>
		/// Stores a reference to the views above and below the item currently
		/// corresponding to the hover cell. It is important to note that if this
		/// item is either at the top or bottom of the list, mAboveItemId or mBelowItemId
		/// may be invalid.
		void UpdateNeighborViewsForID(long itemID)
		{
			int position = GetPositionForID(itemID);
			mAboveItemId = Adapter.GetItemId(position - 1);
			mBelowItemId = Adapter.GetItemId(position + 1);

		}

		/// <summary>
		/// Retrieves the view in the list corresponding to itemID 
		/// </summary>
		public View GetViewForID(long itemID)
		{
			for (int i = 0; i < ChildCount; i++)
			{
				View v = GetChildAt(i);
				int position = FirstVisiblePosition + i;
				long id = Adapter.GetItemId(position);
				if (id == itemID)
				{
					return v;
				}
			}
			return null;
		}

		/// <summary>
		/// Retrieves the position in the list corresponding to itemID
		/// </summary>
		public int GetPositionForID(long itemID)
		{
			View v = GetViewForID(itemID);
			if (v == null)
			{
				return -1;
			}
			else
			{
				return GetPositionForView(v);
			}
		}

		/// <summary>
		///  DispatchDraw gets invoked when all the child views are about to be drawn.
		///  By overriding this method, the hover cell (BitmapDrawable) can be drawn
		/// over the listview's items whenever the listview is redrawn.
		/// </summary>
		protected override void DispatchDraw(Canvas canvas)
		{
			base.DispatchDraw(canvas);
			if (mHoverCell != null)
			{
				mHoverCell.Draw(canvas);
			}
		}

		/// <summary>
		/// Gets data related to touch events, moves the bitmap drawable to location of touch, and calls the HandleCellSwitch method to animate cell swaps
		/// </summary>
		public override bool OnTouchEvent(MotionEvent e)
		{
			try
			{
				dectector.OnTouchEvent(e);
				switch (e.Action)
				{
					case MotionEventActions.Down:
						mDownX = (int)e.GetX();
						mDownY = (int)e.GetY();
						mActivePointerId = e.GetPointerId(0);
						break;
					case MotionEventActions.Move:
						if (mActivePointerId == INVALID_POINTER_ID)
							break;

						int pointerIndex = e.FindPointerIndex(mActivePointerId);
						mLastEventY = (int)e.GetY(pointerIndex);
						int deltaY = mLastEventY - mDownY;


						if (mCellIsMobile)
						{ // Responsible for moving the bitmap drawable to the touch location
							Enabled = false;

							mHoverCellCurrentBounds.OffsetTo(mHoverCellOriginalBounds.Left,
								mHoverCellOriginalBounds.Top + deltaY + mTotalOffset);
							mHoverCell.SetBounds(mHoverCellCurrentBounds.Left, mHoverCellCurrentBounds.Top, mHoverCellCurrentBounds.Right, mHoverCellCurrentBounds.Bottom);
							Invalidate();
							HandleCellSwitch();
						}
						break;
					case MotionEventActions.Up:
						TouchEventsEnded();
						break;
					case MotionEventActions.Cancel:
						TouchEventsCancelled();
						break;
					default:
						break;
				}
			}
			catch (Exception ex)
			{
				Console.WriteLine("Error Processing OnTouchEvent in DynamicListView.cs - Message: {0}", ex.Message);
				Console.WriteLine("Error Processing OnTouchEvent in DynamicListView.cs - Stacktrace: {0}", ex.StackTrace);
			}

			return base.OnTouchEvent(e);
		}


		/// <summary>
		/// This Method handles the animation of the cells switching as also switches the underlying data set
		/// </summary>
		void HandleCellSwitch()
		{
			try
			{
				int deltaY = mLastEventY - mDownY; // total distance moved since the last movement
				int deltaYTotal = mHoverCellOriginalBounds.Top + mTotalOffset + deltaY; // total distance moved from original long press position

				View belowView = GetViewForID(mBelowItemId); // the view currently below the mobile item
				View mobileView = GetViewForID(mMobileItemId); // the current mobile item view (this is NOT what you see moving around, thats just a dummy, this is the "invisible" cell on the list)
				View aboveView = GetViewForID(mAboveItemId); // the view currently above the mobile item

				// Detect if we have moved the drawable enough to justify a cell swap
				bool isBelow = (belowView != null) && (deltaYTotal > belowView.Top);
				bool isAbove = (aboveView != null) && (deltaYTotal < aboveView.Top);

				if (isBelow || isAbove)
				{

					View switchView = isBelow ? belowView : aboveView; // get the view we need to animate

					var diff = GetViewForID(mMobileItemId).Top - switchView.Top; // the difference between the top of the mobile view and top of the view we are about to switch with

					// Lets animate the view sliding into its new position. Remember: the listview cell corresponding the mobile item is invisible so it looks like 
					// the switch view is just sliding into position
					ObjectAnimator anim = ObjectAnimator.OfFloat(switchView, "TranslationY", switchView.TranslationY, switchView.TranslationY + diff);
					anim.SetDuration(100);
					anim.Start();


					// Swap out the mobile item id
					mMobileItemId = GetPositionForView(switchView);

					// Since the mobile item id has been updated, we also need to make sure and update the above and below item ids
					UpdateNeighborViewsForID(mMobileItemId);

					// One the animation ends, we want to adjust out visiblity 
					anim.AnimationEnd += (sender, e) => {
						// Swap the visbility of the views corresponding to the data items being swapped - since the "switchView" will become the "mobileView"
						//						mobileView.Visibility = ViewStates.Visible;
						//						switchView.Visibility = ViewStates.Invisible;

						// Swap the items in the data source and then NotifyDataSetChanged()
						((IDraggableListAdapter)Adapter).SwapItems(GetPositionForView(mobileView), GetPositionForView(switchView));
					};
				}
			}
			catch (Exception ex)
			{
				Console.WriteLine("Error Switching Cells in DynamicListView.cs - Message: {0}", ex.Message);
				Console.WriteLine("Error Switching Cells in DynamicListView.cs - Stacktrace: {0}", ex.StackTrace);

			}

		}

		/// <summary>
		/// Resets all the appropriate fields to a default state while also animating
		/// the hover cell back to its correct location.
		/// </summary>
		void TouchEventsEnded()
		{
			mobileView = GetViewForID(mMobileItemId);
			if (mCellIsMobile)
			{
				mCellIsMobile = false;
				mActivePointerId = INVALID_POINTER_ID;
				((DraggableListAdapter)Adapter).mMobileCellPosition = int.MinValue;

				mHoverCellCurrentBounds.OffsetTo(mHoverCellOriginalBounds.Left, mobileView.Top);

				ObjectAnimator hoverViewAnimator = ObjectAnimator.OfObject(mHoverCell, "Bounds", this, mHoverCellCurrentBounds);
				hoverViewAnimator.Update += HandleHoverAnimatorUpdate;
				hoverViewAnimator.AnimationStart += HandleHoverAnimationStart;
				hoverViewAnimator.AnimationEnd += HandleHoverAnimationEnd;
				hoverViewAnimator.Start();
			}
			else
			{
				TouchEventsCancelled();
			}
		}

		/// <summary>
		/// By Implementing the ITypeEvaluator Inferface, we are able to set this as the the ITypeEvaluator for the hoverViewAnimator
		/// This method is responsible for animating the drawable to its final location after touch events end.
		/// </summary>
		public Java.Lang.Object Evaluate(float fraction, Java.Lang.Object startValue, Java.Lang.Object endValue)
		{
			var startValueRect = startValue as Rect;
			var endValueRect = endValue as Rect;

			return new Rect(Interpolate(startValueRect.Left, endValueRect.Left, fraction),
				Interpolate(startValueRect.Top, endValueRect.Top, fraction),
				Interpolate(startValueRect.Right, endValueRect.Right, fraction),
				Interpolate(startValueRect.Bottom, endValueRect.Bottom, fraction));
		}

		/// <summary>
		/// Interpolate the specified start, end and fraction for use in the Evaluate method above for a smooth animation
		/// </summary>
		public int Interpolate(int start, int end, float fraction)
		{
			return (int)(start + fraction * (end - start));
		}

		/// <summary>
		/// Resets all the appropriate fields to a default state.
		/// Resets the visibility of the currently mobile view
		/// </summary>
		void TouchEventsCancelled()
		{
			mobileView = GetViewForID(mMobileItemId);
			if (mCellIsMobile)
			{
				mAboveItemId = INVALID_ID;
				mMobileItemId = INVALID_ID;
				mBelowItemId = INVALID_ID;
				mHoverCell = null;
				Invalidate();
			}

			if (mobileView != null)
				mobileView.Visibility = ViewStates.Visible;

			Enabled = true;
			mCellIsMobile = false;
			mActivePointerId = INVALID_POINTER_ID;
		}
	}
}
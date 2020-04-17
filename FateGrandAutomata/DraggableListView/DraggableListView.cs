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
        const int LineThickness = 5;
		const int InvalidId = -1;
		const int InvalidPointerId = -1;

		int _mLastEventY = -1;
		int _mDownY = -1;
		int _mDownX = -1;
		int _mTotalOffset;
		int _mActivePointerId = InvalidPointerId;

		bool _mCellIsMobile;

		long _mAboveItemId = InvalidId;
		long _mMobileItemId = InvalidId;
		long _mBelowItemId = InvalidId;

		View _mobileView;
		Rect _mHoverCellCurrentBounds;
		Rect _mHoverCellOriginalBounds;
		BitmapDrawable _mHoverCell;
		GestureDetector _dectector;

		///
		/// Constructors
		///
		public DraggableListView(Context Context) : base(Context)
		{
			Init(Context);
		}

		public DraggableListView(Context Context, IAttributeSet Attrs, int DefStyle) : base(Context, Attrs, DefStyle)
		{
			Init(Context);
		}

		public DraggableListView(Context Context, IAttributeSet Attrs) : base(Context, Attrs)
		{
			Init(Context);
		}

		public void Init(Context Context)
		{
			//	the detector handles all the gestures
			_dectector = new GestureDetector(Context, this);
			ItemLongClick += HandleItemLongClick;
		}

		#region Handlers


		void HandleItemLongClick(object Sender, ItemLongClickEventArgs E)
		{
			// Long press is handeled in the OnLongPress method of the IOnGestureListener Interface
			// for some reason we have to wire this up to detect the long press, otherwise, the gesture gets ignored
		}


		void HandleHoverAnimatorUpdate(object Sender, ValueAnimator.AnimatorUpdateEventArgs E)
		{
			Invalidate();
		}

		void HandleHoverAnimationStart(object Sender, EventArgs E)
		{
			Enabled = false;
		}

		/// <summary>
		///  Resets the global variables and visbility of the mobile view
		/// </summary>
		void HandleHoverAnimationEnd(object Sender, EventArgs E)
		{
			_mAboveItemId = InvalidId;
			_mMobileItemId = InvalidId;
			_mBelowItemId = InvalidId;
			_mHoverCell = null;
			Enabled = true;
			Invalidate();

			_mobileView.Visibility = ViewStates.Visible;
		}

		#endregion

		#region IOnGestureListener Implementation

		public bool OnDown(MotionEvent E)
		{
			return true;
		}

		/// <summary>
		/// Determines if the touch event was right or left swipe
		/// </summary>
		public bool OnFling(MotionEvent E1, MotionEvent E2, float VelocityX, float VelocityY)
		{
			return false;
		}

		public bool OnSingleTapUp(MotionEvent E)
		{
			return false;
		}

		/// <summary>
		/// Handles the item long click by hiding the selected view, creating the dummy view drawable and adding it to the screen
		/// </summary>
		public void OnLongPress(MotionEvent E)
		{
			_mTotalOffset = 0;

			var position = PointToPosition(_mDownX, _mDownY);

			if (position < 0 || !LongClickable)
				return;

			var itemNum = position - FirstVisiblePosition;

			var selectedView = GetChildAt(itemNum);
			_mMobileItemId = Adapter.GetItemId(position); // use this varable to keep track of which view is currently moving
			_mHoverCell = GetAndAddHoverView(selectedView);
			selectedView.Visibility = ViewStates.Invisible; // set the visibility of the selected view to invisible

			_mCellIsMobile = true;

			UpdateNeighborViewsForId(_mMobileItemId);
		}

		public bool OnScroll(MotionEvent E1, MotionEvent E2, float DistanceX, float DistanceY)
		{
			return false;
		}

		public void OnShowPress(MotionEvent E)
		{
		}

		#endregion

		#region Bitmap Drawable Creation

		/// <summary>
		/// Creates the hover cell with the appropriate bitmap and of appropriate
		/// size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
		/// single time an invalidate call is made.
		/// </summary>
		BitmapDrawable GetAndAddHoverView(View V)
		{

			var w = V.Width;
			var h = V.Height;
			var top = V.Top;
			var left = V.Left;

			var b = GetBitmapWithBorder(V);

			var drawable = new BitmapDrawable(Resources, b);

			_mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
			_mHoverCellCurrentBounds = new Rect(_mHoverCellOriginalBounds);

			drawable.SetBounds(left, top, left + w, top + h);

			return drawable;
		}

        static Bitmap GetBitmapWithBorder(View V)
        {
            var bitmap = GetBitmapFromView(V);
            var can = new Canvas(bitmap);

            var rect = new Rect(0, 0, bitmap.Width, bitmap.Height);

            var paint = new Paint();
            paint.SetStyle(Paint.Style.Stroke);
            paint.StrokeWidth = LineThickness;
            paint.Color = Color.Gray;

            can.DrawBitmap(bitmap, 0, 0, null);
            can.DrawRect(rect, paint);

            return bitmap;
        }

		/// <summary>
		/// Returns a bitmap showing a screenshot of the view passed in
		/// </summary>
		static Bitmap GetBitmapFromView(View V)
		{
			try
			{
				var bitmap = Bitmap.CreateBitmap(V.Width, V.Height, Bitmap.Config.Argb8888);
				var canvas = new Canvas(bitmap);
				V.Draw(canvas);
				return bitmap;
			}
			catch (Exception ex)
			{
				Console.WriteLine(ex.Message);
			}
			return default;

		}

		#endregion

		/// <summary>
		/// Stores a reference to the views above and below the item currently
		/// corresponding to the hover cell. It is important to note that if this
		/// item is either at the top or bottom of the list, mAboveItemId or mBelowItemId
		/// may be invalid.
		void UpdateNeighborViewsForId(long ItemId)
		{
			var position = GetPositionForId(ItemId);
			_mAboveItemId = Adapter.GetItemId(position - 1);
			_mBelowItemId = Adapter.GetItemId(position + 1);

		}

		/// <summary>
		/// Retrieves the view in the list corresponding to itemID 
		/// </summary>
		public View GetViewForId(long ItemId)
		{
			for (var i = 0; i < ChildCount; i++)
			{
				var v = GetChildAt(i);
				var position = FirstVisiblePosition + i;
				var id = Adapter.GetItemId(position);
				if (id == ItemId)
				{
					return v;
				}
			}
			return null;
		}

		/// <summary>
		/// Retrieves the position in the list corresponding to itemID
		/// </summary>
		public int GetPositionForId(long ItemId)
		{
			var v = GetViewForId(ItemId);
			if (v == null)
			{
				return -1;
			}

            return GetPositionForView(v);
        }

		/// <summary>
		///  DispatchDraw gets invoked when all the child views are about to be drawn.
		///  By overriding this method, the hover cell (BitmapDrawable) can be drawn
		/// over the listview's items whenever the listview is redrawn.
		/// </summary>
		protected override void DispatchDraw(Canvas Canvas)
		{
			base.DispatchDraw(Canvas);
            _mHoverCell?.Draw(Canvas);
        }

		/// <summary>
		/// Gets data related to touch events, moves the bitmap drawable to location of touch, and calls the HandleCellSwitch method to animate cell swaps
		/// </summary>
		public override bool OnTouchEvent(MotionEvent E)
		{
			try
			{
				_dectector.OnTouchEvent(E);
				switch (E.Action)
				{
					case MotionEventActions.Down:
						_mDownX = (int)E.GetX();
						_mDownY = (int)E.GetY();
						_mActivePointerId = E.GetPointerId(0);
						break;
					case MotionEventActions.Move:
						if (_mActivePointerId == InvalidPointerId)
							break;

						var pointerIndex = E.FindPointerIndex(_mActivePointerId);
						_mLastEventY = (int)E.GetY(pointerIndex);
						var deltaY = _mLastEventY - _mDownY;


						if (_mCellIsMobile)
						{ // Responsible for moving the bitmap drawable to the touch location
							Enabled = false;

							_mHoverCellCurrentBounds.OffsetTo(_mHoverCellOriginalBounds.Left,
								_mHoverCellOriginalBounds.Top + deltaY + _mTotalOffset);
							_mHoverCell.SetBounds(_mHoverCellCurrentBounds.Left, _mHoverCellCurrentBounds.Top, _mHoverCellCurrentBounds.Right, _mHoverCellCurrentBounds.Bottom);
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
                }
			}
			catch (Exception ex)
			{
				Console.WriteLine("Error Processing OnTouchEvent in DynamicListView.cs - Message: {0}", ex.Message);
				Console.WriteLine("Error Processing OnTouchEvent in DynamicListView.cs - Stacktrace: {0}", ex.StackTrace);
			}

			return base.OnTouchEvent(E);
		}


		/// <summary>
		/// This Method handles the animation of the cells switching as also switches the underlying data set
		/// </summary>
		void HandleCellSwitch()
		{
			try
			{
				var deltaY = _mLastEventY - _mDownY; // total distance moved since the last movement
				var deltaYTotal = _mHoverCellOriginalBounds.Top + _mTotalOffset + deltaY; // total distance moved from original long press position

				var belowView = GetViewForId(_mBelowItemId); // the view currently below the mobile item
				var mobileView = GetViewForId(_mMobileItemId); // the current mobile item view (this is NOT what you see moving around, thats just a dummy, this is the "invisible" cell on the list)
				var aboveView = GetViewForId(_mAboveItemId); // the view currently above the mobile item

				// Detect if we have moved the drawable enough to justify a cell swap
				var isBelow = (belowView != null) && (deltaYTotal > belowView.Top);
				var isAbove = (aboveView != null) && (deltaYTotal < aboveView.Top);

				if (isBelow || isAbove)
				{

					var switchView = isBelow ? belowView : aboveView; // get the view we need to animate

					var diff = GetViewForId(_mMobileItemId).Top - switchView.Top; // the difference between the top of the mobile view and top of the view we are about to switch with

					// Lets animate the view sliding into its new position. Remember: the listview cell corresponding the mobile item is invisible so it looks like 
					// the switch view is just sliding into position
					var anim = ObjectAnimator.OfFloat(switchView, "TranslationY", switchView.TranslationY, switchView.TranslationY + diff);
					anim.SetDuration(100);
					anim.Start();


					// Swap out the mobile item id
					_mMobileItemId = GetPositionForView(switchView);

					// Since the mobile item id has been updated, we also need to make sure and update the above and below item ids
					UpdateNeighborViewsForId(_mMobileItemId);

					// One the animation ends, we want to adjust out visiblity 
					anim.AnimationEnd += (Sender, E) => {
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
			_mobileView = GetViewForId(_mMobileItemId);
			if (_mCellIsMobile)
			{
				_mCellIsMobile = false;
				_mActivePointerId = InvalidPointerId;
				((DraggableListAdapter)Adapter).MMobileCellPosition = int.MinValue;

				_mHoverCellCurrentBounds.OffsetTo(_mHoverCellOriginalBounds.Left, _mobileView.Top);

				var hoverViewAnimator = ObjectAnimator.OfObject(_mHoverCell, "Bounds", this, _mHoverCellCurrentBounds);
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
		public Java.Lang.Object Evaluate(float Fraction, Java.Lang.Object StartValue, Java.Lang.Object EndValue)
		{
			var startValueRect = StartValue as Rect;
			var endValueRect = EndValue as Rect;

			return new Rect(Interpolate(startValueRect.Left, endValueRect.Left, Fraction),
				Interpolate(startValueRect.Top, endValueRect.Top, Fraction),
				Interpolate(startValueRect.Right, endValueRect.Right, Fraction),
				Interpolate(startValueRect.Bottom, endValueRect.Bottom, Fraction));
		}

		/// <summary>
		/// Interpolate the specified start, end and fraction for use in the Evaluate method above for a smooth animation
		/// </summary>
		public int Interpolate(int Start, int End, float Fraction)
		{
			return (int)(Start + Fraction * (End - Start));
		}

		/// <summary>
		/// Resets all the appropriate fields to a default state.
		/// Resets the visibility of the currently mobile view
		/// </summary>
		void TouchEventsCancelled()
		{
			_mobileView = GetViewForId(_mMobileItemId);
			if (_mCellIsMobile)
			{
				_mAboveItemId = InvalidId;
				_mMobileItemId = InvalidId;
				_mBelowItemId = InvalidId;
				_mHoverCell = null;
				Invalidate();
			}

			if (_mobileView != null)
				_mobileView.Visibility = ViewStates.Visible;

			Enabled = true;
			_mCellIsMobile = false;
			_mActivePointerId = InvalidPointerId;
		}
	}
}
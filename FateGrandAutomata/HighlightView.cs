using System.Collections.Generic;
using Android.Content;
using Android.Graphics;
using Android.Views;
using Region = CoreAutomata.Region;

namespace FateGrandAutomata
{
    public class HighlightView : View
    {
        static readonly HashSet<Region> Regions = new HashSet<Region>();

        static HighlightView _instance;

        public static void AddRegion(Region Region)
        {
            _instance?.Post(() =>
            {
                Regions.Add(Region);

                _instance.Invalidate();
            });
        }

        public static void RemoveRegion(Region Region)
        {
            _instance?.Post(() =>
            {
                Regions.Remove(Region);

                _instance.Invalidate();
            });
        }

        readonly Paint _paint;

        public HighlightView(Context Context) : base(Context)
        {
            _instance = this;

            _paint = new Paint
            {
                Color = Color.Red,
                StrokeWidth = 5
            };

            _paint.SetStyle(Paint.Style.Stroke);
        }

        protected override void OnDraw(Canvas Canvas)
        {
            base.OnDraw(Canvas);

            foreach (var region in Regions)
            {
                Canvas.DrawRect(region.X, region.Y, region.R, region.B, _paint);
            }
        }
    }
}
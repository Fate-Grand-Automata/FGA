using System;

namespace CoreAutomata
{
    public class Region
    {
        public int X { get; set; }
        public int Y { get; set; }
        public int W { get; set; }
        public int H { get; set; }

        public int R => X + W;

        public int B => Y + H;

        public Location Location => new Location(X, Y);

        public Region(int X, int Y, int W, int H)
        {
            this.X = X;
            this.Y = Y;
            this.W = W;
            this.H = H;
        }

        /// <summary>
        /// Restrict another Region to be within the bounds of this Region.
        /// </summary>
        public Region Clip(Region Region)
        {
            var left = Region.X.Clip(X, R - 1);
            var right = Region.R.Clip(X + 1, R);
            var top = Region.Y.Clip(Y, B - 1);
            var bottom = Region.B.Clip(Y + 1, B);

            return new Region(left, top, right - left, bottom - top);
        }

        public bool Contains(Region Region)
        {
            return X <= Region.X
                   && Y <= Region.Y
                   && R >= Region.R
                   && B >= Region.B;
        }

        public void WaitVanish(IPattern Image, int? Timeout = null) => AutomataApi.WaitVanish(this, Image, Timeout);

        public bool Exists(IPattern Image, int? Timeout = null, double? Similarity = null) => AutomataApi.Exists(this, Image, Timeout, Similarity);

        public IPattern GetPattern() => AutomataApi.GetCopy(this);

        public void Click()
        {
            var center = new Location(X + W / 2, Y + H / 2);

            center.Click();
        }

        public static Region operator *(Region Region, double Scale)
        {
            return new Region(
                (Region.X * Scale).Round(),
                (Region.Y * Scale).Round(),
                (Region.W * Scale).Round(),
                (Region.H * Scale).Round());
        }

        public void Highlight(TimeSpan Duration)
        {
            AutomataApi.Highlight(this, Duration);
        }

        public override string ToString() => $"({X}, {Y}) {W}x{H}";
    }
}
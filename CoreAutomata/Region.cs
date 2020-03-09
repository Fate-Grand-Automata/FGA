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

        public bool Contains(Region R)
        {
            return X <= R.X
                   && X + W <= R.X + R.W
                   && Y <= R.Y
                   && Y + H <= R.Y + R.H;
        }

        public void WaitVanish(IPattern Image, int? Timeout = null) => AutomataApi.WaitVanish(this, Image, Timeout);

        public bool Exists(IPattern Image, int? Timeout = null, double? Similarity = null) => AutomataApi.Exists(this, Image, Timeout, Similarity);

        public IPattern Save() => AutomataApi.Save(this);

        public void Click()
        {
            var center = new Location(X + W / 2, Y + H / 2);

            center.Click();
        }

        public static Region operator *(Region Region, double Scale)
        {
            return new Region((int)(Region.X * Scale),
                (int)(Region.Y * Scale),
                (int)(Region.W * Scale),
                (int)(Region.H * Scale));
        }
    }
}
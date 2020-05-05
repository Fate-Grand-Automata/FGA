namespace CoreAutomata
{
    public struct Location
    {
        public int X { get; }
        public int Y { get; }

        public Location(int X, int Y)
        {
            this.X = X;
            this.Y = Y;
        }

        public static Location operator *(Location Location, double Scale)
        {
            return new Location((Location.X * Scale).Round(), (Location.Y * Scale).Round());
        }

        public override string ToString() => $"({X}, {Y})";
    }
}
namespace CoreAutomata
{
    public class Location
    {
        public int X { get; set; }
        public int Y { get; set; }

        public Location() { }

        public Location(int X, int Y)
        {
            this.X = X;
            this.Y = Y;
        }

        public void Click() => AutomataApi.Click(this);

        public static Location operator *(Location Location, double Scale)
        {
            return new Location((Location.X * Scale).Round(), (Location.Y * Scale).Round());
        }

        public override string ToString() => $"({X}, {Y})";
    }
}
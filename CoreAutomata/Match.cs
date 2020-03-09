namespace CoreAutomata
{
    public class Match : Region
    {
        public double Score { get; }

        public Match(Region Region, double Score)
            : base(Region.X, Region.Y, Region.W, Region.H)
        {
            this.Score = Score;
        }
    }
}
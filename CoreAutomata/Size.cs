namespace CoreAutomata
{
    public struct Size
    {
        public Size(int Width, int Height)
        {
            this.Width = Width;
            this.Height = Height;
        }

        public int Width { get; set; }

        public int Height { get; set; }
    }
}
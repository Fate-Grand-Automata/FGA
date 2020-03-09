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

        public static Size operator *(Size Size, double Scale)
        {
            return new Size((int)(Size.Width * Scale), (int)(Size.Height * Scale));
        }
    }
}
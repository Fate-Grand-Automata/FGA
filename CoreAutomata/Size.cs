using System;

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
            return new Size((int)Math.Round(Size.Width * Scale), (int)Math.Round(Size.Height * Scale));
        }

        public override string ToString() => $"{Width}x{Height}";
    }
}
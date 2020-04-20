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
            return new Size((Size.Width * Scale).Round(), (Size.Height * Scale).Round());
        }

        public override string ToString() => $"{Width}x{Height}";
    }
}
using System;
using System.Linq;
using Android.Graphics;

namespace FateGrandAutomata
{
    public static class ImageComparer
    {
        const int W = 16, H = 16;

        public static Bitmap Resize(this Bitmap Original, int Width, int Height)
        {
            return Bitmap.CreateScaledBitmap(Original, Width, Height, true);
        }

        public static Bitmap Grayscale(this Bitmap Original)
        {
            var w = Original.Width;
            var h = Original.Height;

            var grayscale = Bitmap.CreateBitmap(w, h, Bitmap.Config.Argb8888);

            var c = new Canvas(grayscale);
            var paint = new Paint();
            var cm = new ColorMatrix();
            cm.SetSaturation(0);
            var f = new ColorMatrixColorFilter(cm);
            paint.SetColorFilter(f);
            c.DrawBitmap(Original, 0, 0, paint);

            return grayscale;
        }

        public static byte[,] GetGreyScaleValues(this Bitmap Original)
        {
            using var img = Original.Resize(W, H).Grayscale();

            var grayscale = new byte[W, H];

            for (var y = 0; y < H; ++y)
            {
                for (var x = 0; x < W; ++x)
                {
                    grayscale[x, y] = (byte) Math.Abs(img.GetPixel(x, y));
                }
            }

            return grayscale;
        }

        public static byte[,] GetDifferences(this Bitmap Img1, Bitmap Img2)
        {
            var gray1 = Img1.GetGreyScaleValues();
            var gray2 = Img2.GetGreyScaleValues();

            var differences = new byte[W, H];

            for (var y = 0; y < H; ++y)
            {
                for (var x = 0; x < W; ++x)
                {
                    differences[x, y] = (byte) Math.Abs(gray1[x, y] - gray2[x, y]);
                }
            }

            return differences;
        }

        public static double Diff(this Bitmap Img1, Bitmap Img2, byte Threshold = 3)
        {
            var differences = Img1.GetDifferences(Img2);

            var diffPixels = differences
                .Cast<byte>()
                .Count(B => B > Threshold);

            return diffPixels / 256.0;
        }
    }
}
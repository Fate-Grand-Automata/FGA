using System.Collections.Generic;

namespace CoreAutomata
{
    public interface IPattern
    {
        IPattern Resize(Size Size);

        bool IsMatch(IPattern Template, double Similarity);

        IEnumerable<Match> FindMatches(IPattern Template, double Similarity);

        int Width { get; }

        int Height { get; }

        IPattern Crop(Region Region);
    }
}
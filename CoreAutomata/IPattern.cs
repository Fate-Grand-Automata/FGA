﻿using System;
using System.Collections.Generic;

namespace CoreAutomata
{
    public interface IPattern : IDisposable
    {
        IPattern Resize(Size Size);

        void Resize(IPattern Target, Size Size);

        bool IsMatch(IPattern Template, double Similarity);

        IEnumerable<Match> FindMatches(IPattern Template, double Similarity);

        int Width { get; }

        int Height { get; }

        IPattern Crop(Region Region);

        void Save(string Filename);

        IPattern Copy();
    }
}
using System;

namespace FateGrandAutomata
{
    [Flags]
    public enum CardScore
    {
        Normal = 0,
        Weak = 1,
        Resist = 2,

        Buster = 4,
        Arts = 8,
        Quick = 16,

        ResistBuster = Resist | Buster,
        ResistArts = Resist | Arts,
        ResistQuick = Resist | Quick,

        WeakBuster = Weak | Buster,
        WeakArts = Weak | Arts,
        WeakQuick = Weak | Quick
    }
}